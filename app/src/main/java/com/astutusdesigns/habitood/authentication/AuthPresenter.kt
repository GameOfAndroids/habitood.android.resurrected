package com.astutusdesigns.habitood.authentication

/**
 * Presenter for the Auth activity.
 * Created by timothy on 1/7/18.
 */
class AuthPresenter(private val mAuthView: AuthMvp.View): AuthMvp.Presenter {

    private val mAuthModel: AuthMvp.Model = AuthModel(this)
    private var mFirst: String? = null
    private var mLast: String? = null
    private var mEmail: String? = null
    private var mPassword: String? = null

    override fun register(firstName: String, lastName: String, email: String, password: String) {
        mFirst = firstName
        mLast = lastName
        mEmail = email
        mPassword = password

        mAuthModel.registerNewUser(email, password)
    }

    override fun accountCreationDidFail(resultEnum: AcctCreationResultEnum) {
        mAuthView.userRegistrationFailed(resultEnum)
    }

    override fun accountCreationDidSucceed() {
        mAuthModel.updateUserInfo(mFirst!!, mLast!!, mEmail!!, mPassword!!)
    }

    override fun userUpdateDidFail() {
        updateUserDatabase(mFirst!!, mLast!!, mEmail!!, mPassword!!)
    }

    override fun userUpdateDidSucceed() {
        mAuthView.userRegistrationDidSucceed()
    }

    private fun updateUserDatabase(firstName: String, lastName: String, email: String, password: String) {
        mAuthModel.updateUserInfo(firstName, lastName, email, password)
    }

}