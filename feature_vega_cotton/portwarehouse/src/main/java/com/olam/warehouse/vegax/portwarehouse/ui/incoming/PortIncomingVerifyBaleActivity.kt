package com.olam.warehouse.vegax.portwarehouse.ui.incoming

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
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnBales
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtnGrades
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.extensions.removeSpecialCharacters
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityIncomingVerifyBaleBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.BALE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.GRADES
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.MTN
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.PLANT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 4/5/2021.
 */
class PortIncomingVerifyBaleActivity : HomeBaseActivity() {

    private val vm: PortIncomingClassficationViewModel by viewModel { emptyParametersHolder() }
    private var mtn: PortMtn? = null
    private var grades: ArrayList<PortMtnGrades>? = null
    var found = false
    private var mtnverifiedbale: List<PortMtnBales> = emptyList()
    private var mVerifiedBaleAdapter =
        PortIncomingVerifyBaleAdapter({ showDeleteDialog(it) }, { moveEdit(it) })

    private lateinit var binding: ActivityIncomingVerifyBaleBinding
    override val layoutResourceId: Int = R.layout.activity_incoming_verify_bale
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomingVerifyBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/incoming/PortIncomingVerifyBaleActivity")
            .title("Portwarehouse").with(tracker)
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

        binding.rvVerifiedBale.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvVerifiedBale.setHasFixedSize(true)
        binding.tvWeight.text =
            mtn?.sourceNetWeight?.toDouble()?.formatThreeDigits().toString().plus(" ")
                .plus(mtn?.uom)
        val grades = grades?.map { it.grade }
        binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")

        binding.rvVerifiedBale.adapter = mVerifiedBaleAdapter

        binding.btnVerifyBaleProceed.setOnClickListener {
            moveToBaleReview()
        }

        binding.llScanBale.setOnClickListener {
            startActivityForResult(
                Intent(this, ScannerActivity::class.java), PortWHUtil.ACTIVITY_REQUEST_CODE
            )
        }

        binding.btnAddBale.setOnClickListener {
            val baleId = binding.etBaleID.text.toString().trim()
            validateBaleId(baleId)
        }
        binding.etBaleID.onChange { changeButttonColor(it) }

        binding.tvMtnNumber.text = mtn?.mtnNumber

        mtn?.let { mtn ->
            vm.getVerifiedBales(mtn.mtnNumber)
            vm.verifyMtnBales.observe(this, Observer {
                if (intent.getIntExtra("ViewStatus", 0) == 1) {
                    updateUI(it)
                } else {
                    offlineUpdateUI(it)
                }

            })
        }
        vm.mtnClasify.observe(this, Observer {
            handleResponse(it)
        })
    }

    private fun validateBaleId(baleId: String) {
        val isValid = vm.validateBaleId(baleId)
        mtn?.let {
            var bale = runBlocking {
                withContext(Dispatchers.IO) {
                    vm.getBale(it.mtnNumber, baleId, it.lineItem)
                }
            }
            when {
                isValid && bale == null -> {
                    for (n in mtnverifiedbale) {
                        if (n.baleId.equals(baleId)) {
                            found = true
                            break

                        }
                    }
                    if (found) {
                        found = false
                        showErrorDialogWithFAQLink(
                            this,
                            getString(R.string.bale_already_offloaded)
                        )
                        binding.etBaleID.text.clear()
                    } else {
                        showErrorDialogWithFAQLink(
                            this,
                            getString(R.string.bale_not_available)
                        )
                        binding.etBaleID.text.clear()
                    }

                }
                isValid && bale != null -> moveToBaleStatus(bale)
                else -> showErrorDialogWithFAQLink(
                    this, getString(R.string.error_bale_id)
                )
            }
        }
    }

    private fun moveToBaleReview() {
        val intent = Intent(this, PortIncomingReviewBaleActivity::class.java)
        intent.putExtra(MTN, mtn)
        intent.putParcelableArrayListExtra(GRADES, grades as ArrayList<PortMtnGrades>)
        startActivity(intent)
    }


    private fun moveToBaleStatus(bale: PortMtnBales?) {
        val intent = Intent(this, PortIncomingBaleStatusActivity::class.java)
        intent.putExtra(BALE, bale)
        intent.putExtra(PLANT, mtn?.suplierPlantDesc)
        startActivity(intent)
    }

    private fun clearEditTextFocus() {
        binding.etBaleID.text.clear()
        binding.etBaleID.clearFocus()
    }

    private fun moveEdit(bale: PortMtnBales) {
        val intent = Intent(this, PortIncomingBaleStatusActivity::class.java)
        intent.putExtra(BALE, bale)
        intent.putExtra(PLANT, mtn?.suplierPlantDesc)
        startActivity(intent)
    }


    private fun showDeleteDialog(mtnBale: PortMtnBales) {

        showDialog(resources.getString(R.string.dialog_confirm_bale_delete),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            vm.deleteBale(mtnBale, mVerifiedBaleAdapter.itemCount)
                        }
                    }
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /*MaterialDialog(this).show {
            cancelable(false)
            title(R.string.dialogTitle)
            positiveButton(R.string.yes) { deleteBaleFromContainer(bale) }
            negativeButton(R.string.cancel) { dismiss() }
            message(R.string.dialogDeleteTitle)
        }*/
    }

    private fun updateUI(mtnBales: List<PortMtnBales>) {
        mtnverifiedbale = mtnBales
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
            binding.tvBaleYetToVerify.text = (it.toInt() - mVerifiedBaleAdapter.itemCount).toString()
        }

        if (mVerifiedBaleAdapter.itemCount == 1) {
            mtn?.mtnNumber?.let {
                vm.getMtnInClassification(it)
            }
        }

    }

    private fun handleResponse(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when {
                        it.data?.data?.success!! -> {
                        }
                        else -> {
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }

    }


    private fun offlineUpdateUI(mtnBales: List<PortMtnBales>) {

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
            vm.setMtnWithBales(mtnNumber)
            vm.getTotalCount()?.let {
                binding.tvBaleReceived.text = it
            }
            vm.getVerifiedCount()?.let {
                binding.tvBaleVerified.text = it
            }

            vm.getYetToVerifyCount()?.let {
                binding.tvBaleYetToVerify.text = it
            }
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
            requestCode == PortWHUtil.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                data?.let {
                    val id: String? = it.getStringExtra(Constants.SCANNED_ID)
                    id?.let {
                        validateBaleId(it.trim().removeSpecialCharacters()
                            .uppercase(Locale.getDefault()))
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
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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
