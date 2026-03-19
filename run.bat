@echo off
title WIKI-ROUTER v5.2 Control Panel

:: 1. 서버 실행 (첫 번째 창)
echo 서버를 실행합니다...
start "WIKI-SERVER-CORE" cmd /k "node server.js"

:: 2. 서버가 켜질 때까지 3초 정도 기다려줍니다.
timeout /t 3 /nobreak > nul

:: 3. 로컬 터널 실행 (두 번째 창 - 배꼽 맞춘 3000번 포트)
echo 외부 접속 주소를 생성합니다...
start "WIKI-EXTERNAL-LINK" cmd /k "npx localtunnel --port 3000"

echo 모든 프로세스가 실행되었습니다! 창 두 개를 모두 켜두셔야 합니다.