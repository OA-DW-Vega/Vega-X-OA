package com.olam.warehouse.vegax.portwarehouse.ui.dispatch

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.data.domain.model.GenericMessage
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants.SEAL_ID
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.onChange
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.SealContainerActivityBinding
import com.olam.warehouse.vegax.portwarehouse.ui.success.SuccessPortActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/1/2021.
 */
class PortDispatchSealContainerActivity : HomeBaseActivity() {

    private var containerId: String = ""
    private var sealNo: String = ""
    private var otNumber: String = ""
    private var isDirect: Boolean = false
    private val vm: PortDispatchSealViewModel by viewModel { emptyParametersHolder() }

    override val layoutResourceId = R.layout.seal_container_activity
    private lateinit var binding: SealContainerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SealContainerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/dispatch/PortDispatchSealContainerActivity")
            .title("Portwarehouse").with(tracker)
    }

    override fun onBackPressed() {
        showBackConfirmDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
            when (it.itemId) {
                android.R.id.home -> {
                    showBackConfirmDialog()
                }
            }
        }
        return false
    }


    private fun initUI() {
        binding.etSealNo.onChange { changeButtonColor(it) }
        binding.btnSeal.setOnClickListener { validateSealId() }

        vm.sealContainer.observe(this, Observer { it1 -> updateUI(it1) })

    }

    private fun initExtras() {
        containerId = intent.getStringExtra(PortWHUtil.CONTAINER_ID) ?: ""
        otNumber = intent.getStringExtra(PortWHUtil.OT_NUMBER) ?: ""
        isDirect = intent.getBooleanExtra(PortWHUtil.DISPATCH_TYPE, false)
    }

    private fun validateSealId() {
        sealNo = binding.etSealNo.text.toString()
        val isValid = vm.validateSealId(sealNo)
        when {
            !isValid -> toast(getString(R.string.error_incorrect_seal_id))
            isValid -> showSealConfirmDialog()
            else -> setErrorMessage()
        }
    }

    private fun setErrorMessage() {
        runOnUiThread {
            binding.etSealNo.error = getString(R.string.error_incorrect_seal_id)
            binding.etSealNo.requestFocus()
        }
    }

    private fun sealContainer() {
        containerId.let {
            vm.sealContainer(sealNo, containerId, otNumber, isDirect)
        }

    }

    private fun updateUI(response: Resource<GenericReqAndResp<GenericMessage>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.data?.success == true) {
                        updateSuccessUI()
                    } else {
                        showGRNFailedAlert(it.data?.data?.message ?: " ")
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

    private fun updateSuccessUI() {
        binding.etSealNo.text.clear()
        PreferenceHelper.save(SEAL_ID, sealNo)
        runBlocking {
            withContext(Dispatchers.IO)
            {
                vm.updateSealIDToLocalDB(sealNo, containerId)
            }
        }

        moveToSuccess()
    }

    private fun moveToSuccess() {
        val intent = Intent(this, SuccessPortActivity::class.java)
        intent.putExtra(PortWHUtil.CONTAINER_ID, containerId)
        intent.putExtra(UIUtils.SEAL_ID, sealNo)
        intent.putExtra(PortWHUtil.OT_NUMBER, otNumber)
        intent.putExtra(PortWHUtil.SUCCESS, PortWHUtil.SEAL_SUCCESS)
        intent.putExtra(PortWHUtil.DISPATCH_TYPE, isDirect)
        startActivity(intent)
    }

    private fun changeButtonColor(value: String) {
        when (value.length >= 10) {
            true -> {
                ViewCompat.setBackgroundTintList(
                    binding.btnSeal,
                    ContextCompat.getColorStateList(
                        this,
                        com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                    )
                )
                //btnSeal.setTextColor(ContextCompat.getColorStateList(this,R.color.white))
            }
            else -> {
                ViewCompat.setBackgroundTintList(
                    binding.btnSeal,
                    ContextCompat.getColorStateList(
                        this,
                        com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                    )
                )
                //btnSeal.setTextColor(ContextCompat.getColorStateList(this, R.color.black))
            }
        }
    }

    private fun showSealConfirmDialog() {

        showDialog(resources.getString(R.string.confirm_seal_container),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    showLoading()
                    sealContainer()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })

        /* MaterialDialog(this).show {
             cancelable(false)
             title(R.string.dialogTitle)
             positiveButton(R.string.yes) { sealContainer() }
             negativeButton(R.string.cancel) { dismiss() }
             message(R.string.confirm_seal_container)
         }*/
    }


    private fun showGRNFailedAlert(message: String) {

        showDialog(
            message,
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    dialog.dismiss()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            }, negativeText = R.string.negative_empty, postiveText = R.string.ok
        )

        /* MaterialDialog(this).show {
             cancelable(false)
             title(R.string.dialogTitle)
             positiveButton(R.string.yes) { sealContainer() }
             negativeButton(R.string.cancel) { dismiss() }
             message(R.string.confirm_seal_container)
         }*/
    }

    fun showBackConfirmDialog() {
        showDialog(resources.getString(R.string.confirm_leave_page),
            object : DialogClick {
                override fun onPositive(dialog: DialogInterface) {
                    moveBackAddBale()
                }

                override fun onNegative(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
    }

    private fun moveBackAddBale() {
        startActivity(Intent(this, PortDispatchAddBaleActivity::class.java))
    }

}
