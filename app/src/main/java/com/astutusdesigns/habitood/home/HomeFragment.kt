package com.astutusdesigns.habitood.home


import AddPinpointNoteDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
//import com.astutusdesigns.habitood.bus_admin.BusinessAdminFragment
//import com.astutusdesigns.habitood.bus_admin_teams_list.TeamsListFragment
import com.astutusdesigns.habitood.datamodels.*
//import com.astutusdesigns.habitood.manage_admins.ManageBusinessAdminsFragment
import com.astutusdesigns.habitood.notifications.NotificationsFragment
//import com.astutusdesigns.habitood.pinpoint_query.PinpointQueryFragment
import com.astutusdesigns.habitood.rv_adapters.SwipePinpointsAdapter
//import com.astutusdesigns.habitood.sup_pinpoint_admin.SupPinpointAdmin
//import com.astutusdesigns.habitood.sup_teams_admin.SupTeamsAdminFragment
//import com.astutusdesigns.habitood.user_admin.UserAdminFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.astutusdesigns.habitood.*
import com.astutusdesigns.habitood.teams.TeamsFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//import com.astutusdesigns.habitood.bulk_export.BulkExportFragment


/**
 * HomeFragment which will be the first thing that a logged in user sees.
 */
class HomeFragment : Fragment(), HomeContract.View, SwipePinpointsAdapter.RecordPinpointSwipeCallback {

    companion object {
        fun newInstance(): Fragment {
            return HomeFragment()
        }
    }

    private var mNavInterface: NavInterface? = null
    private var mPresenter: HomeContract.Presenter? = null
    private var mBusinessTextView: TextView? = null
    private var mPpAdapter: SwipePinpointsAdapter? = null
    private var rv: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = HomePresenter(this)
        mNavInterface = activity as? NavInterface
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        rv = v.findViewById(R.id.home_pinpoints_rv)
        rv?.itemAnimator = DefaultItemAnimator()
        rv?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mPpAdapter = SwipePinpointsAdapter(v.context, rv!!, SwipePinpointsAdapter.SwipeCard.Mini, null, this)
        rv?.adapter = mPpAdapter

        with(v) {
            // messages and notifications icons
            findViewById<View>(R.id.homefragment_message_layout)
                .setOnClickListener { corkBoardMessageTapped() }
            findViewById<View>(R.id.homefragment_notification_layout)
                .setOnClickListener { notificationTapped() }
            findViewById<Button>(R.id.select_active_team_button).setOnClickListener {
                mNavInterface?.navigateToFragment(TeamsFragment(), true)
            }

            // admin panel
            findViewById<Button>(R.id.pinpointProgressBtn).setOnClickListener { navToPinpointProgress() }
            findViewById<Button>(R.id.teamsAdminBtn)
                .setOnClickListener { mPresenter?.navToTeamAdminTapped() }
            findViewById<Button>(R.id.pinpointAdminBtn).setOnClickListener { navToPinpointAdmin() }
            findViewById<Button>(R.id.userAdminBtn).setOnClickListener { navToUserAdmin() }
            findViewById<Button>(R.id.businessAdminBtn).setOnClickListener { navToBusinessAdmin() }
            findViewById<Button>(R.id.bulkExportButton).setOnClickListener { navToBulkExport() }
        }

        return v
    }

