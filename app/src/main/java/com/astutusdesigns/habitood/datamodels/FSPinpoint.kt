package com.astutusdesigns.habitood.datamodels


/**
 * Data model for Pinpoints that are stored on Firebase.
 * Created by TMiller on 1/12/2018.
 */
class FSPinpoint {
    var pinpointTitle: String? = null
    var pinpointDescription: String? = null
    var isDeleted = false

    constructor()
    constructor(storedPinpoint: RPinpoint) {
        pinpointTitle = storedPinpoint.pinpointTitle
        pinpointDescription = storedPinpoint.pinpointDescription
        isDeleted = storedPinpoint.deleted
    }
}
