package com.olam.warehouse.vegax.qualityofanylot.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.model.VegaBatchDetails
import com.olam.warehouse.master.common.model.VegaQualityNigeriaCocoaPostResponse
import com.olam.warehouse.master.common.model.VegaQualityNigeriaPost
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.VegaQuality
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualityofanylot.R
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.QualityDetails
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotListData
import com.olam.warehouse.vegax.qualityofanylot.data.domain.model.VegaAnyLotQualityPostRequest
import com.olam.warehouse.vegax.qualityofanylot.databinding.FragmentVegaAnyLotQualitySummaryBinding
import com.olam.warehouse.vegax.qualityofanylot.databinding.ItemVegaAnyQualityLotSummaryParamsBinding
import com.olam.warehouse.vegax.qualityofanylot.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Ramesh Rm on 11/08/2022.
 */
class VegaAnyQualitySummaryFragment : BaseFragment() {

    private var isSap: Boolean= false
    private var qualityParameterList = arrayListOf<VegaQualityParams?>()
    override val layoutResourceId = R.layout.fragment_vega_any_lot_quality_summary
    private lateinit var binding:FragmentVegaAnyLotQualitySummaryBinding
    private val vm: VegaAnyLotQualityViewModel by viewModel()
    private var postRequest = VegaAnyLotQualityPostRequest()
    var qualityDetails: List<QualityDetails> = emptyList()
    private var vegaBatchDetailsList = arrayListOf<VegaBatchDetails?>()
//    private var addMixtureValue: String=""
//    private var mouldValue: String=""
//    private var beanWtDiscGmValue: String=""
//    private var beanCountValue: String=""
//    private var slatyValue: String=""
//    private var moistureValue: String=""
     private var secretId:String?=""
    private var lotbatchNo: String? = ""
    private var typeSelect: String? = ""



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaAnyLotQualitySummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        //fun newInstance() = VegaAnyQualitySummaryFragment().putArgs {}

