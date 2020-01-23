package com.amazon.referral.ui.fragments

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
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.doOnNextLayout
import com.amazon.referral.libs.VideoControllerView
import com.amazon.referral.webservice.PostGetVideosViewModel
import com.amazon.referral.webservice.PostLanguagesViewModel
import com.mapbox.mapboxsdk.style.expressions.Expression.stop

class VideoViewFragment : BaseFragment() , View.OnClickListener,
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl,View.OnTouchListener  {

    lateinit var postGetVideosViewModel: PostGetVideosViewModel
    lateinit var postLanguagesViewModel: PostLanguagesViewModel
    internal var player: MediaPlayer? = null
    internal var controller: VideoControllerView? = null

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
        postLanguagesViewModel.loadData()
        videoSurfaceContainer.setOnTouchListener(this)
        controller = VideoControllerView(activity!!)

        val videoHolder = videoSurface.holder
        videoHolder.addCallback(this)
        player = MediaPlayer()
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener(this);

    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    fun setGetVideosAPIObserver() {
        postGetVideosViewModel = ViewModelProviders.of(this).get(PostGetVideosViewModel::class.java).apply {
            this@VideoViewFragment.let { thisFragReference ->
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
                    playVideo()
                })
            }
        }
    }
    fun setGetLanguagesAPIObserver() {
        postLanguagesViewModel = ViewModelProviders.of(this).get(PostLanguagesViewModel::class.java).apply {
            this@VideoViewFragment.let { thisFragReference ->
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

    fun setLangSpinner() {
        val langadapter = ArrayAdapter<String>(activity, R.layout.simple_spinner_item,  postLanguagesViewModel.obj?.languages);
        langadapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        lang_spinner.setAdapter(langadapter)
       // postGetVideosViewModel.loadData(postLanguagesViewModel.obj?.languages?.get(0)!!)

        lang_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val language = lang_spinner.getSelectedItem().toString()
                postGetVideosViewModel.loadData(language)
            }

        }
    }
    fun playVideo(){

        try {
            player?.stop()
            player?.reset()
            player?.setDataSource(activity, Uri.parse(postGetVideosViewModel.obj?.video?.get(0)?.file))
            player?.prepare();
            player?.start()

            player?.setOnCompletionListener {
                System.out.println("setOnCompletionListener")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
   /* fun playVideo(){

        if(createMediaPlayer(postGetVideosViewModel.obj?.video?.get(0)?.file!!))  {
            val videoHolder = videoSurface.holder
            videoHolder.addCallback(this)
            controller?.setMediaPlayer(this)
            controller?.setAnchorView(videoSurfaceContainer)
            player?.start()
        }

    }*/
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
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    override fun onPrepared(mp: MediaPlayer) {
        System.out.println("setOnCompletionListener onPrepared")

        controller?.setMediaPlayer(this)
        controller?.setAnchorView(videoSurfaceContainer)
        player?.start()
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

    private fun createMediaPlayer(m_soundFile : String): Boolean {
        if ( player != null) {
            if (player?.isPlaying()!!) {
                 player?.stop()
                 player?.reset()
                 player?.release()
                 player = null
            }

        }
         player = MediaPlayer()
         player?.setVolume(1f, 1f)
        try {
             player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
             player?.setDataSource(m_soundFile)
             player?.prepare()
            return true
            // Interop.logDebug(TAG + "-loadAudio: SUCCESS" + m_soundFile);
        } catch (e: Exception) {
               return false
        }

    }
}
