package com.olam.warehouse.ginning.ui.inventory

import android.os.Bundle
import com.olam.warehouse.ginning.utils.INVENTORY_BALE
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginnind_bale_detail.*
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        tvInBale.text = bale?.baleID
        tvInGrade.text = bale?.grade
         tvInWeight.text = "".plus(bale?.netWeight).plus(" kg")
        btnProceed.setOnClickListener { finish() }

    }

    private fun changeBaleType() {

        when (cbBaleStatus.isChecked) {
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
        }

    }


}
