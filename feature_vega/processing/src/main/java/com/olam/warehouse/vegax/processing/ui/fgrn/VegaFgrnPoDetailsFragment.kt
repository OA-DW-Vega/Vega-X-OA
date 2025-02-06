package com.olam.warehouse.vegax.processing.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vega.entity.VegaFgrnProcessingOrder
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processing.databinding.ItemVegaFgrnPoDetailsBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.utils.GRADESELECTION
import com.olam.warehouse.vegax.processing.utils.getColor
import com.olam.warehouse.vegax.processing.utils.weightToProcess
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.DecimalFormat

class VegaFgrnPoDetailsFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_fgrn_po_details
    private lateinit var binding: FragmentVegaFgrnPoDetailsBinding
    private var callBack: CallBack? = null
    private val vm: VegaProcessingViewModel by viewModel()
    private var stageFevor: String? = ""
    private var cfgNo: String? = ""


    interface CallBack {
        fun replaceFragment(fragment: String, poOrder: VegaFgrnProcessingOrder, poGrade: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance() = VegaFgrnPoDetailsFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaFgrnPoDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/fgrn/VegaFgrnPoDetailsFragment").title("Processing").with(tracker)
        initUI()
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llFgrn, it, false)
        }
        vm.fetchStages()
        vm.stages.observe(viewLifecycleOwner, Observer { updageStageUI(it) })
        vm.poDetailList.observe(viewLifecycleOwner, Observer { updateFgrnPoUI(it) })

    }

    private fun updageStageUI(it: List<VegaProcessingStage>?) {
        it?.let {
            val stageItems = it
            stageItems.forEach {item ->
                if(item.processName.equals("RCN Drying process")){
                    stageFevor = item.fevor
                    cfgNo = item.cfgNo
                }
            }
        }
        vm.fetchFgrnPoDetailsList(stageFevor, cfgNo)
    }


    private fun updateFgrnPoUI(response: Resource<GenericReqAndResp<List<VegaFgrnProcessingOrder>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {
                            response.data?.data?.let { updateDetails(it) }
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun updateDetails(poList: List<VegaFgrnProcessingOrder>) {
        val datalist = poList.filter { !it.rminList.isNullOrEmpty() } as MutableList<VegaFgrnProcessingOrder>
        var data = datalist
        when {
            datalist.isEmpty() -> {
                binding.tvError.text = getString(R.string.process_not_avail)
                binding.tvError.visibility = View.VISIBLE
                binding.rvFgrnPoselection.visibility = View.GONE
            }
            else -> {
                binding.tvError.visibility = View.GONE
                binding.rvFgrnPoselection.visibility = View.VISIBLE
                data = data.sortedByDescending {
                    it.startDate?.split('(', ')')?.get(1)?.let { it1 ->
                        DateUtils.getUTCDateTime(
                            it1,
                            App.getAppContext()
                        )
                    }
                } as MutableList<VegaFgrnProcessingOrder>
            }
        }

        binding.rvFgrnPoselection.setUpAdapter(
            data,
            R.layout.item_vega_fgrn_po_details,
            ItemVegaFgrnPoDetailsBinding::inflate,
            { item, pos, bindingItem ->

                if (pos % 2 == 0) {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                } else {
                    this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                }

                bindingItem.tvPonoFgrn.text = item.processOrderNo
                val weightToProcess = weightToProcess(item.rminList, item.rfgrnList)
                val weight = DecimalFormat("#########.###").format(weightToProcess).toString()
                bindingItem.tvWeightFgrn.text =
                    weight.plus(" ").plus(item.rminList!![0].unitsOfMeasure.toString())
                val times = item.startDate?.split('(', ')')
                bindingItem.tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }
                bindingItem.tvBatchNo.text = item.rminList!![0].batchNumber
            }, {
                callBack?.replaceFragment(GRADESELECTION, this, "")
            })
    }
}
