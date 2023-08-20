package com.astutusdesigns.habitood.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.astutusdesigns.habitood.MenuScreens
import com.astutusdesigns.habitood.NavInterface
import com.astutusdesigns.habitood.ProgressBarInterface
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.RankLevel
import com.astutusdesigns.habitood.Utilities
import com.astutusdesigns.habitood.authentication.AuthenticationActivity
import com.astutusdesigns.habitood.datamodels.FSUser
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavInterface, MainContract.View, ProgressBarInterface {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    private val mPresenter = MainPresenter(this,this)
    private lateinit var mNavigationView: NavigationView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mProgressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mNavigationView = findViewById(R.id.nav_view)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mProgressBar = findViewById(R.id.progressBar)

        val toggle = ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mNavigationView.setNavigationItemSelectedListener(this)
        val fragment = mPresenter.getFragment(this, MenuScreens.Home)
        navigateToFragment(fragment, true)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START)
        else {
            if(supportFragmentManager.backStackEntryCount > 0)
                supportFragmentManager.popBackStack()
            else
                super.onBackPressed()

            hideProgressBar()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var f: Fragment? = null
        when (item.itemId) {
            R.id.nav_home ->              f = mPresenter.getFragment(this, MenuScreens.Home)
            R.id.nav_teams ->             f = mPresenter.getFragment(this, MenuScreens.Teams)
            R.id.nav_pinpoints ->         f = mPresenter.getFragment(this, MenuScreens.Pinpoints)
            R.id.nav_notifications ->     f = mPresenter.getFragment(this, MenuScreens.Notifications)
            R.id.nav_cork_board ->        f = mPresenter.getFragment(this, MenuScreens.CorkBoard)
            R.id.menu_settings ->         f = mPresenter.getFragment(this, MenuScreens.Settings)
            R.id.nav_pinpoint_progress -> f = mPresenter.getFragment(this, MenuScreens.PinpointProgress)
            R.id.nav_teams_admin ->       f = mPresenter.getFragment(this, MenuScreens.TeamsAdmin)
            R.id.nav_pinpoint_admin ->    f = mPresenter.getFragment(this, MenuScreens.PinpointAdmin)
            R.id.nav_bulk_export ->       f = mPresenter.getFragment(this, MenuScreens.BulkExport)
            R.id.menu_logout -> {
                lifecycleScope.launch {
                    delay(600)
                    mPresenter.userInitiatedLogout()
                }
            }
            else -> f = mPresenter.getFragment(this, MenuScreens.Home)
        }

        if(f != null) {
            lifecycleScope.launch {
                delay(600)
                navigateToFragment(f, true)
            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun updateUserUIComponents(user: FSUser) {
        loadNavigationHeader(user)
        reloadNavItems(user)
    }

    override fun setBusinessActiveStatus(active: Boolean) {
        if(!active) {
            with(mNavigationView.menu) {
                findItem(R.id.nav_teams_admin).isVisible = false
                findItem(R.id.nav_pinpoint_admin).isVisible = false
                findItem(R.id.nav_pinpoints).isVisible = false
                findItem(R.id.nav_pinpoint_progress).isVisible = false
            }
        }
    }

    private fun loadNavigationHeader(user: FSUser) {
        mNavigationView.getHeaderView(0)?.let {
            it.findViewById<TextView>(R.id.nav_user_name)?.text =
                String.format(getString(R.string.name_concat), user.fname, user.lname)

            it.findViewById<TextView>(R.id.nav_rank)?.text =
                FSUser.RankLevelDescription[RankLevel.fromInt(user.rankLevel)]
        }
        /*val navProfileImageView = header?.findViewById<ImageView>(R.id.nav_profile_image)

        val userProfileImage = ControllerImages.getRoundedBitmap(this, ControllerImages.getImageFromDisk(this, ControllerImages.CURRENT_USER_PHOTO_REQ))
        if (userProfileImage != null) {
            navProfileImageView.setImageDrawable(userProfileImage)
        } else {
            navProfileImageView.setImageResource(R.mipmap.ic_account_circle_white_48dp)
        }*/
    }

    private fun reloadNavItems(user: FSUser) {
        when(RankLevel.fromInt(user.rankLevel)) {
            RankLevel.FrontlinePersonnel -> {
                with(mNavigationView.menu) {
                    findItem(R.id.nav_teams).isVisible = true
                    findItem(R.id.nav_pinpoints).isVisible = true
                    findItem(R.id.nav_teams_admin).isVisible = false
                    findItem(R.id.nav_pinpoint_admin).isVisible = false
                    findItem(R.id.nav_bulk_export).isVisible = false
                }
            }
            RankLevel.CoreTeamLeader -> {
                with(mNavigationView.menu) {
                    findItem(R.id.nav_teams).isVisible = true
                    findItem(R.id.nav_pinpoints).isVisible = true
                    findItem(R.id.nav_admin_panel).isVisible = true
                    if(user.isActive) {
                        findItem(R.id.nav_teams_admin).isVisible = true
                        findItem(R.id.nav_pinpoint_admin).isVisible = true
                    } else {
                        findItem(R.id.nav_teams_admin).isVisible = false
                        findItem(R.id.nav_pinpoint_admin).isVisible = false
                    }
                    findItem(R.id.nav_pinpoint_progress).isVisible = true
                    findItem(R.id.nav_bulk_export).isVisible = false
                }
            }
            RankLevel.Supervisor -> {
                with(mNavigationView.menu) {
                    findItem(R.id.nav_teams).isVisible = false
                    findItem(R.id.nav_pinpoints).isVisible = false
                    findItem(R.id.nav_pinpoint_progress).isVisible = true
                    findItem(R.id.nav_admin_panel).isVisible = true
                    findItem(R.id.nav_teams_admin).isVisible = true
                    findItem(R.id.nav_pinpoint_admin).isVisible = true
                    findItem(R.id.nav_bulk_export).isVisible = true
                }
            }
            RankLevel.BusinessAdmin, RankLevel.Owner -> {
                with(mNavigationView.menu) {
                    findItem(R.id.nav_teams).isVisible = false
                    findItem(R.id.nav_pinpoints).isVisible = false
                    findItem(R.id.nav_teams_admin).isVisible = false
                    findItem(R.id.nav_pinpoint_admin).isVisible = false
                    findItem(R.id.nav_bulk_export).isVisible = false
                }
            }
        }
    }

    override fun userIsLoggedOut() {
        // set intent flags to prevent User from being able to press "back" and come back into this activity after logout.
        val intent = AuthenticationActivity.newInstance(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun navigateToFragment(newFragment: Fragment, addFragmentToBackStack: Boolean) {
        val currentFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            ft.add(R.id.fragment_container, newFragment)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ft.commit()
        } else if (currentFragment.toString() != newFragment.toString()) {
            val ft = supportFragmentManager.beginTransaction()
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            ft.replace(R.id.fragment_container, newFragment)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addFragmentToBackStack) {
                ft.addToBackStack(null)
            }

            ft.commit()
        }
    }

    override fun showProgressBar() {
        mProgressBar.visibility = View.VISIBLE
        Utilities.disableTouch(this)
    }
    override fun hideProgressBar() {
        mProgressBar.visibility = View.INVISIBLE
        Utilities.enableTouch(this)
    }
}