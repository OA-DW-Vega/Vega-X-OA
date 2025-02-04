package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.activity_mtn_dispatch_edit_bale.*
import kotlinx.android.synthetic.main.item_mtn_dispatch_edit_bale.view.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 3/30/2021.
 */
class MtnDispatchEditBaleActivity : HomeBaseActivity() {

    private var baleList: ArrayList<MtnBale>? = null
    private var deliveryNo: String? = ""

    override val layoutResourceId = R.layout.activity_mtn_dispatch_edit_bale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/dispatch/mtntdispatch/MtnDispatchEditBaleActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initExtras() {
        deliveryNo = intent.getStringExtra(PortWHUtil.DELIVERY_NO)!!
        baleList = intent.getParcelableArrayListExtra<MtnBale>(PortWHUtil.BALE_LIST)
    }

    private fun initUI() {
        baleList?.let {
            txt_selectedid.text = it.size.toString()
            setUpAdpater(it)
        }
        btn_ok.setOnClickListener { finish() }
    }

    private fun setUpAdpater(listItem: ArrayList<MtnBale>) {
        lotdetails_recyclerview1.setUp(listItem, R.layout.item_mtn_dispatch_edit_bale, { it, pos ->
            txt_bale_id.text = it.baleID
            txt_grade.text = it.grade
            txt_weight.text = it.grossWeight.toString().plus(" ").plus("KG")
            llClose.setOnClickListener { it1 ->
                showConfirmDialog(it)
            }
        })
    }

    private fun showConfirmDialog(bale: MtnBale) {
        MaterialDialog(this).show {
            message(R.string.delete_bale_alert)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.yes),
                getString(R.string.cancel),
                {
                    baleList?.remove(bale)
                    baleList?.let { it1 -> setUpAdpater(it1) }
                    val resultIntent = Intent()
                    resultIntent.putExtra(PortWHUtil.SELECTED_BALE, bale)
                    setResult(Activity.RESULT_OK, resultIntent)
                    //finish()
                },
                { dismiss() })
        }
    }

}
