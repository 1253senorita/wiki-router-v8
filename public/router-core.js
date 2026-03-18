/* [router-core.js] - 핵심 통신 엔진 (v5.2 룸/아이디 섹션 업그레이드) */
const socket = io();
const peer = new Peer({ host: location.hostname, port: location.port || 3000, path: '/peerjs' });
let myStream, calls = [], rec, chunks = [], isBusy = false;
let isBearActive = true; 

/* --- [추가된 세션 상태 변수] --- */
let currentRoom = null;
let myUserId = null; 
let isAuthenticated = false; // 원칙 2: Boolean-Gated Execution

// DOM 요소 캐싱
const bStat = document.getElementById('b-stat');
const bTrig = document.getElementById('b-trig');
const pTrig = document.getElementById('p-trig');
const lBox = document.getElementById('l-box');





/* [router-core.js] */

const joinRoom = (roomId, userId) => {
    if(!roomId || !userId) return;
    
    currentRoom = roomId; // 전역 변수에 저장
    myUserId = userId;
    isAuthenticated = true;
    
    // 서버로 사용자가 입력한 실제 룸 ID 전달
    socket.emit('join-room', { roomId, userId, peerId: peer.id });
    
    // UI에 현재 접속된 방 이름 표시
    if(bStat) bStat.innerText = `ROOM: ${roomId} (${userId})`;
};




// 피어 등록
peer.on('open', (id) => {
    // 룸 입장 전에는 대기, 입장 시 socket.emit('join-room')에서 처리됨
    console.log('My Peer ID:', id);
});

// 수신 처리
peer.on('call', (c) => {
    if(!isAuthenticated) return; // 인증되지 않은 요청 차단
    c.answer();
    c.on('stream', (s) => { 
        const a = new Audio(); 
        a.srcObject = s; 
        a.play(); 
    });
});

/* --- [Bear: 실시간 전화 모드 로직] --- */
const toggleBear = async () => {
    if(!isBearActive || !isAuthenticated) return; // 원칙 2 준수

    if (!isBusy) {
        isBusy = true;
        bStat.innerText = "ON CALL...";
        bTrig.innerText = "END";
        bTrig.style.filter = "brightness(0.7)";
        
        if (!myStream) myStream = await navigator.mediaDevices.getUserMedia({ audio: true });
        
        // 중요: 전체가 아닌 '현재 룸'의 명단만 요청
        socket.emit('get-room-peers', currentRoom); 
    } else {
        stopBear();
    }
};

const stopBear = () => {
    isBusy = false;
    bStat.innerText = currentRoom ? `READY (${currentRoom})` : "READY";
    bTrig.innerText = "TALK";
    bTrig.style.filter = "none";
    calls.forEach(c => c.close());
    calls = [];
};

/* --- [Peng: 무전기 녹음 모드] --- */
const startPeng = async () => {
    if(!isAuthenticated || (isBusy && bTrig.innerText === "END")) return; 
    
    chunks = [];
    const s = await navigator.mediaDevices.getUserMedia({ audio: true });
    rec = new MediaRecorder(s, { mimeType: 'audio/webm' });
    rec.ondataavailable = e => chunks.push(e.data);
    rec.onstop = () => {
        const blob = new Blob(chunks, { type: 'audio/webm' });
        
        // 중요: 룸 정보와 발신자 아이디 포함
        socket.emit('sync-audio-file', { 
            blob: blob, 
            roomId: currentRoom, 
            senderId: myUserId 
        });
        
        addLog(myUserId, URL.createObjectURL(blob), false);
        s.getTracks().forEach(t => t.stop());
    };
    rec.start();
};

const stopPeng = () => {
    if(rec && rec.state === "recording") rec.stop();
};

/* --- [소켓 이벤트 핸들러] --- */
// 룸 내의 피어 명단 수신 시 자동 연결
socket.on('room-peer-list', (list) => {
    list.forEach(tid => { 
        if(tid !== peer.id) {
            const call = peer.call(tid, myStream);
            calls.push(call);
        }
    });
});


/* 수정 제안: router-core.js 내의 peer-list 수신부 */
socket.on('room-peer-list', (list) => {
    list.forEach(tid => { 
        if(tid !== peer.id) {
            const call = peer.call(tid, myStream);
            if(call) { // call 객체가 정상 생성된 경우에만 push
                calls.push(call);
                // 상대방이 끊었을 때 리스트에서 제거하는 로직 (선택사항)
                call.on('close', () => {
                    calls = calls.filter(c => c.peer !== tid);
                });
            }
        }
    });
});

/* --- [System: 로그 동기화 삭제] --- */

// 1. 삭제 버튼 클릭 이벤트 연결 (UI 레이어에서 호출하거나 여기서 직접 연결)
document.getElementById('clear-btn').onclick = () => {
    if(!isAuthenticated) return;
    
    if(confirm("현재 서버의 모든 녹음 기록과 로그를 삭제하시겠습니까?")) {
        socket.emit('clear-logs-signal'); // 서버로 삭제 신호 송신
    }
};

// 2. 서버로부터 삭제 알림 수신 시 UI 업데이트
socket.on('logs-cleared-notification', (data) => {
    if(lBox) {
        lBox.innerHTML = ""; // 로그 박스 비우기
        
        // 시스템 알림 메시지 (선택 사항)
        const notice = document.createElement('div');
        notice.style.cssText = "text-align:center; color:#888; font-size:0.8rem; padding:10px;";
        notice.innerText = `🧹 ${data.by}님에 의해 로그가 초기화되었습니다.`;
        lBox.appendChild(notice);
    }
    console.log(`Log cleared by: ${data.by}`);
});





socket.on('receive-sync-audio', (d) => {
    // 같은 방 데이터인지 확인은 서버에서 처리하지만, 클라이언트에서도 아이디 매칭
    const url = URL.createObjectURL(new Blob([d.blob], { type: 'audio/webm' }));
    addLog(d.senderId, url, true);
    new Audio(url).play().catch(() => {});
});

// 공통 로그 함수 (디자인 유지)
function addLog(id, url, isOther) {
    const d = document.createElement('div');
    d.className = 'msg';
    // 유저별 섹션 구분을 위해 ID 강조
    d.innerHTML = `
        <div style="font-size: 0.8rem; margin-bottom: 4px; opacity: 0.8;">
            ${isOther ? '🐧 Peer:' : '👤 Me:'} <strong>${id}</strong>
        </div>
        <audio src="${url}" controls style="width:100%; height: 32px;"></audio>
    `;
    lBox.prepend(d);
}