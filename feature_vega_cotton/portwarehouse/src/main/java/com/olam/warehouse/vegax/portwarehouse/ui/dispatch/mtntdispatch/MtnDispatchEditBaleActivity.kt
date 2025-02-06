package com.olam.warehouse.vegax.portwarehouse.ui.dispatch.mtntdispatch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.MtnBale
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.ActivityMtnDispatchEditBaleBinding
import com.olam.warehouse.vegax.portwarehouse.databinding.ItemMtnDispatchEditBaleBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
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
    private lateinit var binding: ActivityMtnDispatchEditBaleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMtnDispatchEditBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.txtSelectedid.text = it.size.toString()
            setUpAdpater(it)
        }
        binding.btnOk.setOnClickListener { finish() }
    }

    private fun setUpAdpater(listItem: ArrayList<MtnBale>) {
        binding.lotdetailsRecyclerview1.setUpAdapter(
            listItem,
            R.layout.item_mtn_dispatch_edit_bale,
            ItemMtnDispatchEditBaleBinding::inflate,
            { it, pos, bindItem ->
                bindItem.txtBaleId.text = it.baleID
                bindItem.txtGrade.text = it.grade
                bindItem.txtWeight.text = it.grossWeight.toString().plus(" ").plus("KG")
                bindItem.llClose.setOnClickListener { it1 ->
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
