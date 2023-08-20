package com.astutusdesigns.habitood.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.astutusdesigns.habitood.NavInterface
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.splash.SplashScreenActivity
import com.google.android.material.snackbar.Snackbar


/**
 * This fragment will handle the login process.
 */
class LoginFragment : Fragment(), LoginMvp.View {

    companion object {
        private const val EMAIL_EXTRA = "EmailExtra"

        fun newInstance(email: String?): Fragment {
            val fragment = LoginFragment()
            val args = Bundle()

            if (email != null) {
                args.putString(EMAIL_EXTRA, email)
                fragment.arguments = args
            }

            return fragment
        }
    }

    private var mFragmentNavListener: NavInterface? = null
    private val mLoginPresenter: LoginMvp.Presenter = LoginPresenter(this)
    private lateinit var mEmailText: EditText
    private lateinit var mPasswordText: EditText
    private lateinit var mLoginButton: Button
    private lateinit var mSignupLink: TextView
    private lateinit var mForgotPasswordLink: TextView

    override fun onStart() {
        super.onStart()
        mFragmentNavListener = activity as? NavInterface
    }

    override fun onStop() {
        super.onStop()
        mFragmentNavListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)!!

        mEmailText = view.findViewById<View>(R.id.input_email) as EditText
        mPasswordText = view.findViewById<View>(R.id.input_password) as EditText
        mSignupLink = view.findViewById<View>(R.id.link_signup) as TextView
        mForgotPasswordLink = view.findViewById<View>(R.id.link_forgot_password) as TextView
        mLoginButton = view.findViewById<View>(R.id.btn_login) as Button

        if (arguments != null) {
            val email = arguments?.getString(EMAIL_EXTRA)
            if (!email.isNullOrEmpty()) mEmailText.setText(email)
        }

        mLoginButton.setOnClickListener{ login() }
        mSignupLink.setOnClickListener{ mFragmentNavListener?.navigateToFragment(CreateAcctFragment.newInstance(), false) }
        mForgotPasswordLink.setOnClickListener { forgotPasswordTapped() }

        return view
    }

    /**
     * This method will make a call to the presenter in an attempt to resend a forgotten password.
     */
    private fun forgotPasswordTapped() {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailText.text.toString()).matches())
            mEmailText.error = (getString(R.string.invalid_email))
        else {
            (activity as AuthenticationActivity).toggleProgressBar(true)
            mEmailText.error = null
            mLoginPresenter.forgotPasswordTapped(mEmailText.text.toString())
        }
    }

    /**
     * This method will be called by the presenter (once operation completes) after the user tapped the forgot email button was tapped.
     */
    override fun emailWasSent() {
        (activity as AuthenticationActivity).toggleProgressBar(false)
        if(view != null) Snackbar.make(requireView(), getString(R.string.recovery_email_sent), Snackbar.LENGTH_LONG).show()
    }

    /**
     * This method will be called by the presenter if the user tapped the send forgot email button was tapped and the
     * email is not found in the system.
     */
    override fun emailWasIncorrect() {
        (activity as AuthenticationActivity).toggleProgressBar(false)
        if(view != null) Snackbar.make(requireView(), getString(R.string.email_not_registered), Snackbar.LENGTH_LONG).show()
        mLoginButton.isEnabled = true
    }

    override fun noInternetConnectivity() {
        (activity as AuthenticationActivity).toggleProgressBar(false)
        Snackbar.make(requireView(), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
        mLoginButton.isEnabled = true
    }

    /**
     * The method will be called when the User clicks the login button. This method will handle the login process.
     */
    private fun login() {
        if (!validate())
            return

        if(!isAdded)
            return

        mLoginButton.isEnabled = false
        (activity as AuthenticationActivity).toggleProgressBar(true)
        mLoginPresenter.signIn(requireActivity(), mEmailText.text.toString(), mPasswordText.text.toString())
    }

    /**
     * The presenter will call this method when the login resultEnum has been received.
     */
    override fun loginResult(resultEnum: LoginResultEnum) {
        (activity as AuthenticationActivity).toggleProgressBar(false)
        when(resultEnum) {
            LoginResultEnum.InvalidEmail -> {
                resetForm()
                Snackbar.make(requireView(), getString(R.string.incorrect_email), Snackbar.LENGTH_LONG).show()
            }
            LoginResultEnum.InvalidPassword -> {
                resetForm()
                Snackbar.make(requireView(), getString(R.string.incorrect_password), Snackbar.LENGTH_LONG).show()
            }
            LoginResultEnum.InvalidRole -> {
                // this is no longer used. All roles are available on Android.
                // if(view != null) Snackbar.make(view!!, getString(R.string.invalid_role), Snackbar.LENGTH_LONG).show()
//                resetForm()
//                FirebaseAuth.getInstance().signOut()
//                if(activity != null)
//                    Toast.makeText(activity, getString(R.string.invalid_role), Toast.LENGTH_LONG).show()
            }
            LoginResultEnum.Success -> {
                if(!isAdded)
                    return

                (activity as AuthenticationActivity).toggleProgressBar(false)
                val intent = SplashScreenActivity.newInstance(requireActivity())
                requireActivity().startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            LoginResultEnum.Other -> {
                Snackbar.make(requireView(), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun resetForm() {
        mPasswordText.setText("")
        mLoginButton.isEnabled = true
    }

    /**
     * Method used to determine if the inputs provided by the User are valid.
     * @return true if inputs are valid and acceptable. false if they are not.
     */
    private fun validate(): Boolean {
        var valid = true

        val email = mEmailText.text.toString()
        val password = mPasswordText.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailText.error = getString(R.string.invalid_email)
            valid = false
        } else
            mEmailText.error = null

        if (password.isEmpty() || password.length < 6 || password.length > 20) {
            mPasswordText.error = getString(R.string.incorrect_password)
            valid = false
        } else
            mPasswordText.error = null

        return valid
    }

    /**
     * This will be called by the presenter in the even the user has not verified their email account.
     */
    override fun displayResendConfirmEmailSnack() {
        (activity as AuthenticationActivity).toggleProgressBar(true)
        if (isAdded) {
            val snack = Snackbar.make(requireView(), getString(R.string.resend_verification_email), Snackbar.LENGTH_INDEFINITE)
            snack.setAction(R.string.resend) { mLoginPresenter.resendEmailVerification() }
            snack.show()
        }
    }
}