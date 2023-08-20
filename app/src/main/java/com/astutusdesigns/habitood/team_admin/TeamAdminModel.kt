package com.astutusdesigns.habitood.team_admin

import android.util.Log
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.Leader
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.FSTeam
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.models.CloudModel
import com.astutusdesigns.habitood.models.FSTeamModel
import com.astutusdesigns.habitood.models.FSUserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Created by timothy on 1/13/18.
 */
class TeamAdminModel(private val presenter: TeamAdminContract.Presenter) : TeamAdminContract.Model, FSTeamModel.RealtimeTeamUpdate {

    private val userModel = FSUserModel.instance
    private var teamCurrentlyObserving: RTeam? = null
    private var teamListener: ListenerRegistration? = null

    override fun addUserToTeam(user: FSUser, team: RTeam) {
        FSTeamModel.instance.addUserToTeam(team, user) { _ ->
            presenter.userOperationComplete()
        }
    }

    override fun getCurrentUserProfile(): FSUser? {
        return userModel.getPersistedUserProfile()
    }

    override fun getTeamMemberProfile(userId: String, callback: (FSUser?) -> Unit) {
        userModel.fetchUserProfileByUid(userId, object : FSUserModel.UserProfileCallback {
            override fun onProfileDownloaded(user: FSUser?) {
                callback(user)
            }

            override fun onDownloadFailed(exception: Exception?) {
                Log.e("TeamAdminModel", "Attempted download of user profile failed. See exception: ${exception.toString()}")
            }
        })
    }

    override fun getTeamMemberProfile(userId: String) {
        userModel.fetchUserProfileByUid(userId, object : FSUserModel.UserProfileCallback {
            override fun onProfileDownloaded(user: FSUser?) {
                if(user != null)
                    presenter.teamMemberProfileDidDownload(user)
            }

            override fun onDownloadFailed(exception: Exception?) {
                Log.e("TeamAdminModel", "Attempted download of user profile failed. See exception: ${exception.toString()}")
            }
        })
    }

    override fun getUsersEligibleForTeams() {
        userModel.downloadUsersEligibleForTeams(object : FSUserModel.UsersDownloadCallback {
            override fun usersDidDownload(users: List<FSUser>) {
                presenter.usersForTeamsDidDownload(users)
            }

            override fun usersDownloadFailed(exception: Exception?) {
                presenter.usersForTeamsDownloadFailed(exception)
            }
        })
    }

    override fun getAvailableCoreTeamLeaders() {
        userModel.downloadAvailableCoreTeamLeaders(object : FSUserModel.UsersDownloadCallback {
            override fun usersDidDownload(users: List<FSUser>) {
                presenter.coreTeamLeadersDidDownload(users)
            }

            override fun usersDownloadFailed(exception: Exception?) {
                presenter.coreTeamLeaderDownloadFailed(exception)
            }
        })
    }

    override fun getTeamUserLeads(teamId: String): RTeam {
        return HabitoodDatabase.getDatabase(HabitoodApp.instance.applicationContext).teamDao().loadTeamById(teamId)
    }

    override fun removeUserFromTeam(user: FSUser, team: RTeam) {
        FSTeamModel.instance.removeUserFromTeam(user, team) { _ ->
            presenter.userOperationComplete()
        }
    }

    override fun replaceLeader(leader: Leader, team: RTeam, user: FSUser, callback: (Exception?) -> Unit) {
        val data = HashMap<String,Any>()
        data["bid"] = team.businessId
        data["tid"] = team.teamId
        data["r"] = if(leader == Leader.Primary) 1 else 2
        data["pctl"] = team.teamLeaderKey!!
        data["nctl"] = user.userId
        data["nctl_name"] = "${user.fname} ${user.lname}"

        CloudModel.instance.firebaseCloudFunction("replace_ctl", data) { _, ex ->
            if(ex != null)
                callback(ex)
        }
    }

    override fun deleteTeam(team: RTeam) {
        FSTeamModel.instance.deleteTeam(team) { resp, err ->
            if(resp != null)
                if(resp != "success")
                    Log.e(this.toString(), "An error occurred deleting a team: ${err.toString()}")

            presenter.teamDeletionComplete()
        }
    }

    override fun startTeamObserving(team: RTeam) {
        if(teamCurrentlyObserving != null && teamCurrentlyObserving!!.teamId != team.teamId)
            stopTeamObserving(teamCurrentlyObserving!!)

        teamCurrentlyObserving = team

        val teamRef = FirebaseFirestore.getInstance().collection("Businesses").document(team.businessId).collection("Teams").document(team.teamId)
        teamListener = teamRef.addSnapshotListener { documentSnapshot, _ ->
            val fsTeam = documentSnapshot?.toObject(FSTeam::class.java)
            if(fsTeam != null) {
                val updatedTeam = RTeam(documentSnapshot.id, fsTeam)
                presenter.teamUpdateReceived(updatedTeam)
            }
        }
    }

    override fun stopTeamObserving(team: RTeam) {
        teamListener?.remove()
        teamListener = null
    }

    override fun teamUpdateDidDownload(team: RTeam) {
        presenter.teamUpdateReceived(team)
    }

    override fun userNotesOnSuccessToggled(ctlTeamLeading: RTeam?, on: Boolean) {
        val t = ctlTeamLeading ?: teamCurrentlyObserving
        t ?: return

        val params = mapOf("bid" to t.businessId,
                           "tid" to t.teamId,
                           "collectNote" to on)

        CloudModel.instance.firebaseCloudFunction("set_team_note_after_success", params) { resp, err ->
            if(err != null) {
                Log.e(this.toString(), err.localizedMessage)
            } else {
                Log.d(this.toString(), "Note-on-success toggled to $on status: ${resp?.get("status") as? String}")
            }
        }
    }

    override fun userNotesOnFailureToggled(ctlTeamLeading: RTeam?, on: Boolean) {
        var t = ctlTeamLeading
        t = ctlTeamLeading ?: teamCurrentlyObserving
        t ?: return

        val params = mapOf("bid" to t.businessId,
                "tid" to t.teamId,
                "collectNote" to on)

        CloudModel.instance.firebaseCloudFunction("set_team_note_after_failure", params) { resp, err ->
            if(err != null) {
                Log.e(this.toString(), err.localizedMessage)
            } else {
                Log.d(this.toString(), "Note-on-failure toggled to $on status: ${resp?.get("status") as? String}")
            }
        }
    }
}