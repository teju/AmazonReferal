package com.amazon.referral.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.R
import com.amazon.referral.libs.BaseHelper
import com.amazon.referral.libs.Keys
import com.amazon.referral.webservice.PostDashBoardViewModel
import com.amazon.referral.webservice.PostForgetPasswordViewModel
import com.amazon.referral.webservice.PostResetPasswordViewModel

import com.facebook.*
import com.iapps.gon.etc.callback.NotifyListener

import kotlinx.android.synthetic.main.reset_forget_password.*
import org.json.JSONObject


class ResetPasswordPasswordFragment : BaseFragment() , View.OnClickListener {

    lateinit var postResetPasswordViewModel: PostResetPasswordViewModel
    var password_reset_token = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.reset_forget_password, container, false)
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
            R.id.btnResetPassword -> {
                if(BaseHelper.isEmpty(password.text.toString())) {
                    password_errror.visibility = View.VISIBLE
                } else if(BaseHelper.isEmpty(otp.text.toString())) {
                    otp_errror.visibility = View.VISIBLE
                    password_errror.visibility = View.GONE
                } else {
                    password_errror.visibility = View.GONE
                    otp_errror.visibility = View.GONE
                    val jsonObject = JSONObject()
                    jsonObject.put(Keys.PASSWORD,password.text.toString())
                    jsonObject.put(Keys.PASSWORD_RESET_TOKEN,password_reset_token)
                    jsonObject.put(Keys.OTP_PASSWORD,otp.text.toString())
                    postResetPasswordViewModel.loadData(jsonObject)
                }
            }

        }
    }

    fun setResetPAsswordAPIObserver() {
        postResetPasswordViewModel = ViewModelProviders.of(this).get(PostResetPasswordViewModel::class.java).apply {
            this@ResetPasswordPasswordFragment.let { thisFragReference ->
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
                    showNotifyDialog(
                            "" ,postResetPasswordViewModel.obj?.result,
                            getString(R.string.ok),"",object : NotifyListener {
                        override fun onButtonClicked(which: Int) {
                            home().setFragment(LoginFragment())
                        }
                    }
                    )
                })
            }
        }
    }
}
