/* [router-core.js] - 핵심 통신 엔진 (전화기 모드 업그레이드) */
const socket = io();
const peer = new Peer({ host: location.hostname, port: location.port || 3000, path: '/peerjs' });
let myStream, calls = [], rec, chunks = [], isBusy = false;
let isBearActive = true; 

// DOM 요소 캐싱
const bStat = document.getElementById('b-stat');
const bTrig = document.getElementById('b-trig');
const pTrig = document.getElementById('p-trig');
const lBox = document.getElementById('l-box');

// 피어 등록
peer.on('open', (id) => socket.emit('register-peer', id));

// 수신 처리 (상대가 나에게 전화를 걸 때)
peer.on('call', (c) => {
    c.answer();
    c.on('stream', (s) => { 
        const a = new Audio(); 
        a.srcObject = s; 
        a.play(); 
    });
});

/* --- [Bear: 실시간 전화 모드 로직] --- */
const toggleBear = async () => {
    if(!isBearActive) return;

    if (!isBusy) {
        // [ON] 통화 시작
        isBusy = true;
        bStat.innerText = "ON CALL...";
        bTrig.innerText = "END"; // 버튼 글자를 END로 변경
        bTrig.style.filter = "brightness(0.7)"; // 눌려있는 느낌 주기
        
        if (!myStream) myStream = await navigator.mediaDevices.getUserMedia({ audio: true });
        socket.emit('get-peers'); // 서버에 접속자 요청 -> 명단 받으면 'peer-list' 이벤트 실행됨
    } else {
        // [OFF] 통화 종료
        stopBear();
    }
};

const stopBear = () => {
    isBusy = false;
    bStat.innerText = isBearActive ? "READY" : "OFF";
    bTrig.innerText = "TALK";
    bTrig.style.filter = "none";
    
    // 연결된 모든 상대방 전화 끊기
    calls.forEach(c => c.close());
    calls = [];
};

/* --- [Peng: 무전기 녹음 모드] --- */
const startPeng = async () => {
    if(isBusy && bTrig.innerText === "END") return; // 전화 중이면 펭귄 금지
    chunks = [];
    const s = await navigator.mediaDevices.getUserMedia({ audio: true });
    rec = new MediaRecorder(s, { mimeType: 'audio/webm' });
    rec.ondataavailable = e => chunks.push(e.data);
    rec.onstop = () => {
        const blob = new Blob(chunks, { type: 'audio/webm' });
        socket.emit('sync-audio-file', { blob: blob });
        addLog("나", URL.createObjectURL(blob), false);
        s.getTracks().forEach(t => t.stop());
    };
    rec.start();
};

const stopPeng = () => {
    if(rec && rec.state === "recording") rec.stop();
};

// 소켓 이벤트: 명단 수신 시 자동 전화 걸기
socket.on('peer-list', (list) => {
    list.forEach(tid => { 
        if(tid !== peer.id) {
            const call = peer.call(tid, myStream);
            calls.push(call);
        }
    });
});

socket.on('receive-sync-audio', (d) => {
    const url = URL.createObjectURL(new Blob([d.blob], { type: 'audio/webm' }));
    addLog(d.id, url, true);
    new Audio(url).play().catch(() => {});
});

// 공통 로그 함수
function addLog(id, url, isOther) {
    const d = document.createElement('div');
    d.className = 'msg';
    d.innerHTML = `<strong>${isOther?'🐧':'👤'} ${id}</strong><audio src="${url}" controls style="width:100%;"></audio>`;
    lBox.prepend(d);
}