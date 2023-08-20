package com.astutusdesigns.habitood.authentication

/**
 * AuthMvp contract. Auth is only for account creation.
 * Created by TMiller on 1/10/2018.
 */
interface AuthMvp {
    interface Model {
        fun registerNewUser(email: String, password: String)
        fun updateUserInfo(fistName: String, lastName: String, email: String, password: String)
    }
    interface View {
        fun userRegistrationDidSucceed()
        fun userRegistrationFailed(failureCode: AcctCreationResultEnum)
    }
    interface Presenter {
        fun register(firstName: String, lastName: String, email: String, password: String)
        fun accountCreationDidFail(resultEnum: AcctCreationResultEnum)
        fun accountCreationDidSucceed()
        fun userUpdateDidFail()
        fun userUpdateDidSucceed()
    }
}