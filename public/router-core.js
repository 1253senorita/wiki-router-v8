/* [router-core.js] - 핵심 통신 엔진 (v5.2 통합본) */
const socket = io();
const peer = new Peer({ host: location.hostname, port: location.port || 3000, path: '/peerjs' });

let myStream, calls = [], rec, chunks = [], isBusy = false;
let isBearActive = true; 

/* --- [세션 상태 및 인증 게이트] --- */
let currentRoom = null;
let myUserId = null; 
let isAuthenticated = false; 

// DOM 캐싱
const bStat = document.getElementById('b-stat');
const bTrig = document.getElementById('b-trig');
const pTrig = document.getElementById('p-trig');
const lBox = document.getElementById('l-box');

/**
 * 1. 시스템 입장 (인증 게이트 오픈)
 */
const joinRoom = (roomId, userId) => {
    if(!roomId || !userId) return;
    
    currentRoom = roomId;
    myUserId = userId;
    isAuthenticated = true; // 이후 모든 통신 허용
    
    socket.emit('join-room', { roomId, userId, peerId: peer.id });
    
    if(bStat) bStat.innerText = `READY (${roomId})`;
    console.log(`🍎 AUTH SUCCESS: ROOM ${roomId} / USER ${userId}`);
};

/**
 * 2. Peer 수신 및 스트림 처리
 */
peer.on('open', (id) => console.log('My Peer ID:', id));

peer.on('call', (c) => {
    if(!isAuthenticated) return; // 미인증 사용자 차단
    c.answer();
    c.on('stream', (s) => { 
        const a = new Audio(); 
        a.srcObject = s; 
        a.play().catch(e => console.error("Audio Play Error:", e)); 
    });
});

/**
 * 3. Bear (실시간 전화) 로직
 */
const toggleBear = async () => {
    if(!isBearActive || !isAuthenticated) return; 

    if (!isBusy) {
        isBusy = true;
        bStat.innerText = "ON CALL...";
        bTrig.innerText = "END";
        bTrig.style.filter = "brightness(0.7)";
        
        try {
            if (!myStream) myStream = await navigator.mediaDevices.getUserMedia({ audio: true });
            // 현재 룸의 피어 명단 요청
            socket.emit('get-room-peers', currentRoom); 
        } catch (e) {
            alert("마이크 권한이 필요합니다.");
            stopBear();
        }
    } else {
        stopBear();
    }
};

const stopBear = () => {
    isBusy = false;
    bStat.innerText = `READY (${currentRoom})`;
    bTrig.innerText = "TALK";
    bTrig.style.filter = "none";
    calls.forEach(c => c.close());
    calls = [];
};

/**
 * 4. Peng (PTT 무전기) 로직
 */
const startPeng = async () => {
    if(!isAuthenticated || (isBusy && bTrig.innerText === "END")) return; 
    
    chunks = [];
    try {
        const s = await navigator.mediaDevices.getUserMedia({ audio: true });
        rec = new MediaRecorder(s, { mimeType: 'audio/webm' });
        rec.ondataavailable = e => chunks.push(e.data);
        rec.onstop = () => {
            const blob = new Blob(chunks, { type: 'audio/webm' });
            socket.emit('sync-audio-file', { 
                blob: blob, 
                roomId: currentRoom, 
                senderId: myUserId 
            });
            addLog(myUserId, URL.createObjectURL(blob), false);
            s.getTracks().forEach(t => t.stop());
        };
        rec.start();
    } catch (e) { console.error("PTT Error:", e); }
};

const stopPeng = () => {
    if(rec && rec.state === "recording") rec.stop();
};

/**
 * 5. 소켓 이벤트 핸들러 (통합)
 */

// [Peer 명단 수신 및 자동 연결]
socket.on('room-peer-list', (list) => {
    list.forEach(tid => { 
        if(tid !== peer.id) {
            const call = peer.call(tid, myStream);
            if(call) {
                calls.push(call);
                call.on('close', () => {
                    calls = calls.filter(c => c.peer !== tid);
                });
            }
        }
    });
});

// [실시간 접속자 명단 UI 업데이트 연결]
socket.on('update-user-list', (list) => {
    if (typeof updateUserListUI === 'function') updateUserListUI(list);
});

// [무전기 음성 수신]
socket.on('receive-sync-audio', (d) => {
    const url = URL.createObjectURL(new Blob([d.blob], { type: 'audio/webm' }));
    addLog(d.senderId, url, true);
    new Audio(url).play().catch(() => {});
});

// [로그 초기화 통지]
socket.on('logs-cleared-notification', (data) => {
    if(lBox) {
        lBox.innerHTML = `<div style="text-align:center; color:#888; font-size:0.8rem; padding:10px;">
            🧹 ${data.by}님에 의해 로그가 초기화되었습니다.
        </div>`;
    }
});

/**
 * 6. 시스템 공통 함수
 */
function addLog(id, url, isOther) {
    const d = document.createElement('div');
    d.className = 'msg';
    d.innerHTML = `
        <div style="font-size: 0.8rem; margin-bottom: 4px; opacity: 0.8;">
            ${isOther ? '🐧 Peer:' : '👤 Me:'} <strong>${id}</strong>
        </div>
        <audio src="${url}" controls style="width:100%; height: 32px;"></audio>
    `;
    if(lBox) lBox.prepend(d);
}