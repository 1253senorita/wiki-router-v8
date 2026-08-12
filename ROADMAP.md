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



