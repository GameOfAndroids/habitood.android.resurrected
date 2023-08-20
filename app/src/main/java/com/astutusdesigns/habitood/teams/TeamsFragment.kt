package com.astutusdesigns.habitood.teams


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.rv_adapters.UserTeamsAdapter


/**
 * A simple [Fragment] subclass.
 */
class TeamsFragment : Fragment(), TeamsContract.View, UserTeamsAdapter.TeamSelectedInterface {

    private var presenter: TeamsContract.Presenter? = null
    private val teamAdapter: UserTeamsAdapter = UserTeamsAdapter(null)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_teams, container, false)

        view.findViewById<RecyclerView>(R.id.teams_recycler_view).apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = teamAdapter
        }

        teamAdapter.setTeamSelectedInterface(this)

        return view
    }

    override fun onStart() {
        super.onStart()
        presenter = TeamsPresenter(this)
        presenter?.start()
    }

    override fun makeTeamActiveTapped(team: RTeam) {
        presenter?.makeTeamActive(team)
    }

    override fun displayTeams(teams: List<RTeam>) {
        teamAdapter.newDataSetReceived(ArrayList(teams))
    }

    override fun finishFragment() {
        activity?.supportFragmentManager?.popBackStack()
    }
}
