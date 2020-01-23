package com.amazon.referral.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.R
import com.amazon.referral.libs.Keys
import com.amazon.referral.webservice.PostDashBoardViewModel
import com.amazon.referral.webservice.PostForgetPasswordViewModel

import com.facebook.*
import com.iapps.gon.etc.callback.NotifyListener
import kotlinx.android.synthetic.main.forget_password.*
import org.json.JSONObject


class ForgetPasswordFragment : BaseFragment() , View.OnClickListener {

    lateinit var postForgetPasswordViewModel: PostForgetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.forget_password, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setResetPAsswordAPIObserver()
        btnResetPassword.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnResetPassword-> {
                if(mobile_no.text.toString().length != 10) {
                    mobile_errror.visibility = View.VISIBLE
                } else {
                    mobile_errror.visibility = View.GONE
                    val jsonObject = JSONObject()
                    jsonObject.put(Keys.MOBILE,mobile_no.text.toString())
                    postForgetPasswordViewModel.loadData(jsonObject)
                }
            }
        }
    }

    fun setResetPAsswordAPIObserver() {
        postForgetPasswordViewModel = ViewModelProviders.of(this).get(PostForgetPasswordViewModel::class.java).apply {
            this@ForgetPasswordFragment.let { thisFragReference ->
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
                        home().setFragment(ResetPasswordPasswordFragment().apply {
                            password_reset_token = postForgetPasswordViewModel.obj?.result?.password_reset_token!!
                        })
                })
            }
        }
    }
}
