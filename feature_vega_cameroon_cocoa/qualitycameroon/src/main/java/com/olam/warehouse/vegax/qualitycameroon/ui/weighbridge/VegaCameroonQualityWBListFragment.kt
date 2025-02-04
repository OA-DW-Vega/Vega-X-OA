package com.olam.warehouse.vegax.qualitycameroon.ui.weighbridge

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.ui.scanner.ScannerActivity
import com.olam.warehouse.master.vega.entity.VegaQualityWBDetails
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.DIRECTIONIN
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.qualitycameroon.R
import com.olam.warehouse.vegax.qualitycameroon.data.domain.usecase.model.VegaCameroonQualitySecretId
import com.olam.warehouse.vegax.qualitycameroon.databinding.FragmentVegaCameroonQualityWeighBridgeListBinding
import com.olam.warehouse.vegax.qualitycameroon.ui.VegaCameroonQualityViewModel
import com.olam.warehouse.vegax.qualitycameroon.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Keerthi Santhanam on 6/27/2020.
 */
class VegaCameroonQualityWBListFragment : BaseFragment() {

    private var mAdapter = VegaCameroonQualityWBListAdapter { moveBagdetail(it) }
    private val vm: VegaCameroonQualityViewModel by viewModel()
    private lateinit var mListener: OnWeighBridgeListener
    private lateinit var binding: FragmentVegaCameroonQualityWeighBridgeListBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_quality_weigh_bridge_list
    private var weightmentType: String? = ""
    private var copiedWbid: String = ""
    private var copiedMaterial: String = ""

    private val mSearchList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var mQualityWBList: MutableList<VegaQualityWBDetails> = mutableListOf()
    private var offlineDataList = mutableListOf<VegaQualityWBDetails>()


    interface OnWeighBridgeListener {
        fun onWeighBridgeClick(
            wbDetails: VegaQualityWBDetails?,
            copiedWbid: String,
            copiedMaterial: String
        )

        fun setQualityWBList(it: List<VegaQualityWBDetails>?)
        fun onQualityOfflineClick()
    }

