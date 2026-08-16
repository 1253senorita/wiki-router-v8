# 🚀 WIKI-ROUTER Clean Architecture & Hilt RoadMap

## 🎯 Target Architecture & Strategy
- **Structure**: Clean Architecture 3-Tier (`presentation` ➔ `domain`  `data`)
- **Pattern**: MVVM + Coroutines StateFlow / SharedFlow
- **Dependency Injection**: Hilt
- **Strategy**:
  - **Native First**: 메인 화면은 순수 네이티브 UI/기능으로 가볍고 빠르게 구축
  - **Modularized Section**: Flutter WIKI-ROUTER 엔진은 별도 전용 섹션(PttSectionActivity)으로 완전 분리 연동

---

## 📌 Phase 1. 패키지 구조 재편성 (Package Restructuring)
- [] 패키지 폴더 3개 생성 (`domain`, `data`, `presentation`)
- [ ] `presentation` 하위 패키지 생성 (`splash`, `auth`, `main`, `ptt`)
- [ ] Activity 3개 안전하게 이동 (`Refactor` / Safe Move):
  - `SplashActivity` ➔ `presentation/splash/`
  - `AuthActivity` ➔ `presentation/auth/`
  - `MainActivity` ➔ `presentation/main/`
- [ ] 빌드 테스트 및 `AndroidManifest.xml` 경로 자동 변경 확인

## 📌 Phase 2. Hilt 의존성 주입 구축 (Hilt Integration)
- [ ] Hilt 라이브러리 추가 (`libs.versions.toml`, `build.gradle.kts`)
- [ ] 최상위 Application 클래스 생성 (`MyPadNoteApplication.kt`) 및 `@HiltAndroidApp` 적용
- [ ] `AndroidManifest.xml`에 `MyPadNoteApplication` 등록
- [ ] Activity들에 `@AndroidEntryPoint` 어노테이션 추가

## 📌 Phase 3. Domain & Data 계층 구축 (Clean Architecture Core)
- [ ] **Domain**: `AuthRepository` 인터페이스 및 `SignOutUseCase` 정의
- [ ] **Data**: `FirebaseAuthDataSource` 및 `AuthRepositoryImpl` 구현
- [ ] **DI**: `AuthModule` 생성하여 Hilt 객체 주입 레시피 작성

## 📌 Phase 4. Pure Native Main UI & State Management
- [ ] `MainViewModel` 생성 및 `SignOutUseCase` 주입
- [ ] **Native Main UI Frame 설계 (`activity_main.xml`)**:
  - 순수 네이티브 헤더, 제어 버튼, 프로필/설정 영역 레이아웃 구성
  - **`🚀 WIKI-ROUTER PTT 열기`** 섹션 이동 버튼 배치
- [ ] **UI State & Event 라인 연결**:
  - ViewModel의 `StateFlow`를 통해 Native UI 상태 동기화
- [ ] **Native Dialog & BottomSheet 구축**:
  - 권한 안내 및 네이티브 설정 팝업 UI 개발
- [ ] `enableEdgeToEdge()` 및 `WindowInsets` 패딩 적용 (상단바/펀치홀 버튼 겹침 완벽 해결)

## 📌 Phase 5. Independent WIKI-ROUTER PTT Section (별도 페이지)
- [ ] `PttSectionActivity` 및 레이아웃 생성 (`presentation/ptt/`)
- [ ] `MainActivity` ➔ `PttSectionActivity` 화면 이동 라인 연결
- [ ] `PttSectionActivity` 내 WebView 배치 및 기본 세팅
- [ ] `WebAppInterface` (JS <-> Native Bridge) 연동 (마이크 권한 및 백그라운드 이벤트)
- [ ] Flutter Web PTT Engine URL 로드 및 통신 디버깅

## 📌 Phase 6. Data Expansion (Firebase & Storage)
- [ ] Firebase Firestore / Storage용 DataSource 및 Repository 추가 (`data/datasource/remote/`)
- [ ] Local DB (DataStore / Room) 필요시 `data/datasource/local/` 확장

===============


## 🛠️ [시스템 메인보드 블록 다이어그램 (전기 회로 구성)]

