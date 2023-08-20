package com.astutusdesigns.habitood.main

import android.content.Context
import androidx.fragment.app.Fragment
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.MenuScreens
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.astutusdesigns.habitood.datamodels.FSUser
import com.astutusdesigns.habitood.models.FSBusinessModel
import com.astutusdesigns.habitood.models.FSUserModel
import com.google.firebase.auth.FirebaseAuth

/**
 * This presenter will manage the MainActivity.
 * Created by TMiller on 1/10/2018.
 */
class MainPresenter(private val context: Context,
                    private val contractView: MainContract.View) : MainContract.Presenter, FSBusinessModel.BusinessCallback {

    private val contractModel = MainModel(this)
    private val mAuth = FirebaseAuth.getInstance()
    private val mUserModel = FSUserModel.instance
    private var mUser: FSUser? = null
    set(value) {
        field = value
        if(value != null)
            contractView.updateUserUIComponents(value)
    }

    init {
        mAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if(user == null)
                contractView.userIsLoggedOut()
            else
                contractModel.startObservingUserProfile()
        }
        FSBusinessModel.instance.registerBusinessObserver(this)
    }

    override fun getFragment(context: Context, screen: MenuScreens): Fragment {
        return contractModel.getOrCreateFragment(context, screen)
    }

    override fun userInitiatedLogout() {
        mAuth.signOut()
        contractView.userIsLoggedOut()
        contractModel.stopObservingUserProfile()
        HabitoodApp.instance.userIsLoggedOut()
    }

    override fun updatedUserProfileReceived(user: FSUser) {
        mUser = user
    }

    override fun businessUpdate(business: FSBusiness) {
        if(!business.isActive) {
            contractView.setBusinessActiveStatus(active = false)
        }
    }
}