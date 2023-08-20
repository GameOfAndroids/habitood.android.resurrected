package com.astutusdesigns.habitood.teams

import com.astutusdesigns.habitood.datamodels.RTeam

/**
 * Created by TMiller on 1/22/2018.
 */
interface TeamsContract {
    interface Model {
        fun getUsersTeams(): List<RTeam>
        fun setActiveTeam(team: RTeam)
    }
    interface View {
        fun displayTeams(teams: List<RTeam>)
        fun finishFragment()
    }
    interface Presenter {
        fun start()
        fun makeTeamActive(team: RTeam)
    }
}