        fun newInstance(data: VegaAnyLotQualityPostRequest, typeSelect: String) =
            VegaAnyQualitySummaryFragment().putArgs {
                putParcelable(ANY_LOT_QUALITY_REQUEST, data)
                putString(TYPE_SELECT,typeSelect)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitynigeria/ui/VegaNigeriaQualitySummaryFragment")
            .title("Approve").with(tracker)
        initUI()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    private fun initUI() {
        typeSelect= arguments?.getString(TYPE_SELECT,"").toString()
        postRequest = arguments?.getParcelable(ANY_LOT_QUALITY_REQUEST)?: VegaAnyLotQualityPostRequest()
        secretId= postRequest.secretKey
        lotbatchNo= postRequest.batchNumber
        qualityDetails=  postRequest.qualityDetails
        binding.tvBatch.text= lotbatchNo

        calculateBatchCharacteristicQuality()

        vm.qualityBatch.observe(viewLifecycleOwner, Observer { updatequalityNigeriaUI(it) })

        vm.qualityPost.observe(viewLifecycleOwner, Observer { updateUIResponse(it) })

        clickEvent()
    }

    private fun clickEvent() {
        binding.btnParamsProceed.setOnClickListener {
            /*TODO after getting batch characteristics need to be add in Quality list during posting*/
            proceedToPost(isSAP = true)
        }
        binding.btnParamsSave.setOnClickListener {
            /*TODO after getting batch characteristics need to be add in Quality list during posting*/
            proceedToPost(isSAP = false)
        }

    }

    private fun updateUIResponse(response: Resource<GenericReqAndResp<List<VegaAnyLotListData>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            moveToSuccess()
                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }

    private fun moveToSuccess() {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        if (isSap){
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_post))

            if(secretId?.isNotEmpty() == true){
                intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.secret_id).plus(" : ").plus(secretId))
            }else intent.putExtra(AppUtils.SUB_TITLE, getString(R.string.batch_number).plus(" : ").plus(lotbatchNo))

        }
        else {
            intent.putExtra(AppUtils.TITLE, getString(R.string.success_save))
            if(secretId?.isNotEmpty() == true){
                intent.putExtra(
                    AppUtils.SUB_TITLE, getString(R.string.secret_id).plus(" : ").plus(secretId)
                )
            }
            intent.putExtra(
                AppUtils.SUB_TITLE, getString(R.string.trans_no).plus(" : ").plus(postRequest.transactionNumber)+"\n".
                plus(getString(R.string.batch_number)).plus(" : ").plus(lotbatchNo)
            )

        }
        intent.putExtra(AppUtils.PRINT_ENABLE, true)
        intent.putExtra(UIUtils.ANY_LOT_QUALITY_PRINT, true)
        intent.putParcelableArrayListExtra(UIUtils.ANY_LOT_QUALITY_LIST,vegaBatchDetailsList)
        startActivity(intent)
        requireActivity().finish()
    }




    private fun updatequalityNigeriaUI(response: Resource<GenericReqAndResp<VegaQualityNigeriaCocoaPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    //  when (it.data?.success) {
                    when (true) {
                        true -> {
                            var arrayListOfQuality= ArrayList(it.data?.data?.batchDetails!!)
                            vegaBatchDetailsList = arrayListOfQuality
                            binding.llBtn.visible()

//                            vegaBatchDetailsList?.forEach { it1 ->
//                                if (it1?.atnam.equals(B_MOULD4)) {
//                                    mouldValue = it1?.atwrt.toString()
//                                }
//                                if (it1?.atnam.equals(NG_ADMIX)) {
//                                    addMixtureValue = it1?.atwrt.toString()
//                                }
//                                if (it1?.atnam.equals(B_DCTBW1)) {
//                                    beanWtDiscGmValue = it1?.atwrt.toString()
//                                }
//                                if (it1?.atnam.equals(ZNGCOCOA_ACTBW)) {
//                                    beanCountValue = it1?.atwrt.toString()
//                                }
//                                if (it1?.atnam.equals(B_SL)) {
//                                    slatyValue = it1?.atwrt.toString()
//                                }
//                                if (it1?.atnam.equals(B_MOIST)) {
//                                    moistureValue = it1?.atwrt.toString()
//                                }
//                            }
                            setUpAdapter(vegaBatchDetailsList)


                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                        }
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    private fun calculateBatchCharacteristicQuality() {
        vm.postQualityParamsForBatchChar(
            VegaQualityNigeriaPost(
                contactNumber = "",
                driverName = "",
                grnApplicable = false,
                grnFlag = false,
                grnNumber = "",
                image = "",
                imageUploadMsg = "",
                key = getCurrentKey(),
                message = "",
                remarks = "",
                plant = getPlantDetails(),
                success = false,
                transportVendorCode = "",
                userName = "",
                vehicleNumber = "",
                vehicleType = "",
                wayBillNo = "",
                lotDetails = prepareLotList()
            )
        )

    }

    private fun prepareLotList(): ArrayList<VegaQualityWBDetails> {
         var lotList = arrayListOf<VegaQualityWBDetails>()
        var data= VegaQualityWBDetails()
        data.batchNumber= ""
        data.grnModel=""
        data.grossWeight= postRequest.weight
        data.materialCode = postRequest.materialCode
        data.materialName = postRequest.materialName
        data.netWeight= postRequest.weight
        data.plant= getPlantDetails().plantId
        data.procurementType=""
        data.storageLocationCode= postRequest.storageLocationCode.toString()
        data.supplierCode=  postRequest.supplierCode
        data.supplierName= postRequest.supplierName
        data.unitsOfMeasure= postRequest.uom
        data.qualityDetails= prepareVegaQualityList(qualityDetails)
        lotList.add(data)
        return lotList
    }

    private fun prepareVegaQualityListBatch(qualityDetails: ArrayList<VegaBatchDetails?>):List<QualityDetails> {
        val qualityList = arrayListOf<QualityDetails>()
        var qualityParams = qualityDetails.filterNot {
            it?.atnam.equals("ZNGCOCOA_DIS_MOULD") || it?.atnam.equals("ZNGCOCOA_DIS_BW") ||
                    it?.atnam.equals("ZNGCOCOA_DIS_ON_OTHERS") || it?.atnam.equals("ZNGCOCOA_DIS_BS")
        } as ArrayList<VegaBatchDetails?>
        qualityParams.forEach { data->
            val quality = QualityDetails()
            quality.descrChar = data?.desc
            quality.nameChar = data?.atnam.toString()
            quality.qualityParameterValue = data?.atwrt
            qualityList.add(quality)
        }
        return qualityList
    }

    private fun prepareVegaQualityList(qualityDetails: List<QualityDetails>):List<VegaQuality> {
        val qualityList = arrayListOf<VegaQuality>()
        qualityDetails.forEach { data->
            val quality = VegaQuality()
            quality.descrChar = data.descrChar
            quality.nameChar = data.nameChar.toString()
            quality.qualityParameterValue = data.qualityParameterValue
            qualityList.add(quality)
        }
        return qualityList
    }

    private fun proceedToPost(isSAP:Boolean) {
        isSap= isSAP
        postRequest.isSap=isSAP
        val msg = if (isSAP) getString(R.string.accept_quality) else getString(R.string.save_quality)

        postRequest.qualityDetails= prepareVegaQualityListBatch(vegaBatchDetailsList)
        showConfirmDialog(msg)
    }

    private fun showConfirmDialog( msg: String) {
        MaterialDialog(requireContext()).show {
            title(text = msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.postOrSaveQualityDetails(postRequest)
                },
                { dismiss() })
        }
    }

    private fun setUpAdapter(data: List<VegaBatchDetails?>) {
        var qualityParams = data.filterNot {
            it?.atnam.equals("ZNGCOCOA_DIS_MOULD") || it?.atnam.equals("ZNGCOCOA_DIS_BW") ||
                    it?.atnam.equals("ZNGCOCOA_DIS_ON_OTHERS") || it?.atnam.equals("ZNGCOCOA_DIS_BS")
        } as ArrayList<VegaBatchDetails?>
        qualityParams.let {
            var i = 0
            binding.rvApproveQuality.setUpAdapter(
                qualityParams,
                R.layout.item_vega_any_quality_lot_summary_params,
                ItemVegaAnyQualityLotSummaryParamsBinding::inflate,
                { item, pos, bindItem ->
                    i++
                    if (i % 2 == 0) {
                        activity?.let { it1 -> this.setBackgroundColor(it1.getColor(com.olam.warehouse.presentation.R.color.grey_lit)) }
                    } else {
                        activity?.let { it1 -> this.setBackgroundColor(it1.getColor(com.olam.warehouse.presentation.R.color.white)) }
                    }

                    bindItem.tvQualityNameApprove.text = item?.desc

                    bindItem.tvUnitApprove.text = item?.atwtb?.replace(",",".")

                },
                {

                })
        }
    }





}
