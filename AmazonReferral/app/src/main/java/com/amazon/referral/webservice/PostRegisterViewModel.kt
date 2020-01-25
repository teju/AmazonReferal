package com.amazon.referral.webservice

import android.app.Application
import com.amazon.referral.libs.*
import com.amazon.referral.model.general.GeneralResponse
import com.amazon.referral.objects.Response
import com.google.gson.GsonBuilder


import org.json.JSONObject


class PostRegisterViewModel(application: Application) : BaseViewModel(application) {

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

    fun loadData(apisignupform: JSONObject, register: Boolean) {
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
        if(register) {
            genericHttpAsyncTask.setUrl(APIs.postRegister)
        } else {
            genericHttpAsyncTask.setUrl(APIs.postReferralRegister)

        }
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setPostParams(Keys.APISIGNUPFORM,apisignupform)
        genericHttpAsyncTask.setCache(false)
        genericHttpAsyncTask.execute()

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}