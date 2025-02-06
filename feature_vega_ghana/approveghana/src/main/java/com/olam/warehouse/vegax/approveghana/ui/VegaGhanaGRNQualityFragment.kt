package com.olam.warehouse.vegax.approveghana.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentWorkflowDetails
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParameter
import com.olam.warehouse.master.vega.entity.WorkflowFields
import com.olam.warehouse.master.vegaecuador.model.VegaEcuadorGrnResponse
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.approveghana.R
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.GhanaGRNQualityDetails
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQuality
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGRNGhanaQualityParams
import com.olam.warehouse.vegax.approveghana.data.domain.usecase.model.VegaGhanaGrnPost
import com.olam.warehouse.vegax.approveghana.databinding.FragmentVegaGhanaGrnQualityBinding
import com.olam.warehouse.vegax.approveghana.databinding.ItemVegaGhanaGrnQualityDetailsBinding
import com.olam.warehouse.vegax.approveghana.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaGhanaGRNQualityFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_ghana_grn_quality
    private lateinit var binding: FragmentVegaGhanaGrnQualityBinding
    private val vm: VegaGhanaGrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var weighBridgeLots = VegaGrnWeighBridgeId()
    private var approveQuality = mutableListOf<VegaGRNGhanaQuality>()
    private var admixtureValue: String = ""
    private var unitHead: String = ""
    private var qualityList = mutableListOf<VegaGRNGhanaQualityParams?>()
    private var workFlowData: WorkflowFields? = null



    interface CallBack {
        fun replaceQualityFragment(moveFrag: String, item: VegaGrnWeighBridgeId)
    }



    companion object {
        fun newInstance(grnData: VegaGrnWeighBridgeId,qualityData: ArrayList<VegaGRNGhanaQuality>, admixtureValue: String,unitHeadValue: String) =
            VegaGhanaGRNQualityFragment().putArgs {
            putParcelable(GRN_DATA, grnData)
                putString(GRN_ADMIXTURE_VALUE,admixtureValue)
                putParcelableArrayList(QUALITY_DATA,qualityData)
                putString(GRN_UNIT_HEAD,unitHeadValue)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaGhanaGrnQualityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/VegaNigeriaSesameGRNQualityFragment").title("Inventory Cocoa")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        workFlowData = getCurrentWorkflowDetails(getPlantDetails().plantId, "11")
        approveQuality =
            arguments?.getParcelableArrayList<VegaGRNGhanaQuality>(QUALITY_DATA) as MutableList<VegaGRNGhanaQuality>
        weighBridgeLots = arguments?.get(GRN_DATA) as VegaGrnWeighBridgeId

        approveQuality.let {
            qualityList.clear()
            approveQuality.forEach {
                qualityList.addAll(it.qualityParameters)
            }

        }

        vm.getQualityDetailsDB(weighBridgeLots.materialCode.toString(),null)
        vm.qualityDetailsDB.observe(viewLifecycleOwner,{
            updateApproveQuality(it)

        })

        admixtureValue= arguments?.getString(GRN_ADMIXTURE_VALUE).toString()
        unitHead= arguments?.getString(GRN_UNIT_HEAD).toString()
        binding.tvOk.setOnClickListener { activity?.onBackPressed() }
        binding.tvLotNo.text = weighBridgeLots.batchNumber


        binding.btnAccept.setOnClickListener {
            showConfirmDialog()
        }

        binding.btnReject.setOnClickListener {
            showRejectDialog()
        }

        vm.grn.observe(viewLifecycleOwner,  {
            updateGrnUI(it)
        })

    }

    private fun updateGrnUI(data: Resource<GenericReqAndResp<VegaEcuadorGrnResponse>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.data?.grnNumber.isNullOrEmpty())
                      moveToQualityRejectPage(getString(R.string.quality_rejected),it.data?.data?.weighBridgeId.toString(),weighBridgeLots.batchNumber)
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }


    private fun showConfirmDialog(){
        MaterialDialog(requireContext()).show {
            message(R.string.accept_msg_lot)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    callBack?.replaceQualityFragment(GRN_FRAG, weighBridgeLots)
                },
                { dismiss() })
        }
    }






    private fun setUpAdapter(data: MutableList<VegaGRNGhanaQuality>) {
        data.let {
            approveQuality = it


            var i = 0
            if(approveQuality.isNotEmpty()) {
                binding.rvQuality.setUpAdapter(
                    approveQuality.first().qualityParameters as MutableList<VegaGRNGhanaQualityParams>,
                    R.layout.item_vega_ghana_grn_quality_details,
                    ItemVegaGhanaGrnQualityDetailsBinding::inflate,
                    { it, pos, bindItem ->
                        i++
                        if (i % 2 == 0) {
                            this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey_lit))
                        } else {
                            this.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.white))
                        }
                        if (it.sapQCName != "ZNG_GR_DATE") {
                            bindItem.tvQualityGRNNameApprove.text = it.descChar
                            //it.sapQCName?.removePrefix("GH_")
                            bindItem.tvQualityValue.text = it.satNam
                        }
                    },
                    {

                    })
            }
        }
    }

    private fun updateApproveQuality(dbQualityList: List<VegaQualityParameter>) {
        dbQualityList.forEach { item ->
            qualityList.forEach {
                if (item.nameChar == it?.sapQCName) {
                    it.descChar = item.descrChar
                }
                println(it?.descChar)
            }
        }
        setUpAdapter(approveQuality)
    }


    private fun showRejectDialog() {

        MaterialDialog(requireContext()).show {
            message(R.string.reject_msg_lot)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    var qualityDetails: ArrayList<GhanaGRNQualityDetails> = ArrayList()
                    approveQuality.forEach {
                        it.qualityParameters.forEach {
                            var quality = GhanaGRNQualityDetails()
                            if (it.sapQCName == "GH_CASHEW_AVG_BAG_WT") {
                                quality.nameChar = it.sapQCName
                                quality.descrChar = it.descChar
                                quality.qualityParameterValue = it.satNam!!.removeSuffix(" MT")
                                qualityDetails.add(quality)
                            } else {
                                quality.nameChar = it.sapQCName
                                quality.descrChar = it.descChar
                                quality.qualityParameterValue = it.satNam
                                qualityDetails.add(quality)
                            }
                        }
                    }
                    weighBridgeLots.finalApproval = FNREJECT
//                wbDetails.storageLocationCode = binding.tvStorageLocation.text.toString()
                    val wbData = preparePostGrnData(weighBridgeLots, admixtureValue, qualityDetails,unitHead)
                    if (AppUtils.isOnline()) {
                        vm.postGrn(
                            VegaGhanaGrnPost(
                                key = getCurrentKey(),
                                plant = getPlantDetails(),
                                grnData = listOf(wbData),
                                qualityDetails = qualityDetails,
                                notificationFlag = workFlowData?.NotificationFlag?.trim().equals("0"),
                                nextWorkFlowRole = workFlowData?.workflowModule,
                                navId = workFlowData?.workflowId,
                                currentWorkFlowRole = workFlowData?.module)
                            )
                    } else {
                        dismiss()
                    }
                },
                { dismiss() })
        }
    }

    private fun moveToQualityRejectPage(message:String,wbId: String,lotId:String?){
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, message)
        intent.putExtra(AppUtils.SUB_TITLE,"WB ID : ".plus(wbId).plus("\n").plus("LOT ID : ${lotId}"))
        startActivity(intent)
        requireActivity().finish()

    }
}
