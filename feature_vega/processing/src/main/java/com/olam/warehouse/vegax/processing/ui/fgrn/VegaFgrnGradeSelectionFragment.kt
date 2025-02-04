package com.olam.warehouse.vegax.processing.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.olam.warehouse.master.vega.entity.VegaFgrnGrades
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaFgrnGradeSelectionBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.ui.fgrn.adapters.GradeSelecionAdapter
import com.olam.warehouse.vegax.processing.utils.POSELECTION
import com.olam.warehouse.vegax.processing.utils.PO_DETAILS
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaFgrnGradeSelectionFragment : BaseFragment(), GradeSelecionAdapter.ItemListener {
    private lateinit var binding: FragmentVegaFgrnGradeSelectionBinding
    override val layoutResourceId = R.layout.fragment_vega_fgrn_grade_selection
    private val vm: VegaProcessingViewModel by viewModel()
    private var callBack: CallBack? = null
    private var poOrderDetails: VegaFgrnProcessingOrder? = null
    private var mAdapter: GradeSelecionAdapter? = null
    private var poGradeList = mutableListOf<VegaFgrnGrades>()
    private var poCheckedGradeList = mutableListOf<VegaFgrnGrades>()

    interface CallBack {
        fun replaceFragment(fragment: String, poOrder: VegaFgrnProcessingOrder, poGrade: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(poOrder: VegaFgrnProcessingOrder) = VegaFgrnGradeSelectionFragment().putArgs {
            putParcelable(PO_DETAILS, poOrder)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaFgrnGradeSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/fgrn/VegaFgrnGradeSelectionFragment").title("Processing")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnProceed, it, true)
        }
        poOrderDetails = arguments?.getParcelable(PO_DETAILS)!!
        vm.fgrnGradeList.observe(viewLifecycleOwner, Observer { updateFgrnGradeUI(it) })
        vm.fetchFgrnGradeList(poOrderDetails!!.processOrderNo)
        binding.tvPono.text = poOrderDetails!!.processOrderNo.toString()
        binding.btnProceed.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        poCheckedGradeList.clear()
        poGradeList.forEach {
            if (it.isGradeChecked == true) poCheckedGradeList.add(it)
        }
        if (poCheckedGradeList.size != 0) {
            val gson = GsonUtils()
            val poGrade = gson.toJson(poCheckedGradeList)
            callBack?.replaceFragment(POSELECTION, poOrderDetails!!, poGrade)
        } else
            showSnack(requireContext().resources.getString(R.string.please_check_grade))
    }


    override fun onItemCheck(item: Boolean, position: Int) {
        poGradeList.get(position).isGradeChecked = true
        updateDetails(poGradeList)
    }

    override fun onItemUncheck(item: Boolean, position: Int) {
        poGradeList.get(position).isGradeChecked = false
        updateDetails(poGradeList)
    }


    private fun updateFgrnGradeUI(response: Resource<GenericReqAndResp<List<VegaFgrnGrades>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let {
                                poGradeList = it as MutableList<VegaFgrnGrades>
                                updateDetails(it)
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun updateDetails(datalist: List<VegaFgrnGrades>) {
        when {
            datalist.isEmpty() -> {
                binding.tvError.text = getString(R.string.process_not_avail)
                binding.tvError.visibility = View.VISIBLE
                binding.rvFgrnGrade.visibility = View.GONE
            }
            else -> {
                binding.tvError.visibility = View.GONE
                binding.rvFgrnGrade.visibility = View.VISIBLE
            }
        }

        mAdapter = GradeSelecionAdapter(this, datalist)
        binding.rvFgrnGrade.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvFgrnGrade.adapter = mAdapter
    }
}
