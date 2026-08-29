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
import com.teminator.mypadnoteone.indep.IndepStreamManager
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*
import com.teminator.mypadnoteone.R

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity() {

    private lateinit var streamManager: IndepStreamManager

    // WebRTC 컴포넌트
    private lateinit var rootEglBase: EglBase
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: org.webrtc.AudioTrack? = null
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
    }

    private fun checkPermissionsAndStart() {
        val cameraCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        if (cameraCheck == PackageManager.PERMISSION_GRANTED && audioCheck == PackageManager.PERMISSION_GRANTED) {
            initWebRtcEngine()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun initWebRtcEngine() {
        // 🚀 소켓 연결 후 화상 통화 방(Room) 입장 및 WebRTC 초기화 수행
        val roomId = intent.getStringExtra("ROOM_ID") ?: "INDEP_VIDEO_ROOM"

        streamManager.connect(
            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "화상 통화 서버 연결 성공", Toast.LENGTH_SHORT).show()
                    streamManager.joinRoom(roomId)
                }
            },
            onError = { err ->
                runOnUiThread {
                    Toast.makeText(this, "화상 통화 연결 실패: $err", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        localVideoView.release()
        remoteVideoView.release()
        rootEglBase.release()
        streamManager.disconnect()
    }
}