package com.astutusdesigns.habitood.home

import android.graphics.Bitmap
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.*
import com.astutusdesigns.habitood.models.*
import java.util.*

/**
 * Home model which will manage data for the home fragment.
 * Created by TMiller on 1/10/2018.
 */
class HomeModel(private val contractPresenter: HomeContract.Presenter) : HomeContract.Model, FSTeamModel.RealtimeTeamUpdate, FSBusinessModel.BusinessCallback, FSNotificationModel.RealtimeNotifsCallback { //, FSCorkBoardModel.RealtimeUnreadCallback {

    private val mUserModel = FSUserModel.instance
    private var mActiveTeamObserverRunning = false

    override fun getUserProfile(): FSUser? {
        return mUserModel.getPersistedUserProfile()
    }

    override fun getBusinessProfile() {
        FSBusinessModel.instance.registerBusinessObserver(this)
    }

    override fun getVendorProfile() {
        FSBusinessModel.instance.getPersistedVendorProfile()?.let { v ->
            contractPresenter.onVendorProfileAvailable(v)
        }
//        doAsync {
//            var v: FSVendor? = null
//            while (v == null) {
//                v = FSBusinessModel.instance.getPersistedVendorProfile()
//                if(v == null) {
//                    Thread.sleep(1000)
//                    Log.d("HomeModel", "Thread sleeping awaiting download of vendor profile.")
//                }
//            }
//            uiThread { contractPresenter.onVendorProfileAvailable(v) }
//        }
    }

    override fun getBusinessLogo() {
        val business = FSBusinessModel.instance.getPersistedBusinessProfile()
        business ?: return

        ImagesModel.getBusinessLogo(business, object : ImagesModel.ImageDownloadCallback {
            override fun imageDownloaded(bitmap: Bitmap?) {
                if(bitmap != null) { contractPresenter.businessLogoIsAvailable(bitmap) }
            }
        })
    }

    override fun getVendorLogo() {
        val vendor = FSBusinessModel.instance.getPersistedVendorProfile()
        vendor ?: return

        ImagesModel.getVendorLogo(vendor, object : ImagesModel.ImageDownloadCallback {
            override fun imageDownloaded(bitmap: Bitmap?) {
                if(bitmap != null) { contractPresenter.vendorLogoIsAvailable(bitmap) }
            }
        })
    }

    override fun businessUpdate(business: FSBusiness) {
        contractPresenter.onBusinessProfileAvailable(business)
    }

    override fun getActiveTeam(): RTeam? {
        return FSTeamModel.instance.getActiveTeam()
    }

    override fun getActivePinpoints(): List<RPinpoint> {
        val team = FSTeamModel.instance.getActiveTeam()
        val db = HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext)
        val pp = ArrayList<RPinpoint>()
        team?.pinpointKeys?.forEach { pId ->
            val p = db.pinpointDao().loadPinpointById(pId)
            if(p != null) pp.add(p)
        }
        return pp
    }

    override fun onSwipedPinpoint(p: RPinpoint, success: Boolean, note: String?): Boolean {
        val team = FSTeamModel.instance.getActiveTeam()
        val endUser = FSUserModel.instance.getPersistedUserProfile()
        endUser ?: return false
        team ?: return false

        if(team.teamLeaderKey != null) {
            FSUserModel.instance.fetchUserProfileByUid(team.teamLeaderKey!!, object : FSUserModel.UserProfileCallback {
                override fun onProfileDownloaded(user: FSUser?) {
                    val rec = FSPinpointRecord(Date(), endUser.userId, endUser.getDisplayName(), endUser.email, team.teamId, team.teamName, p.pinpointId, p.pinpointTitle, team.teamLeaderKey, team.teamLeaderName, user?.email, success, if(note.isNullOrEmpty()) null else note)
                    FSPinpointModel.instance.recordPinpoint(rec)
                }

                override fun onDownloadFailed(exception: Exception?) {
                    val rec = FSPinpointRecord(Date(), endUser.userId, endUser.getDisplayName(), endUser.email, team.teamId, team.teamName, p.pinpointId, p.pinpointTitle, team.teamLeaderKey, team.teamLeaderName, "NA", success, if(note.isNullOrEmpty()) null else note)
                    FSPinpointModel.instance.recordPinpoint(rec)
                }
            })
        } else {
            val rec = FSPinpointRecord(Date(), endUser.userId, endUser.getDisplayName(), endUser.email, team.teamId, team.teamName, p.pinpointId, p.pinpointTitle, team.teamLeaderKey, team.teamLeaderName, "NA", success, if(note.isNullOrEmpty()) null else note)
            FSPinpointModel.instance.recordPinpoint(rec)
        }

        return true
    }

    override fun startObservingActiveTeam() {
        if(!mActiveTeamObserverRunning) {
            mActiveTeamObserverRunning = true
            val team = getActiveTeam()
            team ?: return
            FSTeamModel.instance.registerTeamObserver(team, this)
        }
    }

    override fun teamUpdateDidDownload(team: RTeam) {
        contractPresenter.teamUpdateOccurred()
    }

    override fun stopObservingActiveTeam() {
        if(mActiveTeamObserverRunning) {
            val team = getActiveTeam()
            team ?: return
            FSTeamModel.instance.removeTeamObserver(team, this)
            mActiveTeamObserverRunning = false
        }
    }

    override fun startObservingNotificationCount() {
        FSNotificationModel.instance.registerRealtimeObserver(this)
    }

    override fun stopObservingNotifications() {
        FSNotificationModel.instance.removeRealtimeObserver(this)
    }

    override fun onNotificationsReceived(n: ArrayList<FSNotification>) {
        contractPresenter.numberNotificationsFound(n.size)
    }

    override fun startObservingNewMessageCount() {
//        FSCorkBoardModel.instance.registerUnreadNoteCountObserver(this)
    }

    override fun stopObservingNewMessageCount() {
//        FSCorkBoardModel.instance.removeUnreadNoteCountObserver(this)
    }

//    override fun updatedNewNoteCount(count: Int) {
//        contractPresenter.newMessageCountAvailable(count)
//    }
}