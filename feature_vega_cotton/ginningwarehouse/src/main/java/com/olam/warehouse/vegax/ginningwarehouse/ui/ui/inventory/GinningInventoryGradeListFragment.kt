package com.olam.warehouse.ginning.ui.inventory

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.ginning.data.model.BaleGrade
import com.olam.warehouse.ginning.utils.BALE_LIST
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import kotlinx.android.synthetic.main.activity_ginning_inventory_grade.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningInventoryGradeListFragment : BaseFragment() {

    private val vm: InventoryViewModel by viewModel { emptyParametersHolder() }
    private var callBack: CallBack? = null
    override val layoutResourceId: Int = R.layout.activity_ginning_inventory_grade
    private lateinit var mAdapter: InventoryGradeAdapter

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            grades: ArrayList<String>,
            gradeList: ArrayList<BaleGrade>
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = GinningInventoryGradeListFragment().putArgs{

        }

        const val SEARCH_HINT_BALE_TEXT = "Search Bale"
        const val BALE_DETAIL_REQUEST = 10
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/inventory/GinningInventoryGradeListFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun fetchGrades() {
        showLoading()
        vm.getInventoryGrades()
          vm.getInventoryGrades .observe(viewLifecycleOwner, Observer {
            hideLoading()
             when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let { baleGrades ->
                                if (baleGrades.isNotEmpty()) {
                                    hideEmptyView()
                                    mAdapter = InventoryGradeAdapter(
                                        baleGrades.toMutableList(),
                                        { onGradeCheck() },
                                        { onGradeSync(it) })
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
    }

    private fun initUI() {
        rvInventoryGrades.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        btnProceed.setOnClickListener {
            val grades = mAdapter.getSelectedGrades()
            callBack?.replaceFragment(BALE_LIST, grades, mAdapter.getItem())
        }
        fetchGrades()
    }

    private fun onGradeCheck() {
        val selectedGrades = mAdapter.getSelectedGrades()
        if (selectedGrades.isNotEmpty()) {
            enableButton()
        } else {
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

                            activity?.toast("Grade Sync Successfully")

                        }

                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showPendingAlertDialog(requireContext(), it.error.toString())
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
