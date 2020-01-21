package com.amazon.referral.ui.fragments

import android.app.Activity
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
import android.widget.AdapterView
import com.facebook.*
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazon.referral.libs.Helper
import com.amazon.referral.libs.Keys
import com.amazon.referral.libs.UserInfoManager
import com.amazon.referral.webservice.PostLoginViewModel
import com.amazon.referral.webservice.PostRegisterOtpViewModel
import com.amazon.referral.webservice.PostRegisterViewModel
import com.amazon.referral.webservice.PostUploadProfilePicViewModel
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.libs.helpers.BaseHelper
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import kotlinx.android.synthetic.main.register_fragment.*
import kotlinx.android.synthetic.main.register_fragment.mobile_no
import kotlinx.android.synthetic.main.register_fragment.password
import org.json.JSONObject




class RegisterFragment : BaseFragment() , View.OnClickListener {


    private val PICK_PHOTO_DOC: Int = 1001
    lateinit var postRegisterViewModel: PostRegisterViewModel
    lateinit var postRegisterOtpViewModel: PostRegisterOtpViewModel
    lateinit var postUploadDocViewModel: PostUploadProfilePicViewModel
    var isRegister = true
    var file_id = ""
    var file_name = ""
    var gender = ""
    var age = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.register_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setRegisterAPIObserver()
        setUploadPRofilePicAPIObserver()
        setRegisterOtpAPIObserver()
        val adapter = ArrayAdapter.createFromResource(activity,
                R.array.gender, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        gender_spinner.setAdapter(adapter)
        gender_spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                gender = gender_spinner.getSelectedItem().toString()

            }

        }
        val ageadapter = ArrayAdapter.createFromResource(activity,
                R.array.age, R.layout.simple_spinner_item)
        ageadapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        age_spinner.setAdapter(ageadapter)
        age_spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                age = age_spinner.getSelectedItem().toString()

            }

        }

        if(!isRegister) {
            btnRegister.setText("Add Referral")
        }

        btnRegister.setOnClickListener(this)
        upload_pic.setOnClickListener(this)
    }

    fun validate():Boolean {
        if(BaseHelper.isEmpty(name.text.toString())) {
            name_errror.visibility = View.VISIBLE
            return false
        }

        if(mobile_no.length() != 10) {
            name_errror.visibility = View.GONE
            mobile_errror.visibility = View.VISIBLE
            return false
        }
        if(BaseHelper.isEmpty(password.text.toString())) {
            mobile_errror.visibility = View.GONE
            password_errror.visibility = View.VISIBLE
            return false
        }
        password_errror.visibility = View.GONE
        name_errror.visibility = View.GONE
        mobile_errror.visibility = View.GONE

        return true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRegister -> {
                if(validate()) {
                    if(isRegister) {
                        postRegisterViewModel.loadData(params())
                    }
                }
            }
            R.id.upload_pic -> {
                pickImage()
            }
        }
    }
    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_DOC);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                val imageuri = data?.getData();// Get intent
                // Get real path and show over text view
                val real_Path = BaseHelper.getRealPathFromUri(activity, imageuri);
                postUploadDocViewModel.loadData(real_Path)
            } catch (e: Exception) {
            }

        }
    }

    fun params() : JSONObject {
        val jsonObject = JSONObject()
        if(isRegister) {
            jsonObject.put(Keys.USER_TYPE, "associate")
        } else {
            jsonObject.put(Keys.USER_TYPE, "referral")
        }
        jsonObject.put(Keys.FIRST_NAME, name.text.toString())
        jsonObject.put(Keys.GENDER, gender)
        jsonObject.put(Keys.AGE, age)
        jsonObject.put(Keys.MOBILE, mobile_no.text.toString())
        jsonObject.put(Keys.PASSWORD, password.text.toString())
        jsonObject.put(Keys.FILE_ID, file_id)
        jsonObject.put(Keys.FILE_NAME, file_name)

        return  jsonObject
    }

    fun setRegisterAPIObserver() {
        postRegisterViewModel = ViewModelProviders.of(this).get(PostRegisterViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                    if( isRegister) {
                        showOtpDialog(object : NotifyListener {
                            override fun onButtonClicked(which: Int) {
                                val jsonObject = JSONObject()
                                jsonObject.put(Keys.OTP_CODE, which)
                                postRegisterOtpViewModel.loadData(params(), jsonObject)
                            }
                        })
                    } else {
                        showNotifyDialog(
                                "", postRegisterOtpViewModel.obj?.message,
                                getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) {
                                home().proceedDoOnBackPressed()
                            }}
                        )
                    }
                })
            }
        }
    }

    fun setRegisterOtpAPIObserver() {
        postRegisterOtpViewModel = ViewModelProviders.of(this).get(PostRegisterOtpViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                            "", postRegisterOtpViewModel.obj?.message,
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

    fun setUploadPRofilePicAPIObserver() {
        postUploadDocViewModel = ViewModelProviders.of(this).get(PostUploadProfilePicViewModel::class.java).apply {
            this@RegisterFragment.let { thisFragReference ->
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
                    file_id = postUploadDocViewModel.obj?.file_id!!
                    file_name = postUploadDocViewModel.obj?.file_name!!
                })
            }
        }
    }

}