```text
       [ 외부 전원 및 데이터 버스 (Manifests / Res) ]
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │        [ DI (의존성 주입 전원 공급기) ]          │
        │             (AuthModule 등)           │
        └───────────────────┬───────────────────┘
                            │ (전력 및 바이어스 공급)
        ┌───────────────────┴───────────────────┐
        │          메인 버스 시스템 (Data)         │
        │   ┌───────────────────────────────┐   │
        │   │  Remote DataSource / Repo     │   │
        │   └───────────────┬───────────────┘   │
        └───────────────────┼───────────────────┘
                            │ (데이터 신호선)
        ┌───────────────────┴───────────────────┐
        │        코어 로직 칩셋 (Domain)         │
        │   ┌───────────────────────────────┐   │
        │   │  Repository Interface / Usecase│  │
        │   └───────────────┬───────────────┘   │
        └───────────────────┼───────────────────┘
                            │ (제어 명령 신호)
        ┌───────────────────┴───────────────────┐
        │       프론트엔드 인터페이스 (Presentation) │
        │   (AeroRouter / Auth / Main / Splash) │
        └───────────────────┬───────────────────┘
                            │ (백그라운드 인터럽트)
        ┌───────────────────┴───────────────────┐
        │        서브 루틴 모듈 (Service)         │
        └───────────────────────────────────────┘

```

---

## ⚡ 패키지별 회로 동작 및 부품 설명

### 1. 전원 공급 및 배선 부품 (`manifests`, `res`)

* **회로 부품:** 시스템 버스 바(Bus Bar) 및 자원 하니스(Harness)
* **회로 동작:** 앱 전체에 필요한 권한, 하드웨어 설정, 그리고 UI 렌더링에 필요한 리소스(레이아웃, 이미지 등)를 각 회로 블록에 안정적으로 공급하는 기본 배선 역할을 합니다.

### 2. 전력 제어 및 의존성 주입 회로 (`di`)

* **회로 부품:** PMIC (전력 관리 IC) 및 모듈 소켓 (`AuthModule`)
* **회로 동작:** 시스템 내부에 필요한 객체(인스턴스)들을 필요한 부품 위치에 알맞은 전압과 타이밍으로 자동 공급(`Hilt`)하여, 각 회로가 서로 강하게 얽히지(Decoupling) 않고 독립적으로 작동하도록 제어합니다.

### 3. 데이터 송수신 및 인터페이스 회로 (`data`)

* **회로 부품:** 외부 통신 송수신기 및 메모리 버퍼 (`datasource.remote`, `repository`)
* **회로 동작:** 외부 서버 또는 클라우드 DB와 직접 통신하여 raw 데이터를 받아오는 데이터 통로입니다. 외부 노이즈나 네트워크 변화에 대응하며 데이터를 수집합니다.

### 4. 중앙 연산 및 논리 게이트 회로 (`domain`)

* **회로 부품:** 중앙 처리 장치(CPU) 코어 및 순수 로직 게이트 (`repository`, `usecase`)
* **회로 동작:** 프레임워크나 외부 기술에 종속되지 않는 순수 비즈니스 로직(Pure Logic)이 집적된 핵심 칩셋입니다. 데이터가 어떻게 가공되고 판정될지 결정하는 순수 회로 영역입니다.

### 5. 사용자 입출력 및 디스플레이 회로 (`presentation`)

* **회로 부품:** I/O 인터페이스 및 디스플레이 드라이버 IC (`aerorouter`, `auth`, `main`, `splash`)
* **회로 동작:** 사용자가 직접 눈으로 보고 조작하는 패널과 연결됩니다.
* 스플래시 및 인증 회로(`splash`, `auth`)로 초기 진입 제어를 거쳐, 메인 대시보드(`main`)와 무전기 코어(`aerorouter`)라는 최종 출력 장치로 신호를 전달합니다.



### 6. 백그라운드 인터럽트 회로 (`service`)

* **회로 부품:** 타이머 인터럽트 및 상시 대기 릴레이(`PttService`)
* **회로 동작:** 앱이 화면 뒤로 넘어가거나 메인 UI가 꺼져 있어도, 마이크 센서 신호와 실시간 통신을 끊기지 않게 유지하는 상시 구동(Foreground) 하부 릴레이 회로입니다.




**********



[학습 자료] 



BaroBaro 데이터 흐름 및 상태 관리 전략


1. 개요
   본 문서는 BaroBaroRepository 인터페이스를 통해 데이터를 저장하고, ViewModel이 이를 StateFlow로 관리하여 UI(Compose)에 반영하는 데이터 파이프라인을 다룹니다.

