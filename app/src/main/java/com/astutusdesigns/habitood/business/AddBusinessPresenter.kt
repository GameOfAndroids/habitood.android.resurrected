package com.astutusdesigns.habitood.business

import android.util.Log
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.google.firebase.firestore.FirebaseFirestore

/**
 * MVP Presenter for the business package. Created by TMiller on 1/9/2018.
 */
class AddBusinessPresenter(mvpView: AddBusinessContract.View) : AddBusinessContract.Presenter {

    private val TAG = "AddBusinessPresenter"
    private val businessView = mvpView
    private val businessModel = AddBusinessModel(this)
    private val store = FirebaseFirestore.getInstance()
    private var business: FSBusiness? = null

    override fun onSubmitBusinessId(prefix: String, key1: String, key2: String, key3: String) {
        val key = "$prefix-$key1-$key2-$key3"
        businessModel.downloadBusinessProfile(key)
    }

    override fun businessProfileWasDownloaded(business: FSBusiness?) {
        this.business = business
        businessView.businessSuccessfullyDownloaded(business)
    }

    override fun businessProfileDownloadFailed(exception: Exception?) {
        exception?.toString()?.let {
            Log.e(TAG, it)
        }
        businessView.businessDownloadFailed()
    }

    override fun userConfirmedBusiness(userId: String) {
        businessModel.addUserToBusiness(business!!.businessId, userId)
    }

    override fun userWasAddedToBusiness() {
        businessView.userAddedToBusiness()
    }

    override fun userAddedToBusinessFailed(exception: Exception?) {
        Log.e(TAG, exception.toString())
        businessView.userAddedToBusinessFailed()
    }
}