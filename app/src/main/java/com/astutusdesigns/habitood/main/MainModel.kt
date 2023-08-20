package com.astutusdesigns.habitood.main

import android.content.Context
import androidx.fragment.app.Fragment
import com.astutusdesigns.habitood.MenuScreens
import com.astutusdesigns.habitood.authentication.LoginFragment
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.home.HomeFragment
import com.astutusdesigns.habitood.models.FSTeamModel
import com.astutusdesigns.habitood.models.FSUserModel
import com.astutusdesigns.habitood.notifications.NotificationsFragment

//import com.astutusdesigns.habitood.home.HomeFragment
//import com.astutusdesigns.habitood.bulk_export.BulkExportFragment
//import com.astutusdesigns.habitood.corkboard_list.CorkBoardListFragment
//import com.astutusdesigns.habitood.notifications.NotificationsFragment
//import com.astutusdesigns.habitood.pinpoint_admin.PinpointAdminFragment
//import com.astutusdesigns.habitood.pinpoint_query.PinpointQueryFragment
//import com.astutusdesigns.habitood.pinpoints.PinpointsFragment
//import com.astutusdesigns.habitood.settings.SettingsFragment
//import com.astutusdesigns.habitood.sup_pinpoint_admin.SupPinpointAdmin
//import com.astutusdesigns.habitood.sup_teams_admin.SupTeamsAdminFragment
//import com.astutusdesigns.habitood.team_admin.TeamAdminFragment
//import com.astutusdesigns.habitood.teams.TeamsFragment

/**
 * Model for the Main Activity following the MVP Design Pattern.
 * Created by TMiller on 1/10/2018.
 */
class MainModel(private val contractPresenter: MainContract.Presenter) : MainContract.Model, FSUserModel.RealtimeUserProfileCallback {

    private val mUserModel = FSUserModel.instance
    private val mTeamModel = FSTeamModel.instance
    private var mFragments: HashMap<MenuScreens, Fragment> = hashMapOf()

    override fun startObservingUserProfile() {
        mUserModel.registerUserProfileObserver(this)
    }

    override fun stopObservingUserProfile() {
        mUserModel.removeUserProfileObserver(this)
    }

    override fun onUserProfileUpdateReceived(user: FSUser) {
        contractPresenter.updatedUserProfileReceived(user)
    }

    override fun getOrCreateFragment(context: Context, screen: MenuScreens): Fragment {
        val user = mUserModel.getPersistedUserProfile()
        val frag: Fragment? =
            if(mFragments.containsKey(screen))
                mFragments[screen]
            else {
                when (screen) {
                    MenuScreens.Home -> HomeFragment.newInstance()
//                    MenuScreens.CorkBoard -> CorkBoardListFragment()
                    MenuScreens.Notifications -> NotificationsFragment()
//                    MenuScreens.Teams -> TeamsFragment()
//                    MenuScreens.Pinpoints -> PinpointsFragment()
//                    MenuScreens.PinpointAdmin -> {
//                        if(user!!.rankLevel != 3)
//                            PinpointAdminFragment()
//                        else
//                            SupPinpointAdmin()
//
////                        if(user!!.rank != FSUser.Rank.Supervisor)
////                            PinpointAdminFragment()
////                        else
////                            SupPinpointAdmin()
//                    }
//                    MenuScreens.TeamsAdmin -> {
//                        when(user!!.rankLevel) {
//                            2 -> TeamAdminFragment.newInstance(null)
//                            3 -> SupTeamsAdminFragment()
//                            else -> null
//                        }

//                        when(user!!.rank) {
//                            FSUser.Rank.CoreTeamLeader -> TeamAdminFragment.newInstance(null)
//                            FSUser.Rank.Supervisor -> SupTeamsAdminFragment()
//                            /*FSUser.Rank.Manager -> TeamsListFragment.newInstance()
//                            FSUser.Rank.Admin -> TeamsListFragment.newInstance()*/
//                            else -> null
//                        }
//                    }
//                    MenuScreens.PinpointProgress -> PinpointQueryFragment()
//                    MenuScreens.BulkExport -> BulkExportFragment()
//                    MenuScreens.Settings -> SettingsFragment()
                    else -> TODO("You gotta implement this navigation yo.")
                }
            }

        if(frag != null && screen != MenuScreens.BulkExport)
            mFragments[screen] = frag

        return frag!!
    }
}