package com.astutusdesigns.habitood.authentication


import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.astutusdesigns.habitood.NavInterface
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.splash.SplashScreenActivity


/**
 * A simple [Fragment] subclass.
 */
class CreateAcctFragment : Fragment(), AuthMvp.View {

    companion object {
        fun newInstance(): Fragment {
            return CreateAcctFragment()
        }
    }

    private val TAG = "CreateAcctFragment"
    private var mNavInterface: NavInterface? = null
    private lateinit var mFirstName: EditText
    private lateinit var mLastName: EditText
    private lateinit var mEmail: EditText
    private lateinit var mPassword: EditText
    private lateinit var mReenterPassword: EditText
    private lateinit var mSignupButton: Button
    private lateinit var mLoginLink: TextView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mAuthPresenter: AuthMvp.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuthPresenter = AuthPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_acct, container, false)?.apply {
            mFirstName = findViewById(R.id.input_first_name)
            mLastName = findViewById(R.id.input_last_name)
            mLoginLink = findViewById(R.id.link_login)
            mEmail = findViewById(R.id.input_email)
            mPassword = findViewById(R.id.input_password)
            mReenterPassword = findViewById(R.id.input_reEnterPassword)
            mSignupButton = findViewById(R.id.btn_signup)

            mSignupButton.setOnClickListener { register() }

            mLoginLink.setOnClickListener { mNavInterface?.navigateToFragment(LoginFragment.newInstance(null), false) }

            mPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    if (editable.length < 8 || !editable.toString().matches(".*\\d.*".toRegex()))
                        mPassword.error = getString(R.string.password_not_strong)
                    else
                        mPassword.error = null

                    if (mReenterPassword.text.toString() == editable.toString())
                        mReenterPassword.error = null
                    else
                        mReenterPassword.error = getString(R.string.nonmatching_passwords)
                }
            })

            mReenterPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                override fun afterTextChanged(editable: Editable) {
                    if (mPassword.text.toString() == editable.toString())
                        mReenterPassword.error = null
                    else
                        mReenterPassword.error = getString(R.string.nonmatching_passwords)

                }
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if (activity !is NavInterface)
            throw RuntimeException("Activities which host CreateAcctFragment must implement FragmentNavigationListener.")
        else
            mNavInterface = activity as NavInterface
    }

    override fun onStop() {
        super.onStop()
        mNavInterface = null
    }

    private fun register() {
        Log.d(TAG, "Register")

        if (!validate())
            return

        (activity as AuthenticationActivity).toggleProgressBar(true)
        mSignupButton.isEnabled = false

        mAuthPresenter.register(
                mFirstName.text.toString(),
                mLastName.text.toString(),
                mEmail.text.toString(),
                mPassword.text.toString()
        )
    }

    override fun userRegistrationFailed(failureCode: AcctCreationResultEnum) {
        when (failureCode) {
            AcctCreationResultEnum.WeakPassword -> mPassword.error = getString(R.string.password_not_strong)
            AcctCreationResultEnum.EmailMalformed -> mEmail.error = getString(R.string.invalid_email)
            AcctCreationResultEnum.EmailExists -> mEmail.error = getString(R.string.email_exists)
        }
        mSignupButton.isEnabled = true
        (activity as? AuthenticationActivity)?.toggleProgressBar(false)
    }

    override fun userRegistrationDidSucceed() {
        (activity as? AuthenticationActivity)?.toggleProgressBar(false)

        // get rid of the previous login screen. Otherwise the one in the stack will cause multiple calls to start splashscreen.
        for (i in 0 until (fragmentManager?.backStackEntryCount ?: 0)) {
            fragmentManager?.popBackStack()
        }

        // navigate to splash.
        val intent = SplashScreenActivity.newInstance(activity as Context)
        startActivity(intent)
    }

    /**
     * This method handles all the checking of input to determine if all input is correctly entered.
     * @return true if and only if all data input passes the required specifications. Otherwise false is returned.
     */
    private fun validate(): Boolean {
        var valid = true

        val fname = mFirstName.text.toString()
        val lname = mLastName.text.toString()
        val email = mEmail.text.toString()

        if (fname.isEmpty()) {
            mFirstName.error = getString(R.string.empty_name)
            valid = false
        } else
            mFirstName.error = null

        if (lname.isEmpty())
            mLastName.error = getString(R.string.empty_name)
        else
            mLastName.error = null

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.error = getString(R.string.invalid_email)
            valid = false
        } else
            mEmail.error = null

        return valid
    }
}