package com.astutusdesigns.habitood.authentication


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.astutusdesigns.habitood.NavInterface
import com.astutusdesigns.habitood.R

class AuthenticationActivity : AppCompatActivity(), NavInterface {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, AuthenticationActivity::class.java)
        }
    }

    private lateinit var mProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        mProgressBar = findViewById(R.id.progressBar)

        /*
        val view = findViewById<ImageView> (R.id.safe_by_choice_logo_view)
        val drawableLogo = ContextCompat.getDrawable(this, R.drawable.safe_by_choice_logo)
        val logoBitmap = (drawableLogo as BitmapDrawable).bitmap
        view.setImageDrawable(ImagesModel.getRoundedBitmap(this, logoBitmap))
        */

        val loginFragment = LoginFragment.newInstance(null)

        val fm = supportFragmentManager
        fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .add(R.id.authentication_fragment_container, loginFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
    }

    fun toggleProgressBar(show: Boolean) {
        mProgressBar.visibility = if(show) View.VISIBLE else View.INVISIBLE
    }

    override fun navigateToFragment(newFragment: Fragment, addFragmentToBackStack: Boolean) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()

        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        transaction.replace(R.id.authentication_fragment_container, newFragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        if (addFragmentToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }
}