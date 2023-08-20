package com.astutusdesigns.habitood.team_admin

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.astutusdesigns.habitood.*
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.models.FSTeamModel
import com.astutusdesigns.habitood.models.FSUserModel

/**
 * Created by TMiller on 1/12/2018.
 */
class TeamAdminPresenter(private val context: Context,
                         private val view: TeamAdminContract.View,
                         private val navInterface: NavInterface) : TeamAdminContract.Presenter {

    private val tag = "TeamAdminPresenter"
    private val model: TeamAdminContract.Model = TeamAdminModel(this)
    private val user = model.getCurrentUserProfile()
    private var teamLeaderToChange: Leader? = null
    private var teamMembers: ArrayList<String>? = null
    private var usersSwipedProcessing = 0
    private var teamMembersToDownload = 0
    private var team: RTeam? = null
    set(value) {
        field = value
        if(value != null) {
            view.showTeamDetails(team!!)
            updateTeamMembersData()
            view.setNoteOnFailureSwitch(team?.noteOnFailure ?: false)
            view.setNoteOnSuccessSwitch(team?.noteOnSuccess ?: false)
        }
    }

    init {
        if(user?.rankLevel == 2) {
            if(!user.ctlTeamLeading.isNullOrEmpty()) {
                team = model.getTeamUserLeads(user.ctlTeamLeading!!)
            }
            if(!user.isActive)
                view.hideFab()
        }
    }

    override fun setTeamToOperateOn(team: RTeam) {
        // work with the team that was passed in.
        this.team = team

        // if no team was passed, look to see if user is a CTL and has a team they're already leading.
        if(this.team == null) {
            if(user?.rankLevel == 2) {
                if(user.ctlTeamLeading != null) {
                    this.team = HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().loadTeamById(user.ctlTeamLeading!!)
                }
            }
        }
    }

    override fun onStartCalled() {
        val level = RankLevel.fromInt(user?.rankLevel)

        when(level) {
            RankLevel.Supervisor -> view.showAdminTools(showDeleteTeam = false)
            RankLevel.BusinessAdmin, RankLevel.Owner -> view.showAdminTools(showDeleteTeam = true)
            else -> return
        }

        if(team != null)
            model.startTeamObserving(team!!)
    }

    override fun onStopCalled() {
        if(team != null)
            model.stopTeamObserving(team!!)
    }

    override fun replaceCtlTapped(leader: Leader) {
        teamLeaderToChange = leader

        val userDialog = SelectUserDialog()
        val userPaginator = FSUserModel.instance.getActiveCtlPaginator(true, userDialog)
        userDialog.setPaginator(userPaginator!!)
        userDialog.setOnSwipeCallback(object : SelectUserDialog.UserSwipedInterface {
            override fun swipedLeftUser(user: FSUser) { /* Not used in this implementation. */ }
            override fun swipedRightUser(user: FSUser) {
                userDialog.dismiss()
                view.showProgressBar()


                // clear the replaced leader from the view. When the team listener catches the update,
                // the new ctl will be displayed.
                when(leader) {
                    Leader.Primary ->
                        model.getTeamMemberProfile(team!!.teamLeaderKey!!) { userProfile ->
                            if(userProfile != null)
                                view.removeTeamLeaderFromView(userProfile)
                        }
//                    Leader.Secondary ->
//                        model.getTeamMemberProfile(team!!.secondaryLeaderKey!!) { userProfile ->
//                            if(userProfile != null)
//                                view.removeTeamLeaderFromView(userProfile)
//                        }
                }

                // once the operation is complete, hide progress bar. ctl will be displayed automatically.
                model.replaceLeader(teamLeaderToChange!!, team!!, user) { _ ->
                    view.hideProgressBar()
                }
            }
        })

        userDialog.createUserSelectorDialog(
                context,
                ArrayList(),
                HabitoodApp.instance.getString(R.string.swipe_to_select_ctl),
                SwipeToGenerate.Right).show()
    }

    override fun deleteTeamTapped() {
        val dialog = AlertDialog.Builder(context).create()
        dialog.setTitle(context.getString(R.string.delete_team))
        dialog.setMessage(context.getString(R.string.delete_team_explanation))
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.delete))         { _, _ -> deleteTeam()     }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { _, _ -> dialog.dismiss() }

        dialog.show()
    }

    private fun deleteTeam() {
        view.showProgressBar()
        model.deleteTeam(team!!)
    }

    override fun teamDeletionComplete() {
        view.hideProgressBar()
        view.finishFragment()
    }

    override fun teamUpdateReceived(team: RTeam) {
        this.team = team
    }

    override fun coreTeamLeadersDidDownload(users: List<FSUser>) {
        val sortedUsers = users.sortedWith(compareBy { it.fname })
        val userDialog = SelectUserDialog()
        userDialog.setOnSwipeCallback(object : SelectUserDialog.UserSwipedInterface {
            override fun swipedLeftUser(user: FSUser) { /* Not used in this implementation. */ }
            override fun swipedRightUser(user: FSUser) {
                userDialog.dismiss()
                view.showProgressBar()
                model.replaceLeader(teamLeaderToChange!!, team!!, user) { _ ->
                    view.hideProgressBar()
                }
            }
        })
        view.hideProgressBar()
        userDialog.createUserSelectorDialog(
                context,
                sortedUsers,
                HabitoodApp.instance.getString(R.string.swipe_to_select_ctl),
                SwipeToGenerate.Right).show()
    }

    override fun coreTeamLeaderDownloadFailed(exception: Exception?) {
        view.hideProgressBar()
        view.showErrorMessage()
    }

    override fun teamMemberProfileDidDownload(user: FSUser) {
        teamMembersToDownload -= 1
        if(teamMembersToDownload == 0)
            view.hideProgressBar()

        if(user.userId == team?.teamLeaderKey /*|| user.userId == team?.secondaryLeaderKey*/)
            view.addTeamLeaderToView(user)
        else
            view.addTeamMemberToView(user)
    }

    override fun addFabTapped() {
        val level = RankLevel.fromInt(user?.rankLevel)

        when(level) {
            RankLevel.FrontlinePersonnel -> { /* front-line personnel do not have access to this screen. */ }
            RankLevel.CoreTeamLeader -> if(user?.ctlTeamLeading.isNullOrEmpty()) createTeamFragment() else displayAddUsersDialog()
            RankLevel.Supervisor, RankLevel.BusinessAdmin, RankLevel.Owner -> displayAddUsersDialog()
        }
    }

    private fun createTeamFragment() {
        TODO("Implement me!")
//        navInterface.navigateToFragment(CreateTeamFragment())
    }

    private fun displayAddUsersDialog() {
        if(team != null)
            TODO("Implement me!")
//        navInterface.navigateToFragment(AddUsersToTeamFragment.newInstance(team))
    }

    override fun teamMemberSwipeRemoved(user: FSUser) {
        if(user.userId == team!!.teamLeaderKey /*|| user.userId == team!!.secondaryLeaderKey*/)
            view.undoRemoveUser()
        else
            model.removeUserFromTeam(user, team!!)
    }

    override fun usersForTeamsDidDownload(users: List<FSUser>) {
        var filteredUsers: List<FSUser>? = users

        // for all team members, remove them from the list of users to add to the team.
        teamMembers?.forEach { userId ->
            filteredUsers = filteredUsers?.filter { u -> u.userId != userId }
        }

        // sort the remaining users by first name ascending.
        filteredUsers = filteredUsers?.sortedWith(compareBy { it.fname })

        // add all to arraylist.
        val filteredArrayList = ArrayList<FSUser>()
        filteredArrayList.addAll(filteredUsers!!)

        view.hideProgressBar()

        // show the dialog.
        val userChooser = SelectUserDialog()
        // user chooser must have swipe callback set before creating dialog.
        userChooser.setOnSwipeCallback(object : SelectUserDialog.UserSwipedInterface {
            override fun swipedLeftUser(user: FSUser) { /* Not used in this implementation */ }
            override fun swipedRightUser(user: FSUser) {
                view.showProgressBar()
                usersSwipedProcessing += 1
                model.addUserToTeam(user, team!!)
            }
        })
        // create the dialog.
        userChooser.createUserSelectorDialog(context,
                filteredArrayList,
                context.getString(R.string.add_team_members),
                SwipeToGenerate.Right).show()
    }

    override fun userOperationComplete() {
        usersSwipedProcessing -= 1
        if(usersSwipedProcessing == 0)
            view.hideProgressBar()

        updateTeamMembersData()
    }

    private fun updateTeamMembersData() {
        view.showProgressBar()
        FSTeamModel.instance.getTeamMemberIds(team!!) { members, err ->
            if(err == null) {
                teamMembers = members
                teamMembersToDownload = members?.count() ?: 0
                members?.forEach { memberId ->
                    model.getTeamMemberProfile(memberId)
                }
                view.showTeamMembersCount(members?.size ?: 0)
            }
        }
    }

    override fun usersForTeamsDownloadFailed(exception: Exception?) {
        view.hideProgressBar()
        view.showErrorMessage()
        Log.e(tag, "An error occurred downloading user profiles to add to team: " + exception.toString())
    }

    override fun userNotesOnFailureToggled(on: Boolean) {
        model.userNotesOnFailureToggled(team, on)
    }

    override fun userNotesOnSuccessToggled(on: Boolean) {
        model.userNotesOnSuccessToggled(team, on)
    }
}