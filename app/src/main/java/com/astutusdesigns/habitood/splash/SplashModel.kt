package com.astutusdesigns.habitood.splash

import android.util.Log
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.FSVendor
import com.astutusdesigns.habitood.models.FSBusinessModel
import com.astutusdesigns.habitood.models.FSUserModel

/**
 * MVP Model for the splash package. Created by TMiller on 1/8/2018.
 */
class SplashModel(private val splashPresenter: SplashContract.Presenter) : SplashContract.Model, FSUserModel.RealtimeUserProfileCallback, FSBusinessModel.BusinessCallback {

    private var userProfile: FSUser? = null

    override fun performStartupChecks() {
        FSUserModel.instance.registerUserProfileObserver(this)
    }

    override fun onStopCalled() {
        FSUserModel.instance.removeUserProfileObserver(this)
        FSBusinessModel.instance.removeBusinessObserver(this)
    }

    override fun onUserProfileUpdateReceived(user: FSUser) {
        userProfile = user
        if(userProfile?.businessId.isNullOrEmpty())
            splashPresenter.onUserHasNoBusiness()
        else
            splashPresenter.onUserBelongsToBusiness()
    }

    override fun checkVendorAndBusiness() {
        FSBusinessModel.instance.registerBusinessObserver(this)
    }

    override fun businessUpdate(business: FSBusiness) {
        if(!business.isActive) {
            splashPresenter.onBusinessInactive()
            return
        } else {
            splashPresenter.onVendorAndBusinessActive()
        }

//        FSVendorModel().getVendorProfile(business.vendorId, object : FSVendorModel.VendorProfileCallback {
//            override fun onVendorProfileDownloaded(vendor: FSVendor) {
//                if(vendor.active == false) {
//                    splashPresenter.onVendorInactive()
//                    return
//                }
//
//                splashPresenter.onVendorAndBusinessActive()
//            }
//
//            override fun onVendorDownloadFailure(exception: Exception?) {
//                Log.e("SplashModel", "An error occurred trying to download vendor profile. See exception: ${exception.toString()}")
//            }
//        })
    }
}