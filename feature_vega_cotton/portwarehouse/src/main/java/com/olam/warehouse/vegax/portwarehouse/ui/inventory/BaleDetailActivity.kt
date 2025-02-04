package com.olam.warehouse.portwarehouse.ui.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.portwarehouse.utils.enums.BaleStatus
import com.olam.warehouse.portwarehouse.viewmodel.InventoryGradeViewModel
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.di.injectPortInventoryFeaturee
import kotlinx.android.synthetic.main.activity_bale_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class BaleDetailActivity : HomeBaseActivity() {

    private var bale: PortBale? = null
    private var status: Int? = null

    private val vm: InventoryGradeViewModel by viewModel { emptyParametersHolder() }
    override val layoutResourceId: Int = R.layout.activity_bale_detail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectPortInventoryFeaturee()
        initNavigationView()
        initExtra()
        initView()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/BaleDetailActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initExtra() {
        bale = intent.extras?.get(UIUtils.INVENTORY_BALE) as? PortBale
        status = intent.extras?.getInt(UIUtils.BALE_STATUS)
    }

    private fun initView() {
        tvInBale.text = bale?.baleId
        if (bale != null && bale?.baleMark != null && bale?.baleMark?.isNotEmpty()!!)
            tvInBaleMark.text = bale?.baleMark
        else tvInBaleMark.text = getString(R.string.not_available)
        tvInGrade.text = bale?.grade
        tvInWeight.text =
            "".plus(
                if (bale?.netWeight?.toString()
                        ?.equals("0.0")!!
                ) bale?.delyNetQty else bale?.netWeight
            )
                .plus(" kg")
        tvInBaleType.text =
            BaleStatus.isGoodOrDamaged(if (status == 1) bale?.typeOfBale else bale?.typeofBale)


        if (bale?.typeofBale.equals("Good") || bale?.typeOfBale.equals("Good")) {
            llDamageType.gone()
            btnProceed.gone()
        } else {
            llDamageType.visible()
            when (status) {
                1 -> {
                    tvDamageType.text = bale?.typeOfBale
                    llChangeBaleStatus.gone()
                }
                else -> {
                    tvDamageType.text = bale?.typeofBale
                    llChangeBaleStatus.visible()
                }
            }

        }

        cbBaleStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> {
                    btnProceed.isEnabled = true
                    ViewCompat.setBackgroundTintList(btnProceed, ContextCompat.getColorStateList(this, R.color.green))
                }
                else -> {
                    btnProceed.isEnabled = false
                    ViewCompat.setBackgroundTintList(
                        btnProceed,
                        ContextCompat.getColorStateList(this, R.color.light_grey)
                    )
                }
            }
        }
        btnProceed.setOnClickListener {
            if (status == 1) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                showLoading()
                changeBaleType()
            }

        }
        if (status == 1) {
            btnProceed.visible()
            btnProceed.text = resources.getString(R.string.ok)
            btnProceed.setBackgroundColor(resources.getColor(R.color.green))
        }
    }

    private fun changeBaleType() {

        when (cbBaleStatus.isChecked) {
            true -> {
                val changeBaleStatus = ChangeBaleStatus(
                    inventoryBaleDTO = PortBale(
                        baleId = bale?.baleId!!,
                        fromSloc = bale?.typeofBale?.let { BaleStatus.from(it)?.id }.toString(),
                        grade = bale?.grade,
                        grossWeight = bale?.grossWeight,
                        netWeight = bale?.netWeight,
                        toSloc = BaleStatus.Good.id.toString(),
                        typeofBale = BaleStatus.Good.status
                    ),
                    istogrnPost = "",
                    itransferPost = "X"
                )
                vm.changeBaleStatus(changeBaleStatus)
                    vm.changeBaleStatus.observe(this, Observer {
                        hideLoading()
                        when (it.status) {
                            Resource.Status.SUCCESS -> {
                                it.data?.let {
                                    it.data.let {
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    }

                                }

                                hideLoading()
                            }
                            Resource.Status.LOADING -> showLoading()
                            Resource.Status.ERROR -> {
                                hideLoading()
                                UIUtils.showErrorDialog(this, it.error.toString())
                            }
                        } })
            }
        }

    }
    
}
