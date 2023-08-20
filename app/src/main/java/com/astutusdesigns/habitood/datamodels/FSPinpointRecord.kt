package com.astutusdesigns.habitood.datamodels

import com.google.firebase.firestore.Exclude
import java.util.*

/**
 * Created by timothy on 1/25/18.
 */
class FSPinpointRecord() {

    var pinpointDate = Date()
    var uid: String? = null
    var tid: String? = null
    var teamName: String? = null
    var pid: String? = null
    var pinpointTitle: String? = null
    var userName: String? = null
    var userEmail: String? = null
    var succeeded: Boolean? = null
    var ctlId: String? = null
    var ctlName: String? = null
    var ctlEmail: String? = null
    var userNotes: String? = null
    var recordId = ""
        @Exclude get

    constructor(pinpointDate: Date = Date(),
                uid: String,
                userName: String?,
                userEmail: String?,
                tid: String,
                teamName: String?,
                pid: String,
                pinpointTitle: String?,
                ctlId: String?,
                ctlName: String?,
                ctlEmail: String?,
                succeeded: Boolean,
                userNotes: String?) : this() {

        this.pinpointDate = pinpointDate
        this.uid = uid
        this.userName = userName
        this.userEmail = userEmail
        this.tid = tid
        this.teamName = teamName
        this.pid = pid
        this.pinpointTitle = pinpointTitle
        this.ctlId = ctlId
        this.ctlName = ctlName
        this.ctlEmail = ctlEmail
        this.succeeded = succeeded
        this.userNotes = userNotes
    }
}