2. 데이터 흐름도
   사용자의 입력이 데이터베이스/서버에 저장되고, 화면에 반영되는 순서는 다음과 같습니다:

UI Layer: 사용자가 데이터를 입력하고 버튼을 클릭합니다.

ViewModel: addOrder 명령을 실행합니다.

Repository: 실제 비즈니스 로직 및 데이터 저장(DB/Network)을 수행합니다.

State Update: ViewModel이 최신 데이터를 다시 fetch하여 _orderList를 갱신합니다.

UI Recomposition: collectAsState 중인 Compose 화면이 변경된 데이터를 감지하여 리스트를 재구성합니다.

3. 핵심 코드 구조
   Repository Interface
   데이터 접근 계층의 규약입니다.

Kotlin
interface BaroBaroRepository {
suspend fun getOrders(): List<DispatchOrder>
suspend fun addOrder(order: DispatchOrder)
suspend fun updateOrderStatus(orderId: String, status: String)
}
ViewModel Logic
상태 관리를 담당하는 핵심부입니다.

Kotlin
// 1. 상태 선언
private val _orderList = MutableStateFlow<List<DispatchOrder>>(emptyList())
val orderList: StateFlow<List<DispatchOrder>> = _orderList.asStateFlow()

// 2. 데이터 추가 및 갱신 흐름
fun addNewOrder(order: DispatchOrder) {
viewModelScope.launch {
// 비동기 작업 수행
repository.addOrder(order)

        // 데이터 저장 후 최신 리스트 동기화
        val updatedList = repository.getOrders()
        _orderList.value = updatedList
    }
}
4. 학습 포인트
   단일 진실 공급원(Single Source of Truth): _orderList를 MutableStateFlow로 관리함으로써 데이터의 변경 지점을 단일화하여 유지보수성을 높였습니다.

비동기 처리: suspend 키워드와 viewModelScope.launch를 사용하여 UI 스레드 차단 없이 네트워크/DB 작업을 안전하게 처리합니다.

반응형 UI: 데이터가 변경될 때마다 UI가 별도의 조작 없이 자동으로 업데이트되는 Compose의 핵심 원리가 이 흐름에 녹아있습니다.

이 내용을 바탕으로 작성하신 코드를 검토해 보시면 데이터 흐름이 더 명확하게 보이실 겁니다. 학습하시다가 궁금한 점이 생기면 언제든 "보미"를 불러주세요!

(문서 생성이 완료되었습니다. 이 내용은 추후 개발 참고 자료로 활용하세요.)






@@@@@@@@@@@@@@



가상 룸 ID(ROOM_TEST_X)와 함께 매칭 룸 화면으로 전환되도록 네비게이션을 연결했던 핵심 로직 코드는 다음과 같습니다.



1. 상세 화면 (BaroBaroDetailScreen.kt)에서 네비게이션 호출부
   상세 화면에서 콜 수락 버튼이나 테스트 버튼을 눌렀을 때, MainNavigator를 통해 가상 룸 ID를 담아 프래그먼트 전환(내비게이션)을 트리거하는 핵심 로직입니다.

Kotlin
// BaroBaroDetailScreen.kt (예시 핵심 로직)
Button(
onClick = {
val testRoomId = "ROOM_TEST_X"
// 네비게이터를 통해 가상 룸 ID를 전달하며 매칭 룸 화면으로 이동
navigator.navigateToMatchingRoom(testRoomId)
}
) {
Text("강제 세컨드 룸 통신 테스트")
}
2. 네비게이터 (MainNavigator.kt)에서의 화면 전환 라우팅 처리
   전달받은 룸 ID를 인자로 하여 MatchingRoomFragment를 생성하고, 프래그먼트 트랜잭션을 통해 화면을 교체하는 로직입니다.

Kotlin
// MainNavigator.kt (예시 핵심 로직)
fun navigateToMatchingRoom(roomId: String) {
val fragment = MatchingRoomFragment.newInstance(roomId)

    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .addToBackStack(null)
        .commit()
}
3. 매칭 룸 프래그먼트 (MatchingRoomFragment.kt)에서 룸 ID 수신부
   전달받은 ROOM_TEST_X 값을 Arguments를 통해 안전하게 꺼내어 뷰모델 초기화 및 통신에 사용하는 로직입니다.

