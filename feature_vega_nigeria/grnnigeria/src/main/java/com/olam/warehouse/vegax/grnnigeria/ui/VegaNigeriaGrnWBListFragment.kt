package com.olam.warehouse.vegax.grnnigeria.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vega.entity.VegaCustomStLocation
import com.olam.warehouse.master.vega.entity.VegaGrnWeighBridgeId
import com.olam.warehouse.master.vega.entity.VegaQualityParams
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnnigeria.R
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.NigeriaGrnFilterList
import com.olam.warehouse.vegax.grnnigeria.data.domain.model.VegaNigeriaGRNQuality
import com.olam.warehouse.vegax.grnnigeria.databinding.FragmentVegaNigeriaGrnWeighbridgeListBinding
import com.olam.warehouse.vegax.grnnigeria.utils.*
import kotlinx.android.synthetic.main.item_vega_nigeria_weighbridge_grn.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Roshna Parambil on 9/9/2020.
 */
class VegaNigeriaGrnWBListFragment : BaseFragment() {

    private val vm: VegaNigeriaGrnViewModel by viewModel()
    private var weighBridgeList = mutableListOf<VegaGrnWeighBridgeId>()
    private val mSearchList = mutableListOf<VegaGrnWeighBridgeId>()
    private var custonSupplierList = mutableListOf<VegaCustomStLocation>()
    private var listFliterWhLoc = ArrayList<NigeriaGrnFilterList>()
    private lateinit var binding: FragmentVegaNigeriaGrnWeighbridgeListBinding
    private var callBack: CallBack? = null
    override val layoutResourceId = R.layout.fragment_vega_nigeria_grn_weighbridge_list
    private var offlineDataList = mutableListOf<VegaGrnWeighBridgeId>()
    private var byDate = Date()
    private var qualitylist = arrayListOf<VegaQualityParams>()
    private var approveQualityList = ArrayList<VegaNigeriaGRNQuality>()
    private var admixtureValue: String = ""
    private var wbDetails = VegaGrnWeighBridgeId()

    interface CallBack {
        fun replaceQualityFragment(
            moveFrag: String,
            wbDetails: VegaGrnWeighBridgeId,
            approveQualityList: ArrayList<VegaNigeriaGRNQuality>
        )