    companion object {
        fun newInstance() = VegaCameroonQualityWBListFragment().putArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as OnWeighBridgeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonQualityWeighBridgeListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("qualitycameroon/ui/weighbridge/VegaCameroonQualityWBListFragment")
            .title("Vega_Cameroon/Quality").with(tracker)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity!!.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        activity?.menuInflater?.inflate(R.menu.search_vega_cameroon_quality_menu, menu)
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_sample_id)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            mAdapter.addItems(mQualityWBList)
                        } else {
                            mSearchList.clear()
                            mQualityWBList.forEach { qtyWb ->
                                newText?.let { text ->
                                    if (qtyWb.challan?.contains(text)!!) {
                                        mSearchList.add(qtyWb)
                                    }
                                }
                            }
                            mAdapter.addItems(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        binding.rvWeighbridge.layoutManager = LinearLayoutManager(this.context)
        binding.rvWeighbridge.adapter = mAdapter
        vm.secretId.observe(viewLifecycleOwner, Observer { updateDashboardUI(it) })


        binding.ivSortDownUp.setOnClickListener {
            var data: MutableList<VegaQualityWBDetails>
            if (mQualityWBList.isNotEmpty()) {
                mQualityWBList.let { data = it }
                data = data.asReversed()
                mQualityWBList = data
                mAdapter.addItems(mQualityWBList)
            }

        }

        binding.clScan.setOnClickListener { moveToScan() }
        binding.tvFromDate.text = DateUtils.getUTCDateTimeCameroon(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )
        binding.tvToDate.text = DateUtils.getUTCDateTimeCameroon(
            System.currentTimeMillis().toString(),
            App.getAppContext()
        )
        binding.tvFromDate.setOnClickListener {
            getDatePickerDialog(
                binding.tvFromDate.text.toString(),
                "From"
            )
        }
        binding.tvToDate.setOnClickListener {
            if (!binding.tvFromDate.text.toString().isEmpty())
                getDatePickerDialog(binding.tvToDate.text.toString(), "To")
            else
                Toast.makeText(context, "Please enter From date", Toast.LENGTH_SHORT).show()
        }
        binding.btnGo.setOnClickListener {
            if (!(binding.tvFromDate.text.toString().isEmpty() || binding.tvToDate.text.toString()
                    .isEmpty())
            )
                vm.getSecretIdList(
                    DateUtils.formatDate(binding.tvFromDate.text.toString()),
                    DateUtils.formatDate(binding.tvToDate.text.toString()), false
                )
            else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }
        vm.getSecretIdList(
            DateUtils.formatDate(binding.tvFromDate.text.toString()),
            DateUtils.formatDate(binding.tvToDate.text.toString()), false
        )
    }

    private fun getDatePickerDialog(date: String, label: String) {
        val cal = Calendar.getInstance()
        if (date != "") {
            val dateTxt = date.split("/")
            cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        }
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
                    when (label) {
                        "From" -> {
                            binding.tvFromDate.text = sdf.format(cal.time)
                            binding.tvToDate.text = ""
                        }
                        "To" ->
                            binding.tvToDate.text = sdf.format(cal.time)
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            when (label) {

                "To" -> {
                    var fromDate = binding.tvFromDate.text.toString()
                    var maxToDate = DateUtils.addingDaysToDate(fromDate, 5)
                    val sdformat = SimpleDateFormat("MM/dd/yyyy")
                    val d1 = sdformat.parse(fromDate)
                    datePicker.datePicker.minDate = d1.time
                    val currentDate = sdformat.parse(
                        DateUtils.getUTCDateTime(
                            System.currentTimeMillis().toString(),
                            App.getAppContext()
                        )
                    )
                    val d2 = sdformat.parse(maxToDate)
                    if (d2.compareTo(currentDate) > 0) {
                        datePicker.datePicker.maxDate = System.currentTimeMillis()
                    } else if (d2.compareTo(currentDate) < 0) {
                        datePicker.datePicker.maxDate = d2.time
                    } else if (d2.compareTo(d2) === 0) {
                        datePicker.datePicker.maxDate = System.currentTimeMillis()
                    }
                }
            }
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun moveToScan() {
        val intent = Intent(requireContext(), ScannerActivity::class.java)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.SCAN_QR && (resultCode == Activity.RESULT_OK)) {

            data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {

                val msg = getString(
                    R.string.scanned_sample_id,
                    "$it"
                )

                MaterialDialog(requireContext()).show {
                    message(
                        R.string.scanned_sample_id, msg
                    )
                    positiveButton(
                        text = UIUtils.getSpannedText(
                            getString(com.olam.warehouse.presentation.R.string.ok),
                            true
                        )
                    ) {
                        dismiss()
                    }

                }

            }
        }
    }

    private fun initExtra() {
        arguments?.let {
            weightmentType = it.getString(WEIGHBRIDGE_LIST_TYPE)
            copiedWbid = it.getString(COPIED_WBID).toString()
            copiedMaterial = it.getString(COPIED_MATERIAL).toString()
        }

        when (weightmentType) {
            SUPPLIER -> {
                binding.tvType.text =
                    getString(R.string.quality_analysis).plus(" - ").plus(getString(R.string.supplier))
            }
            MTNR -> {
                binding.tvType.text = getString(R.string.quality_analysis).plus(" - ").plus(MTNR)
            }
        }
    }

    private fun moveBagdetail(wbDetails: VegaQualityWBDetails?) {
        mListener.onWeighBridgeClick(wbDetails, copiedWbid, copiedMaterial)
    }


    private fun updateDashboardUI(response: Resource<GenericReqAndResp<List<VegaCameroonQualitySecretId>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            hideLoading()
                            if (response.data?.data?.size ?: 0 > 0) {
                                /*var data =
                                    response.data?.data?.filter { value -> !value.qcFlag.contains("X") }*/
                                val offloadedwbids: List<String> =
                                    response.data?.data?.get(0)?.qcRecordSetDetails?.filter { it.qcFlag == "" }
                                        ?.map { it.weighBridgeId } as List<String>
                                val data =
                                    response.data?.data?.filter { it.weighBridgeId in offloadedwbids }
                                var weighTypedata: List<VegaCameroonQualitySecretId>? = null
                                when (weightmentType) {
                                    SUPPLIER -> {
                                        weighTypedata =
                                            data?.filter { wb -> wb.direction == DIRECTIONIN }
                                                ?.filter { value -> value.wtype == PROCURE }
                                                ?.filter { value -> !value.netWeight.equals("0.000") }
                                                ?.filter { value -> value.challan.isNotEmpty() }
                                                ?.filter { value -> value.qcFlag.isNullOrEmpty() }
                                    }
                                    MTNR -> {
                                        weighTypedata =
                                            data?.filter { wb -> wb.direction == DIRECTIONIN }
                                                ?.filter { value -> value.wtype == STO }
                                                ?.filter { value -> !value.netWeight.equals("0.000") }
                                    }
                                }
                                var weighTypedataUpdate = prepareWeighTypeData(weighTypedata)
                                weighTypedataUpdate.let { it1 ->
                                    /*var data1 = listOf<VegaQualityWBDetails>()*/
                                    var filteredList = arrayListOf<VegaQualityWBDetails>()
                                    val data1 =
                                        if (!copiedMaterial.equals("null") && copiedMaterial.isNotEmpty()) it1.filter {
                                            it.materialCode.equals(copiedMaterial)
                                        } else it1
                                    if (data1.isNotEmpty()) {
                                        data1.forEach { wb ->
                                            if (!offlineDataList.map { it.weighBridgeId }
                                                    .contains(wb.weighBridgeId)) {
                                                filteredList.add(wb)
                                            }
                                        }
                                        val weighBridgeList2 = data1.reversed()
                                        mListener.setQualityWBList(weighBridgeList2)
                                        if (mQualityWBList.size > 0)
                                            mQualityWBList.clear()
                                        mQualityWBList = weighBridgeList2.toMutableList()
                                        mAdapter.addItems(weighBridgeList2)
                                        binding.tvNoData.gone()
                                        binding.rvWeighbridge.visible()
                                    } else {
                                        binding.tvNoData.visible()
                                        binding.rvWeighbridge.gone()
                                    }
                                }
                            } else {
                                UIUtils.showErrorDialog(
                                    requireContext(),
                                    getString(R.string.no_data)
                                )
                            }
                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
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

}
