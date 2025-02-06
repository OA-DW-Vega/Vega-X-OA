package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.inventory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningInventoryGradeBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.data.model.BaleGrade
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.BALE_LIST
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class GinningInventoryGradeListFragment : BaseFragment() {

    private val vm: InventoryViewModel by viewModel { emptyParametersHolder() }
    private var callBack: CallBack? = null
    override val layoutResourceId: Int = R.layout.activity_ginning_inventory_grade
    private lateinit var mAdapter: InventoryGradeAdapter
    private lateinit var binding: ActivityGinningInventoryGradeBinding

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
        fun newInstance() = GinningInventoryGradeListFragment().putArgs {

        }

        const val SEARCH_HINT_BALE_TEXT = "Search Bale"
        const val BALE_DETAIL_REQUEST = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityGinningInventoryGradeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observerCall()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/inventory/GinningInventoryGradeListFragment")
            .title("Ginningwarehouse").with(tracker)
    }


  private  fun observerCall(){

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
                                binding.rvInventoryGrades.adapter = mAdapter
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
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }

        })

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

    private fun fetchGrades() {
        showLoading()
        vm.getInventoryGrades()


    }

    private fun initUI() {
        binding.rvInventoryGrades.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        binding.btnProceed.setOnClickListener {
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

    }

    private fun disableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey)
            )
            isEnabled = false
        }
    }

    private fun enableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            isEnabled = true
        }
    }

    private fun hideEmptyView() {
        binding.tvGradesEmpty.visibility = View.INVISIBLE
        binding.rvInventoryGrades.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        binding.tvGradesEmpty.visibility = View.VISIBLE
        binding.rvInventoryGrades.visibility = View.INVISIBLE
    }


}
