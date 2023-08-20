package com.astutusdesigns.habitood.splash

import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.models.FSUserModel
import com.google.firebase.auth.FirebaseAuth

/**
 * MVP Presenter for the splash package. This class is the "startup" class. Once the user is authenticated,
 * this class will synchronize the firebase database with the habitood local database.
 * Created by TMiller on 1/8/2018.
 */
class SplashPresenter(private val splashView: SplashContract.View) : SplashContract.Presenter {

    enum class Options { Authentication, Home, BusinessKeyEntry, InactiveVendor, InactiveBusiness }

    private val mSplashModel: SplashContract.Model = SplashModel(this)

    override fun onStartCalled() {
        start()
    }

    private fun start() {
        if (FirebaseAuth.getInstance().currentUser == null)
            onUserNotAuthenticated()
        else {
            HabitoodApp.instance.userIsLoggedIn()
            mSplashModel.performStartupChecks()
        }
    }

    private fun onUserNotAuthenticated() {
        HabitoodApp.instance.userIsLoggedOut()
        navigateTo(Options.Authentication)
    }

    override fun onStopCalled() {
        mSplashModel.onStopCalled()
    }

    override fun onUserBelongsToBusiness() {
        mSplashModel.checkVendorAndBusiness()
    }

    private fun navigateTo(option: Options) {
        splashView.navigateTo(option)
    }

    override fun onVendorInactive() {
        navigateTo(Options.InactiveVendor)
    }

    override fun onBusinessInactive() {
        navigateTo(Options.InactiveBusiness)
    }

    override fun onVendorAndBusinessActive() {
        navigateTo(Options.Home)
    }

    override fun onUserHasNoBusiness() {
        navigateTo(Options.BusinessKeyEntry)
    }
}