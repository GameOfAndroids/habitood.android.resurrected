package com.astutusdesigns.habitood

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.astutusdesigns.habitood.models.FSBusinessModel
import com.astutusdesigns.habitood.models.FSUserModel
//import com.astutusdesigns.habitood.database.HabitoodDatabase
//import com.astutusdesigns.habitood.models.*
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Global Application variable which will hold variables and query data during the application lifecycle.
 * NEVER CREATE AN INSTANCE OF THIS CLASS!!!!!
 * Android automatically creates it. ONLY use the singleton pattern. A second instance of this class would
 * be very bad for performance.
 * Created by TMiller on 1/22/2018.
 */
class HabitoodApp : Application() {

    private var dataSyncRunning = false
    private var firebaseAnalytics: FirebaseAnalytics? = null

    companion object {
        // this must be false if email verification is required.
        const val NO_EMAIL_VERIFICATION_MODE = true
        const val STORAGE_URL = "gs://habitood.appspot.com"
        lateinit var instance: HabitoodApp
    }

    override fun onCreate() {
        instance = this

        super.onCreate()
        Log.d("HabitoodApp", "HabitoodApp created.")
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
//        Utilities.overrideFont(this, "SERIF", "font/roboto.xml")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d("HabitoodApp", "HabitoodApp terminated.")
    }

    fun userIsLoggedIn() {
        startDataSynchronization()
    }

    fun userIsLoggedOut() {
        stopDataSynchronization()
        wipe()
    }

    private fun startDataSynchronization() {
        if(!dataSyncRunning) {
            dataSyncRunning = true
            FSUserModel.instance.startRealtimeUserProfileSync()
            FSBusinessModel.instance.refreshBusinessProfile()
//            FSTeamModel.instance.startRealtimeTeamSync()
//            val bid = FSBusinessModel.instance.getPersistedBusinessProfile()?.businessId
//            if(bid != null) {
//                ImagesModel.checkForNewBusinessLogo(bid)
//            }
//            val vid = FSBusinessModel.instance.getPersistedBusinessProfile()?.vendorId
//            if(vid != null) {
//                ImagesModel.checkForNewVendorLogo(vid)
//            }
            // FSNotificationModel.instance.startRealtimeNotificationSync()
            // FSCorkBoardModel.instance.startNewNoteCountSync()
            Log.d("HabitoodApp", "Data synchronization started.")
        }
    }

    private fun stopDataSynchronization() {
        if(dataSyncRunning) {
            dataSyncRunning = false
//            FSUserModel.instance.stopRealtimeUserProfileSync()
//            FSTeamModel.instance.stopRealtimeTeamSync()
            // FSNotificationModel.instance.stopRealtimeNotificationSync()
            // FSCorkBoardModel.instance.stopNewNoteCountSync()
            Log.d("HabitoodApp", "Data synchronization stopped.")
        }
    }

    private fun wipe() {
//        val db = HabitoodDatabase.getDatabase(this)
//        db.pinpointDao().deleteAll()
//        db.teamDao().deleteAll()
//        db.userDao().deleteAll()
//        FSUserModel.instance.wipe()
//        FSBusinessModel.instance.wipe()
//        FSTeamModel.instance.wipe()
//        FSNotificationModel.instance.wipe()
//        ImagesModel.removeCompanyLogo()
//        ImagesModel.removeVendorLogo()
        Log.d("HabitoodApp", "Data wipe occurred.")
    }

    fun analyticsPinpointMarkedComplete() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "PinpointMarkedComplete")
        firebaseAnalytics?.logEvent("PinpointCompletion", bundle)
    }

    fun analyticsPinpointAddedToTeam() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "PinpointAddedToTeam")
        firebaseAnalytics?.logEvent("PinpointInteraction", bundle)
    }
}