package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vega.entity.*
import com.olam.warehouse.master.vega.model.VegaQualityParamsWithQualitative
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.model.VegaNicaraguaFgrnQuality
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeLot
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.databinding.DialogStartLoadingLayoutBinding
import com.olam.warehouse.presentation.databinding.ItemSingleSelectBinding
import com.olam.warehouse.presentation.enums.ConfigItems
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.data.domain.model.VegaQualityBatchAndTicket
import com.olam.warehouse.vegax.processingindiacoffee.databinding.DialogItemBatchListBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaNicaraguaQualityParamsBinding
import com.olam.warehouse.vegax.processingindiacoffee.databinding.ItemLotBatchBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraCoffeeQualityFragment : BaseFragment(){
    //VegaNicaraguaCoffeeQualityAdapter.ClickListener{

   // private lateinit var dialogView: DialogItemBatchListBinding
    private var qualityParameterList = arrayListOf<VegaQualityParameter?>()
    private var mAdapter = VegaNicaraguaCoffeeQualityAdapter { enableProceedBtn(it) }
    private var preQualityList = mutableListOf<VegaQualityParams>()
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var lotItems = arrayListOf<VegaCoffeeLot>()
    private var qualityPostList = arrayListOf<VegaCoffeeLot>()
    private var qualitySupplierPostList = arrayListOf<VegaQualityWBDetails>()
   // private var qualityBatchTicketList = mutableListOf<VegaQualityBatchAndTicket>()
    val value = mutableListOf<VegaQualityParamsWithQualitative>()
    //var tallySheet: String? = ""
    var tallySequence: String? = ""
    private var model = VegaCocoaFgrnItems()
    private var fgrnId: String = ""
    var material_code = ""
    private var batchNo1: String? = ""
    private var isData: Boolean? = false
    private var materialNo: String? = ""
    private var netWeight: String? = ""
    private var tarWeight: String? = ""
    private var challanNo: String? = ""
    private var callBack: VegaNicaraCoffeeQualityFragment.CallBack? = null
    private var qualityParameterNicList = arrayListOf<VegaNicaraguaFgrnQuality>()
    private val plantId = getPlantDetails().plantId
    private var materialcode = ""
    private var isMillingPlant=false
    private var isDryingPlant=false
    private var clickedBatchNumber=""



    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems, id: String) =
            VegaNicaraCoffeeQualityFragment().putArgs {
                putParcelable(FRAG_LOTS, model)
                putString(FRAG_ID, id)
            }
    }

    private lateinit var binding: FragmentVegaNicaraguaQualityParamsBinding
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_quality_params

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaNicaraguaQualityParamsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("quality/ui/params/VegaNicaraCoffeeQualityFragment")
            .title("Quality")
            .with(tracker)
        initUI()

     /*   vm.getLotQuality.observe(viewLifecycleOwner, Observer {
            updatePreQuality(true,clickedBatchNumber,it)
        })*/

    }

    private fun initUI() {
        model = arguments?.getParcelable(FRAG_LOTS) ?: VegaCocoaFgrnItems()
        fgrnId = arguments?.getString(FRAG_ID) ?: ""
        binding.rvBagDetail.layoutManager = LinearLayoutManager(this.context)
        binding.rvBagDetail.adapter = mAdapter
        binding.etBatchNo.filters = arrayOf<InputFilter?>(LengthFilter(11))
        binding.etBatchNo.setText(batch_no, TextView.BufferType.EDITABLE)
        binding.btnParamsProceed.setOnClickListener { proceeToPost() }

        vm.getConfigItems(UserRoles.PROCESSING.role)
        vm.configItems.observe(viewLifecycleOwner, Observer { updateConfigItems(it) })
    }

    private fun updateConfigItems(configItems: List<VegaConfigDetails>) {
        val dryingPlants = configItems.filter { it.process.equals(ConfigItems.DRYING_PLANT.item) }
        if(!dryingPlants.isNullOrEmpty()){
            isDryingPlant= true
        }

        val millingPlants = configItems.filter { it.process.equals(ConfigItems.MILLING_PLANT.item) }

        if (!millingPlants.isNullOrEmpty()) {
            isMillingPlant = true
        }
        if(isDryingPlant) {
            model.rminList?.forEach {
                tallysheet = it.batchNumber.toString()
                /*materialNo =
                if (!it.materialCode?.length?.equals(18)!!) "000000".plus(it.materialCode) else it.materialCode*/
            }
        }

        if(isMillingPlant){
            tallysheet= vm.generateFGRNTicketNumber()
        }

        if(!isMillingPlant)
            vm.getPreSamplingQualitydata(batch_no.trim(), materialNo?.trim()!!)

        initExtra()
    }


    private fun initExtra() {

        materialNo =
            if (!material_no.length.equals(18)) "000000".plus(material_no) else material_no
        vm.materialQualityGrades.observe(viewLifecycleOwner, Observer {
            materialQualityGradeList = it.toMutableList()
        })
        vm.getMaterialQualityGrades(materialNo.toString())
        vm.preSamplingQuality.observe(viewLifecycleOwner, Observer { updatePreQuality(it) })

        //vm.getPreSamplingQualitydata(batch_no.trim(), model.materialCode?.trim()!!)
        vm.qualitylist.observe(viewLifecycleOwner, Observer { updateUI(it) })

        if(isMillingPlant)
            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun proceeToPost() {
        var isValueNeed = true
        var total = 0.0
        batchNo1 = binding.etBatchNo.text.toString()
        qualityParameterList.clear()
        qualityParameterNicList.clear()
        val missedPos = mutableListOf<Int>()
        val invalidDataPos = mutableListOf<Int>()
        var data = arrayListOf<VegaQualityParamsWithQualitative?>()
        data = mAdapter.getItems()
        mAdapter.getItems().forEachIndexed { index, itValue ->
            var qualityData = VegaNicaraguaFgrnQuality()
            qualityData.materialCode = materialNo.toString()
            qualityData.plantId = getPlantDetails().plantId
            qualityData.storageLocationCode = storageLocation
            qualityData.nameCharValue = itValue?.qualityParameter?.nameChar.toString()
            qualityData.descrCharValue = itValue?.qualityParameter?.descrChar.toString()
            qualityData.qualityParameterValue =
                itValue?.qualityParameter?.qualityParameterValue.toString()
            qualityData.batchNumber = batch_no

            if ((itValue?.qualityParameter?.entryObligatory.equals("X")) || (itValue?.qualityParameter?.vegaMandatory.equals(
                    "X"
                )) || (!itValue?.qualityParameter?.preSampling.equals(""))
            ) {
                if (itValue?.qualityParameter?.qualityParameterValue.isNullOrEmpty()) {
                    if (getCurrentKey().split("_")[1].contains("NI")) {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIDANO")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014")
                            && itValue?.qualityParameter?.qualityParameterValue.equals("Select")
                            && model.stageFevor!!.isNotEmpty() && model.stageFevor!!.contains("Certificado",true)) {
                            itValue?.qualityParameter?.mandatory = 1
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFG0014") &&
                            !model.stageFevor!!.contains("Certificado",true)) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else if (itValue?.qualityParameter?.nameChar.equals("NIFGICO")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    } else {
                        if (itValue?.qualityParameter?.formulaParam.equals("X")) {
                            itValue?.qualityParameter?.mandatory = 0
                        } else {
                            isValueNeed = false
                            missedPos.add(index)
                            itValue?.qualityParameter?.mandatory = 1
                        }
                    }
                } else {
                    if (itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals(
                            "NICASCAB"
                        )
                        || itValue?.qualityParameter?.nameChar.equals("NIDESA") || itValue?.qualityParameter?.nameChar.equals(
                            "NIDESC"
                        )
                        || itValue?.qualityParameter?.nameChar.equals("NIDESD")
                    ) {
                        itValue?.qualityParameter?.qualityParameterValue = itValue?.qualityParameter?.qualityParameterValue?.replace("%","")
                        total += itValue?.qualityParameter?.qualityParameterValue?.trim()?.toDouble()!!
                    }

                    itValue?.qualityParameter?.mandatory = 0
                }

                qualityParameterList.add(itValue?.qualityParameter)
            }
            qualityParameterNicList.add(qualityData)
        }
        if (isValueNeed) {
            paramsNicaraguaList = qualityParameterNicList
            var sum = total.toInt()
            if(sum!=100) {
                mAdapter.getItems().forEachIndexed { index, itValue ->
                    if(itValue?.qualityParameter?.nameChar.equals("NIEXPOP") || itValue?.qualityParameter?.nameChar.equals("NICASCAB")
                        || itValue?.qualityParameter?.nameChar.equals("NIDESA") || itValue?.qualityParameter?.nameChar.equals("NIDESC")
                        || itValue?.qualityParameter?.nameChar.equals("NIDESD")) {
                        itValue?.qualityParameter?.mandatory = 1
                        invalidDataPos.add(index)
                    }
                }
                showSnack(requireContext().resources.getString(R.string.ente_fields_equal))
                mAdapter.updateMissedPos(invalidDataPos, data)
            } else
            callBack?.replaceFgrnFragment(FRAG_SIFFT, model, "")
        } else {
            showSnack(requireContext().resources.getString(R.string.enter_all_mandatory_fields))
            mAdapter.updateMissedPos(missedPos, data)
        }
    }


    private fun updateUI(params: List<VegaQualityParamsWithQualitative>?) {
        params?.let {
            when (it.isNotEmpty()) {
                true -> {
                   // val value = mutableListOf<VegaQualityParamsWithQualitative>()
                    if (!isData!!) {
                        it.forEach { item ->
                            if (item.qualityParameter.vegaMandatory.equals("X") || item.qualityParameter.entryObligatory.equals(
                                    "X"
                                ) || (!item.qualityParameter.preSampling.equals("") && item.qualityParameter.qualityParameterValue?.isNotEmpty()!!)
                            ) {
                                value.add(item)
                            }
                            preQualityList.forEach { item1 ->
                                if (item.qualityParameter.nameChar == item1.sapQCName) {
                                    if(item.qualityParameter.nameChar=="NIFG0014" || item.qualityParameter.nameChar=="NIPOSITI"){
                                        item.qualityParameter.qualityParameterValue = item1.satNam!!
                                    }else item.qualityParameter.qualityParameterValue = item1.satNam!!.split(" ")[0]
                                    value.add(item)
                                }
                            }

                        }
                    } else {
                        value.addAll(it)
                    }

                    if (getCurrentKey().split("_")[1].contains("NI"))
                        mAdapter.addItems(
                            sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                            "",
                            "",
                            model.stageFevor!!
                        )
                        else
                    mAdapter.addItems(
                        sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                        "",
                        "",
                        ""
                    )

//                    if(getCurrentKey().contains("VEGA_NI")){
//                        if(isMillingPlant) {
//                            showDialogForLots()
//                        }
//                    }
                }
                else -> setErrorContentView("Quality params not available for this material")
            }
        }
    }



    private fun updatePreQuality(response: Resource<GenericReqAndResp<List<VegaQualityPreParameter>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    when (it.data?.success) {
                        true -> {

                            preQualityList =
                                it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                            materialNo?.let { vm.getQualityParams(materialNo!!, false, "") }

                           /*if(isViewTicket){
                               val qualityList= it.data?.data?.get(0)?.qualityParameters as MutableList<VegaQualityParams>
                               if(qualityList.isNotEmpty()){
                                   if(qualityList.filter { it.sapQCName == "NICERTI" }.isNotEmpty()) {
                                       tallysheet = qualityList.filter { it.sapQCName == "NICERTI" }.get(0).satNam.toString()
                                       qualityBatchTicketList.filter { it.batchNumber == batchNumber }.get(0).ticket= tallysheet

                                   }else{
                                       tallysheet =""
                                       qualityBatchTicketList.filter { it.batchNumber == batchNumber }.get(0).ticket= getString(R.string.ticket_not_available)
                                   }
                               } else  qualityBatchTicketList.filter { it.batchNumber == batchNumber }.get(0).ticket= getString(R.string.ticket_not_available)

                               dialogView.rv.adapter?.notifyDataSetChanged()

                           }*/

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

    private fun enableProceedBtn(item: List<VegaQualityParamsWithQualitative?>) {
        var isEnable = false
        item.forEach {
            it?.qualityParameter?.qualityParameterValue?.let { it1 ->
                if (it1.isNotEmpty()) isEnable = true
            }
        }
        if (isEnable) {
            binding.btnParamsProceed.isEnabled = true
            binding.btnParamsProceed.setBackgroundColor(
                getColor(
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
        } else {
            binding.btnParamsProceed.isEnabled = false
            binding.btnParamsProceed.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.grey))
        }
    }

  /*  fun showDialogForLots() {
        qualityBatchTicketList.clear()
        dialogView = DialogItemBatchListBinding.inflate(layoutInflater)
        val alertBuilder = AlertDialog.Builder(requireActivity()).setView(dialogView.root)
        val alertDialog = alertBuilder.create()

        val list= model.rminList?.map { it.batchNumber }?.distinct()?.toMutableList() as MutableList<String>
        list.forEach {
            val data= VegaQualityBatchAndTicket()
            data.batchNumber= it
            data.ticket=""
            qualityBatchTicketList.add(data)
        }

        dialogView.rv.setUpAdapter(qualityBatchTicketList,R.layout.item_lot_batch,
            ItemLotBatchBinding::inflate,{data, pos , bindingItem ->
               // bindingItem.pb.gone()
                bindingItem.tvViewTicket.visible()

                bindingItem.tvLot.text= data.batchNumber
                bindingItem.tvTicketNo.text = data.ticket
                bindingItem.rbLot.isChecked = data.isSelected

                bindingItem.tvViewTicket.setOnClickListener {
                    clickedBatchNumber= data.batchNumber
                 bindingItem.tvViewTicket.gone()
                    //bindingItem.pb.visible()
                    vm.getLotQualityData(data.batchNumber, model.rminList?.get(pos)?.materialCode.toString())

                }
                bindingItem.clItem.setOnClickListener {
                    qualityBatchTicketList.forEach { it.isSelected=false }
                    qualityBatchTicketList[pos].isSelected = !it.isSelected
                    dialogView.rv.adapter?.notifyDataSetChanged()
                }

                dialogView.btConform.setOnClickListener {
                    val list = qualityBatchTicketList.filter { it.isSelected }

                    if(list.isNotEmpty()) {
                        val ticket= list.get(0).ticket
                        if(ticket.isNotEmpty() && !ticket.equals(getString(R.string.ticket_not_available))) {
                            value.forEach {
                                if(it.qualityParameter.nameChar=="NICERTI"){
                                    val qualityList= VegaQualitative(nameChar = "NICERTI", descValue = "ticket")
                                    it.qualitative= listOf(qualityList)
                                }
                            }
                            mAdapter.addItems(
                                sortByListOfItems(value.distinctBy { it.qualityParameter.nameChar }),
                                "",
                                "",
                                model.stageFevor!!, this@VegaNicaraCoffeeQualityFragment
                            )
                            alertDialog.dismiss()
                        }else Toast.makeText(requireContext(), "Please View Ticket and Proceed", Toast.LENGTH_SHORT).show()

                    }else Toast.makeText(requireContext(), "Please select batch", Toast.LENGTH_SHORT).show()

                }


            })

        alertDialog.setCanceledOnTouchOutside(false)

        dialogView.btCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        ViewCompat.setBackgroundTintList(
            dialogView.btCancel,
            ContextCompat.getColorStateList(
                activity?.applicationContext!!,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi
            )
        )
        alertDialog.show()
    }
*/
//    override fun onClickBatchDialog() {
//        showDialogForLots()
//    }


}
