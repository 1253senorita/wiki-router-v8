/* [router-logic.js] - UI 컨트롤 및 엔진 브릿지 (v5.2 통합본) */

// 1. DOM 요소 캐싱
const authScreen = document.getElementById('auth-screen');
const unlockScreen = document.getElementById('unlock');
const joinBtn = document.getElementById('join-btn');
const masterSwitch = document.getElementById('master-switch');
const handle = document.getElementById('slide-handle');
const fill = document.getElementById('slide-fill');
const statusSub = document.getElementById('status-sub');
const mainStatus = document.getElementById('status');

/**
 * 2. 가입 및 시스템 시작
 */
joinBtn.onclick = () => {
    const roomId = document.getElementById('join-room-id').value.trim();
    const userId = document.getElementById('join-id').value.trim();
    
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
        mainStatus.style.color = "#00ff00";
    }
};

/**
 * 3. 언락 화면 (브라우저 오디오 정책 대응)
 */
unlockScreen.onclick = function() {
    this.style.display = 'none';
    console.log("🍎 WIKI-ROUTER UI UNLOCKED");
};

/**
 * 4. 슬라이딩 마스터 스위치 (Bear 통화 권한 제어)
 */
masterSwitch.onclick = () => {
    if (isBearActive) {
        // [OFF 상태로 전환]
        isBearActive = false;
        if(isBusy) stopBear(); // 통화 중이면 강제 종료

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
        bStat.innerText = `READY (${currentRoom})`;
        
        bTrig.style.opacity = "1";
        bTrig.style.pointerEvents = "auto";
    }
};

/**
 * 5. 실시간 접속자 명단 업데이트 UI
 */
function updateUserListUI(list) {
    const listBox = document.getElementById('user-list-box');
    const countTag = document.getElementById('user-count');
    if (!listBox || !countTag) return;

    listBox.innerHTML = ""; // 기존 명단 비우기
    countTag.innerText = list.length;

    list.forEach(user => {
        const item = document.createElement('div');
        item.style.cssText = `
            padding: 12px 15px;
            background: var(--bg);
            border-radius: 20px;
            box-shadow: 6px 6px 12px #bebebe, -6px -6px 12px #ffffff;
            font-size: 0.85rem;
            display: flex;
            align-items: center;
            justify-content: space-between;
            animation: fadeIn 0.3s ease;
        `;
        
        const isMe = user.userId === myUserId;
        item.innerHTML = `
            <span style="font-weight: ${isMe ? 'bold' : 'normal'}; color: ${isMe ? 'var(--bear)' : 'inherit'};">
                ${isMe ? '⭐' : '👤'} ${user.userId}
            </span>
            <span style="font-size: 0.6rem; color: #2ecc71; font-weight: bold;">● LIVE</span>
        `;
        listBox.appendChild(item);
    });
}

/**
 * 6. 오디오 엔진(Bear/Peng) 이벤트 연결
 */
// Bear (Talk 버튼)
bTrig.onclick = () => { if(typeof toggleBear === 'function') toggleBear(); };

// Peng (PTT 무전기)
const handleStop = (e) => { 
    if(e) e.preventDefault();
    if (typeof stopPeng === "function") stopPeng(); 
};

pTrig.onmousedown = () => { if(typeof startPeng === 'function') startPeng(); };
pTrig.onmouseup = handleStop;
pTrig.ontouchstart = (e) => { e.preventDefault(); if(typeof startPeng === 'function') startPeng(); };
pTrig.ontouchend = handleStop;

// 전역 안전장치 (마우스 뗐을 때 녹음 중단)
window.addEventListener('mouseup', () => { if (typeof stopPeng === "function") stopPeng(); });

/**
 * 7. 로그 삭제 버튼 (엔진과 동기화)
 */
document.getElementById('clear-btn').onclick = () => {
    if(!isAuthenticated) return;
    if(confirm("모든 로그와 서버 파일을 삭제하시겠습니까?")) {
        socket.emit('clear-logs-signal');
    }
};