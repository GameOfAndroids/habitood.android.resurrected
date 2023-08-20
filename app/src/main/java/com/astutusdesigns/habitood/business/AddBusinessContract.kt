package com.astutusdesigns.habitood.business

import com.astutusdesigns.habitood.datamodels.FSBusiness

/**
 * Created by timothy on 1/18/18.
 */
interface AddBusinessContract {
    interface Model {
        fun downloadBusinessProfile(businessId: String)
        fun addUserToBusiness(businessId: String, userId: String)
    }
    interface View {
        fun businessSuccessfullyDownloaded(business: FSBusiness?)
        fun businessDownloadFailed()
        fun userAddedToBusiness()
        fun userAddedToBusinessFailed()
    }
    interface Presenter {
        fun onSubmitBusinessId(prefix: String = "sbc", key1: String, key2: String, key3: String)
        fun businessProfileWasDownloaded(business: FSBusiness?)
        fun businessProfileDownloadFailed(exception: Exception?)
        fun userConfirmedBusiness(userId: String)
        fun userWasAddedToBusiness()
        fun userAddedToBusinessFailed(exception: Exception?)
    }
}