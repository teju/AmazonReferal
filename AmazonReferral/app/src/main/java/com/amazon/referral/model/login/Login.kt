package com.amazon.referral.model.login

data class Login(
    val access_token: String = "",
    val name: String = "",
    val profile_img: String = "",
    val role_id: Int = 0,
    val role_type: String = "",
    val status: String = "",
    val user_id: Int = 0
)