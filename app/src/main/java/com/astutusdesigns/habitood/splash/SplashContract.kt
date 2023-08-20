package com.astutusdesigns.habitood.splash

/**
 * Mvp design pattern interfaces
 * Created by timothy on 1/10/18.
 */
interface SplashContract {
    interface Model {
        fun onStopCalled()
        fun performStartupChecks()
        fun checkVendorAndBusiness()
    }
    interface View {
        fun navigateTo(options: SplashPresenter.Options)
    }
    interface Presenter {
        fun onStartCalled()
        fun onStopCalled()
        fun onUserHasNoBusiness()
        fun onUserBelongsToBusiness()
        fun onVendorInactive()
        fun onBusinessInactive()
        fun onVendorAndBusinessActive()
    }
}