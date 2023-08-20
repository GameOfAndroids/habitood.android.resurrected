package com.astutusdesigns.habitood.authentication

import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.models.FSUserModel
import com.google.firebase.auth.*

/**
 * Auth Model for the Authentication package.
 * Created by timothy on 1/7/18.
 */
class AuthModel(presenter: AuthMvp.Presenter): AuthMvp.Model {

    val mAuthPresenter = presenter

    override fun registerNewUser(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnFailureListener { e ->
                    var failureCode: AcctCreationResultEnum? = null

                    when(e.javaClass) {
                        FirebaseAuthWeakPasswordException::class.java -> failureCode = AcctCreationResultEnum.WeakPassword
                        FirebaseAuthInvalidCredentialsException::class.java -> failureCode = AcctCreationResultEnum.EmailMalformed
                        FirebaseAuthUserCollisionException::class.java -> failureCode = AcctCreationResultEnum.EmailExists
                    }

                    mAuthPresenter.accountCreationDidFail(failureCode!!)
                }
                .addOnSuccessListener {
                    mAuthPresenter.accountCreationDidSucceed()
                }
    }

    override fun updateUserInfo(fistName: String, lastName: String, email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                    storeUserData(fistName, lastName, email)
                }
                .addOnFailureListener {
                    mAuthPresenter.userUpdateDidFail()
                }

    }

    private fun storeUserData(firstName: String, lastName: String, email: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val displayName = "$firstName $lastName"

        firebaseUser?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            it.updateProfile(profileUpdates)

            val fsUser = FSUser()
            fsUser.fname = firstName
            fsUser.lname = lastName
            fsUser.email = email
            fsUser.userId = it.uid

            FSUserModel.instance.createUserProfile(fsUser, object: FSUserModel.UserProfileOperationCallback {
                override fun onSuccess() {
                    mAuthPresenter.userUpdateDidSucceed()
                }

                override fun onFailure() {
                    mAuthPresenter.userUpdateDidFail()
                }
            })
        }

    }

}