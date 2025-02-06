package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.FragmentGiningBaleListBinding
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ItemGiningBaleSummaryBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.di.injectGinningPileFeature
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.CONFIRM_PILE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.PILE_LIST
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SELECTED_BALE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SELECTED_GRADE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningPileBaleInfoActivity : HomeBaseActivity() {

    override val layoutResourceId = R.layout.fragment_gining_bale_list
    private val vm: GinningPileAddBaleViewModel by viewModel()
    private var balesList: ArrayList<PileBale>? = null
    private var isConformPile = false
    private var selectedGrade: String? = ""
    private lateinit var binding: FragmentGiningBaleListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentGiningBaleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injectGinningPileFeature()
        initNavigationView()
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/pile/GinningPileBaleInfoActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtras() {
        balesList = intent.getParcelableArrayListExtra(PILE_LIST)
        if (intent.hasExtra(CONFIRM_PILE)) {
            isConformPile = true
            binding.viDelete.gone()
        }
    }

    private fun initUI() {
        binding.tvBaleCount.text = balesList?.size.toString()
        binding.btnOk.setOnClickListener {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.let {
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

    fun removeBale(bale: PileBale) {
        runBlocking {
            withContext(Dispatchers.IO) {
                vm.removeBaleFromStorage(bale)
            }
        }
    }

    private fun setUpAdpater(listItem: ArrayList<PileBale>) {
        binding.rvBaleList.setUpAdapter(
            listItem,
            R.layout.item_gining_bale_summary,
            ItemGiningBaleSummaryBinding::inflate,
            { it, pos, bindItem ->
                selectedGrade = it.grade
                if (isConformPile) bindItem.llClose.gone() else bindItem.llClose.visible()
                bindItem.txtBaleId.text = it.baleID
                bindItem.txtGrade.text = it.grade
                bindItem.txtWeight.text = it.netWeight.toString().plus(" ").plus("KG")
                bindItem.llClose.setOnClickListener { it1 ->
                    showConfirmDialog(it)
                }
            })
    }

    private fun showConfirmDialog(bale: PileBale) {
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
