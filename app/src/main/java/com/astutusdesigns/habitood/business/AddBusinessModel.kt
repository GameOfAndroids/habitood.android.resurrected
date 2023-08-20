package com.astutusdesigns.habitood.business

import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.models.FSBusinessModel

/**
 * MVP Model for the business package. Created by TMiller on 1/9/2018.
 */
class AddBusinessModel(mvpPresenter: AddBusinessContract.Presenter) : AddBusinessContract.Model {

    private val businessPresenter = mvpPresenter

    override fun downloadBusinessProfile(businessId: String) {
        FSBusinessModel.instance.downloadBusinessProfile(businessId, object : FSBusinessModel.FSBusinessModelCallback {
            override fun businessProfileWasDownloaded(business: FSBusiness?) {
                businessPresenter.businessProfileWasDownloaded(business)
            }

            override fun businessProfileDownloadFailed(exception: Exception?) {
                businessPresenter.businessProfileDownloadFailed(exception)
            }
        })
    }

    override fun addUserToBusiness(businessId: String, userId: String) {
        FSBusinessModel.instance.addBusinessUser(businessId, userId, object : FSBusinessModel.FSBusinessUserAddedCallback {
            override fun userWasAddedToBusiness() {
                businessPresenter.userWasAddedToBusiness()
            }

            override fun userAddedToBusinessFailed(exception: Exception?) {
                businessPresenter.userAddedToBusinessFailed(exception)
            }
        })
    }
}