package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.dispatch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Bale
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningDispatchEditBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGinningDispatchEditBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.BALE_LIST
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.DELIVERY_NO
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SELECTED_BALE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SELECTED_GRADE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 3/24/2020.
 */
class GinningDispatchEditBaleActivity : HomeBaseActivity() {

    private var baleList: ArrayList<Bale>? = null
    private var deliveryNo: String? = ""
    private var selectedGrade: String? = ""
    private val vm: GinningDispatchViewModel by viewModel()
    private lateinit var binding: ActivityGinningDispatchEditBaleBinding

    override val layoutResourceId = R.layout.activity_ginning_dispatch_edit_bale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningDispatchEditBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            binding.txtSelectedid.text = it.size.toString()
            setUpAdpater(it)
        }
        binding.btnOk.setOnClickListener {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
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
        binding.lotdetailsRecyclerview1.setUpAdapter(
            listItem,
            R.layout.item_ginning_dispatch_edit_bale,
            ItemGinningDispatchEditBaleBinding::inflate,
            { it, pos, bindItem ->
                selectedGrade = it.grade
                bindItem.txtBaleId.text = it.baleID
                bindItem.txtGrade.text = it.grade
                bindItem.txtWeight.text = it.netWeight.toString().plus(" ").plus("KG")
                bindItem.llClose.setOnClickListener { it1 ->
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
