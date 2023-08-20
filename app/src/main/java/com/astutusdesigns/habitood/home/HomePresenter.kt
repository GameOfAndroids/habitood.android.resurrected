package com.astutusdesigns.habitood.home

import android.graphics.Bitmap
import android.util.Log
import com.astutusdesigns.habitood.RankLevel
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.FSVendor
import com.astutusdesigns.habitood.datamodels.RPinpoint
import com.astutusdesigns.habitood.models.FSUserModel

/**
 * Presenter class for the HomeFragment.
 * Created by TMiller on 1/10/2018.
 */
class HomePresenter(private val view: HomeContract.View) : HomeContract.Presenter, FSUserModel.RealtimeUserProfileCallback {

    private val model = HomeModel(this)

    private var user: FSUser? = null
        set(value) {
            field = value
            if(user?.rankLevel != null)
                view.prepareHomeExperienceForRole(RankLevel.fromInt(user?.rankLevel))
            else
                view.prepareHomeExperienceForRole(RankLevel.FrontlinePersonnel)
        }
        get() {
            if(field == null)
                field = model.getUserProfile()
            return field
        }

    private var business: FSBusiness? = null
    set(value) {
        field = value
        if(value != null) {
            view.updateBusinessTextWidgets(value)
            model.getBusinessLogo()

            if(business?.isActive == false)
                view.setBusinessActiveStatus(active = false)
            else {
                view.setBusinessActiveStatus(active = true)
            }
        }
    }

    private var vendor: FSVendor? = null
    set(value) {
        field = value
        if(value != null) {
            view.updateVendorTextWidgets(value)
            model.getVendorLogo()
        }
    }

    override fun onStartCalled() {
        FSUserModel.instance.registerUserProfileObserver(this)
        model.getBusinessProfile()
        model.getVendorProfile()
        if(model.getActiveTeam() != null) {
            model.startObservingActiveTeam()
        }
        model.startObservingNotificationCount()
        model.startObservingNewMessageCount()
    }

    override fun onStopCalled() {
        FSUserModel.instance.removeUserProfileObserver(this)
        model.stopObservingActiveTeam()
        model.stopObservingNotifications()
        model.stopObservingNewMessageCount()
    }

    override fun newMessageCountAvailable(i: Int) {
        view.showNewMessageCount(i)
    }

    override fun numberNotificationsFound(num: Int) {
        view.showNotificationCount(num)
    }

    override fun onUserProfileUpdateReceived(user: FSUser) {
        this.user = user
    }

    override fun onBusinessProfileAvailable(business: FSBusiness) {
        this.business = business
    }

    override fun onVendorProfileAvailable(vendor: FSVendor) {
        this.vendor = vendor
    }

    override fun businessLogoIsAvailable(b: Bitmap) {
        view.showBusinessLogo(b)
    }

    override fun vendorLogoIsAvailable(b: Bitmap) {
        view.showVendorLogo(b)
    }

    override fun teamUpdateOccurred() {
        Log.d("HomePresenter", "TEAM UPDATE called.")
        displayUpdatedTeamData()
    }

    private fun displayUpdatedTeamData() {
        val activeTeam = model.getActiveTeam()
        if(activeTeam != null) {
            view.updateActiveTeam(activeTeam)
            view.showActivePinpoints(model.getActivePinpoints())
        }
    }

    override fun onSwipedPinpoint(p: RPinpoint, success: Boolean) {
        // determine if we should ask for a note, or if we should just
        // record the pinpoint record.
        when(success) {
            true ->
                if(askForAnonymousNoteOnSuccess())
                    view.showAddNoteDialog { note ->
                        recordPinpoint(p, success, note)
                    }
                else
                    recordPinpoint(p, success, null)

            false ->
                if(askForAnonymousNoteOnFailure())
                    view.showAddNoteDialog { note ->
                        recordPinpoint(p, success, note)
                    }
                else
                    recordPinpoint(p, success, null)
        }
    }

    private fun recordPinpoint(p: RPinpoint, success: Boolean, note: String?) {
        val setRecordStatus = model.onSwipedPinpoint(p, success, note)
        view.showPinpointSwipeFeedback(setRecordStatus)
    }

    override fun navToTeamAdminTapped() {
        when(user?.rankLevel) {
            3 -> view.navToSupTeamAdmin()
            4, 5 -> view.navToBusAdminTeamAdmin()
        }
    }

    override fun askForAnonymousNoteOnSuccess(): Boolean {
        return model.getActiveTeam()?.noteOnSuccess ?: false
    }

    override fun askForAnonymousNoteOnFailure(): Boolean {
        return model.getActiveTeam()?.noteOnFailure ?: false
    }
}