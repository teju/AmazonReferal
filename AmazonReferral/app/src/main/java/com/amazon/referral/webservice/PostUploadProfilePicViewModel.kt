package com.amazon.referral.webservice

import android.app.Application
import com.amazon.referral.libs.*
import com.amazon.referral.model.uploadProfilePic.UploadProfilePic
import com.amazon.referral.objects.Response
import com.google.gson.GsonBuilder



class PostUploadProfilePicViewModel(application: Application) : BaseViewModel(application) {

    private val trigger = SingleLiveEvent<Integer>()

    lateinit var genericHttpAsyncTask : Helper.GenericHttpAsyncTask

    var apl: Application

    var obj: UploadProfilePic? = null


    fun getTrigger(): SingleLiveEvent<Integer> {
        return trigger
    }

    init {
        this.apl = application
    }

    fun loadData(path : String) {
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
                        obj = gson.fromJson(response!!.content.toString(), UploadProfilePic::class.java)
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
        genericHttpAsyncTask.setUrl(APIs.postUploadProfilePic)
        genericHttpAsyncTask.context = apl.applicationContext
        genericHttpAsyncTask.setFileParams(Keys.PROFILE,path,"multipart/form-data; boundar")
        genericHttpAsyncTask.setCache(false)
        Helper.applyHeader(apl,genericHttpAsyncTask)
        genericHttpAsyncTask.execute()
        System.out.println("genericHttpAsyncTask "+genericHttpAsyncTask.url)

    }

    companion object {
        @JvmField
        var NEXT_STEP: Integer? = Integer(1)
    }

}