//    private fun navToManageAdmins() {
//        mNavInterface?.navigateToFragment(ManageBusinessAdminsFragment())
//    }

    private fun navToUserAdmin() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(UserAdminFragment())
    }

    private fun navToBusinessAdmin() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(BusinessAdminFragment())
    }

    private fun navToPinpointProgress() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(PinpointQueryFragment())
    }

    private fun navToPinpointAdmin() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(SupPinpointAdmin())
    }

    private fun navToBulkExport() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(BulkExportFragment())
    }

    override fun navToSupTeamAdmin() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(SupTeamsAdminFragment())
    }

    override fun navToBusAdminTeamAdmin() {
        // TODO: IMPLEMENT ME
//        mNavInterface?.navigateToFragment(TeamsListFragment())
    }

    override fun onStart() {
        super.onStart()
        mPresenter?.onStartCalled()
    }

    override fun onStop() {
        super.onStop()
        mPresenter?.onStopCalled()
    }

    override fun setBusinessActiveStatus(active: Boolean) {
        if(active) {
            view?.findViewById<CardView>(R.id.home_team_card)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<CardView>(R.id.home_team_card)?.visibility = View.GONE
            view?.findViewById<View>(R.id.home_pinpoint_layout)?.visibility = View.GONE
        }
    }

    override fun updateBusinessTextWidgets(business: FSBusiness) {
        view?.findViewById<TextView>(R.id.home_business_textview)?.text = business.businessName
    }

    override fun showBusinessLogo(b: Bitmap) {
        (view?.findViewById<ImageView>(R.id.home_business_logo))?.setImageBitmap(b)
    }

    override fun updateVendorTextWidgets(vendor: FSVendor) {
        view?.findViewById<TextView>(R.id.powered_by_view)?.text = String.format(getString(R.string.powered_by), vendor.name)
    }

    override fun showVendorLogo(b: Bitmap) {
        (view?.findViewById<ImageView>(R.id.home_vendor_logo))?.setImageBitmap(b)
    }

    override fun updateActiveTeam(team: RTeam) {
        view?.findViewById<TextView>(R.id.home_team_textview)?.text = team.teamName
    }

    override fun showActivePinpoints(pp: List<RPinpoint>) {
        val pinpointsView = view?.findViewById<LinearLayout>(R.id.home_pinpoint_layout)
        pinpointsView?.visibility = View.VISIBLE
        mPpAdapter?.newDataSet(ArrayList(pp))
    }

    override fun hideActivePinpoints() {
        view?.findViewById<LinearLayout>(R.id.home_pinpoint_layout)?.visibility = View.GONE
    }

    override fun showActiveTeamCard() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            view?.findViewById<CardView>(R.id.home_team_card)?.visibility = View.VISIBLE
        }
    }

    override fun hideActiveTeamCard() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            view?.findViewById<CardView>(R.id.home_team_card)?.visibility = View.GONE
        }
    }

    override fun onLeftSwipe(p: RPinpoint) {
        mPresenter?.onSwipedPinpoint(p, false)
    }

    override fun onRightSwipe(p: RPinpoint) {
        mPresenter?.onSwipedPinpoint(p, true)
    }

    override fun showProgressBar(show: Boolean) {
        when(show) {
            true -> (activity as? ProgressBarInterface)?.showProgressBar()
            false -> (activity as? ProgressBarInterface)?.hideProgressBar()
        }
    }

    override fun showAddNoteDialog(callback: (note: String?) -> Unit) {
        val dialog = AddPinpointNoteDialog().createDialog(requireActivity()) { note ->
            callback(note)
        }
        dialog.show()
    }

    /**
     * This method is a self-contained View function that will present the progress bar
     * for 700 ms, dismiss the progress bar, and show a snack to provide user with swipe feedback.
     */
    override fun showPinpointSwipeFeedback(success: Boolean) {
        showProgressBar(true)

        lifecycleScope.launch {
            delay(700)
            showProgressBar(false)

            if (isVisible) {
                when (success) {
                    true -> Snackbar.make(
                        requireView(),
                        getString(R.string.pinpoint_recorded),
                        Snackbar.LENGTH_SHORT
                    ).show()

                    false -> Snackbar.make(
                        requireView(),
                        getString(R.string.error_occurred),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun showNewMessageCount(count: Int) {
        val countView = view?.findViewById<TextView>(R.id.new_message_count)
        countView?.text = count.toString()
        countView?.visibility = if(count > 0) View.VISIBLE else View.GONE
    }

    override fun showNotificationCount(count: Int) {
        val countView = view?.findViewById<TextView>(R.id.new_notification_count)
        countView?.text = count.toString()
        countView?.visibility = if(count > 0) View.VISIBLE else View.GONE
    }

    private fun corkBoardMessageTapped() {
//        (activity as? NavInterface)?.navigateToFragment(CorkBoardListFragment())
    }

    private fun notificationTapped() {
        // TODO: IMPLEMENT ME
//        (activity as? NavInterface)?.navigateToFragment(NotificationsFragment())
    }

    override fun prepareHomeExperienceForRole(rankLevel: RankLevel) {
        when(rankLevel) {
            RankLevel.FrontlinePersonnel, RankLevel.CoreTeamLeader -> {
                showActiveTeamCard()
                showAdminCard(show = false)
            }
            RankLevel.Supervisor -> {
                hideActiveTeamCard()
                showAdminCard()
                showBulkAdminButton()
                showUserAdmin(show = false)
                showBusinessAdmin(show = false)
            }
            RankLevel.BusinessAdmin -> {
                hideActiveTeamCard()
                showAdminCard()
                showUserAdmin()
                showBusinessAdmin()
                showBulkAdminButton(show = false)
            }
            RankLevel.Owner -> {
                hideActiveTeamCard()
                showAdminCard()
                showUserAdmin()
                showBusinessAdmin()
                showBulkAdminButton(show = false)
            }
            else -> { /* Do Nothing */ }
        }
    }

    private fun showAdminCard(show: Boolean = true) {
        when(show) {
            true -> view?.findViewById<View>(R.id.home_admin_card)?.visibility = View.VISIBLE
            false -> view?.findViewById<View>(R.id.home_admin_card)?.visibility = View.GONE
        }
    }

    private fun showUserAdmin(show: Boolean = true) {
        when(show) {
            true -> view?.findViewById<Button>(R.id.userAdminBtn)?.visibility = View.VISIBLE
            false -> view?.findViewById<Button>(R.id.userAdminBtn)?.visibility = View.GONE
        }
    }

    private fun showBusinessAdmin(show: Boolean = true) {
        when(show) {
            true -> view?.findViewById<Button>(R.id.businessAdminBtn)?.visibility = View.VISIBLE
            false -> view?.findViewById<Button>(R.id.businessAdminBtn)?.visibility = View.GONE
        }
    }

    private fun showBulkAdminButton(show: Boolean = true) {
        when(show) {
            true -> view?.findViewById<Button>(R.id.bulkExportButton)?.visibility = View.VISIBLE
            false -> view?.findViewById<Button>(R.id.bulkExportButton)?.visibility = View.GONE
        }
    }
}