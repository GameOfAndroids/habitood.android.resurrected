package com.astutusdesigns.habitood.team_admin


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.*
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.datamodels.RTeam
import com.astutusdesigns.habitood.rv_adapters.RVItemClicked
import com.astutusdesigns.habitood.sectionrvadapter.OnUserTappedListener
import com.astutusdesigns.habitood.sectionrvadapter.SectionedAdapterInterface
import com.astutusdesigns.habitood.sectionrvadapter.SectionedRecyclerViewAdapter
import com.astutusdesigns.habitood.sectionrvadapter.UserSection
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*


/**
 * This Fragment will be available to Team Managers to manage their teams. It will also be available
 * with a few more features to supervisors to manage teams.
 */
class TeamAdminFragment : Fragment(), TeamAdminContract.View, RVItemClicked<FSUser>,
    OnUserTappedListener, SectionedAdapterInterface {

    companion object {
        private const val TEAM_ARG = "TeamArg"

        fun newInstance(team: RTeam?): TeamAdminFragment {
            val frag = TeamAdminFragment()

            if(team != null) {
                val args = Bundle()
                args.putSerializable(TEAM_ARG, team)
                frag.arguments = args
            }

            return frag
        }
    }

    private          val ctlSection = "CTLSection"
    private          val flpSection = "FLPSection"
    private          var mProgressBarInterface   : ProgressBarInterface? = null
    private lateinit var mTeamMembersRecyclerView: RecyclerView
    private lateinit var mUsersOnTeamAdapter     : SectionedRecyclerViewAdapter
    private lateinit var mTeamNameTextView       : TextView
    private lateinit var mCoreTeamLeaderTextView : TextView
//    private lateinit var mSecondaryLeaderTextView: TextView
    private lateinit var mCreateDateTextView     : TextView
    private lateinit var mTeamSizeTextView       : TextView
    private lateinit var mPresenter              : TeamAdminContract.Presenter
    private lateinit var mCtlUserSection         : UserSection
    private lateinit var mFlpUserSection         : UserSection
    private lateinit var mPinpointSuccessSwitch  : SwitchCompat
    private lateinit var mPinpointFailSwitch     : SwitchCompat

    override fun getPositionInSection(index: Int): Int {
        return mUsersOnTeamAdapter.getPositionInSection(index)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_team_admin, container, false)

        mTeamNameTextView = v.findViewById(R.id.admin_team_textview)
        mCoreTeamLeaderTextView = v.findViewById(R.id.admin_ctl_view)
//        mSecondaryLeaderTextView = v.findViewById(R.id.admin_sctl_view)
        mCreateDateTextView = v.findViewById(R.id.team_create_date)
        mTeamSizeTextView = v.findViewById(R.id.team_size)

        mTeamMembersRecyclerView = v.findViewById(R.id.admin_team_members_rv)
        mTeamMembersRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mTeamMembersRecyclerView.itemAnimator = DefaultItemAnimator()
        if(isAdded) {
            mUsersOnTeamAdapter = SectionedRecyclerViewAdapter()

            mCtlUserSection = UserSection(getString(R.string.core_team_leader),    this, this)
            mFlpUserSection = UserSection(getString(R.string.frontline_personnel), this, this)

            mUsersOnTeamAdapter.addSection(mCtlUserSection)
            mUsersOnTeamAdapter.addSection(mFlpUserSection)
        }

        mTeamMembersRecyclerView.adapter = mUsersOnTeamAdapter
        mPinpointSuccessSwitch = v.findViewById(R.id.pinpointSuccessSwitch)
        mPinpointFailSwitch    = v.findViewById(R.id.pinpointFailSwitch)

        if(isAdded) {
            val swipeTools = RvSwipeTools(requireActivity(), object : RvSwipeTools.OnSwipeCallback {
                override fun onRightSwipe(adapterPosition: Int) { /* Not used in this implementation. */ }
                override fun onLeftSwipe(adapterPosition: Int) {
                    val index = mUsersOnTeamAdapter.getPositionInSection(adapterPosition)
                    val swipedUserSection = (mUsersOnTeamAdapter.getSectionForPosition(adapterPosition) as UserSection)
                    val swipedUser = swipedUserSection.users.getUser(index)
                    swipedUserSection.users.removeUser(index)
                    if(swipedUserSection == mCtlUserSection) {
                        swipedUserSection.users.addUser(swipedUser)
                        mUsersOnTeamAdapter.notifyDataSetChanged()
                        Snackbar.make(view!!, getString(R.string.cannot_remove_leader), Snackbar.LENGTH_SHORT).show()
                    } else {
                        mPresenter.teamMemberSwipeRemoved(swipedUser)
                        mUsersOnTeamAdapter.notifyItemRemovedFromSection(mFlpUserSection, index)
                    }
                }
            })
            swipeTools.generateLeftSwipeCallback(mTeamMembersRecyclerView, R.mipmap.ic_delete_white_24dp)
        }

        v.findViewById<FloatingActionButton>(R.id.team_admin_add_fab).setOnClickListener { addFabTapped() }

        return v
    }

    private fun attachSwitchCheckedListeners() {
        mPinpointSuccessSwitch.setOnCheckedChangeListener { _, isChecked ->
            mPresenter.userNotesOnSuccessToggled(isChecked)
        }
        mPinpointFailSwitch.setOnCheckedChangeListener { _, isChecked ->
            mPresenter.userNotesOnFailureToggled(isChecked)
        }
    }

    private fun removeSwitchCheckedListeners() {
        mPinpointSuccessSwitch.setOnCheckedChangeListener(null)
        mPinpointFailSwitch.setOnCheckedChangeListener(null)
    }

    override fun setNoteOnSuccessSwitch(on: Boolean) {
        removeSwitchCheckedListeners()
        mPinpointSuccessSwitch.isChecked = on
        attachSwitchCheckedListeners()
    }

    override fun setNoteOnFailureSwitch(on: Boolean) {
        removeSwitchCheckedListeners()
        mPinpointFailSwitch.isChecked = on
        attachSwitchCheckedListeners()
    }

    override fun onUserTapped(user: FSUser) {
        TODO("IMPLEMENT ME")
//        (activity as? NavInterface)?.navigateToFragment(UserOverviewFragment.newInstance(user), true)
    }

    override fun onStart() {
        super.onStart()
        if(isAdded) {
            mPresenter = TeamAdminPresenter(requireActivity(), this, activity as NavInterface)
            mProgressBarInterface = activity as ProgressBarInterface
            if (arguments != null)
                mPresenter.setTeamToOperateOn(arguments?.get(TEAM_ARG) as RTeam)

            mPresenter.onStartCalled()
            attachSwitchCheckedListeners()
        }
    }

    override fun onStop() {
        super.onStop()
        mPresenter.onStopCalled()
        mProgressBarInterface = null
    }

    override fun undoRemoveUser() {
//        mCtlUserSection
//        mUsersOnTeamAdapter.undoRemoveItem()
    }

    override fun hideFab() {
        view?.findViewById<FloatingActionButton>(R.id.team_admin_add_fab)?.visibility = View.INVISIBLE
    }

    override fun showAdminTools(showDeleteTeam: Boolean) {
        view?.findViewById<TextView>(R.id.pctl_tap_to_change)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.sctl_tap_to_change)?.visibility = View.VISIBLE
        mCoreTeamLeaderTextView.setOnClickListener  { mPresenter.replaceCtlTapped(Leader.Primary)   }
