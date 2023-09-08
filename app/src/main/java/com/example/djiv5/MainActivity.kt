package com.example.djiv5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import dji.sdk.keyvalue.key.BatteryKey
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.camera.VideoRecordMode
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.common.video.channel.VideoChannelType
import dji.v5.common.video.decoder.DecoderOutputMode
import dji.v5.common.video.decoder.DecoderState
import dji.v5.common.video.decoder.VideoDecoder
import dji.v5.common.video.interfaces.IVideoDecoder
import dji.v5.manager.KeyManager
import dji.v5.manager.SDKManager
import dji.v5.manager.datacenter.MediaDataCenter
import dji.v5.manager.interfaces.ILiveStreamManager
import dji.v5.manager.interfaces.SDKManagerCallback

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val TAG = "SDK_TESTING_MAIN_ACTIVITY"
    private var isConnected = false
    private var videoDecoder: IVideoDecoder? = null
    private var id = ""
    lateinit var tv1 : TextView
    lateinit var tv2 : TextView
    lateinit var startRecordingButton : Button
    lateinit var stopRecordingButton : Button
    private lateinit var surfaceView: SurfaceView
    private val streamManager: ILiveStreamManager = MediaDataCenter.getInstance().liveStreamManager
    var curWidth: Int = -1
    var curHeight: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv1 = findViewById(R.id.tv1)
        tv2 = findViewById(R.id.tv2)
        startRecordingButton = findViewById(R.id.btn_start_record)
        stopRecordingButton = findViewById(R.id.btn_stop_record)
        registerApp()

        startRecordingButton.setOnClickListener { startRecordingVideo() }
        stopRecordingButton.setOnClickListener { stopRecordingVideo() }


    }

    private fun startRecordingVideo() {

        val cameraMode = KeyTools.createKey(CameraKey.KeyVideoRecordMode)
        KeyManager.getInstance().getValue(cameraMode,object : CommonCallbacks.CompletionCallbackWithParam<VideoRecordMode>{
            override fun onSuccess(t: VideoRecordMode?) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity,"Video record mode connection success",Toast.LENGTH_SHORT).show()

                    val key = KeyTools.createKey(CameraKey.KeyConnection)
                    KeyManager.getInstance().getValue(key,object : CommonCallbacks.CompletionCallbackWithParam<Boolean>{
                        override fun onSuccess(t: Boolean?) {

                            runOnUiThread {
                                Toast.makeText(this@MainActivity,"Camera connection success",Toast.LENGTH_SHORT).show()
                            }

                            KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyStartShootPhoto),object :CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>{
                                override fun onSuccess(t: EmptyMsg?) {

                                    runOnUiThread {
                                        Toast.makeText(this@MainActivity,"Recording started",Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(error: IDJIError) {

                                    runOnUiThread {
                                        Toast.makeText(this@MainActivity,"Recording start failed ${error.toString()}",Toast.LENGTH_SHORT).show()
                                        tv2.text =error.toString()
                                        Log.i(TAG,error.toString())
                                    }
                                }
                            })

                        }

                        override fun onFailure(error: IDJIError) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity,"Camera connection failed ${error.description()}",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                }
            }

            override fun onFailure(error: IDJIError) {
                TODO("Not yet implemented")
            }
        })


    }

    private fun stopRecordingVideo() {
        val key = KeyTools.createKey(CameraKey.KeyConnection)
        KeyManager.getInstance().getValue(key,object : CommonCallbacks.CompletionCallbackWithParam<Boolean>{
            override fun onSuccess(t: Boolean?) {

                runOnUiThread {
                    Toast.makeText(this@MainActivity,"Camera connection success",Toast.LENGTH_SHORT).show()
                }

                KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyStopRecord),object :CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>{
                    override fun onSuccess(t: EmptyMsg?) {

                        runOnUiThread {
                            Toast.makeText(this@MainActivity,"Recording stopped",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(error: IDJIError) {

                        runOnUiThread {
                            Toast.makeText(this@MainActivity,"Recording stop failed ${error.description()}",Toast.LENGTH_SHORT).show()
                        }
                    }
                })

            }

            override fun onFailure(error: IDJIError) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity,"Camera connection failed ${error.description()}",Toast.LENGTH_SHORT).show()
                }
            }
        })

    }


    private fun registerApp()
    {
        SDKManager.getInstance().init(this,object: SDKManagerCallback {
            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
                Log.i(TAG, "onInitProcess: ")
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    SDKManager.getInstance().registerApp()
                }
            }
            override fun onRegisterSuccess() {
                Log.i(TAG, "onRegisterSuccess: ")
                runOnUiThread{
                    Toast.makeText(this@MainActivity,"Register Success",Toast.LENGTH_SHORT).show()

                }
            }
            override fun onRegisterFailure(error: IDJIError?) {
                Log.i(TAG, "onRegisterFailure: $error ")
                runOnUiThread{
                    Toast.makeText(this@MainActivity,error.toString(),Toast.LENGTH_SHORT).show()
                }

            }
            override fun onProductConnect(productId: Int)
            {

                initialisedDroneComponents()
                initView()
                isConnected = true
                id = productId.toString()
                Log.i(TAG, "onProductConnect: ")

                runOnUiThread{
                    Toast.makeText(this@MainActivity,"Product Connect",Toast.LENGTH_SHORT).show()

                    val key = KeyTools.createKey(BatteryKey.KeyConnection)
                    KeyManager.getInstance().getValue(key,object : CommonCallbacks.CompletionCallbackWithParam<Boolean>{
                        override fun onSuccess(t: Boolean?) {
                            runOnUiThread{
                                Toast.makeText(this@MainActivity,"battery connection success",Toast.LENGTH_SHORT).show()
                            }

                            KeyManager.getInstance().getValue(KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent),object : CommonCallbacks.CompletionCallbackWithParam<Int>{
                                override fun onSuccess(t: Int?) {
                                    tv1.text = t.toString()

                                }

                                override fun onFailure(error: IDJIError) {
                                }
                            })


                        }
                        override fun onFailure(error: IDJIError) {
                            runOnUiThread{
                                Toast.makeText(this@MainActivity,"battery connection failure ${error.description()}",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })

                }

            }
            override fun onProductDisconnect(productId: Int) {
                isConnected = false
                Log.i(TAG, "onProductDisconnect: ")
                runOnUiThread{
                    Toast.makeText(this@MainActivity,"Product Disconnect",Toast.LENGTH_SHORT).show()
                }

            }
            override fun onProductChanged(productId: Int)
            {
                Log.i(TAG, "onProductChanged: ")
                runOnUiThread {
                    Toast.makeText(this@MainActivity,"Product Changed",Toast.LENGTH_SHORT).show()
                }

            }
            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                Log.i(TAG, "onDatabaseDownloadProgress: ${current/total}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity,"onDatabaseDownloadProgress",Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    private fun initialisedDroneComponents() {

        val camera = KeyTools.createKey(CameraKey.KeyConnection)




    }

    private fun initView() {
        surfaceView = findViewById(R.id.live_stream_surface_views)
        surfaceView.holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (videoDecoder == null) {
            videoDecoder = VideoDecoder(
                this,
                getVideoChannel(),
                DecoderOutputMode.SURFACE_MODE,
                surfaceView.holder,
                -1,
                -1,
                true
            )
        } else if (videoDecoder?.decoderStatus == DecoderState.PAUSED) {
            videoDecoder?.onResume()
        }

        curWidth = surfaceView.width
        curHeight = surfaceView.height
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (videoDecoder == null) {
            videoDecoder = VideoDecoder(
                this,
                getVideoChannel(),
                DecoderOutputMode.SURFACE_MODE,
                surfaceView.holder,
                -1,
                -1,
                true
            )
        } else if (videoDecoder?.decoderStatus == DecoderState.PAUSED) {
            videoDecoder?.onResume()
        }
        curWidth = width
        curHeight = height
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecoder?.let {
            videoDecoder?.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoDecoder?.destroy()
//        if (isStreaming) {
//            stopStream()
//        }
    }

    private fun getVideoChannel(): VideoChannelType {
        return streamManager.videoChannelType
    }

}