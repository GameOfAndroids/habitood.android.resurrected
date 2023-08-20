package com.astutusdesigns.habitood.authentication

import android.content.Context

/**
 * Login contract as required by the MVP design pattern.
 * Created by TMiller on 1/10/2018.
 */
interface LoginMvp {
    interface Model {
        // not used in this implementation yet.
    }
    interface View {
        fun emailWasSent()
        fun emailWasIncorrect()
        fun loginResult(resultEnum: LoginResultEnum)
        fun displayResendConfirmEmailSnack()
        fun noInternetConnectivity()
    }
    interface Presenter {
        fun forgotPasswordTapped(email: String)
        fun signIn(context: Context, email: String, password: String)
        fun resendEmailVerification()
    }
}