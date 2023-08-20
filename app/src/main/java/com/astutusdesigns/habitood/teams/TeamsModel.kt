package com.astutusdesigns.habitood.teams

import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.database.HabitoodDatabase
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.models.FSTeamModel

/**
 * Created by TMiller on 1/22/2018.
 */
class TeamsModel(private val presenter: TeamsContract.Presenter) : TeamsContract.Model {

    override fun getUsersTeams(): List<RTeam> {
        val db = HabitoodDatabase.getDatabase(HabitoodApp.instance.baseContext)
        return db.teamDao().loadNonDeletedTeams()
    }

    override fun setActiveTeam(team: RTeam) {
        FSTeamModel.instance.setActiveTeam(team)
    }
}