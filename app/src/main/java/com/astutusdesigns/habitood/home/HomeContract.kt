package com.astutusdesigns.habitood.home

import android.graphics.Bitmap
import com.astutusdesigns.habitood.RankLevel
import com.astutusdesigns.habitood.datamodels.*

/**
 * MVP interface contract for the home package.
 * Created by TMiller on 1/10/2018.
 */
interface HomeContract {
    interface Model {
        fun getUserProfile(): FSUser?
        fun getBusinessProfile()
        fun getVendorProfile()
        fun getBusinessLogo()
        fun getVendorLogo()
        fun getActiveTeam(): RTeam?
        fun getActivePinpoints(): List<RPinpoint>
        fun onSwipedPinpoint(p: RPinpoint, success: Boolean, note: String?): Boolean
        fun startObservingActiveTeam()
        fun stopObservingActiveTeam()
        fun startObservingNotificationCount()
        fun stopObservingNotifications()
        fun startObservingNewMessageCount()
        fun stopObservingNewMessageCount()
    }
    interface Presenter {
        fun onStartCalled()
        fun onStopCalled()
        fun onBusinessProfileAvailable(business: FSBusiness)
        fun onVendorProfileAvailable(vendor: FSVendor)
        fun businessLogoIsAvailable(b: Bitmap)
        fun vendorLogoIsAvailable(b: Bitmap)
        fun onSwipedPinpoint(p: RPinpoint, success: Boolean)
        fun newMessageCountAvailable(i: Int)
        fun numberNotificationsFound(num: Int)
        fun navToTeamAdminTapped()
        fun teamUpdateOccurred()
        fun askForAnonymousNoteOnSuccess(): Boolean
        fun askForAnonymousNoteOnFailure(): Boolean
    }
    interface View {
        fun setBusinessActiveStatus(active: Boolean)
        fun updateBusinessTextWidgets(business: FSBusiness)
        fun showBusinessLogo(b: Bitmap)
        fun updateVendorTextWidgets(vendor: FSVendor)
        fun showVendorLogo(b: Bitmap)
        fun showActivePinpoints(pp: List<RPinpoint>)
        fun showActiveTeamCard()
        fun hideActivePinpoints()
        fun hideActiveTeamCard()
        fun updateActiveTeam(team: RTeam)
        fun showNewMessageCount(count: Int)
        fun showNotificationCount(count: Int)
        fun showProgressBar(show: Boolean)
        fun prepareHomeExperienceForRole(rankLevel: RankLevel)
        fun navToSupTeamAdmin()
        fun navToBusAdminTeamAdmin()
        fun showPinpointSwipeFeedback(success: Boolean)
        fun showAddNoteDialog(callback: (note: String?) -> Unit)
    }
}