Kotlin
// MatchingRoomFragment.kt (예시 핵심 로직)
class MatchingRoomFragment : Fragment() {

    private val viewModel: MatchingRoomViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 전달된 가상 룸 ID 받기
        val roomId = arguments?.getString(ARG_ROOM_ID) ?: "DEFAULT_ROOM"
        
        // 뷰모델에 룸 ID 전달하여 통신/소켓 연결 초기화
        viewModel.joinMatchingRoom(roomId)
    }

    companion object {
        private const val ARG_ROOM_ID = "arg_room_id"

        fun newInstance(roomId: String): MatchingRoomFragment {
            return MatchingRoomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ROOM_ID, roomId)
                }
            }
        }
    }
}



==================


천하는 최적화 폴더 구조 플랜
현재 com.terminator.mypadnoteone 아래에 있는 패키지들을 이렇게 역할에 맞게 딱 정리해 두시면 코딩할 때 길을 잃지 않습니다.

Plaintext
com.terminator.mypadnoteone/
│
├── data/                      # 데이터 레이어 (서버, 로컬 DB, 소켓 통신)
│   ├── datasource/remote/     # 소켓 / API 통신 담당
│   └── repository/            # ⭐ [위키 라우터 코어 엔진 위치] 실제 소켓 통신 및 데이터 처리
│
├── di/                        # 의존성 주입 (Hilt Modules)
│   ├── AuthModule.kt
│   └── BaroBaroModule.kt
│
├── domain/                    # 도메인 레이어 (핵심 비즈니스 로직, 모델, 유즈케이스)
│   ├── model/
│   ├── repository/            # 인터페이스 정의
│   └── usecase/
│
└── presentation/              # 프레젠테이션 레이어 (UI, 뷰, 뷰모델)
├── baroBaro/              # 화면 및 UI 컴포넌트 (Detail, Room 등)
│   ├── detail/
│   └── room/
└── main/                  # MainActivity, MainNavigator 등
🛠️ 앞으로 우리가 손봐야 할 핵심 포인트
data/repository/ 레이어 집중 공략

여기에 기존 위키 라우터의 소켓 엔진 및 데이터 핑퐁 로직을 심어둡니다.

UI(View)는 여기를 직접 건드리지 않고, Domain의 UseCase를 통해서만 데이터를 주고받게 철저히 격리합니다.

AI 보미 후킹 인터페이스 위치

데이터가 data/repository/를 통해 들어오고 나갈 때 가로챌 수 있는 인터셉트/후킹 콜백 메서드를 이 데이터 레이어 진입점에 꽂아두면 완벽합니다.

오빠, 지금 구조 자체는 이미 현업 표준 클린 아키텍처를 잘 따르고 있으니 이 토대 위에서 데이터 통신 레이어만 살을 덧붙여 나가면 됩니다. 이대로 계속 진행해 볼까요 오빠? ☕️👍



@@@@@@@@@@@@@







오빠, 완벽한 표준 아키텍처 토대 위에서 앞으로 우리가 한 단계씩 밟아 나갈 **전체 구현 로드맵**을 보기 쉽게 정리해 드립니다! 🎯

---

## 🗺️ WIKI-ROUTER + AI 보미 통합 프로젝트 전체 로드맵

### [1단계] 네이티브 통제권 확립 (View & Permission Layer)

* **목표:** 웹뷰의 한계와 브라우저 권한 제약을 완전히 깨부수고, 안드로이드 네이티브 단에서 마이크 및 시스템 권한을 완벽하게 장악합니다.
* **주요 작업:**
* `AndroidManifest.xml`에 필요한 오디오/네트워크 권한 최종 확인.
* 프레그먼트와 액티비티(`presentation/baroBaro`, `presentation/main`) 간의 네이티브 화면 전환 및 마이크 입력 세팅 점검.



### [2단계] 데이터 레이어에 위키 라우터 코어 이식 (Data & Socket Repository)

* **목표:** UI와 통신을 철저히 분리하여, `data/repository/` 내부에서 위키 라우터의 핵심 소켓/네트워크 엔진이 쌩쌩 돌아가도록 구축합니다.
* **주요 작업:**
* 기존 위키 라우터의 소켓/데이터 핑퐁 엔진을 `data/datasource/remote` 및 `repository`에 안착.
* UI가 백그라운드로 가더라도 네트워크 파이프라인이 끊기지 않는 비동기 데이터 흐름 구성.



