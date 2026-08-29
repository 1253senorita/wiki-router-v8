package com.teminator.mypadnoteone.indep

object IndepConfig {
    const val TAG = "IND_PTT_LOG"

    // 실제 휴대폰 테스트용 서버 IP 주소 적용
    //const val SERVER_URL = "http://192.168.219.100:8080"

    // 에뮬레이터에서 PC 서버(localhost:8080)로 접근할 때 사용하는 주소
    const val SERVER_URL = "http://10.0.2.2:8080"




    // 🚀 [수정] 실제 휴대폰 테스트 시 PC의 로컬 IP와 서버 포트 3000번 적용
    //const val SERVER_URL = "http://192.168.0.5:3000"




    //const val SERVER_URL = "https://discretely-ultrabasic-daniele.ngrok-free.dev"
}