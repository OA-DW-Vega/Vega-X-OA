package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory

import android.os.Bundle
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinnindBaleDetailBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.INVENTORY_BALE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by SangiliPandian C on 18-03-2020.
 */
class GinningBaleDetailActivity : HomeBaseActivity() {

    private var bale: Bale? = null
    private var status: Int? = null

    private val vm: InventoryViewModel by viewModel { emptyParametersHolder() }
    override val layoutResourceId: Int = R.layout.activity_ginnind_bale_detail
    private lateinit var binding: ActivityGinnindBaleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinnindBaleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtra()
        initView()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/inventory/GinningBaleDetailActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtra() {
        bale = intent.extras?.get(INVENTORY_BALE) as? Bale
        //status = intent.extras?.getInt(BALE_STATUS)
    }

    private fun initView() {
        binding.tvInBale.text = bale?.baleID
        binding.tvInGrade.text = bale?.grade
        binding.tvInWeight.text = "".plus(bale?.netWeight).plus(" kg")
        binding.btnProceed.setOnClickListener { finish() }

    }

    private fun changeBaleType() {

        when (binding.cbBaleStatus.isChecked) {
            true -> {
                /*val changeBaleStatus = ChangeBaleStatus(
                    inventoryBaleDTO = Bale(
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
                vm.chageBaleStatus(changeBaleStatus).observe(this, Observer { bindToList(it) })*/
            }
            else -> {}
        }

    }


}
