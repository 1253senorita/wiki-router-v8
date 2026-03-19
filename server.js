/* [SRV(🏗️🏗️🏗️)] WIKI-ROUTER v5.2 CORE ENGINE (Power-Control Upgraded) */
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const { ExpressPeerServer } = require('peer');
const path = require('path');
const fs = require('fs');
const wol = require('wake_on_lan'); // ⬅️ WOL 모듈 추가

const app = express();
const server = http.createServer(app);

/* [PORT(🚪🚪🚪)] 인프라 및 경로 설정 */
const io = new Server(server, { maxHttpBufferSize: 2e7, cors: { origin: "*" } });
const peerServer = ExpressPeerServer(server, { debug: false, path: '/' });
const recDir = path.join(__dirname, 'recordings');

if (!fs.existsSync(recDir)) fs.mkdirSync(recDir);

app.use('/peerjs', peerServer);
app.use(express.static(path.join(__dirname, 'public')));

// ------------------------------------------------------------
// ⚡ [POWER(🔌🔌🔌)] 원격 전원 및 타이머 로직 추가
// ------------------------------------------------------------
const TARGET_MAC = '00:E0:4C:88:1D:49'; // 오빠의 PC

// 1. 즉시 깨우기 (외부에서 /power-on 접속 시)
app.get('/power-on', (req, res) => {
    wol.wake(TARGET_MAC, (err) => {
        if (err) return res.status(500).send("신호 전송 실패!");
        res.send(`<script>alert("오빠의 PC로 즉시 부팅 신호를 보냈습니다!"); window.location.href="/";</script>`);
    });
});

// 2. 13분 타이머 깨우기 (외부에서 /power-timer 접속 시)
app.get('/power-timer', (req, res) => {
    const delay = 13 * 60 * 1000; // 13분
    
    // 즉시 응답을 주고 백그라운드에서 타이머 작동
    res.send(`<h1>⏲️ 13분 타이머가 시작되었습니다.</h1><p>13분 뒤에 자동으로 PC가 켜집니다. 창을 닫으셔도 됩니다.</p><button onclick="window.location.href='/'">무전기로 돌아가기</button>`);

    setTimeout(() => {
        console.log(`[TIMER] 13분이 지났습니다. ${TARGET_MAC} 깨우기 실행!`);
        wol.wake(TARGET_MAC);
    }, delay);
});
// ------------------------------------------------------------

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

/* ... (이후 기존 emitUserList 및 io.on 로직 동일) ... */

// (파일 하단 생략 - 기존 코드 그대로 유지)
const PORT = 3000;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 [WIKI-ROUTER v5.2] ONLINE: http://localhost:${PORT}`);
});