        fun replaceFragment(moveFrag: String, item: VegaGrnWeighBridgeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaGrnWeighbridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("grnnigeria/ui/weighbridge/VegaNigeriaGrnWBListFragment")
            .title("Ecuador GRN")
            .with(tracker)
        initUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if (getCurrentOriginEntity().contains("OFI")) com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_wb_item)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(weighBridgeList)
                        } else {
                            mSearchList.clear()
                            weighBridgeList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.weighBridgeId?.contains(text)!!) {
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    private fun initUI() {
        if (isOnline()) {
            vm.weighBridge.observe(viewLifecycleOwner, Observer { updateUI(it) })
            vm.getWeighBridgeList()
        } else {
            vm.weighBridgeLocal.observe(viewLifecycleOwner, Observer { updateUIWithLocalData(it) })
            vm.getWeighBridgeDetail()
        }
        binding.ivSortDownUp.setOnClickListener {
            val data1 = weighBridgeList.filter { it.wbTempId.contains("TMP") }
            val data2 = weighBridgeList.filter { !it.wbTempId.contains("TMP") }
            weighBridgeList.clear()
            weighBridgeList.addAll(data2.asReversed())
            weighBridgeList.addAll(data1)
            setUpAdapter(weighBridgeList)
        }

        vm.suppplier.observe(viewLifecycleOwner, Observer {
            val suppplier = it.filter { data -> data.bcApprover?.isNotEmpty()!! }
                .map { data -> data.vendorCode.plus(" - ").plus(data.vendorName) }
        })
        vm.getSuppliers()
        binding.tvBySupplier.setOnClickListener {
            showGRNSupplierDialog(custonSupplierList.filter {
                !it.storageLocationType.equals(
                    "B"
                )
            })
        }
        vm.weighBridgeOfflineCount.observe(this, Observer { enableOfflineBar(it) })
        vm.getOfflineWeighBridgeDetailCount()
        binding.llQualityOffline.setOnClickListener {
            callBack?.replaceFragment(
                GRN_OFFLINE_FRAG,
                VegaGrnWeighBridgeId()
            )
        }

        vm.qualityDetails.observe(viewLifecycleOwner, Observer { updateQualityUI(it) })
    }
    private fun showGRNSupplierDialog(it: List<VegaCustomStLocation>) {
        val location =
            it.map { data -> data.procureLocationCode.plus(" - ").plus(data.procureLocationName) }
        MaterialDialog(requireContext()).show {
            title(R.string.supplier_popup)
            listItemsSingleChoice(items = location) { _, index, text ->
                /* binding.tvReceivingLocation.text = text
                 gateEntryData.storageLocationCode = it[index].procureLocationCode
                 gateEntryData.storageLocationName = it[index].procureLocationName*/
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.text_ok), true))
        }
    }

    private fun enableOfflineBar(items: List<VegaGrnWeighBridgeId>) {
        if (items.size > 0) {
            binding.llQualityOffline.visible()
            offlineDataList = items as MutableList<VegaGrnWeighBridgeId>
        } else binding.llQualityOffline.gone()
    }

    private fun updateUIWithLocalData(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridge =
            data?.filter { it.qcStatus.isNullOrEmpty() && it.grnNumber.isNullOrEmpty() }
        if (weighBridge?.size!! > 0) {
            weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
            setUpAdapter(weighBridgeList)
            binding.tvNoData.gone()
            binding.rvWeighBridgeId.visible()
        } else {
            binding.tvNoData.visible()
            binding.rvWeighBridgeId.gone()
        }
    }

    private fun updateUI(data: Resource<GenericReqAndResp<List<VegaGrnWeighBridgeId>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val weighBridge =
                        it.data?.data?.filter {
                            it.qcStatus == "X" && it.grnNumber.isNullOrEmpty() && it.weighBridgeType == "PROCURE" && (!it.procurementType.equals(
                                DR
                            ))
                        }
                    //it.data?.data?.filter { it.qcStatus == "X" && it.grnNumber.isNullOrEmpty() && it.weighBridgeType == "PROCURE" }
                    if (weighBridge?.size!! > 0) {
                        //weighBridgeList = weighBridge as MutableList<VegaGrnWeighBridgeId>
                        weighBridge.forEach { wb ->
                            if (!offlineDataList.map { it.weighBridgeId }
                                    .contains(wb.weighBridgeId)) {
                                weighBridgeList.add(wb)
                            }
                        }
                        setUpAdapter(weighBridgeList)
                        binding.tvNoData.gone()
                        binding.rvWeighBridgeId.visible()
                    } else {
                        binding.tvNoData.visible()
                        binding.rvWeighBridgeId.gone()
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                    hideLoading()
                }
                else -> {

                }
            }
        }
    }

    private fun updateQualityUI(data: Resource<GenericReqAndResp<List<VegaNigeriaGRNQuality>>>) {
        data.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    qualitylist.clear()
                    it.data?.data?.let { it1 ->
                        if (it1.isNotEmpty()) approveQualityList =
                            it1 as ArrayList<VegaNigeriaGRNQuality>
                        approveQualityList.forEach {
                            it.qualityParameters.forEach {
                                if (it.sapQCName == "ZNG_ADMIXTURE") {
                                    admixtureValue = it.satNam!!
                                }
                                val item = VegaQualityParams()
                                item.qualityParameterName = it.qualityParameterName
                                item.sapQCName = it.sapQCName
                                item.satNam = it.satNam
                                qualitylist.add(item)
                            }
                        }
                        //updateUIValues()
                        callBack?.replaceQualityFragment(
                            GRN_QUALITY_DETAILS,
                            wbDetails,
                            approveQualityList
                        )
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    UIUtils.showErrorDialog(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }
    }


    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
//        val dateTxt = binding.tvDate.text.split("/")
//        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        val DATE_FORMAT = "MM/dd/yyyy"
        val UTC = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val sdf = SimpleDateFormat(DATE_FORMAT, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(UTC)
                    byDate = cal.time
//                    binding.tvDate.text = sdf.format(cal.time)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

    }

    private fun setUpAdapter(data: List<VegaGrnWeighBridgeId>?) {
        val weighBridgeList1 = data as MutableList<VegaGrnWeighBridgeId>
        var count = 0
        binding.rvWeighBridgeId.setUp(
            weighBridgeList1.asReversed(),
            R.layout.item_vega_nigeria_weighbridge_grn,
            { it, pos ->
                it.unitPrice = it.unitPrice.toString().replace("\\s".toRegex(), "")
//                tvProcurementType.text =
//                    if (it.purchaseDocNum.isNullOrEmpty()) getString(R.string.spot_purchase) else getString(R.string.fixed_purchase)
                tvSupplierName.text = it.supplierName
                tvWeight.text = it.netWeight.plus(it.unitsOfMeasure)
                tvWeighBridgeId.text = it.weighBridgeId
                tvMaterial.text = it.materialName
                tvLotNo.text = it.batchNumber
                var materialCode = it.materialCode
                println("Roshna =>" + tvLotNo.text)
                // vm.getQualityDetails(it.batchNumber.toString(), it.materialCode.toString())
                tvQualityDetails.setOnClickListener {
                    wbDetails.batchNumber = tvLotNo.text.toString()
                    vm.getQualityDetails(tvLotNo.text.toString(), materialCode.toString())
                    //  callBack?.replaceQualityFragment(GRN_QUALITY_DETAILS, it, approveQualityList)
                }

                cbLotID.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        //weighBridgeList1.forEach { it.isSelected = true }
                    }
                    binding.rvWeighBridgeId.adapter?.notifyDataSetChanged()
                }
                val times = it.erdat?.split('(', ')')
                tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }

                count++
            },
            {
                val item = this
                callBack?.replaceFragment(GRN_FRAG, item)
            })
    }
}
