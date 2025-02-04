package com.olam.warehouse.portwarehouse.ui.inventory

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleMark
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.CropYears
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PileModel
import com.olam.warehouse.portwarehouse.viewmodel.InventoryGradeViewModel
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.fragment_inventory_pile_list.*
import kotlinx.android.synthetic.main.row_inventory_piles.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/15/2020.
 */
class InventoryPileListFragment : BaseFragment() {

    private var selectedPileList = ArrayList<String>()
    private var pileList = ArrayList<PileModel>()
    private var markList = ArrayList<BaleMark>()
    private var yearList = ArrayList<CropYears>()
    private var callBack: CallBack? = null

    interface CallBack {
        fun replaceFragment(
            piles: ArrayList<PileModel>,
            markList: ArrayList<BaleMark>,
            yearList: ArrayList<CropYears>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = InventoryPileListFragment().putArgs {

        }

        const val SEARCH_HINT_BALE_TEXT = "Search Bale"
    }

    private val vm: InventoryGradeViewModel by viewModel { emptyParametersHolder() }

    override val layoutResourceId = R.layout.fragment_inventory_pile_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/InventoryPileListFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {

        vm.getPileList()
        vm.getPileList .observe(viewLifecycleOwner, Observer {
            hideLoading()
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        it.data.let {
                            markList.clear()
                            yearList.clear()
                            pileList.clear()

                            pileList = it.piles as ArrayList<PileModel>
                            it.baleMark.forEach {
                                val marks = BaleMark()
                                marks.baleMark = it
                                markList.add(marks)
                            }
                            it.cropYear.forEach {
                                val years = CropYears()
                                years.cropYear = it
                                yearList.add(years)
                            }
                            setUpAdpater(pileList)
                        }

                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
            }

        })


        btnProceed.setOnClickListener {
            val piles = pileList.filter { it.isChecked }.map { item -> item.storageLocationCode }
            callBack?.replaceFragment(pileList, markList, yearList)
        }
    }

    private fun setUpAdpater(pileList: ArrayList<PileModel>) {
        rvInventoryPiles.setUp(pileList, R.layout.row_inventory_piles, { it, pos ->
            tvPileId.text = it.storageLocationCode.plus(" - ").plus(it.classification)
            cbPile.isChecked = it.isChecked
            cbPile.setOnClickListener {
                changeSelection(pos)
            }

        })
    }

    private fun changeSelection(position: Int) {

        val grade = pileList[position]
        grade.isChecked = !grade.isChecked
        onGradeCheck()
        rvInventoryPiles.adapter?.notifyDataSetChanged()
    }

    private fun onGradeCheck() {
        val selectedGrades = pileList.filter { it.isChecked }

        if (selectedGrades.isNotEmpty()) {
            enableButton()
        } else {
            disableButton()
        }
    }

    private fun disableButton() {
        btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey)
            )
            isEnabled = false
        }
    }

    private fun enableButton() {
        btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.green)
            )
            isEnabled = true
        }
    }
}