### [3단계] AI '보미' 후킹 인터페이스 장착 (Hooking & Filter Layer)

* **목표:** 데이터가 오고 가는 통로에 인터셉트(Hook) 포인트를 뚫어, AI 보미가 중간 메신저로서 필터링과 임시 홀드를 수행할 수 있게 만듭니다.
* **주요 작업:**
* 소켓 수신 지점에 `onInterceptMessage` 또는 콜백 후킹 메서드 구현.
* 단골 고객 하이패스 및 불필요한 콜 1차 필터링 로직을 `Domain` 및 `Repository` 레이어에 연동.



### [4단계] 통합 UI 바인딩 및 실전 필드 테스트 (Presentation & Test)

* **목표:** `ViewModel`을 통해 AI 보미의 필터링 상태와 위키 라우터의 실시간 데이터가 화면(`MatchingRoomScreen` 등)에 매끄럽게 그려지는지 검증합니다.
* **주요 작업:**
* 실제 기기 빌드 후 마이크 통신 및 AI 에이전트 응대 시나리오 테스트.
* 예외 상황(네트워크 끊김 등)에서의 로컬 Fallback 처리 및 안정성 확보.



---

이 로드맵을 기준 삼아서, 이제 1단계(네이티브 권한 및 기본 뷰 환경 정돈)부터 차근차근 코드를 만져나가면 됩니다 오빠! 다음으로 손볼 구체적인 코드를 열어볼까요? ☕️🚀


@@@@@@@@@@@@@@@@@@@@@


💡 오늘의 하이라이트 요약!
데이터 소스 (WikiRouterSocketDataSource): 소켓 통신 및 AI 보미의 1차 후킹 처리 (돼지 통로 꿀꺽! 🐷)

리포지토리 구현체 (WikiRouterRepositoryImpl): 데이터 소스를 감싸고 Hilt로 주입받는 실무형 저장소

도메인 인터페이스 (WikiRouterRepository): 클린 아키텍처를 지키기 위한 표준 규격서

유즈케이스 (WikiInterceptAndHoldOrderUseCase): 뷰모델에서 깔끔하게 갖다 쓸 비즈니스 로직 단위

이로써 위키 라우터와 AI 보미 에이전트를 위한 도메인 및 데이터 레이어의 뼈대가 완벽하게 조립되었습니다 오빠!





333333333333333



오빠가 구상 중인 온디바이스 AI 관제 시스템(MyPadNoteOne)을 완벽하게 완성하기 위한 **단계별 개발 로드맵**을 체계적으로 정리해 줄게!

이 순서대로 차근차근 빌드업해가면 복잡한 AI 기능도 에러 없이 단단하게 얹을 수 있어.

---

# 🗺️ 온디바이스 AI 관제 시스템 개발 로드맵

## Phase 1: 로컬 데이터 영구 저장소 구축 (Foundation)

* **목표**: 앱을 껐다 켜도 AI 대화 내역과 관제/오더 로그가 날아가지 않도록 로컬 DB 구축
* **주요 작업**:
* `Room Database` 의존성 추가 (`build.gradle`)
* 채팅 메시지(`ChatMessageEntity`) 및 관제 로그 데이터 구조(Entity, DAO) 설계
* `Repository` 패턴을 적용해 Clean Architecture 구조 맞추기



## Phase 2: 온디바이스 AI 라이브러리 및 엔진 의존성 추가 (AI Core)

* **목표**: 서버 통신 없이 기기 자체(On-Device)에서 동작하는 AI/LLM 또는 머신러닝 기반 마련
* **주요 작업**:
* 온디바이스 추론을 위한 라이브러리 선정 및 추가 (예: **MediaPipe LLM Inference** API 또는 **LiteRT / TFLite**)
* 앱 내에서 로컬 AI 모델 파일(.bin, .tflite 등)을 로드하고 실행할 수 있는 매니저 클래스 작성
* 텍스트 분석 및 간단한 오더 필터링을 온디바이스 모델로 처리하는 `UseCase` 구현



## Phase 3: ViewModel과 온디바이스 엔진 연결 (Business Logic)

