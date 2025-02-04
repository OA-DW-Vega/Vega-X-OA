package com.olam.warehouse.portwarehouse.ui.pile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_BALE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_GRADE
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.fragment_port_bale_list.*
import kotlinx.android.synthetic.main.item_port_bale_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class PortPileBaleInfoActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.fragment_port_bale_list
    private val vm: PortPileAddBaleViewModel by viewModel()
    private var balesList: ArrayList<PortPileBale>? = null
    private var isConformPile = false
    private var selectedGrade: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  injectPortPileFeature()
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/pile/PortPileBaleInfoActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun initExtras() {
        balesList = intent.getParcelableArrayListExtra(PILE_LIST)
        if (intent.hasExtra(PortWHUtil.CONFIRM_PILE)) {
            isConformPile = true
            vi_delete.gone()
        }
    }

    private fun initUI() {
        tvBaleCount.text = balesList?.size.toString()
        btnOk.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(SELECTED_BALE, balesList)
            resultIntent.putExtra(SELECTED_GRADE, selectedGrade)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        balesList?.let { setUpAdpater(it) }
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        resultIntent.putExtra(SELECTED_BALE, balesList)
        resultIntent.putExtra(SELECTED_GRADE, selectedGrade)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                android.R.id.home -> {
                    val resultIntent = Intent()
                    resultIntent.putExtra(SELECTED_BALE, balesList)
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

    fun removeBale(bale: PortPileBale) {
        vm.removeBaleFromStorage(bale)
        //vm.removeBaleFromStorage(bale)
    }

    private fun setUpAdpater(listItem: ArrayList<PortPileBale>) {
        rvBaleList.setUp(
            listItem,
            R.layout.item_port_bale_summary,
            { it, pos ->
                selectedGrade = it.grade
                if (isConformPile) llClose.gone() else llClose.visible()
                txt_bale_id.text = it.baleID
                txt_grade.text = it.grade
                txt_weight.text = it.netWeight.toString().plus(" ").plus("KG")
                llClose.setOnClickListener { it1 ->
                    showConfirmDialog(it)
                }
            })
    }

    private fun showConfirmDialog(bale: PortPileBale) {
        MaterialDialog(this).show {
            message(R.string.delete_bale_alert)
            getMetirialCustomView(this, getString(R.string.yes), getString(R.string.cancel), {
                balesList?.remove(bale)
                balesList?.let { it1 -> setUpAdpater(it1) }
                removeBale(bale)
                val resultIntent = Intent()
                resultIntent.putExtra(SELECTED_BALE, bale)
                setResult(Activity.RESULT_OK, resultIntent)
            }, { dismiss() })
        }

    }
}
