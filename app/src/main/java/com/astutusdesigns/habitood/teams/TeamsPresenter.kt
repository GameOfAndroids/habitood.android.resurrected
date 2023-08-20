package com.astutusdesigns.habitood.teams

import com.astutusdesigns.habitood.datamodels.RTeam

/**
 * Created by TMiller on 1/22/2018.
 */
class TeamsPresenter(private val view: TeamsContract.View) : TeamsContract.Presenter {

    private val model: TeamsContract.Model = TeamsModel(this)
    private var teams: List<RTeam>? = null
    set(value) {
        if(value != null)
            view.displayTeams(value)
    }

    override fun start() {
        teams = model.getUsersTeams()
    }

    override fun makeTeamActive(team: RTeam) {
        model.setActiveTeam(team)
        view.finishFragment()
    }

}