* **목표**: UI와 온디바이스 AI 엔진을 `AIClientViewModel`을 통해 유기적으로 연결
* **주요 작업**:
* 오빠가 채팅창에 입력한 텍스트를 온디바이스 AI 모델에 전달해 실시간 응답 생성
* 위키 라우터에서 넘어오는 실시간 오더 데이터를 온디바이스 AI가 분석하여 '하이패스 통과' 또는 '보미 홀드'를 자동 판정하는 로직 고도화
* 대화 내역이 바뀔 때마다 Phase 1에서 만든 Room DB에 자동 저장되도록 연동



## Phase 4: UI/UX 고도화 및 음성(STT/TTS) 연동 확장 (Interface)

* **목표**: 사용자가 눈으로 보고 귀로 들으며 소통할 수 있는 완벽한 관제 인터페이스 완성
* **주요 작업**:
* `AIScreen` 채팅 UI 스크롤 최적화 및 로딩/상태 애니메이션 추가
* 안드로이드 내장 음성 인식(Speech-to-Text, STT)을 연동해 타이핑 없이 말로 AI 보미에게 명령 내리기 기능 추가
* AI의 답변을 음성(Text-to-Speech, TTS)으로 읽어주는 기능 검토



## Phase 5: 실기기 테스트 및 성능 최적화 (Testing & Release)

* **목표**: 실제 태블릿/스마트폰(Pad)에서 메모리 누수나 발열 없이 부드럽게 돌아가는지 검증
* **주요 작업**:
* 온디바이스 AI 구동 시 메모리(RAM) 사용량 최적화
* 백그라운드 상태에서도 위키 라우터와 관제 시스템이 안정적으로 유지되는지 테스트
* 최종 빌드 및 프로덕션 배포 준비



---

오빠, 이 로드맵 중에서 가장 먼저 시작하고 싶은 단계(예: **1단계 Room DB 구축**부터 할지, **2단계 온디바이스 AI 라이브러리 추가**부터 할지)를 골라주면 바로 구체적인 코드로 밀어줄게! 어떤 걸 먼저 해볼까? 💙


@@@@@@@@@@@@@@@@@

오빠, 우리가 이야기한 트리 형태의 계층 구조와 데이터 흐름(바통 전달 방식)을 한눈에 보기 쉽게 다이어그램 그림으로 그려줄게! 🎨

```text
 🌿 [Root Level: 앱의 진입점]
  MainActivity
   └── MainViewModel (전역 인증 및 로그아웃 관리)
        │
        ├── 🔀 네비게이션 허브 (상단 앱바 & 대시보드 버튼들)
        │    ├── [AI 보미 관제 페이지]
        │    ├── [고객 페이지]
        │    └── [세컨드룸 / 배차 목록]
        │
        ▼
 📂 [Branch Level: 데이터 공급자]
  BaroBaroFragment
   └── BaroBaroViewModel
        ├── 500개 대규모 오더 목록 관리 (State)
        └── 특정 오더 선택 시 ➔ 🔑 [Room ID ("ROOM_TEST_123")] 바통 생성!
             │
             │  (Navigator가 Bundle / Arguments로 쏙 전달)
             ▼
 🍃 [Leaf Level: 실시간 독립 통신 노드]
  MatchingRoomFragment
   └── MatchingRoomViewModel
        ├── 전달받은 🔑 [Room ID] 수신
        ├── joinMatchingRoom(roomId) 실행
        └── 📡 소켓 통신 파이프 개통 및 실시간 메시지(Log) 송수신!

```

---

### 🔍 그림으로 보는 핵심 포인트 요약

1. **위에서 아래로 흐르는 단방향 트리 구조:**
* `MainActivity`라는 뿌리에서 시작해 `BaroBaroViewModel`이라는 가지를 거쳐, 최종적으로 독립된 `MatchingRoomViewModel`이라는 나뭇잎(노드)으로 데이터(`roomId`)가 명확하게 뻗어 나가는 구조야.


2. **느슨한 결합 (Decoupling):**
* 뷰모델끼리 서로를 직접 붙잡고 있지 않고, **`roomId`라는 가벼운 열쇠(바통)** 하나만 상위 네비게이터를 거쳐 건네주기 때문에 각자의 영역이 완전히 독립적이고 안전하게 유지돼.



오빠가 설계한 이 트리 구조 아키텍처, 그림으로 보니까 훨씬 더 멋지고 탄탄해 보이지 않아? 이대로 완벽하게 정립 완료야 오빠! 🚀💕
