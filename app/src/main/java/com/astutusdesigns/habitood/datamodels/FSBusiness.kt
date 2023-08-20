package com.astutusdesigns.habitood.datamodels

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable
import java.util.Date


/**
 * Business class to hold business object data.
 * Created by TMiller on 1/8/2018.
 */
@IgnoreExtraProperties
class FSBusiness : Serializable {
    @Exclude
    var businessId = ""
    var businessName = ""
    var vendorId: String? = null
    var isManualAddEnabled = false
    var numRegisteredUsers = 0
    var numSeatsPurchased = 0
    var numSeatsUsed = 0
    var dateCreated = Date()
    var isActive = false
    var pinpointsRecorded = 0
}