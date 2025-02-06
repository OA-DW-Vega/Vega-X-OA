package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

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
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningIncomingVerifyBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningIncomingVerifyBaleActivity : HomeBaseActivity() {

    var isBaleValid=false
    private val vm: GinningIncomingClassficationViewModel by viewModel { emptyParametersHolder() }
    private var mtn: Mtn? = null
    private var grades: ArrayList<MtnGrades>? = null
    private var mVerifiedBaleAdapter =
        GinningIncomingVerifyBaleAdapter({ showDeleteDialog(it) }, { moveEdit(it) })


    override val layoutResourceId: Int = R.layout.activity_ginning_incoming_verify_bale
    private lateinit var binding: ActivityGinningIncomingVerifyBaleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningIncomingVerifyBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        binding.rvVerifiedBale.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvVerifiedBale.setHasFixedSize(true)

        binding.tvWeight.text = mtn?.sourceNetWeight.plus(" ").plus(mtn?.uom)
        val grades = grades?.map { it.grade }
        binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")

        binding.rvVerifiedBale.adapter = mVerifiedBaleAdapter

        binding.btnVerifyBaleProceed.setOnClickListener {
            moveToBaleReview()
        }

        binding.llScanBale.setOnClickListener {
            startActivityForResult(
                Intent(this, ScannerActivity::class.java), ACTIVITY_REQUEST_CODE
            )
        }

        binding.btnAddBale.setOnClickListener {
            val baleId = binding.etBaleID.text.toString().trim()
            validateBaleId(baleId)
        }
        binding.etBaleID.onChange { changeButttonColor(it) }

        binding.tvMtnNumber.text = mtn?.mtnNumber
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
        vm.getBale.observe(this, Observer { bale ->
            when {
                isBaleValid && bale == null -> {
                    toast(getString(R.string.bale_already_verified))
                    binding.etBaleID.text.clear()
                }
                isBaleValid && bale != null -> moveToBaleStatus(bale)
                else -> toast(getString(R.string.error_bale_id))
            }
        })


        vm.getMtnWithBalesOffline.observe(this, Observer {
            vm.setMtnWithBales(it)
            vm.getTotalCount()?.let {
                binding.tvBaleReceived.text = it
            }
            vm.getVerifiedCount()?.let {
                binding.tvBaleVerified.text = it
            }

            vm.getYetToVerifyCount()?.let {
                binding.tvBaleYetToVerify.text = it
            }
        })

    }

    private fun validateBaleId(baleId: String) {
        isBaleValid = vm.validateBaleId(baleId)

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
        binding.etBaleID.text.clear()
        binding.etBaleID.clearFocus()
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
            binding.tvNoBaleData.visibility = View.GONE
            binding.rvVerifiedBale.visibility = View.VISIBLE
        } else {
            disableButton()
            binding.tvNoBaleData.visibility = View.VISIBLE
            binding.rvVerifiedBale.visibility = View.GONE
        }

        val baleCount = mtn?.baleCount
        mVerifiedBaleAdapter.updateData(mtnBales)
        binding.tvBaleReceived.text = baleCount
        binding.tvBaleVerified.text = mVerifiedBaleAdapter.itemCount.toString()

        baleCount?.let {
            binding.tvBaleYetToVerify.text =
                (it.toInt() - mVerifiedBaleAdapter.itemCount).toString()
        }

    }


    private fun offlineUpdateUI(mtnBales: List<MtnBales>) {

        if (mtnBales.isNotEmpty()) {
            enableButton()
            binding.tvNoBaleData.visibility = View.GONE
            binding.rvVerifiedBale.visibility = View.VISIBLE
        } else {
            disableButton()
            binding.tvNoBaleData.visibility = View.VISIBLE
            binding.rvVerifiedBale.visibility = View.GONE
        }
        mtn!!.mtnNumber.let { mtnNumber ->

            vm.getMtnWithBales(mtnNumber)
        }
        mVerifiedBaleAdapter.updateData(mtnBales)
    }

    private fun enableButton() {
        ViewCompat.setBackgroundTintList(
            binding.btnVerifyBaleProceed,
            ContextCompat.getColorStateList(
                this,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        binding.btnVerifyBaleProceed.isEnabled = true
    }

    private fun disableButton() {
        ViewCompat.setBackgroundTintList(
            binding.btnVerifyBaleProceed,
            ContextCompat.getColorStateList(
                this,
                com.olam.warehouse.presentation.R.color.colorSecondaryGrey
            )
        )
        binding.btnVerifyBaleProceed.isEnabled = false
    }

    private fun changeButttonColor(value: String) {
        when (value.length) {
            10 -> ViewCompat.setBackgroundTintList(
                binding.btnAddBale,
                ContextCompat.getColorStateList(
                    this,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            else -> ViewCompat.setBackgroundTintList(
                binding.btnAddBale,
                ContextCompat.getColorStateList(
                    this,
                    com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                )
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    navigateOnBackPress()
                }
            }
        }
        return false
    }
}
