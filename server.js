/* [SRV(🏗️🏗️🏗️)] WIKI-ROUTER v5.2 CORE ENGINE (Room & ID Upgraded) */
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

/* --- [파일 순환 시스템 유지] --- */
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

/* [SIO_S(📡📡📡)] 소켓 서버 로직 */
io.on('connection', (socket) => {
    
    // 💎 [SIO_S] 룸 진입 및 사용자 등록
    socket.on('join-room', (data) => {
        const { roomId, userId, peerId } = data;
        
        socket.join(roomId); // 소켓을 특정 방에 할당
        socket.myRoom = roomId;
        socket.myUserId = userId;
        socket.myPeerId = peerId;

        console.log(`📡 [JOIN] 룸: ${roomId} | 아이디: ${userId} | Peer: ${peerId}`);
        
        // 해당 방에 있는 다른 유저들에게 입장을 알림 (선택 사항)
        socket.to(roomId).emit('peer-joined', { userId, peerId });
    });

    // 🐻 [BEAR] 특정 룸의 피어 리스트만 반환
    socket.on('get-room-peers', (roomId) => {
        const room = io.sockets.adapter.rooms.get(roomId);
        const peers = [];
        
        if (room) {
            room.forEach(socketId => {
                const s = io.sockets.sockets.get(socketId);
                if (s && s.myPeerId) peers.push(s.myPeerId);
            });
        }
        // 요청한 유저에게만 명단 전송
        socket.emit('room-peer-list', peers);
    });

    // 🐧 [PENG] 무전기 음성 (해당 룸 유저들에게만 방송)
    socket.on('sync-audio-file', (data) => {
        const { blob, roomId, senderId } = data;
        if (!blob || !roomId) return;
        
        // 룸 내의 다른 사람들에게만 전송
        socket.to(roomId).emit('receive-sync-audio', { 
            blob: blob, 
            senderId: senderId 
        });

        // 파일 저장 (기록 유지)
        const fName = `v_${roomId}_${senderId}_${Date.now()}.webm`;
        fs.writeFile(path.join(recDir, fName), Buffer.from(blob), (err) => {
            if (!err) rotateLogs();
        });
    });

    // 🗑️ [EV] 해당 룸의 로그만 삭제 (또는 전체 관리자 기능)
    socket.on('clear-logs-signal', () => {
        // 현재는 전체 삭제 유지 (필요 시 특정 룸 파일만 필터링 가능)
        if (fs.existsSync(recDir)) {
            fs.readdirSync(recDir).forEach(f => fs.unlinkSync(path.join(recDir, f)));
        }
        io.emit('logs-cleared-notification', { by: socket.myUserId || 'Unknown' });
    });

    // 🔌 [DISCONNECT]
    socket.on('disconnect', () => {
        if (socket.myRoom) {
            console.log(`👋 [퇴장] 룸: ${socket.myRoom} | 아이디: ${socket.myUserId}`);
            // 필요 시 룸 퇴장 알림 송신
            socket.to(socket.myRoom).emit('peer-left', socket.myPeerId);
        }
    });
});

const PORT = 3000;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 [WIKI-ROUTER v5.2] ONLINE: http://localhost:${PORT}`);
});