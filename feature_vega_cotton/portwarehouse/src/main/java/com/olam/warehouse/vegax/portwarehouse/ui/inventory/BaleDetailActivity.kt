package com.olam.warehouse.vegax.portwarehouse.ui.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeActivity
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.ChangeBaleStatus
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityBaleDetailBinding
import com.olam.warehouse.vegax.portwarehouse.di.injectPortInventoryFeaturee
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class BaleDetailActivity : HomeBaseActivity() {

    private var bale: PortBale? = null
    private var status: Int? = null

    private val vm: InventoryGradeViewModel by viewModel { emptyParametersHolder() }
    override val layoutResourceId: Int = R.layout.activity_bale_detail
    private lateinit var binding: ActivityBaleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.tvInBale.text = bale?.baleId
        if (bale != null && bale?.baleMark != null && bale?.baleMark?.isNotEmpty()!!)
            binding.tvInBaleMark.text = bale?.baleMark
        else binding.tvInBaleMark.text = getString(R.string.not_available)
        binding.tvInGrade.text = bale?.grade
        binding.tvInWeight.text =
            "".plus(
                if (bale?.netWeight?.toString()
                        ?.equals("0.0")!!
                ) bale?.delyNetQty else bale?.netWeight
            )
                .plus(" kg")
        binding.tvInBaleType.text =
            BaleStatus.isGoodOrDamaged(if (status == 1) bale?.typeOfBale else bale?.typeofBale)


        if (bale?.typeofBale.equals("Good") || bale?.typeOfBale.equals("Good")) {
            binding.llDamageType.gone()
            binding.btnProceed.gone()
        } else {
            binding.llDamageType.visible()
            when (status) {
                1 -> {
                    binding.tvDamageType.text = bale?.typeOfBale
                    binding.llChangeBaleStatus.gone()
                }
                else -> {
                    binding.tvDamageType.text = bale?.typeofBale
                    binding.llChangeBaleStatus.visible()
                }
            }

        }

        binding.cbBaleStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> {
                    binding.btnProceed.isEnabled = true
                    ViewCompat.setBackgroundTintList(
                        binding.btnProceed,
                        ContextCompat.getColorStateList(
                            this,
                            com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                        )
                    )
                }
                else -> {
                    binding.btnProceed.isEnabled = false
                    ViewCompat.setBackgroundTintList(
                        binding.btnProceed,
                        ContextCompat.getColorStateList(
                            this,
                            com.olam.warehouse.presentation.R.color.colorSecondaryGrey
                        )
                    )
                }
            }
        }
        binding.btnProceed.setOnClickListener {
            if (status == 1) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                showLoading()
                changeBaleType()
            }

        }
        if (status == 1) {
            binding.btnProceed.visible()
            binding.btnProceed.text = resources.getString(R.string.ok)
            binding.btnProceed.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        }

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
                    showErrorDialogWithFAQLink(this, it.error.toString())
                }
            }
        })

    }

    private fun changeBaleType() {

        when (binding.cbBaleStatus.isChecked) {
            true -> {
                val changeBaleStatus = ChangeBaleStatus(
                    inventoryBaleDTO = PortBale(
                        baleId = bale?.baleId!!,
                        fromSloc = bale?.typeofBale?.let { BaleStatus.from(it)?.id }.toString(),
                        grade = bale?.grade,
                        grossWeight = bale?.grossWeight,
                        netWeight = bale?.netWeight,
                        toSloc = if(getCurrentKey().split("_")[2].contains("COTTPORT")){PortWHUtil.getStorageID()}else{BaleStatus.Good.id.toString()},
                        typeofBale = BaleStatus.Good.status
                    ),
                    istogrnPost = "",
                    itransferPost = "X"
                )
                vm.changeBaleStatus(changeBaleStatus)
            }
            else -> {}
        }

    }
    
}
