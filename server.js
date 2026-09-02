/* [SRV(🏗️🏗️🏗️)] WIKI-ROUTER v5.2 CORE ENGINE (Communication Base) */
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const { ExpressPeerServer } = require('peer');
const path = require('path');
const fs = require('fs');

// OpenAI (GPT-4o-mini) 설정
const { OpenAI } = require('openai');
const openai = new OpenAI({
    apiKey: process.env.OPENAI_API_KEY || 'YOUR_API_KEY_HERE'
});

const app = express();
const server = http.createServer(app);

const io = new Server(server, { maxHttpBufferSize: 2e7, cors: { origin: "*" } });
const peerServer = ExpressPeerServer(server, { debug: false, path: '/' });
const recDir = path.join(__dirname, 'recordings');

if (!fs.existsSync(recDir)) fs.mkdirSync(recDir);

app.use('/peerjs', peerServer);
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json()); // JSON 요청 본문 파싱 미들웨어

/* --- [GPT-4o-mini AI 처리 엔드포인트 & 상세 로그] --- */
app.post('/api/ai-process', async (req, res) => {
    const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
    console.log(`\n========================================`);
    console.log(`🤖 [AI REQUEST] 새로운 AI 요청 수신됨`);
    console.log(`🌐 [Client IP]: ${clientIp}`);
    
    try {
        const { prompt } = req.body;
        
        if (!prompt) {
            console.warn(`⚠️ [AI WARNING] 프롬프트가 누락되었습니다.`);
            return res.status(400).json({ success: false, error: "프롬프트가 전달되지 않았습니다." });
        }

        console.log(`📝 [Prompt 내용]: "${prompt.length > 50 ? prompt.substring(0, 50) + '...' : prompt}"`);
        console.log(`⏳ [OpenAI 통신] gpt-4o-mini 모델로 요청 전송 중...`);

        const startTime = Date.now();
        const completion = await openai.chat.completions.create({
            model: "gpt-4o-mini",
            messages: [{ role: "user", content: prompt }],
        });
        const duration = Date.now() - startTime;

        console.log(`✅ [OpenAI 성공] 응답 수신 완료 (${duration}ms 소요)`);
        console.log(`========================================\n`);

        res.json({ 
            success: true, 
            result: completion.choices[0].message.content 
        });

    } catch (error) {
        console.error(`\n❌ [AI ERROR] OpenAI 통신 중 예외 발생!`);
        console.error(`📌 [Error Message]: ${error.message}`);
        if (error.status) {
            console.error(`📌 [HTTP Status]: ${error.status}`);
        }
        if (error.code) {
            console.error(`📌 [Error Code]: ${error.code}`);
        }
        console.log(`========================================\n`);
        
        res.status(500).json({ success: false, error: error.message });
    }
});

/* --- [방(Room) 및 비활동 타이머 관리] --- */
const rooms = {}; 
const roomTimers = {}; // 방별 비활동 자동 종료 타이머 보관함
const INACTIVE_TIMEOUT = 3 * 60 * 1000; // 통신이 없을 경우 종료할 제한 시간 (3분)

function emitUserList(roomId) {
    if (rooms[roomId]) {
        io.to(roomId).emit('update-user-list', rooms[roomId]);
    }
}

// 방의 비활동 타이머를 초기화하거나 재설정하는 함수
function resetRoomActivityTimer(roomId) {
    if (roomTimers[roomId]) {
        clearTimeout(roomTimers[roomId]);
    }

    // 지정된 시간 동안 통신이 없으면 자동 종료 및 세션 만료 신호 전송
    roomTimers[roomId] = setTimeout(() => {
        console.log(`⏰ [자동 종료] 방(${roomId})에 장기간 통신/사용자가 없어 연결을 정리합니다.`);
        
        io.to(roomId).emit('session-expired', { message: '장기간 통신이 없어 세션이 종료되었습니다.' });
        
        delete rooms[roomId];
        delete roomTimers[roomId];
    }, INACTIVE_TIMEOUT);
}

// 방이 완전히 비었을 때 타이머와 리소스를 깔끔하게 날리는 함수
function checkAndClearEmptyRoom(roomId) {
    if (!rooms[roomId] || rooms[roomId].length === 0) {
        console.log(`🧹 방(${roomId})에 남은 유저가 없습니다. 리소스를 즉시 정리합니다.`);
        if (roomTimers[roomId]) {
            clearTimeout(roomTimers[roomId]);
            delete roomTimers[roomId];
        }
        delete rooms[roomId];
    }
}

/* --- [Socket.io 통신 허브] --- */
io.on('connection', (socket) => {
    console.log(`🔗 [Socket] Client Connected: ${socket.id}`);

    // 사용자가 특정 방에 들어왔을 때 (중복 제거 및 완벽 방어 처리된 단일 리스너)
    socket.on('join-room', (data) => {
        let parsed = data;

        // 1. 문자열로 들어온 경우 객체로 파싱 시도
        if (typeof parsed === 'string') {
            try {
                parsed = JSON.parse(parsed);
            } catch (e) {
                parsed = { roomId: data, userId: null };
            }
        }

        // 2. 객체에서 값 추출 (다양한 키 이름 호환)
        const roomId = (parsed && typeof parsed === 'object') ? (parsed.roomId || parsed.room) : parsed;
        const userId = (parsed && typeof parsed === 'object') ? (parsed.userId || parsed.id || parsed.name) : null;

        const finalRoomId = (roomId && typeof roomId === 'string') ? roomId : 'default_room';
        
        // 3. userId가 없거나 undefined면 socket.id 기반으로 강제 할당 (undefined 원천 차단)
        const finalUserId = (userId && userId !== 'undefined' && userId !== '[object Object]') 
            ? userId 
            : `User_${socket.id.substring(0, 4)}`;

        socket.join(finalRoomId);
        if (!rooms[finalRoomId]) rooms[finalRoomId] = [];
        
        // 중복 추가 방지
        rooms[finalRoomId] = rooms[finalRoomId].filter(u => u.socketId !== socket.id);
        rooms[finalRoomId].push({ socketId: socket.id, userId: finalUserId });
        
        console.log(`🏠 [Room] User ${finalUserId} (${socket.id}) joined Room: ${finalRoomId}`);
        emitUserList(finalRoomId);
        resetRoomActivityTimer(finalRoomId);
    });

    // 사용자와 통신(데이터 교환 등 액션)이 일어날 때마다 타이머를 갱신
    socket.on('user-activity-ping', (roomId) => {
        if (rooms[roomId]) {
            resetRoomActivityTimer(roomId);
        }
    });

    socket.on('disconnect', () => {
        console.log(`❌ [Socket] Client Disconnected: ${socket.id}`);
        for (const roomId in rooms) {
            const beforeLen = rooms[roomId].length;
            rooms[roomId] = rooms[roomId].filter(u => u.socketId !== socket.id);
            
            if (rooms[roomId].length !== beforeLen) {
                emitUserList(roomId);
                
                if (rooms[roomId].length === 0) {
                    checkAndClearEmptyRoom(roomId);
                } else {
                    resetRoomActivityTimer(roomId);
                }
            }
        }
    });
});

const PORT = 3000;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`\n🚀 [WIKI-ROUTER v5.2 통신 전용 모드] ONLINE: http://localhost:${PORT}`);
    console.log(`💡 OpenAI API 연동 상태: ${process.env.OPENAI_API_KEY ? 'API 키 설정됨 (정상 연동 가능)' : '⚠️ 경고: OPENAI_API_KEY 환경 변수가 설정되지 않았습니다!'}\n`);
});