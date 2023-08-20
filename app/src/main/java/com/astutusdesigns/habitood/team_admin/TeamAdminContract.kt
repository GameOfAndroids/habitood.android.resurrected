package com.astutusdesigns.habitood.team_admin

import com.astutusdesigns.habitood.Leader
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RTeam

/**
 * Created by TMiller on 1/12/2018.
 */
interface TeamAdminContract {
    interface Model {
        fun addUserToTeam(user: FSUser, team: RTeam)
        fun getCurrentUserProfile(): FSUser?
        fun getTeamMemberProfile(userId: String)
        fun getTeamMemberProfile(userId: String, callback: (FSUser?) -> Unit)
        fun getUsersEligibleForTeams()
        fun getAvailableCoreTeamLeaders()
        fun getTeamUserLeads(teamId: String): RTeam
        fun startTeamObserving(team: RTeam)
        fun stopTeamObserving(team: RTeam)
        fun removeUserFromTeam(user: FSUser, team: RTeam)
        fun replaceLeader(leader: Leader, team: RTeam, user: FSUser, callback: (Exception?) -> Unit)
        fun deleteTeam(team: RTeam)
        fun userNotesOnSuccessToggled(ctlTeamLeading: RTeam?, on: Boolean)
        fun userNotesOnFailureToggled(ctlTeamLeading: RTeam?, on: Boolean)
    }
    interface Presenter {
        fun addFabTapped()
        fun replaceCtlTapped(leader: Leader)
        fun deleteTeamTapped()
        fun teamDeletionComplete()
        fun setTeamToOperateOn(team: RTeam)
        fun teamUpdateReceived(team: RTeam)
        fun usersForTeamsDidDownload(users: List<FSUser>)
        fun usersForTeamsDownloadFailed(exception: Exception?)
        fun teamMemberProfileDidDownload(user: FSUser)
        fun userOperationComplete()
        fun teamMemberSwipeRemoved(user: FSUser)
        fun coreTeamLeadersDidDownload(users: List<FSUser>)
        fun coreTeamLeaderDownloadFailed(exception: Exception?)
        fun onStartCalled()
        fun onStopCalled()
        fun userNotesOnSuccessToggled(on: Boolean)
        fun userNotesOnFailureToggled(on: Boolean)
    }
    interface View {
        fun showAddTeamMembersFragment(team: RTeam)
        fun showAdminTools(showDeleteTeam: Boolean)
        fun showTeamDetails(team: RTeam)
        fun showTeamMembersCount(count: Int)
        fun addTeamLeaderToView(user: FSUser)
        fun removeTeamLeaderFromView(user: FSUser)
        fun addTeamMemberToView(user: FSUser)
        fun showProgressBar()
        fun hideProgressBar()
        fun showErrorMessage()
        fun undoRemoveUser()
        fun finishFragment()
        fun hideFab()
        fun setNoteOnSuccessSwitch(on: Boolean)
        fun setNoteOnFailureSwitch(on: Boolean)
    }
}