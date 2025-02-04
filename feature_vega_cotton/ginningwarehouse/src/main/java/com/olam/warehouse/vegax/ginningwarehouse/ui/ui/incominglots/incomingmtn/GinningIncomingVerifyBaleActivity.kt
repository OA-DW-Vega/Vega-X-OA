package com.olam.warehouse.ginning.ui.incominglots.incomingmtn

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.ginning.utils.*
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnGrades
import com.olam.warehouse.presentation.extensions.removeSpecialCharacters
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginning_incoming_verify_bale.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class GinningIncomingVerifyBaleActivity : HomeBaseActivity() {

    private val vm: GinningIncomingClassficationViewModel by viewModel { emptyParametersHolder() }
    private var mtn: Mtn? = null
    private var grades: ArrayList<MtnGrades>? = null
    private var mVerifiedBaleAdapter =
        GinningIncomingVerifyBaleAdapter({ showDeleteDialog(it) }, { moveEdit(it) })


    override val layoutResourceId: Int = R.layout.activity_ginning_incoming_verify_bale
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /// injectGinningIncomingLOTsIncomingMtnFeature()
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incomingmtn/GinningIncomingVerifyBaleActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    override fun onResume() {
        super.onResume()
        clearEditTextFocus()
    }

    private fun initExtras() {
        mtn = intent.getParcelableExtra(MTN)
        grades = intent.getParcelableArrayListExtra(GRADES)
    }

    private fun initUI() {

        rvVerifiedBale.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvVerifiedBale.setHasFixedSize(true)

        tvWeight.text = mtn?.sourceNetWeight.plus(" ").plus(mtn?.uom)
        val grades = grades?.map { it.grade }
        tvGrades.text = grades.toString().replace("[", "").replace("]", "")

        rvVerifiedBale.adapter = mVerifiedBaleAdapter

        btnVerifyBaleProceed.setOnClickListener {
            moveToBaleReview()
        }

        llScanBale.setOnClickListener {
            startActivityForResult(
                Intent(this, ScannerActivity::class.java), ACTIVITY_REQUEST_CODE
            )
        }

        btnAddBale.setOnClickListener {
            val baleId = etBaleID.text.toString().trim()
            validateBaleId(baleId)
        }
        etBaleID.onChange { changeButttonColor(it) }

        tvMtnNumber.text = mtn?.mtnNumber
        vm.getVerifiedBales.observe(this, Observer {
            if (intent.getIntExtra("ViewStatus", 0) == 1) {
                updateUI(it)
            } else {
                offlineUpdateUI(it)
            }
        })
        mtn?.let { mtn ->
            vm.getVerifiedBales(mtn.mtnNumber)
        }
    }

    private fun validateBaleId(baleId: String) {
        val isValid = vm.validateBaleId(baleId)

        vm.getBale.observe(this, Observer {bale ->
            when {
                isValid && bale == null -> {
                    toast(getString(R.string.bale_already_verified))
                    etBaleID.text.clear()
                }
                isValid && bale != null -> moveToBaleStatus(bale)
                else -> toast(getString(R.string.error_bale_id))
            }
        })
        mtn?.let {
             vm.getBale(it.mtnNumber, baleId, it.lineItem)
        }
    }

    private fun moveToBaleReview() {
        val intent = Intent(this, GinningIncomingReviewBaleActivity::class.java)
        intent.putExtra(MTN, mtn)
        intent.putParcelableArrayListExtra(GRADES, grades as ArrayList<MtnGrades>)
        startActivity(intent)
    }


    private fun moveToBaleStatus(bale: MtnBales?) {
        val intent = Intent(this, GinningIncomingBaleStatusActivity::class.java)
        intent.putExtra(BALE, bale)
        intent.putExtra(PLANT, mtn?.suplierPlantDesc)
        startActivity(intent)
    }

    private fun clearEditTextFocus() {
        etBaleID.text.clear()
        etBaleID.clearFocus()
    }

    private fun moveEdit(bale: MtnBales) {
        val intent = Intent(this, GinningIncomingBaleStatusActivity::class.java)
        intent.putExtra(BALE, bale)
        intent.putExtra(PLANT, mtn?.suplierPlantDesc)
        startActivity(intent)
    }


    private fun showDeleteDialog(mtnBale: MtnBales) {

        showDialog(getString(R.string.dialog_confirm_bale_delete),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    vm.deleteBale(mtnBale, mVerifiedBaleAdapter.itemCount)
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            } )

        /*MaterialDialog(this).show {
            cancelable(false)
            title(R.string.dialogTitle)
            positiveButton(R.string.yes) { deleteBaleFromContainer(bale) }
            negativeButton(R.string.cancel) { dismiss() }
            message(R.string.dialogDeleteTitle)
        }*/
    }

    private fun updateUI(mtnBales: List<MtnBales>) {
        if (mtnBales.isNotEmpty()) {
            enableButton()
            tvNoBaleData.visibility = View.GONE
            rvVerifiedBale.visibility = View.VISIBLE
        } else {
            disableButton()
            tvNoBaleData.visibility = View.VISIBLE
            rvVerifiedBale.visibility = View.GONE
        }

        val baleCount = mtn?.baleCount
        mVerifiedBaleAdapter.updateData(mtnBales)
        tvBaleReceived.text = baleCount
        tvBaleVerified.text = mVerifiedBaleAdapter.itemCount.toString()

        baleCount?.let {
            tvBaleYetToVerify.text = (it.toInt() - mVerifiedBaleAdapter.itemCount).toString()
        }

    }


    private fun offlineUpdateUI(mtnBales: List<MtnBales>) {

        if (mtnBales.isNotEmpty()) {
            enableButton()
            tvNoBaleData.visibility = View.GONE
            rvVerifiedBale.visibility = View.VISIBLE
        } else {
            disableButton()
            tvNoBaleData.visibility = View.VISIBLE
            rvVerifiedBale.visibility = View.GONE
        }
      vm.getMtnWithBalesOffline.observe(this, Observer {
          vm.setMtnWithBales(it)
          vm.getTotalCount()?.let {
              tvBaleReceived.text = it
          }
          vm.getVerifiedCount()?.let {
              tvBaleVerified.text = it
          }

          vm.getYetToVerifyCount()?.let {
              tvBaleYetToVerify.text = it
          }
      })
        mtn!!.mtnNumber.let { mtnNumber ->

            vm.getMtnWithBales(mtnNumber)
        }
        mVerifiedBaleAdapter.updateData(mtnBales)
    }

    private fun enableButton() {
        ViewCompat.setBackgroundTintList(
            btnVerifyBaleProceed,
            ContextCompat.getColorStateList(this, R.color.green)
        )
        btnVerifyBaleProceed.isEnabled = true
    }

    private fun disableButton() {
        ViewCompat.setBackgroundTintList(
            btnVerifyBaleProceed,
            ContextCompat.getColorStateList(this, R.color.light_grey)
        )
        btnVerifyBaleProceed.isEnabled = false
    }

    private fun changeButttonColor(value: String) {
        when (value.length) {
            10 -> ViewCompat.setBackgroundTintList(
                btnAddBale,
                ContextCompat.getColorStateList(this, R.color.green)
            )
            else -> ViewCompat.setBackgroundTintList(
                btnAddBale,
                ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.let {
                    val id: String? = it.getStringExtra(Constants.SCANNED_ID)
                    id?.let {
                        validateBaleId(it.trim().removeSpecialCharacters().toUpperCase())
                    }
                }
            }
        }
    }


    override fun onBackPressed() {
        navigateOnBackPress()
    }

    private fun navigateOnBackPress() {
        if (mVerifiedBaleAdapter.itemCount > 0) {
            showDialog(resources.getString(R.string.update_verified_bales),
                object : DialogClick {
                    override fun onPositive(dialog: DialogInterface) {
                        moveToBaleReview()
                    }

                    override fun onNegative(dialog: DialogInterface) {
                        dialog.dismiss()
                    }
                })
        } else {
            /*val intent = Intent(this, GinningHomeActivity::class.java)
            startActivity(intent)*/
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    navigateOnBackPress()
                }
            }
        }
        return false
    }
}
