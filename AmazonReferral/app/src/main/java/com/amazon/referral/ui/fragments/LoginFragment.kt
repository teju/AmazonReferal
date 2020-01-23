package com.amazon.referral.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazon.referral.R

import com.facebook.login.LoginResult
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest.getProfilePictureUri
import com.squareup.picasso.Picasso
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.libs.Keys
import com.amazon.referral.libs.UserInfoManager
import com.amazon.referral.webservice.PostLoginViewModel
import com.facebook.*
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import kotlinx.android.synthetic.main.login_fragment.*
import org.json.JSONObject

class LoginFragment : BaseFragment() , View.OnClickListener {
    lateinit var postLoginViewModel: PostLoginViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.login_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    override fun onBackTriggered() {
        home().exitApp()
    }
    private fun initUI() {
        setLoginAPIObserver()

        btnLogin.setOnClickListener(this)
        sign_up.setOnClickListener(this)
        forget_password.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> {
                if(validate()) {
                    val jsonObject = JSONObject()
                    jsonObject.put(Keys.USERNAME,mobile_no.text.toString())
                    jsonObject.put(Keys.PASSWORD,password.text.toString())
                   postLoginViewModel.loadData(jsonObject)
                }
            }
            R.id.sign_up -> {
                home().setFragment(RegisterFragment())
            }
            R.id.forget_password -> {
                home().setFragment(ForgetPasswordFragment())
            }
        }
    }
    fun validate():Boolean {
        if(mobile_no.length() != 10) {
            errror.visibility = View.VISIBLE
            errror.setText("Enter Valid Mobile Number")
            return false
        }
        if(BaseHelper.isEmpty(password.text.toString())) {
            errror.visibility = View.VISIBLE
            errror.setText("Enter Valid Password / Pin")
            return false
        }
        errror.visibility = View.GONE

        return true
    }

    fun setLoginAPIObserver() {
        postLoginViewModel = ViewModelProviders.of(this).get(PostLoginViewModel::class.java).apply {
            this@LoginFragment.let { thisFragReference ->
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
                    UserInfoManager.getInstance(activity!!).saveAccountId(postLoginViewModel.obj?.user_id.toString())
                    UserInfoManager.getInstance(activity!!).saveAuthToken(postLoginViewModel.obj?.access_token.toString())
                    UserInfoManager.getInstance(activity!!).saveAccountName(postLoginViewModel.obj?.name.toString())
                    home().setFragment(MainFragment())
                })
            }
        }
    }


}
