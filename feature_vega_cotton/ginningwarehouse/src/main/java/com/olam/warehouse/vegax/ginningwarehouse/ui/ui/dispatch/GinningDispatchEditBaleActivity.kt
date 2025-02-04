package com.olam.warehouse.ginning.ui.dispatch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.ginning.utils.BALE_LIST
import com.olam.warehouse.ginning.utils.DELIVERY_NO
import com.olam.warehouse.ginning.utils.SELECTED_BALE
import com.olam.warehouse.ginning.utils.SELECTED_GRADE
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginning_dispatch_edit_bale.*
import kotlinx.android.synthetic.main.item_gining_bale_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 3/24/2020.
 */
class GinningDispatchEditBaleActivity : HomeBaseActivity() {

    private var baleList: ArrayList<Bale>? = null
    private var deliveryNo: String? = ""
    private var selectedGrade: String? = ""
    private val vm: GinningDispatchViewModel by viewModel()

    override val layoutResourceId = R.layout.activity_ginning_dispatch_edit_bale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/dispatch/GinningDispatchEditBaleActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtras() {
        deliveryNo = intent.getStringExtra(DELIVERY_NO)!!
        baleList = intent.getParcelableArrayListExtra<Bale>(BALE_LIST)
    }

    private fun initUI() {
        baleList?.let {
            txt_selectedid.text = it.size.toString()
            setUpAdpater(it)
        }
        btn_ok.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(SELECTED_BALE, baleList)
            resultIntent.putExtra(SELECTED_GRADE, selectedGrade)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        val resultIntent = Intent()
        resultIntent.putExtra(SELECTED_BALE, baleList)
        resultIntent.putExtra(SELECTED_GRADE, selectedGrade)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    val resultIntent = Intent()
                    resultIntent.putExtra(SELECTED_BALE, baleList)
                    resultIntent.putExtra(SELECTED_GRADE, selectedGrade)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                    return true
                }
                R.id.search -> return true
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpAdpater(listItem: ArrayList<Bale>) {
        lotdetails_recyclerview1.setUp(
            listItem,
            R.layout.item_ginning_dispatch_edit_bale,
            { it, pos ->
                selectedGrade = it.grade
                txt_bale_id.text = it.baleID
                txt_grade.text = it.grade
                txt_weight.text = it.netWeight.toString().plus(" ").plus("KG")
                llClose.setOnClickListener { it1 ->
                    showConfirmDialog(it)
                }
        })
    }

    private fun showConfirmDialog(bale: Bale) {
        MaterialDialog(this).show {
            message(R.string.delete_bale_alert)
            getMetirialCustomView(this, getString(R.string.yes), getString(R.string.cancel), {
                baleList?.remove(bale)
                deleteBale(bale)
                baleList?.let { it1 -> setUpAdpater(it1) }
                val resultIntent = Intent()
                resultIntent.putExtra(SELECTED_BALE, bale)
                setResult(Activity.RESULT_OK, resultIntent)
                //finish()
            }, { dismiss() })
        }
    }

    private fun deleteBale(it: Bale) {
        it.isUsed = false
        it.deliveryNumber = ""
        vm.saveBale(it)
    }


}
