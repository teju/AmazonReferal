package com.amazon.referral.webservice

import android.app.Application
import com.amazon.referral.libs.*
import com.amazon.referral.model.general.GeneralResponse
import com.amazon.referral.model.login.Login
import com.amazon.referral.model.referralUpdate.ReferralUpdate
import com.amazon.referral.model.uploadProfilePic.UploadProfilePic
import com.amazon.referral.objects.Response
import com.google.gson.GsonBuilder


import org.json.JSONObject


class PostReferralUpdateViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: ReferralUpdate? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(referral_id : String,video_id : String) {
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
                        obj = gson.fromJson(response!!.content.toString(), ReferralUpdate::class.java)
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
        genericHttpAsyncTask.setUrl(APIs.postReferralUpdate)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setPostParams(Keys.USER_ID, UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.ASSOCIATE_ID, UserInfoManager.getInstance(apl).getAccountId())
        genericHttpAsyncTask.setPostParams(Keys.REFERRAL_ID, referral_id)
        genericHttpAsyncTask.setPostParams(Keys.ACCESS_TOKEN, UserInfoManager.getInstance(apl).authToken)
        genericHttpAsyncTask.setPostParams(Keys.VIDEO_ID, video_id)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}