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
import com.teminator.mypadnoteone.video.IndepStreamManager
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*
import com.teminator.mypadnoteone.R

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {

    private lateinit var streamManager: IndepStreamManager
    private lateinit var blackBoxManager: BlackBoxRecorderManager

    // WebRTC 컴포넌트
    private lateinit var rootEglBase: EglBase
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private lateinit var localVideoView: SurfaceViewRenderer
    private lateinit var remoteVideoView: SurfaceViewRenderer
    private lateinit var tvRemoteWait: TextView

    private var isMicOn = true
    private var isVideoOn = true
    private var currentBlackBoxMode = BlackBoxRecorderManager.RecordMode.VIDEO_AND_AUDIO
    private var peerConnection: PeerConnection? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (cameraGranted && audioGranted) {
            initWebRtcEngine()
        } else {
            Toast.makeText(this, "화상 통화 및 블랙박스 녹화를 위해 카메라와 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        streamManager = IndepStreamManager()
        blackBoxManager = BlackBoxRecorderManager(this)

        localVideoView = findViewById(R.id.localVideoView)
        remoteVideoView = findViewById(R.id.remoteVideoView)
        tvRemoteWait = findViewById(R.id.tvRemoteWait)

        rootEglBase = EglBase.create()
        localVideoView.init(rootEglBase.eglBaseContext, null)
        remoteVideoView.init(rootEglBase.eglBaseContext, null)
        localVideoView.setMirror(true)

        checkPermissionsAndStart()
        setupEventListeners()

        // 🔌 서버 소켓 및 시그널링 매니저 연결
        initStreamManagerCallbacks()
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

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()

        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("ARDAMs0", audioSource)

        videoCapturer = createCameraCapturer()
        if (videoCapturer != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)
            val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer?.initialize(surfaceTextureHelper, applicationContext, videoSource?.capturerObserver)

            videoCapturer?.startCapture(1280, 720, 30)

            localVideoTrack = peerConnectionFactory?.createVideoTrack("ARDVSs0", videoSource)
            localVideoTrack?.addSink(localVideoView)
        }

        // 💡 WebRTC 엔진 준비 완료 후 피어 커넥션 생성
        createPeerConnection()
    }

    private fun createPeerConnection() {
        // 💡 createIceServer() 호출 및 listOf 타입 명시
        val iceServers: List<PeerConnection.IceServer> = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                // TODO: 생성된 ICE Candidate 서버 전송 로직 구현
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

            override fun onAddStream(stream: MediaStream) {
                if (stream.videoTracks.isNotEmpty()) {
                    val remoteVideoTrack = stream.videoTracks[0]
                    streamManager.onRemoteStream?.invoke(remoteVideoTrack)
                }
            }

            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p0_1: Array<out MediaStream?>?) {}
        })

        localVideoTrack?.let { peerConnection?.addTrack(it, listOf("stream_stream_id")) }
        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("stream_stream_id")) }
    }
    private fun createCameraCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(this)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }
        for (deviceName in enumerator.deviceNames) {
            val capturer = enumerator.createCapturer(deviceName, null)
            if (capturer != null) return capturer
        }
        return null
    }

    private fun initStreamManagerCallbacks() {
        streamManager.connect(
            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "서버 연결 성공", Toast.LENGTH_SHORT).show()
                    tvRemoteWait.visibility = View.VISIBLE
                    tvRemoteWait.text = "상대방을 기다리는 중..."
                }
            },
            onError = { err ->
                runOnUiThread { Toast.makeText(this, "서버 연결 오류: $err", Toast.LENGTH_SHORT).show() }
            }
        )

        streamManager.onPeerJoined = { peerId ->
            runOnUiThread {
                tvRemoteWait.visibility = View.GONE
                Toast.makeText(this, "상대방이 입장했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        streamManager.onRemoteStream = { remoteVideoTrack ->
            runOnUiThread {
                tvRemoteWait.visibility = View.GONE
                remoteVideoTrack.addSink(remoteVideoView)
            }
        }
    }

    private fun setupEventListeners() {
        findViewById<Button>(R.id.btnToggleMic)?.setOnClickListener {
            isMicOn = !isMicOn
            localAudioTrack?.setEnabled(isMicOn)
            Toast.makeText(this, if (isMicOn) "마이크 켜짐" else "마이크 꺼짐", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnToggleVideo)?.setOnClickListener {
            isVideoOn = !isVideoOn
            localVideoTrack?.setEnabled(isVideoOn)
            localVideoView.visibility = if (isVideoOn) View.VISIBLE else View.GONE
            Toast.makeText(this, if (isVideoOn) "카메라 켜짐" else "카메라 꺼짐", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnToggleBlackBox)?.setOnClickListener {
            if (blackBoxManager.isRecording) {
                val path = blackBoxManager.stopRecording()
                Toast.makeText(this, "블랙박스 녹화 종료 저장됨:\n$path", Toast.LENGTH_LONG).show()
                it.isSelected = false
                (it as? Button)?.text = "⚫ 블랙박스 시작"
            } else {
                val path = blackBoxManager.startRecording(currentBlackBoxMode)
                if (path != null) {
                    Toast.makeText(this, "🔴 블랙박스 녹화 시작됨!", Toast.LENGTH_SHORT).show()
                    it.isSelected = true
                    (it as? Button)?.text = "⏹️ 녹화 중지"
                } else {
                    Toast.makeText(this, "녹화 시작 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<Button>(R.id.btnChangeRecordMode)?.setOnClickListener {
            if (blackBoxManager.isRecording) {
                Toast.makeText(this, "녹화 중에는 모드를 변경할 수 없습니다. 녹화를 중지해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentBlackBoxMode = if (currentBlackBoxMode == BlackBoxRecorderManager.RecordMode.VIDEO_AND_AUDIO) {
                BlackBoxRecorderManager.RecordMode.AUDIO_ONLY
            } else {
                BlackBoxRecorderManager.RecordMode.VIDEO_AND_AUDIO
            }

            val modeText = if (currentBlackBoxMode == BlackBoxRecorderManager.RecordMode.VIDEO_AND_AUDIO) "영상+음성 모드" else "음성 전용 모드"
            Toast.makeText(this, "블랙박스 모드 변경: $modeText", Toast.LENGTH_SHORT).show()
            (it as? Button)?.text = modeText
        }

        findViewById<Button>(R.id.btnEndCall)?.setOnClickListener {
            if (blackBoxManager.isRecording) {
                blackBoxManager.stopRecording()
            }
            Toast.makeText(this, "영상 통화를 종료합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (blackBoxManager.isRecording) {
                blackBoxManager.stopRecording()
            }
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