package com.astutusdesigns.habitood

import androidx.fragment.app.Fragment


/**
 * Interface which will allow fragments to communicate with their hosting activity in regards to navigating to other fragments.
 * Created by TMiller on 1/10/2018.
 */
interface NavInterface {
    fun navigateToFragment(newFragment: Fragment, addFragmentToBackStack: Boolean = true)
    // fun navigateTo(screen: MenuScreens, instantiatedFragment: Fragment? = null, addFragmentToBackStack: Boolean = true)
}