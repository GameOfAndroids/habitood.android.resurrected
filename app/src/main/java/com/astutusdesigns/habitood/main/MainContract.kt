package com.astutusdesigns.habitood.main

import android.content.Context
import androidx.fragment.app.Fragment
import com.astutusdesigns.habitood.MenuScreens
import com.astutusdesigns.habitood.datamodels.FSUser

/**
 * Main Activity MVP interfaces for MVP Design Pattern
 * Created by TMiller on 1/10/2018.
 */
interface MainContract {
    interface View {
        fun userIsLoggedOut()
        fun updateUserUIComponents(user: FSUser)
        fun setBusinessActiveStatus(active: Boolean)
    }
    interface Presenter {
        fun updatedUserProfileReceived(user: FSUser)
        fun userInitiatedLogout()
        fun getFragment(context: Context, screen: MenuScreens): Fragment
    }
    interface Model {
        fun startObservingUserProfile()
        fun stopObservingUserProfile()
        fun getOrCreateFragment(context: Context, screen: MenuScreens): Fragment
    }
}