package com.amazon.referral.libs

import com.iapps.libs.helpers.BaseKeys

class APIs : BaseKeys() {
    companion object {
        val BASE_URL = "http://amazonreferrals.com/api/web/"


        val postUploadProfilePic: String
            get() = BASE_URL!!  +  "user/upload-profile-image"

        val postRegister: String
            get() = BASE_URL!!  +  "user/register"

        val postRegisterOtp: String
            get() = BASE_URL!!  +  "user/register-otp"

        val postLogin: String
            get() = BASE_URL!!  +  "user/login"

        val postReferralRegister: String
            get() = BASE_URL!!  +  "user/referral-register"

        val postProfileDashboard: String
            get() = BASE_URL!!  +  "profile/dashboard"


    }
}