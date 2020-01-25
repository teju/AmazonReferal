package com.amazon.referral.libs


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

        val postRequestPasswordReset: String
            get() = BASE_URL!!  +  "user/request-password-reset"

        val postResetPasswordReset: String
            get() = BASE_URL!!  +  "user/reset-password"

        val postLanguages: String
            get() = BASE_URL!!  +  "profile/languages"

        val postGetVideos: String
            get() = BASE_URL!!  +  "profile/get-videos"

        val postReferralUpdate: String
            get() = BASE_URL!!  +  "profile/referral-update"


    }
}