//        mSecondaryLeaderTextView.setOnClickListener { mPresenter.replaceCtlTapped(Leader.Secondary) }
        if(showDeleteTeam) {
            view?.findViewById<Button>(R.id.delete_team_button)?.visibility = View.VISIBLE
            view?.findViewById<Button>(R.id.delete_team_button)?.setOnClickListener { mPresenter.deleteTeamTapped() }
        }
    }

    override fun showTeamDetails(team: RTeam) {
        mTeamNameTextView.text = team.teamName
        mCoreTeamLeaderTextView.text = team.teamLeaderName
//        mSecondaryLeaderTextView.text = team.secondaryLeaderName
        mCreateDateTextView.text = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(team.createDate)
    }

    override fun showAddTeamMembersFragment(team: RTeam) {

    }

    override fun showTeamMembersCount(count: Int) {
        mTeamSizeTextView.text = count.toString()
    }

    private fun addFabTapped() {
        mPresenter.addFabTapped()
    }

    override fun showProgressBar() {
        mProgressBarInterface?.showProgressBar()
    }

    override fun hideProgressBar() {
        mProgressBarInterface?.hideProgressBar()
    }

    override fun showErrorMessage() {
        if(isVisible)
            Snackbar.make(requireView(), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }

    override fun addTeamLeaderToView(user: FSUser) {
        mCtlUserSection.addUser(user)
        mUsersOnTeamAdapter.notifyDataSetChanged()
    }

    override fun removeTeamLeaderFromView(user: FSUser) {
        mCtlUserSection.users.removeUser(user)
        mUsersOnTeamAdapter.notifyDataSetChanged()
    }

    override fun addTeamMemberToView(user: FSUser) {
        mFlpUserSection.addUser(user)
        mUsersOnTeamAdapter.notifyDataSetChanged()
    }

    override fun finishFragment() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onItemClicked(item: FSUser) {
        TODO("Implement me")
//        (activity as? NavInterface)?.navigateToFragment(UserOverviewFragment.newInstance(item))
    }
}