package com.astutusdesigns.habitood.authentication

import android.content.Context
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.Utilities
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.models.FSUserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * Presenter for the Login fragment.
 * Created by timothy on 1/7/18.
 */
class LoginPresenter(private val mLoginView: LoginMvp.View) : LoginMvp.Presenter {

    private val mAuth = FirebaseAuth.getInstance()

    override fun forgotPasswordTapped(email: String) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener { mLoginView.emailWasSent() }
                .addOnFailureListener { mLoginView.emailWasIncorrect() }
    }

    override fun signIn(context: Context, email: String, password: String) {
        if(!Utilities.isNetworkAvailable(context)) {
            mLoginView.noInternetConnectivity()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnFailureListener { e ->
                    when(e.javaClass) {
                        FirebaseAuthInvalidUserException::class.java -> mLoginView.loginResult(LoginResultEnum.InvalidEmail)
                        FirebaseAuthInvalidCredentialsException::class.java -> mLoginView.loginResult(LoginResultEnum.InvalidPassword)
                        else -> mLoginView.loginResult(LoginResultEnum.Other)
                    }
                }
                .addOnSuccessListener {
                    if(mAuth.currentUser?.isEmailVerified!! || HabitoodApp.NO_EMAIL_VERIFICATION_MODE) {
                        val uid = mAuth.currentUser!!.uid
                        FSUserModel.instance.downloadUserProfileById(uid, object : FSUserModel.UserProfileCallback {
                            override fun onProfileDownloaded(user: FSUser?) {
                                if(user?.rankLevel ?: 6 <= 5)
                                    mLoginView.loginResult(LoginResultEnum.Success)
                                else
                                    mLoginView.loginResult(LoginResultEnum.InvalidRole)
//                                if(user?.rank == FSUser.Rank.FrontlinePersonnel || user?.rank == FSUser.Rank.CoreTeamLeader || user?.rank == FSUser.Rank.Supervisor || user?.rank == FSUser.Rank.BusinessAdmin || user?.rank == FSUser.Rank.Owner)
//                                    mLoginView.loginResult(LoginResultEnum.Success)
//                                else
//                                    mLoginView.loginResult(LoginResultEnum.InvalidRole)
                            }

                            override fun onDownloadFailed(exception: Exception?) {
                                mLoginView.emailWasIncorrect()
                            }
                        })

                    } else
                        mLoginView.displayResendConfirmEmailSnack()
                }
    }

    override fun resendEmailVerification() {
        mAuth.currentUser?.sendEmailVerification()
    }
}