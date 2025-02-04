package com.olam.warehouse.vegax.processing.ui.rmin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.master.common.data.domain.model.VegaProcessingRminPo
import com.olam.warehouse.master.vega.entity.VegaDispatchLots
import com.olam.warehouse.master.vega.entity.VegaProcessingRminBoms
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processing.R
import com.olam.warehouse.vegax.processing.databinding.FragmentVegaRminSummaryBinding
import com.olam.warehouse.vegax.processing.ui.VegaProcessingViewModel
import com.olam.warehouse.vegax.processing.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaRminSummaryFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_rmin_summary
    private lateinit var binding: FragmentVegaRminSummaryBinding
    private var materialNo: String? = ""
    private var materialName: String? = ""
    private var bom: VegaProcessingRminBoms? = null
    private var whLotsList: VegaDispatchLots? = null
    private val vm: VegaProcessingViewModel by viewModel()
    private var stageFevor: String? = ""
    private var cfgNumber: String? = ""

    companion object {
        fun newInstance(
            stageFevor: String,
            cfgNumber: String,
            materialName: String,
            materialNo: String,
            bom: VegaProcessingRminBoms,
            lotDetails: VegaDispatchLots
        ) = VegaRminSummaryFragment().putArgs {
            putString(MATERIAL_NAME, materialName)
            putString(MATERIAL_NUMBER, materialNo)
            putString(STAGEFEVOR, stageFevor)
            putString(CFG_NO, cfgNumber)
            putParcelable(BOM_ITEM, bom)
            putParcelable(LOT_TO_PROCESS, lotDetails)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaRminSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processing/ui/rmin/VegaRminSummaryFragment").title("Processing").with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvType, it, false)
            getActionBtnChangedView(binding.btnConfim, it, true)
        }
        bom = arguments?.getParcelable(BOM_ITEM)!!
        materialNo = arguments?.getString(MATERIAL_NUMBER)!!
        materialName = arguments?.getString(MATERIAL_NAME)!!
        whLotsList = arguments?.getParcelable(LOT_TO_PROCESS)!!
        stageFevor = arguments?.getString(STAGEFEVOR)!!
        cfgNumber = arguments?.getString(CFG_NO)!!
        vm.createPo.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.tvDate.text = DateUtils.fromMillisToTimeString(DateUtils.getCurrentTimeInMills())
        binding.tvBatchNo.text = whLotsList!!.batchNumber.toString()
        binding.tvMaterialName.text = whLotsList!!.materialName.toString()
        binding.tvBomNo.text = bom!!.cfgno.toString()
        binding.tvWeight.text = whLotsList!!.weight!!.plus(whLotsList!!.unitOfMeasure)


        binding.btnConfim.setOnClickListener {
            showConfirmDialog()
        }
    }


    private fun updateUI(response: Resource<GenericReqAndResp<VegaProcessingRminPo>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            val success = it.data?.data
                            if (!success?.autoPoResponse.isNullOrEmpty())
                                saveData(success?.autoPoResponse?.get(0)?.messages?.get(0)?.message.toString(), true)
                            else
                                saveData(it.data?.message.toString(), true)
                            moveToSuccessPage(success)
                        }
                        else -> {
                            saveData(it.data?.message.toString(), false)
                            UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun saveData(msg: String, syncStatus: Boolean) {
        vm.updateRminData(whLotsList?.batchNumber, msg, syncStatus)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_rmin)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(R.string.cancel),
                {
                    val poRequest = prepareRminCreatePoRequest(
                        stageFevor!!,
                        cfgNumber!!,
                        materialName!!,
                        materialNo!!,
                        bom!!,
                        whLotsList!!
                    )
                    vm.createPoRequest(poRequest)
                },
                { dismiss() })
        }
    }


    private fun moveToSuccessPage(processRmin: VegaProcessingRminPo?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString(R.string.rmin_saved))
        intent.putExtra(AppUtils.SUB_TITLE, processRmin?.autoPoResponse?.get(0)?.messages?.get(0)?.message)
        startActivity(intent)
        requireActivity().finish()
    }

}
