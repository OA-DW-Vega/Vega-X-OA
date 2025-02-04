package com.olam.warehouse.vegax.gateentryapprovalnigeria.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import com.olam.warehouse.login.databinding.ItemPrintLotCardPreviewBinding
import com.olam.warehouse.login.utils.bitmapToString
import com.olam.warehouse.login.utils.getBitmap
import com.olam.warehouse.login.utils.getBitmapFromView
import com.olam.warehouse.master.common.model.VegaGateEntryApprovalPostResponse
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.master.vega.entity.VegaGateEntryDetails
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.wifiprinter.WifiMainActivity
import com.olam.warehouse.presentation.utils.*
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.gateentryapprovalnigeria.R
import com.olam.warehouse.vegax.gateentryapprovalnigeria.databinding.FragmentVegaApprovalNigeriaWaitingTruckListBinding
import com.olam.warehouse.vegax.gateentryapprovalnigeria.utils.*
import kotlinx.android.synthetic.main.item_vega_approval_nigeria_waiting_truck_list.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Keerthi Santhanam on 3/4/2020.
 */
class VegaApprovalNigeriaWaitingTruckListFragment : BaseFragment() {
    private lateinit var binding: FragmentVegaApprovalNigeriaWaitingTruckListBinding
    private var callBack: CallBack? = null
    private var gateEntryData = VegaGateEntryDetails()
    private var gateEntry = mutableListOf<VegaGateEntryDetails>()
    private val mSearchList = mutableListOf<VegaGateEntryDetails>()

    override val layoutResourceId = R.layout.fragment_vega_approval_nigeria_waiting_truck_list
    private val vm: VegaGateEntryApprovalNigeriaViewModel by viewModel()
    private var vegaWbIds = listOf<VegaGateEntryDetails>()
    private var tallyPrintKeys = ArrayList<String>()
    private var lotList = ArrayList<VegaCoffeeSalesLots>()
    private var selectedPlantId = ""
    private var plantList = mutableListOf<Plant>()


    interface CallBack {
        fun replaceFragment(paramsListFrag: String, item: VegaGateEntryDetails, plantDetails: Plant)
    }

    companion object {
        fun newInstance(gateEntryData: VegaGateEntryDetails) =
            VegaApprovalNigeriaWaitingTruckListFragment().putArgs {
                putParcelable(GATE_ENTRY_DATA, gateEntryData)
            }

        const val SEARCH_HINT_TEXT = "Search WB Item"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaApprovalNigeriaWaitingTruckListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        val wType = gateEntryData.wtype
        TrackHelper.track().screen("gateentryApprovalNigeria/ui/VegaApprovalNigeriaWaitingTruckListFragment - $wType").title("Vega_ApprovalNigeria/Gate Entry")
            .with(tracker)
    }

    private fun initUI() {
        gateEntryData = arguments?.getParcelable(GATE_ENTRY_DATA)!!
        binding.llWeighbridge.gone()
        //vm.waitingTrucks1.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })

        vm.gateEntryTruck.observe(viewLifecycleOwner, Observer { updateUIWithOnlineData(it) })
//        binding.tvType.text =
//            getString(R.string.secret_sample_code_header).plus(" ")
//                .plus(if (gateEntryData.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR)
//        binding.tvType.text =
//            getString(R.string.waiting_truck_header).plus(" ")
//                .plus(if (gateEntryData.weighBridgeType.equals(PROCURE)) SUPPLIER else MTNR)

        binding.tvType.text =
            getString(R.string.gate_entry_waiting_list)


        //binding.llAddNewTruck.setOnClickListener { callBack?.replaceFragment(PARAMS_LIST_FRAG, gateEntryData) }

        binding.ivSortDownUp.setOnClickListener {
            if (gateEntry.isNotEmpty()) {
                val data = gateEntry
                gateEntry = data.asReversed()
                setUpAdapter(gateEntry)
            }
        }
        binding.btnGo.setOnClickListener {
            if (selectedPlantId.isNotEmpty()) {
                if (isOnline()) vm.fetchWbDetails(selectedPlantId)
            } else
                Toast.makeText(context, "Select Plant to continue", Toast.LENGTH_SHORT).show()
        }

        plantList = getMultiPlantList() as MutableList<Plant>
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)

