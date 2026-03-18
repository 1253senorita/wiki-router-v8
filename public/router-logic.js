/* [router-logic.js] - 가입, 방 관리 및 슬라이딩 UI 제어 */

// 1. DOM 요소 캐싱 (HTML ID와 정확히 매칭)
const authScreen = document.getElementById('auth-screen');
const unlockScreen = document.getElementById('unlock');
const joinBtn = document.getElementById('join-btn');
const masterSwitch = document.getElementById('master-switch');
const handle = document.getElementById('slide-handle');
const fill = document.getElementById('slide-fill');
const statusSub = document.getElementById('status-sub');
const mainStatus = document.getElementById('status');

let currentRoom = 'Lobby';

// [가입 로직]
joinBtn.onclick = () => {
    const idVal = document.getElementById('join-id').value;
    if(idVal.trim() === "") return alert("ID를 입력하세요!");
    
    authScreen.style.display = 'none';
    unlockScreen.style.display = 'flex';

    // 🍎 비밀방 배정 및 서버 통지
    currentRoom = 'Secret_Lab_01';
    socket.emit('join-room', currentRoom);
    mainStatus.innerText = `💎 ROOM: ${currentRoom}`;
};

// [언락 화면]
unlockScreen.onclick = function() {
    this.style.display = 'none';
    mainStatus.innerText = `💎 ROOM: ${currentRoom}`;
};

/* --- 🍎 핵심: 슬라이딩 스위치 & 전화 종료 핸들링 --- */
masterSwitch.onclick = () => {
    if (isBearActive) {
        // [1] 스위치 OFF (비활성화)
        isBearActive = false;
        
        // 🚨 통화 중이었다면 즉시 종료 (Core의 함수 호출)
        if(isBusy) {
            stopBear(); // router-core.js에 정의된 종료 함수
        }

        // [2] 슬라이딩 애니메이션 및 아이콘 변경
        handle.style.left = '5px';           // 왼쪽으로 이동
        handle.innerText = "🔌";             // 플러그 아이콘으로 변경
        fill.style.width = '0%';             // 초록색 게이지 제거
        fill.style.background = '#ff4757';   // 배경색 빨강으로 준비
        
        // [3] 텍스트 및 버튼 상태 변경
        statusSub.innerText = "BEAR-OFFLINE";
        statusSub.style.color = "#ff4757";
        bStat.innerText = "OFF";
        
        // 버튼 잠금 (실수로 눌리지 않게)
        bTrig.style.opacity = "0.3";
        bTrig.style.pointerEvents = "none";

    } else {
        // [1] 스위치 ON (활성화)
        isBearActive = true;

        // [2] 슬라이딩 애니메이션 및 아이콘 변경
        handle.style.left = '70px';          // 오른쪽으로 이동 (기존 70px 유지)
        handle.innerText = "📞";             // 전화기 아이콘으로 변경
        fill.style.width = '100%';           // 게이지 꽉 채우기
        fill.style.background = '#2ecc71';   // 초록색 활성화
        
        // [3] 텍스트 및 버튼 상태 변경
        statusSub.innerText = "💎 ONLINE";
        statusSub.style.color = "#00ff00";
        bStat.innerText = "READY";
        
        // 버튼 잠금 해제
        bTrig.style.opacity = "1";
        bTrig.style.pointerEvents = "auto";
    }
};


/* [router-logic.js 맨 아래에 추가하면 좋은 안전장치] */

// 마우스가 버튼 밖에서 떼져도 무전기 녹음이 멈추도록 보장
window.addEventListener('mouseup', () => {
    if (typeof stopPeng === "function") stopPeng();
});

// 모바일에서 화면을 벗어나면 종료
window.addEventListener('touchend', () => {
    if (typeof stopPeng === "function") stopPeng();
});



/* --- [버튼 이벤트 연결] --- */

// Bear: 토글식 전화기 (클릭)
bTrig.onclick = toggleBear; 

// Peng: PTT 무전기 (누르고 떼기)
pTrig.onmousedown = startPeng;
pTrig.onmouseup = stopPeng;

// 터치 대응
pTrig.ontouchstart = (e) => { e.preventDefault(); startPeng(); };
pTrig.ontouchend = (e) => { e.preventDefault(); stopPeng(); };

// 로그 삭제
document.getElementById('clear-btn').onclick = () => {
    socket.emit('clear-logs-signal');
    lBox.innerHTML = '';
};