package com.amazon.referral.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.R
import com.amazon.referral.webservice.PostDashBoardViewModel
import com.amazon.referral.webservice.PostRegisterOtpViewModel
import com.amazon.referral.webservice.PostUploadProfilePicViewModel

import com.facebook.*
import com.iapps.gon.etc.callback.NotifyListener
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : BaseFragment() , View.OnClickListener {

    lateinit var postDashBoardViewModel: PostDashBoardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.main_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setDashBoardAPIObserver()
        btnReferral.setOnClickListener(this)
        postDashBoardViewModel.loadData()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnReferral -> {
                home().setFragment(RegisterFragment().apply {
                    isRegister = false
                })
            }
        }
    }

    fun setDashBoardAPIObserver() {
        postDashBoardViewModel = ViewModelProviders.of(this).get(PostDashBoardViewModel::class.java).apply {
            this@MainFragment.let { thisFragReference ->
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
                    referral_count.setText(postDashBoardViewModel.obj?.referral_count)
                })
            }
        }
    }


}
