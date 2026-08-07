오빠, 내일 이어서 작업하기 편하게 오늘 우리가 해낸 핵심 내용들을 짚어서 깔끔한 로드맵 해시(체크리스트)로 정리해 둘게!

---

### 🗺️ AeroRouter & MyPadNoteOne 개발 로드맵 (2026.08.06)

* **[완료] 1. 깃 브랜치 및 프로젝트 환경 정돈**
* 충돌 마커 정리 및 `toml`, `build.gradle.kts` 등 설정 파일 동기화 완료


* **[완료] 2. 네이티브 PTT 무전기 엔진 및 소켓 매니저 연동 (`AeroRouterActivity`)**
* `AeroAudioEngine`과 `AeroSocketManager`(`sendAudioData`) 연결 고리 구축
* 마이크 권한 체크 및 실시간 바이너리 스트리밍 송신 구조 마련


* **[완료] 3. 매니페스트(`AndroidManifest.xml`) 최적화**
* 중복 선언되었던 `AeroRouterEntryActivity` 정리 및 앱 실행 진입점(런처)을 채널 게이트웨이 화면으로 안착


* **[완료] 4. 원격 저장소 백그라운드 박제 완료**
* 빌드 성공(4초 컷) 및 `main` 브랜치 푸시 완료 (`feat: AeroRouter native PTT and socket streaming engine integration success`)


* **[대기] 5. 내일(다음 작업) 이어갈 항목**
* `MainActivity.kt` 내 주석 처리된 `setupUI()` 버튼 분기 로직(PTT 진입 및 로그아웃 등) XML 레이아웃 아이디와 매칭 후 실전 검증



---

이렇게 딱 정리해 두었으니까 내일 이것만 슥 보고 바로 이어서 달리자구 오빠! 진짜로 굿밤 보내고 편히 쉬어! 🌙✨



33333333333333333333333333333333


현재 `MyPadNoteOne` 폴더 위치에서 Firebase 배포를 차근차근 진행하실 수 있도록 명확한 **로드맵**을 정리해 드립니다.

현재 이 폴더에는 배포 설정 파일(`firebase.json`)이 아직 없거나 초기화가 필요한 상태이므로, 아래 순서대로 진행하시면 깔끔하게 완료하실 수 있습니다.

---

### 🗺️ Firebase 배포 실행 로드맵

#### **1단계: Firebase 프로젝트 초기화 (`firebase init`)**

현재 폴더를 Firebase 프로젝트와 연결하고 배포 설정을 생성합니다.

* **실행할 명령어:**
```powershell
firebase init --token "1//0ejrpwNj2STLyCgYIARAAGA4SNwF-L9Irg5mERU33Iy01CFsI25347Zln83F8BDaPC2bIN4J2CKTWDIbflbTquAlQlPzC4NZyzg8"

```


* **주의 사항:** 화면에 여러 기능 목록이 나오면 방향키로 이동 후 스페이스바(`Space`)를 눌러 필요한 항목(예: **Hosting** 등)을 선택하고 엔터(`Enter`)를 누르세요. 중간에 기존 프로젝트 선택이나 설정 파일 덮어쓰기 여부를 물어보면 상황에 맞게 `Yes` 또는 기존 설정을 유지하도록 선택하시면 됩니다.

#### **2단계: 배포 실행 (`firebase deploy`)**

설정 파일이 정상적으로 생성된 후, 실제 빌드 파일이나 웹 소스를 서버에 업로드합니다.

* **전체 배포 시:**
```powershell
firebase deploy --token "1//0ejrpwNj2STLyCgYIARAAGA4SNwF-L9Irg5mERU33Iy01CFsI25347Zln83F8BDaPC2bIN4J2CKTWDIbflbTquAlQlPzC4NZyzg8"

```


* **호스팅(웹)만 지정해서 배포 시:**
```powershell
firebase deploy --only hosting --token "1//0ejrpwNj2STLyCgYIARAAGA4SNwF-L9Irg5mERU33Iy01CFsI25347Zln83F8BDaPC2bIN4J2CKTWDIbflbTquAlQlPzC4NZyzg8"

```



---

> 💡 **참고사항:**
> 업데이트 프로젝트 파일을 다루실 때는 기존 설정이나 중요한 소스 파일이 유실되지 않도록 원본 파일을 잘 보존하면서 필요한 부분만 신중하게 갱신해 주세요.

먼저 **1단계** 명령어를 터미널에 입력해서 초기화부터 시작해 보세요!