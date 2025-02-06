package com.olam.warehouse.odreceiving.ui.blt

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.olam.warehouse.master.dorigin.entity.DispatchDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.DODispatchDetailPost
import com.olam.warehouse.odreceiving.data.domain.model.DispatchDetailsResponse
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingDispatchDetailsBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils.isOnline
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*


class DOReceivingDispatchDetailsFragment : BaseFragment() {

    private var isCalled: Boolean = true
    private var dispatchOffline: List<DispatchDetail>? = mutableListOf()
    private lateinit var binding: FragmentDoReceivingDispatchDetailsBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_dispatch_details
    private val vm: DOReceivingViewModel by viewModel()

    private var fromDate: Long = -1
    private var toDate: Long = -1
    private var sapMaterialIds = mutableListOf<String>()

    companion object {
        fun newInstance() = DOReceivingDispatchDetailsFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showCustomLoading()
        setHasOptionsMenu(true)
        binding = FragmentDoReceivingDispatchDetailsBinding.inflate(layoutInflater)

        initUI()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/blt/DOReceivingDispatchDetailsFragment")
            .title("OD/Receiving")
            .with(tracker)

    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnOk, it, true)
        }
        val sapMaterialIds = mutableListOf<String>()

        if (isOnline()) {
            binding.dateLayout.visibility = View.VISIBLE
        } else {
            binding.dateLayout.visibility = View.GONE
        }

        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat)

        val myCalendar = Calendar.getInstance(Locale.getDefault())
        binding.tvToDate.text = sdf.format(myCalendar.time)
        myCalendar.set(Calendar.HOUR_OF_DAY, 23)
        myCalendar.set(Calendar.MINUTE, 59)
        myCalendar.set(Calendar.SECOND, 59)
        toDate = myCalendar.timeInMillis

        myCalendar.add(Calendar.YEAR, -1)
        binding.tvFromDate.text = sdf.format(myCalendar.time)
        myCalendar.set(Calendar.HOUR_OF_DAY, 0)
        myCalendar.set(Calendar.MINUTE, 0)
        myCalendar.set(Calendar.SECOND, 0)
        fromDate = myCalendar.timeInMillis

        Log.i("FromDateInit", fromDate.toString())
        Log.i("FromToInit", toDate.toString())

        binding.fromDate.setOnClickListener {
            val myCalendar = Calendar.getInstance(Locale.getDefault())
            val date =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                    var selectedDate: Date = Date(year, monthOfYear, dayOfMonth, 0, 0, 0)
                    val calendar = Calendar.getInstance(Locale.getDefault())
                    val currentDateConfigured = Date(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH], 0, 0, 0)
                    if (selectedDate <= currentDateConfigured) {
                        myCalendar.set(Calendar.YEAR, year)
                        myCalendar.set(Calendar.MONTH, monthOfYear)
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        myCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        myCalendar.set(Calendar.MINUTE, 0)
                        myCalendar.set(Calendar.SECOND, 0)

                        val myFormat = "MM/dd/yyyy"
                        val sdf = SimpleDateFormat(myFormat)
                        binding.tvFromDate.text = sdf.format(myCalendar.time)
                        fromDate = myCalendar.timeInMillis
                        Log.i("DateFrom", myCalendar.timeInMillis.toString())

                        activity?.runOnUiThread {
                            showLoading()
                        }
                        callDispatchApi()
                    } else {
                        activity?.toast(getString(R.string.from_date_validate_msg))
                    }
                }

            DatePickerDialog(
                this.requireActivity(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.toDate.setOnClickListener {
            val myCalendar = Calendar.getInstance(Locale.getDefault())
            val date =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                    var selectedDate: Date = Date(year, monthOfYear, dayOfMonth, 23, 59, 59)
                    val calendar = Calendar.getInstance(Locale.getDefault())
                    val currentDateConfigured = Date(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH], 23, 59, 59)
                    if (selectedDate <= currentDateConfigured) {
                        myCalendar.set(Calendar.YEAR, year)
                        myCalendar.set(Calendar.MONTH, monthOfYear)
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        myCalendar.set(Calendar.HOUR_OF_DAY, 23)
                        myCalendar.set(Calendar.MINUTE, 59)
                        myCalendar.set(Calendar.SECOND, 59)

                        val myFormat = "MM/dd/yyyy"
                        val sdf = SimpleDateFormat(myFormat)
                        binding.tvToDate.text = sdf.format(myCalendar.time)
                        toDate = myCalendar.timeInMillis
                        Log.i("DateTo", myCalendar.timeInMillis.toString())

                        activity?.runOnUiThread {
                            showLoading()
                        }
                        callDispatchApi()
                    } else {
                        activity?.toast(getString(R.string.end_date_validate_msg))
                    }
                }

            DatePickerDialog(
                this.requireActivity(), date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }

        if (isOnline()) {
            vm.getSAPMaterials()
            vm.sapMaterial.observe(viewLifecycleOwner, Observer {
                Log.i("sap", it.toString())
                it?.let {
                    sapMaterialIds.clear()
                    for (sapMaterial in it) {
                        if (sapMaterial.bltEnabled != null && sapMaterial.bltEnabled!!)
                            sapMaterialIds.add(sapMaterial.materialCode)
                    }

                    hideCustomLoading()
                    setSpinnerMaterialCodeAdapter(sapMaterialIds)
                }
            })
        } else {
            vm.getDispatchDetailsOffline()
            vm.dispatchOffline.observe(viewLifecycleOwner, Observer { list ->
                hideCustomLoading()
                sapMaterialIds.clear()
                dispatchOffline = list

                dispatchOffline?.distinctBy { it.sapMaterialId }?.forEach {
                    sapMaterialIds.add(it.sapMaterialId)
                }

                setSpinnerMaterialCodeAdapter(sapMaterialIds)
            })
        }
    }

    private fun setSpinnerMaterialCodeAdapter(sapIds: MutableList<String>) {
        val data: MutableList<KeyPairBoolData> = mutableListOf()
        sapIds.forEachIndexed { id, element ->
            val keyPairBoolData = KeyPairBoolData()
            keyPairBoolData.id = id.toLong()
            keyPairBoolData.name = element
            keyPairBoolData.`object` = element
            keyPairBoolData.isSelected = true
            data.add(keyPairBoolData)
        }
        /*val spinnerAdapter = ArrayAdapter(this.requireActivity(), android.R.layout.simple_spinner_item, sapMaterialIds)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(binding.spinnerMaterialCode) {
            adapter = spinnerAdapter
            setSelection(0, false)
            onItemSelectedListener = this@DOReceivingDispatchDetailsFragment
        }*/
        binding.spinnerMaterialCode.isSearchEnabled = false
        binding.spinnerMaterialCode.isShowSelectAllButton = true
        binding.spinnerMaterialCode.setItems(data) { selectedItems ->
            run {
                Log.i("selectedItems", sapMaterialIds.toString())
                if (isOnline()) {
                    sapMaterialIds.clear()
                    selectedItems?.forEach {
                        sapMaterialIds.add(it.name)
                    }
                    activity?.runOnUiThread {
                        showLoading()
                    }
                    callDispatchApi()
                } else {
                    updateUIOffline(selectedItems)
                }
            }
        }

        val selectedItems = binding.spinnerMaterialCode.selectedItems
        Log.i("selectedItems", sapMaterialIds.toString())
        if (isOnline()) {
            sapMaterialIds.clear()
            selectedItems?.forEach {
                sapMaterialIds.add(it.name)
            }
            activity?.runOnUiThread {
                showLoading()
            }
            callDispatchApi()
        } else {
            updateUIOffline(selectedItems)
        }
    }

    private fun updateUI(it: Resource<GenericReqAndResp<DispatchDetailsResponse>>?) {
        Log.i("DispatchRsp", it.toString())
        when (it?.status) {
            Resource.Status.SUCCESS -> {
                if (it.data?.data != null) {
                    activity?.runOnUiThread {
                        binding.tvLotCompleteValue.text = it.data?.data?.totalQCComplete.toString()
                        binding.tvLotDispatchValue.text = it.data?.data?.totalDispatched.toString()
                        binding.tvLotReceiveValue.text = it.data?.data?.totalReceived.toString()
                        hideLoading()
                        isCalled = true
                    }
                }
            }
            Resource.Status.LOADING -> {
                showLoading()
                isCalled = true
            }
            Resource.Status.ERROR -> {
                hideLoading()
                activity?.toast(it.error!!)
                isCalled = true
            }
            else -> {}
        }
    }

    private fun callDispatchApi() {
        if (sapMaterialIds.isNotEmpty()) {
            if (fromDate != -1L && toDate != -1L) {
                if (isCalled) {
                    isCalled = false
                    val dispatchDetail = DODispatchDetailPost(sapMaterialIds, fromDate, toDate)
                    vm.getDispatchDetails("", dispatchDetail)
                    vm.dispatchDetail.observe(viewLifecycleOwner, Observer { dispatchDetails ->
                        updateUI(dispatchDetails)
                    })
                }
            }
        } else {
            if (fromDate != -1L && toDate != -1L) {
                activity?.toast(getString(R.string.select_material_id))
                activity?.runOnUiThread {
                    binding.tvLotCompleteValue.text = "0"
                    binding.tvLotDispatchValue.text = "0"
                    binding.tvLotReceiveValue.text = "0"
                    hideLoading()
                }
            }
        }
    }

    /*override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (isOnline()) {
            sapMaterialIds.clear()
            sapMaterialIds.add(spinnerMaterialCode.selectedItem.toString())
            callDispatchApi()
        } else {
            if (spinnerMaterialCode.selectedItemPosition == 0) {
                binding.tvLotCompleteValue.text = "0"
                binding.tvLotDispatchValue.text = "0"
                binding.tvLotReceiveValue.text = "0"
            } else {
                updateUIOffline(spinnerMaterialCode.selectedItem.toString())
            }
        }
    }*/

    private fun updateUIOffline(selectedItems: MutableList<KeyPairBoolData>) {
        var selectedItemStrings = mutableListOf<String>()
        selectedItems.forEach {
            selectedItemStrings.add(it.name)
        }
        var totalReceived: Int = 0
        var totalDispatched: Int = 0
        var totalCompleted: Int = 0
        dispatchOffline?.forEach {
            if (selectedItemStrings.contains(it.sapMaterialId)) {
                totalReceived += it.totalReceived
                totalCompleted += it.totalQCComplete
                totalDispatched += it.totalDispatched
            }
        }
        binding.tvLotCompleteValue.text = totalCompleted.toString()
        binding.tvLotDispatchValue.text = totalDispatched.toString()
        binding.tvLotReceiveValue.text = totalReceived.toString()
    }
}
