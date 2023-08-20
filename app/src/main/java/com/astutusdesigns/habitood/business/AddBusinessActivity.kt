package com.astutusdesigns.habitood.business

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.astutusdesigns.habitood.HabitoodApp
import com.astutusdesigns.habitood.R
import com.astutusdesigns.habitood.authentication.AuthenticationActivity
import com.astutusdesigns.habitood.datamodels.FSBusiness
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class AddBusinessActivity : AppCompatActivity(), AddBusinessContract.View {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, AddBusinessActivity::class.java)
        }
    }

    private lateinit var mLogoutText: TextView
    private lateinit var mFiveDigit: EditText
    private lateinit var mThreeChars: EditText
    private lateinit var mThreeDigit: EditText
    private lateinit var mSubmitButton: Button
    private lateinit var mProgressBar: ProgressBar
    private val mPresenter: AddBusinessContract.Presenter = AddBusinessPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_business)

        // hide status bar.
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions

        mLogoutText = findViewById(R.id.logout_text)
        mFiveDigit = findViewById(R.id.five_digit)
        mThreeChars = findViewById(R.id.two_digit_with_char)
        mThreeDigit = findViewById(R.id.three_digit)
        mSubmitButton = findViewById(R.id.submit_business_id_btn)
        mProgressBar = findViewById(R.id.progressBar)

        mSubmitButton.setOnClickListener { onSubmitTapped() }
        mLogoutText.setOnClickListener { onLogoutTapped() }
    }

    private fun showProgressBar() { mProgressBar.visibility = View.VISIBLE }
    private fun hideProgressBar() { mProgressBar.visibility = View.INVISIBLE }

    private fun onSubmitTapped() {
        showProgressBar()
        mPresenter.onSubmitBusinessId(key1=mFiveDigit.text.toString(), key2= mThreeChars.text.toString(), key3=mThreeDigit.text.toString())
    }

    private fun onLogoutTapped() {
        FirebaseAuth.getInstance().signOut()
        HabitoodApp.instance.userIsLoggedOut()
        val intent = Intent(AuthenticationActivity.newInstance(this))
        startActivity(intent)
    }

    override fun businessSuccessfullyDownloaded(business: FSBusiness?) {
        hideProgressBar()
        if(business == null) {
            Snackbar.make(findViewById<ConstraintLayout>(R.id.parentView), getString(R.string.no_biz_found), Snackbar.LENGTH_LONG).show()
        } else {
            if (business.isManualAddEnabled)
                displayConfirmationSheet(business)
            else {
                val b = AlertDialog.Builder(this)
                b.setTitle(getString(R.string.add_business_error))
                b.setMessage(getString(R.string.add_business_error_msg))
                b.setPositiveButton(android.R.string.ok) { dialogInterface, _ -> dialogInterface.dismiss() }
                b.show()
            }
        }
    }

    override fun businessDownloadFailed() {
        hideProgressBar()
        Snackbar.make(findViewById<ConstraintLayout>(R.id.parentView), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }

    private fun displayConfirmationSheet(business: FSBusiness?) {
        val dialog = BottomSheetDialog(this)
        val sheet = layoutInflater.inflate(R.layout.layout_confirm_business_sheet, null)
        dialog.setContentView(sheet)

        dialog.findViewById<TextView>(R.id.sheetCompanyNameText)?.text = business?.businessName
        dialog.findViewById<Button>(R.id.confirmButton)?.setOnClickListener {
            showProgressBar()
            dialog.dismiss()
            mPresenter.userConfirmedBusiness(FirebaseAuth.getInstance().currentUser!!.uid)
        }
        dialog.findViewById<Button>(R.id.cancelButton)?.setOnClickListener{ dialog.dismiss() }
        dialog.show()
    }

    override fun userAddedToBusiness() {
        hideProgressBar()
        Toast.makeText(this, getString(R.string.success_reauth), Toast.LENGTH_LONG).show()
        onLogoutTapped()
//        val intent = MainActivity.newInstance(this)
//        startActivity(intent)
//        finish()
    }

    override fun userAddedToBusinessFailed() {
        hideProgressBar()
        Snackbar.make(findViewById<ConstraintLayout>(R.id.parentView), getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }
}