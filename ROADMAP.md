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