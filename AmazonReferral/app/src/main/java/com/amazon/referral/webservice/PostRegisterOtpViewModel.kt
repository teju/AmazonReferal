package com.amazon.referral.webservice

import android.app.Application
import com.amazon.referral.libs.APIs
import com.amazon.referral.libs.Helper
import com.amazon.referral.libs.Keys
import com.amazon.referral.libs.SingleLiveEvent
import com.amazon.referral.model.general.GeneralResponse
import com.amazon.referral.model.login.Login
import com.amazon.referral.model.uploadProfilePic.UploadProfilePic
import com.google.gson.GsonBuilder

import com.iapps.libs.helpers.BaseConstants
import com.iapps.libs.objects.Response
import org.json.JSONObject


class PostRegisterOtpViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: GeneralResponse? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(apisignupform : JSONObject,otpform : JSONObject) {
        genericHttpAsyncTask = Helper.GenericHttpAsyncTask(object : Helper.GenericHttpAsyncTask.TaskListener {

            override fun onPreExecute() {
                isLoading.postValue(true)
            }

            override fun onPostExecute(response: Response?) {
                isLoading.postValue(false)

                if (!Helper.isNetworkAvailable(apl)) {
                    isNetworkAvailable.postValue(false)
                    return
                }

                val json = checkResponse(response, apl)

                if (json != null) {
                    try {
                        val gson = GsonBuilder().create()
                        obj = gson.fromJson(response!!.content.toString(), GeneralResponse::class.java)
                        if (obj!!.status.equals(Keys.STATUS_CODE)) {
                            trigger.postValue(NEXT_STEP)
                        }else{
                            errorMessage.value = createErrorMessageObject(response)

                        }
                    } catch (e: Exception) {
                        showUnknowResponseErrorMessage()
                    }
                }

            }
        })

        genericHttpAsyncTask.method = BaseConstants.POST
        genericHttpAsyncTask.setUrl(APIs.postRegisterOtp)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setPostParams(Keys.APISIGNUPFORM,apisignupform)
        genericHttpAsyncTask.setPostParams(Keys.OTPFORM,otpform)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}