package com.amazon.referral.ui.fragments

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.R
import com.amazon.referral.webservice.PostDashBoardViewModel

import com.iapps.gon.etc.callback.NotifyListener
import kotlinx.android.synthetic.main.video_fragment.*
import android.net.Uri
import android.media.MediaPlayer
import android.os.Environment
import android.view.*
import androidx.core.view.doOnNextLayout
import com.amazon.referral.libs.DownloadTask
import com.amazon.referral.libs.VideoControllerView
import com.amazon.referral.webservice.PostGetVideosViewModel
import com.amazon.referral.webservice.PostLanguagesViewModel
import com.amazon.referral.webservice.PostReferralUpdateViewModel
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.amazon.referral.ui.activity.MainActivity
import com.iapps.gon.etc.callback.PermissionListener
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.net.URISyntaxException
import android.opengl.ETC1.getWidth
import com.google.android.libraries.places.internal.i
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getWidth
import android.widget.*

class VideoFragment : BaseFragment() , View.OnClickListener,
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl,View.OnTouchListener  {

    lateinit var postGetVideosViewModel: PostGetVideosViewModel
    lateinit var postLanguagesViewModel: PostLanguagesViewModel
    lateinit var postReferralUpdateViewModel: PostReferralUpdateViewModel
    internal var player: MediaPlayer? = null
    internal var controller: VideoControllerView? = null
    var referral_id = ""
    var completed = false
    override val bufferPercentage: Int
        get() = 0

    override val currentPosition: Int
        get() = player?.currentPosition!!

    override val duration: Int
        get() = player?.duration!!

    override val isPlaying: Boolean
        get() = player?.isPlaying!!

    override val isFullScreen: Boolean
        get() = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.video_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setGetVideosAPIObserver()
        setGetLanguagesAPIObserver()
        setReferralUpdateAPIObserver()
        btnDownloadVideo.setOnClickListener(this)
        postLanguagesViewModel.loadData()
        videoSurfaceContainer.setOnTouchListener(this)
        controller = VideoControllerView(activity!!)

        val videoHolder = videoSurface.holder
        videoHolder.addCallback(this)
        player = MediaPlayer()
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener(this);
        btnDownloadVideo.isEnabled = false
        //playDownloadedVideo()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDownloadVideo -> {
                DownloadTask(activity, btnDownloadVideo,postGetVideosViewModel.obj?.video?.get(0)?.file ,
                        ld,object  : NotifyListener {
                    override fun onButtonClicked(which: Int) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
            }
        }
    }

    fun setGetVideosAPIObserver() {
        postGetVideosViewModel = ViewModelProviders.of(this).get(PostGetVideosViewModel::class.java).apply {
            this@VideoFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                            s.title, s.message!!,
                            getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) { }
                    })
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    btnDownloadVideo.isEnabled = true
                    val file = File(getFilePath())
                    if(!file.exists()) {
                        DownloadTask(activity, btnDownloadVideo, postGetVideosViewModel.obj?.video?.get(0)?.file,
                                ld, object : NotifyListener {
                            override fun onButtonClicked(which: Int) {
                                if (which == 1) {
                                    playVideo()
                                } else {
                                    showNotifyDialog(
                                            "", "Download Failed",
                                            getString(R.string.ok), "", object : NotifyListener {
                                        override fun onButtonClicked(which: Int) {}
                                    })
                                }
                            }
                        })
                    } else {
                        playVideo()
                    }

                })
            }
        }
    }
    fun setGetLanguagesAPIObserver() {
        postLanguagesViewModel = ViewModelProviders.of(this).get(PostLanguagesViewModel::class.java).apply {
            this@VideoFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                            s.title, s.message!!,
                            getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) { }
                    }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->

                    setLangSpinner()
                })
            }
        }
    }
    fun setReferralUpdateAPIObserver() {
        postReferralUpdateViewModel = ViewModelProviders.of(this).get(PostReferralUpdateViewModel::class.java).apply {
            this@VideoFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                            s.title, s.message!!,
                            getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) { }
                    }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    home().setFragment(MainFragment())
                })
            }
        }
    }

    fun setLangSpinner() {
        val langadapter = ArrayAdapter<String>(activity, R.layout.simple_spinner_item,  postLanguagesViewModel.obj?.languages);
        langadapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        lang_spinner.setAdapter(langadapter)
       // postGetVideosViewModel.loadData(postLanguagesViewModel.obj?.languages?.get(0)!!)
        lang_spinner.setSelection(0,false)

        lang_spinner.setOnItemSelectedEvenIfUnchangedListener( object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val language = lang_spinner.getSelectedItem().toString()
                postGetVideosViewModel.loadData(language)
            }

        })
    }
    fun playVideo(){
        video_container.visibility = View.VISIBLE
        ll1.visibility = View.GONE

        try {
            player?.stop()
            player?.reset()

            //player?.setDataSource(activity, Uri.parse(postGetVideosViewModel.obj?.video?.get(0)?.file))
            player?.setDataSource(activity, Uri.parse(getFilePath()))
            player?.prepare();
            player?.start()
            player?.setOnCompletionListener {
                video_container.visibility = View.GONE
                ll1.visibility = View.VISIBLE

                postReferralUpdateViewModel.loadData(referral_id,
                        postGetVideosViewModel.obj?.video?.get(0)?.id.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFilePath() : String {
        var uri: URI? = null
        try {
            uri = URI(postGetVideosViewModel.obj?.video?.get(0)?.file)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        val path = uri?.getPath()
        val idStr = path?.substring(path.lastIndexOf('/') + 1)
        val apkStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filename = apkStorage.toString() + "/" + idStr
        return filename
    }

    override fun onBackTriggered() {

    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        controller?.show()
        return false
    }

    // Implement SurfaceHolder.Callback
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        player?.setDisplay(holder)
        val videoWidth = player?.videoWidth
        val videoHeight = player?.videoHeight

        //Get the width of the screen
        val screenWidth = activity?.getWindowManager()?.getDefaultDisplay()?.getWidth()

        //Get the SurfaceView layout parameters
        val lp = videoSurface.getLayoutParams() as FrameLayout.LayoutParams
        lp.gravity  = Gravity.CENTER_VERTICAL
        //Set the width of the SurfaceView to the width of the screen
        lp.width = screenWidth!!

        //Set the height of the SurfaceView to match the aspect ratio of the video
        //be sure to cast these as floats otherwise the calculation will likely be 0
        lp.height = (videoHeight!!.toFloat() / videoWidth!!.toFloat() * screenWidth.toFloat()).toInt()


        //Commit the layout parameters
        videoSurface.setLayoutParams(lp)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    override fun onPrepared(mp: MediaPlayer) {
        System.out.println("setOnCompletionListener onPrepared")
        try {
            controller?.setMediaPlayer(this)
            controller?.setAnchorView(videoSurfaceContainer)


            player?.start()
        } catch (e : Exception){

        }
    }
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun pause() {
        player?.pause()
    }

    override fun seekTo(i: Int) {
        player?.seekTo(i)
    }

    override fun start() {
        player?.start()
    }

    override fun toggleFullScreen() {

    }

}