        //Multi Plant selection
        //Should be moved to observer of plant list
        /* vm.multiPlant.observe(viewLifecycleOwner, Observer {
             plantList = it.toMutableList()
             var ids = plantList.map { it.plantId }
             updatePlantListUI(ids as ArrayList<String>)

         })
         vm.getMultiPlantList()*/

        /*var plantList = arrayListOf<Plant>()
        plantList.add(Plant("2741", "", "", "", "", CountryDetail("", 0, "")))
        var ids = plantList.map { it.plantId }
        updatePlantListUI(ids as ArrayList<String>)*/

    }

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        var plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(requireContext(), R.layout.item_vega_approval_nigeria_gate_entry_plant_select, plantIdList)
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (position > 0) {
                    selectedPlantId = plantIdList[position]
//                    validateLot(selectedPlantId)
                }
//                binding.spPlantSelection.setSelection(0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(gateEntry)
                        } else {
                            mSearchList.clear()
                            gateEntry.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.wbid.toString().contains(text)) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setUpAdapter(data: List<VegaGateEntryDetails>) {
        val gateEntry1 = data as MutableList<VegaGateEntryDetails>
        binding.tvTruckTotal.text =
            getString(R.string.truck_count).plus(" ").plus(gateEntry1.size.toString())
        binding.rvWeighbridge.setUp(
            gateEntry1.asReversed(),
            R.layout.item_vega_approval_nigeria_waiting_truck_list,
            { it, pos ->
                tvTruckNo.text = if (it.truckNumber.isNullOrEmpty()) "-" else it.truckNumber
                if (it.wtype == PROCURE) {
                    tvdifference.text = SUPPLIER
                    tvSupplierName.text = it.vendorCode.vendorName ?: it.vendorCode.vendorCode
                } else {
                    tvdifference.visibility = View.GONE
                    tvSupplierName.visibility = View.GONE
                    tvdifference.text = WAREHOUSE
                    tvSupplierName.text = "-"
                }
                tvWeighbridgeIdValue.text = it.wbid
                tvWeighBridgeId.text = it.materialCode.materialName
                val times = it.updatedBy.toString().split('(', ')')
                tvDate.text = it.updatedAt.toString().split("T").get(0)
            },
            {
                val item = this
                //item.wbid = gateEntryData.wtype
                var plantDetails = getPlantDetails(item.plant.plantId).single()
                callBack?.replaceFragment(SUMMARY_FRAG, item, plantDetails)
            })
    }

    private fun getPlantDetails(plantId: String?): List<Plant> {
        return plantList.filter { it.plantId == plantId }
    }

    private fun createLotCardBitMap(lotList: ArrayList<VegaCoffeeSalesLots>) {
        tallyPrintKeys.clear()
        showCustomLoading()
        DoAsync {
            lotList.forEachIndexed { index, item ->
                val view = LayoutInflater.from(activity)
                    .inflate(com.olam.warehouse.login.R.layout.item_print_lot_card_preview, null)
                val viewBinder = ItemPrintLotCardPreviewBinding.bind(view)
                if (true) {
                    viewBinder.tvLot.text = "Sample ID"
                }
                viewBinder.ivPreview.setImageBitmap(getBitmap(item.batchNumber))
                viewBinder.tvLotValue.text = item.batchNumber
                viewBinder.tvMaterialValue.text = item.materialName
                if (item.grade?.isNotEmpty() == true) {
                    viewBinder.tvGradeValue.text = item.grade
                    viewBinder.tvGrade.visible()
                    viewBinder.tvGradeValue.visible()
                }
                if (item.certificate?.isNotEmpty() == true) {
                    viewBinder.tvCertificateValue.text = item.certificate
                    viewBinder.tvCertificate.visible()
                    viewBinder.tvCertificateValue.visible()
                }
                viewBinder.tvWeightValue.visibility = View.GONE
                viewBinder.tvWeight.visibility = View.GONE

//                viewBinder.tvWeightValue.text = item.editedWeight.plus(" ").plus(item.unitOfMeasure)
/*
            val parent = LinearLayout(this)
            parent.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            parent.orientation = LinearLayout.VERTICAL
            parent.background = ContextCompat.getDrawable(parent.context, R.color.white)

            //children of parent linearlayout
            val iv = ImageView(this)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(10, 16, 10, 0)
            val lpText = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lpText.setMargins(10, 0, 10, 5)
            lpText.gravity = Gravity.CENTER
            iv.layoutParams = lp
            iv.setImageBitmap(getBitmap(item.batchNumber))
            iv.layoutParams.height = 512
            iv.layoutParams.width = 512

            val tv1 = TextView(this)
            tv1.text = getString(R.string.lot_id).plus(" : ").plus(item.batchNumber)
            val tv2 = TextView(this)
            tv2.text = getString(R.string.material).plus(" : ").plus(item.materialName)
            val tv4 = TextView(this)
            if (item.grade!!.isNotEmpty()) {
                tv4.text = getString(R.string.grade).plus(" : ").plus(item.grade)
            }
            val tv5 = TextView(this)
            if (item.certificate!!.isNotEmpty()) {
                tv5.text = getString(R.string.certificate).plus(" : ").plus(item.certificate)
            }
            val tv3 = TextView(this)
            if (!item.editedWeight.isNullOrEmpty())
                tv3.text =
                    getString(R.string.weight).plus(" : ").plus(item.editedWeight?.toDouble()?.formatThreeDigits())
                        .plus(" ").plus(item.unitOfMeasure)
            tv1.layoutParams = lpText
            tv2.layoutParams = lpText
            if (item.grade!!.isNotEmpty()) tv4.layoutParams = lpText
            tv3.layoutParams = lpText
            parent.addView(iv) // lo agregamos al layout
            parent.addView(tv1)
            parent.addView(tv2)
            if (item.grade!!.isNotEmpty()) parent.addView(tv4)
            if (item.certificate!!.isNotEmpty()) parent.addView(tv5)
            parent.addView(tv3)*/
                //bitmapValue.put(index, getBitmapFromView(parent))
                tallyPrintKeys.add(
                    bitmapToString(
                        getBitmapFromView(
                            view, Color.WHITE
                        )
                    )
                )
            }
            runOnUiThread {
                val gson = GsonUtils()
                PreferenceHelper.save(Constants.BITMAP_KEYS, gson.toJson(tallyPrintKeys))
                startActivity(Intent(activity, WifiMainActivity::class.java))
            }
        }.execute()
    }

    override fun onResume() {
        super.onResume()
        hideCustomLoading()
    }

    private fun updateUIWithOnlineData(response: Resource<GenericReqAndResp<VegaGateEntryApprovalPostResponse>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.vegaGateEntryDetails.let { it1 ->
                        if (it1?.isNotEmpty() == true) {
                            vegaWbIds = listOf()
                            vegaWbIds = if (gateEntryData.wtype == PROCURE)
                                it1.filter { wb -> wb.wtype == PROCURE }
                            //.filter { wb -> wb.netWeight.equals("0.000") }
                            else
                                it1.filter { wb -> wb.wtype == STO }
                                    .filter { wb -> wb.netWeight.equals("0.000") }
                            gateEntry = vegaWbIds as MutableList<VegaGateEntryDetails>
                            if (vegaWbIds.size > 0) {
                                setUpAdapter(vegaWbIds)
                                binding.tvNoData.gone()
                                binding.llWeighbridge.visible()
                            } else {
                                binding.tvNoData.visible()
                                binding.llWeighbridge.gone()
                            }
                        } else {
                            binding.tvNoData.visible()
                            binding.llWeighbridge.gone()
                        }
                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), response.error.toString())
                }
            }
        }

    }

}
