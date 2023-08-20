package com.astutusdesigns.habitood.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.datamodels.RTeam
import java.util.*

/**
 * Created by TMiller on 1/22/2018.
 */
class UserTeamsAdapter(teams: ArrayList<RTeam>?) : RecyclerView.Adapter<UserTeamsAdapter.TeamHolder>() {

    interface TeamSelectedInterface {
        fun makeTeamActiveTapped(team: RTeam)
    }

    private var mAllTeams = teams
    private var mTeamCallback: TeamSelectedInterface? = null

    fun setTeamSelectedInterface(callback: TeamSelectedInterface) {
        mTeamCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTeamsAdapter.TeamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val cardView = inflater.inflate(R.layout.layout_user_team_card, parent, false)
        return TeamHolder(cardView)
    }

    override fun onBindViewHolder(holder: UserTeamsAdapter.TeamHolder, position: Int) {
        val team = mAllTeams!![position]
        holder.setTeam(team)
    }

    override fun getItemCount(): Int {
        return mAllTeams!!.size
    }

    fun newDataSetReceived(newTeamList: ArrayList<RTeam>) {
        mAllTeams = newTeamList
        notifyDataSetChanged()
    }

    fun getItem(position: Int): RTeam {
        return mAllTeams!![position]
    }

    inner class TeamHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val mTeamTextView = itemView.findViewById<TextView>(R.id.fragment_teams_card_team_title_text)
        private val mMakeActiveBtn = itemView.findViewById<Button>(R.id.make_active_btn)

        init {
            mMakeActiveBtn.setOnClickListener(this)
        }

        fun setTeam(team: RTeam) {
            mTeamTextView.text = team.teamName
        }

        override fun onClick(view: View) {
            mTeamCallback?.makeTeamActiveTapped(mAllTeams!![adapterPosition])
        }
    }
}