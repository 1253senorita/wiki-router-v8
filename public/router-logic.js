/* [router-logic.js] - UI 컨트롤 및 엔진 브릿지 */

// 1. DOM 요소 캐싱
const authScreen = document.getElementById('auth-screen');
const unlockScreen = document.getElementById('unlock');
const joinBtn = document.getElementById('join-btn');
const masterSwitch = document.getElementById('master-switch');
const handle = document.getElementById('slide-handle');
const fill = document.getElementById('slide-fill');
const statusSub = document.getElementById('status-sub');
const mainStatus = document.getElementById('status');

// 2. 가입 및 시스템 시작 (중복 로직 통합)
joinBtn.onclick = () => {
    const roomId = document.getElementById('join-room-id').value;
    const userId = document.getElementById('join-id').value;
    
    if(!roomId || !userId) {
        alert("ROOM 이름과 ID를 모두 입력해주세요!");
        return;
    }

    // [Core 엔진 호출] 실제 입력값 전달
    if(typeof joinRoom === 'function') {
        joinRoom(roomId, userId);
        
        // UI 레이어 전환
        authScreen.style.display = 'none';
        unlockScreen.style.display = 'flex';
        mainStatus.innerText = `💎 ROOM: ${roomId}`;
    }
};

// 3. 언락 화면 (오디오 엔진 가동)
unlockScreen.onclick = function() {
    this.style.display = 'none';
    // 브라우저 오디오 컨텍스트 재개 (필요 시 core에서 처리)
    console.log("🍎 WIKI-ROUTER UI UNLOCKED");
};

// 4. 슬라이딩 마스터 스위치 (Bear 통화 권한 제어)
masterSwitch.onclick = () => {
    if (isBearActive) {
        // [OFF 상태로 전환]
        isBearActive = false;
        
        if(isBusy) stopBear(); // 통화 중이면 즉시 강제 종료

        handle.style.left = '5px';
        handle.innerText = "🔌";
        fill.style.width = '0%';
        fill.style.background = '#ff4757';
        
        statusSub.innerText = "BEAR-OFFLINE";
        statusSub.style.color = "#ff4757";
        bStat.innerText = "OFF";
        
        bTrig.style.opacity = "0.3";
        bTrig.style.pointerEvents = "none";
    } else {
        // [ON 상태로 전환]
        isBearActive = true;

        handle.style.left = '70px';
        handle.innerText = "📞";
        fill.style.width = '100%';
        fill.style.background = '#2ecc71';
        
        statusSub.innerText = "💎 ONLINE";
        statusSub.style.color = "#00ff00";
        bStat.innerText = "READY";
        
        bTrig.style.opacity = "1";
        bTrig.style.pointerEvents = "auto";
    }
};

// 5. 무전기(Peng) PTT 안전장치
const handleStop = () => { if (typeof stopPeng === "function") stopPeng(); };
window.addEventListener('mouseup', handleStop);
window.addEventListener('touchend', handleStop);

// 6. 버튼 이벤트 최종 연결
bTrig.onclick = () => { if(typeof toggleBear === 'function') toggleBear(); };

pTrig.onmousedown = () => { if(typeof startPeng === 'function') startPeng(); };
pTrig.onmouseup = handleStop;

pTrig.ontouchstart = (e) => { e.preventDefault(); if(typeof startPeng === 'function') startPeng(); };
pTrig.ontouchend = (e) => { e.preventDefault(); handleStop(); };

// 7. 로그 전체 삭제 (동기화 신호 포함)
document.getElementById('clear-btn').onclick = () => {
    if(confirm("모든 로그와 파일을 삭제하시겠습니까?")) {
        socket.emit('clear-logs-signal');
        lBox.innerHTML = '';
    }
};