package com.teminator.mypadnoteone.video

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.teminator.mypadnoteone.R
import com.teminator.mypadnoteone.indep.IndepStreamManager
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {

    private lateinit var streamManager: IndepStreamManager

    // WebRTC 컴포넌트
    private lateinit var rootEglBase: EglBase
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: org.webrtc.VideoTrack? = null
    private var localAudioTrack: org.webrtc.AudioTrack? = null // 🔥 패키지 명확히 지정
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var tvRemoteWait: TextView

    private var isMicOn = true
    private var isVideoOn = true

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (cameraGranted && audioGranted) {
            initWebRtcEngine()
        } else {
            Toast.makeText(this, "화상 통화를 위해 카메라와 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        streamManager = IndepStreamManager()

        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)
        tvRemoteWait = findViewById(R.id.tvRemoteWait)

        rootEglBase = EglBase.create()
        localVideoView.init(rootEglBase.eglBaseContext, null)
        remoteVideoView.init(rootEglBase.eglBaseContext, null)
        localVideoView.setMirror(true)

        checkPermissionsAndStart()
        setupEventListeners()

        streamManager.connect(
            onConnected = {
                runOnUiThread { Toast.makeText(this, "서버 연결 성공", Toast.LENGTH_SHORT).show() }
            },
            onError = { err ->
                runOnUiThread { Toast.makeText(this, "서버 연결 오류: $err", Toast.LENGTH_SHORT).show() }
            }
        )
    }

    private fun checkPermissionsAndStart() {
        val cam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val mic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        if (cam == PackageManager.PERMISSION_GRANTED && mic == PackageManager.PERMISSION_GRANTED) {
            initWebRtcEngine()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun initWebRtcEngine() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMs0", audioSource)

        // 🔥 카메라 캡처러 안전하게 생성 (Unresolved reference 'CaptureFacing' 방지)
        videoCapturer = createCameraCapturer()
        if (videoCapturer != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
            val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer?.initialize(surfaceTextureHelper, applicationContext, videoSource?.capturerObserver)

            videoCapturer?.startCapture(1280, 720, 30)

            localVideoTrack = peerConnectionFactory?.createVideoTrack("ARDVSs0", videoSource)
            localVideoTrack?.addSink(localVideoView)
        }
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(this)
        // 전면 카메라를 우선적으로 탐색
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }
        // 전면 카메라가 없다면 후면 등 다른 카메라 탐색
        for (deviceName in enumerator.deviceNames) {
            val capturer = enumerator.createCapturer(deviceName, null)
            if (capturer != null) return capturer
        }
        return null
    }

    private fun setupEventListeners() {
        // 🎤 마이크 음소거 토글 (setEnabled 메서드 사용)
        findViewById<Button>(R.id.btnToggleMic)?.setOnClickListener {
            isMicOn = !isMicOn
            localAudioTrack?.setEnabled(isMicOn)
            Toast.makeText(this, if (isMicOn) "마이크 켜짐" else "마이크 꺼짐", Toast.LENGTH_SHORT).show()
        }

        // 📹 비디오 켜기/끄기 토글 (setEnabled 메서드 사용)
        findViewById<Button>(R.id.btnToggleVideo)?.setOnClickListener {
            isVideoOn = !isVideoOn
            localVideoTrack?.setEnabled(isVideoOn)
            localVideoView.visibility = if (isVideoOn) View.VISIBLE else View.GONE
            Toast.makeText(this, if (isVideoOn) "카메라 켜짐" else "카메라 꺼짐", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEndCall)?.setOnClickListener {
            Toast.makeText(this, "영상 통화를 종료합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            surfaceTextureHelper?.dispose()
            localVideoView.release()
            remoteVideoView.release()
            peerConnectionFactory?.dispose()
            rootEglBase.release()
            streamManager.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}