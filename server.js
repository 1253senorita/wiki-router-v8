/* [SRV(🏗️🏗️🏗️)] WIKI-ROUTER v5.2 CORE ENGINE (User List Sync Upgraded) */
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const { ExpressPeerServer } = require('peer');
const path = require('path');
const fs = require('fs');

const app = express();
const server = http.createServer(app);

/* [PORT(🚪🚪🚪)] 인프라 및 경로 설정 */
const io = new Server(server, { maxHttpBufferSize: 2e7, cors: { origin: "*" } });
const peerServer = ExpressPeerServer(server, { debug: false, path: '/' });
const recDir = path.join(__dirname, 'recordings');

if (!fs.existsSync(recDir)) fs.mkdirSync(recDir);

app.use('/peerjs', peerServer);
app.use(express.static(path.join(__dirname, 'public')));

/* --- [파일 순환 시스템] --- */
function rotateLogs() {
    try {
        const files = fs.readdirSync(recDir).map(f => ({ 
            name: f, time: fs.statSync(path.join(recDir, f)).mtime.getTime() 
        })).sort((a, b) => a.time - b.time);
        if (files.length > 100) {
            files.slice(0, files.length - 100).forEach(f => fs.unlinkSync(path.join(recDir, f.name)));
        }
    } catch (e) {}
}

/* --- [Helper: 특정 룸의 유저 명단 배포] --- */
function emitUserList(roomId) {
    const room = io.sockets.adapter.rooms.get(roomId);
    const userList = [];
    if (room) {
        room.forEach(socketId => {
            const s = io.sockets.sockets.get(socketId);
            if (s && s.myUserId) {
                userList.push({ userId: s.myUserId, peerId: s.myPeerId });
            }
        });
    }
    io.to(roomId).emit('update-user-list', userList);
}

/* [SIO_S(📡📡📡)] 소켓 서버 통합 로직 */
io.on('connection', (socket) => {
    
    // 💎 [JOIN] 룸 진입 및 명단 갱신
    socket.on('join-room', (data) => {
        const { roomId, userId, peerId } = data;
        
        socket.join(roomId);
        socket.myRoom = roomId;
        socket.myUserId = userId;
        socket.myPeerId = peerId;

        console.log(`📡 [JOIN] 룸: ${roomId} | 아이디: ${userId}`);
        
        // 1. 기존 유저들에게 입장 알림
        socket.to(roomId).emit('peer-joined', { userId, peerId });
        // 2. 룸 전체에 최신 명단 배포
        emitUserList(roomId);
    });

    // 🐻 [BEAR] 특정 룸의 피어 리스트 반환
    socket.on('get-room-peers', (roomId) => {
        const room = io.sockets.adapter.rooms.get(roomId);
        const peers = [];
        if (room) {
            room.forEach(socketId => {
                const s = io.sockets.sockets.get(socketId);
                if (s && s.myPeerId) peers.push(s.myPeerId);
            });
        }
        socket.emit('room-peer-list', peers);
    });

    // 🐧 [PENG] 음성 파일 공유
    socket.on('sync-audio-file', (data) => {
        const { blob, roomId, senderId } = data;
        if (!blob || !roomId) return;
        
        socket.to(roomId).emit('receive-sync-audio', { blob, senderId });

        const fName = `v_${roomId}_${senderId}_${Date.now()}.webm`;
        fs.writeFile(path.join(recDir, fName), Buffer.from(blob), (err) => {
            if (!err) rotateLogs();
        });
    });

    // 🗑️ [EV] 로그 삭제
    socket.on('clear-logs-signal', () => {
        if (fs.existsSync(recDir)) {
            fs.readdirSync(recDir).forEach(f => fs.unlinkSync(path.join(recDir, f)));
        }
        io.emit('logs-cleared-notification', { by: socket.myUserId || 'Unknown' });
    });

    // 🔌 [DISCONNECT] 연결 종료 및 명단 갱신
    socket.on('disconnect', () => {
        if (socket.myRoom) {
            const oldRoom = socket.myRoom;
            console.log(`👋 [퇴장] 룸: ${oldRoom} | 아이디: ${socket.myUserId}`);
            
            socket.to(oldRoom).emit('peer-left', socket.myPeerId);
            // 유저가 나갔으므로 명단 다시 배포
            emitUserList(oldRoom);
        }
    });
});

const PORT = 3000;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 [WIKI-ROUTER v5.2] ONLINE: http://localhost:${PORT}`);
});