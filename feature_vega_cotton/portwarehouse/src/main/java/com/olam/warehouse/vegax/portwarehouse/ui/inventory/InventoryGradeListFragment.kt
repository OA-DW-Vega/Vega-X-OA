package com.olam.warehouse.portwarehouse.ui.inventory

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleGrade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PileModel
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.BALE_LIST
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_PILES
import com.olam.warehouse.portwarehouse.viewmodel.InventoryGradeViewModel
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.activity_inventory_grade.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class InventoryGradeListFragment : BaseFragment() {

    private val vm: InventoryGradeViewModel by viewModel { emptyParametersHolder() }
    private var callBack: CallBack? = null
    private var selectedPileList = ArrayList<PileModel>()

    override val layoutResourceId: Int = R.layout.activity_inventory_grade

    private lateinit var mAdapter: InventoryGradeAdapter

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            grades: ArrayList<String>,
            item: MutableList<BaleGrade>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(piles: ArrayList<PileModel>) = InventoryGradeListFragment().putArgs {
            putParcelableArrayList(SELECTED_PILES, piles)
        }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/InventoryGradeListFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun initUI() {
        selectedPileList =
            arguments?.getParcelableArrayList<PileModel>(SELECTED_PILES) as ArrayList<PileModel>
        rvInventoryGrades.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )

        showLoading()
        val piles = selectedPileList.filter { it.isChecked }.map { it.storageLocationCode }
        vm.getInventoryGrades(piles)
        vm.getInventoryGrades .observe(viewLifecycleOwner, Observer {
            hideLoading()
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {

                        it.data.let { baleGrades ->
                            if (baleGrades.isNotEmpty()) {
                                hideEmptyView()
                                mAdapter = InventoryGradeAdapter(
                                    baleGrades.toMutableList(), { onGradeCheck() }, { onGradeSync(it) })
                                rvInventoryGrades.adapter = mAdapter
                            } else {
                                showEmptyView()
                            }

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
            val grades = mAdapter.getSelectedGrades()
            callBack?.replaceFragment(BALE_LIST, grades, mAdapter.getItem())
        }
    }

    private fun onGradeCheck(){
        val selectedGrades = mAdapter.getSelectedGrades()

        if(selectedGrades.isNotEmpty()) {
            enableButton()
        }else {
            disableButton()
        }
    }

    private fun onGradeSync(grade: String) {
        showLoading()
        vm.syncBaleByGrade(grade)
        vm.syncBaleByGrade .observe(viewLifecycleOwner, Observer {
            hideLoading()
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let {
                        it.data.let {

                            showDialog("Grade Sync Successfully")

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

    private fun hideEmptyView() {
        tvGradesEmpty.visibility = View.INVISIBLE
        rvInventoryGrades.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        tvGradesEmpty.visibility = View.VISIBLE
        rvInventoryGrades.visibility = View.INVISIBLE
    }


}
