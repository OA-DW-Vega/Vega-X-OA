package com.olam.warehouse.vegax.secretid.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.addingDaysToDate
import com.olam.warehouse.presentation.utils.DateUtils.formatDate
import com.olam.warehouse.presentation.utils.DateUtils.getFormatedDate
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.secretid.R
import com.olam.warehouse.vegax.secretid.data.domain.model.VegaCameroonSecretId
import com.olam.warehouse.vegax.secretid.databinding.FragmentVegaCameroonSecretIdListBinding
import com.olam.warehouse.vegax.secretid.databinding.ItemVegaCameroonSecretidDetailsBinding
import com.olam.warehouse.vegax.secretid.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.secretid.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Baskaran Kannan on 8/5/2020.
 */

class VegaCameroonSecretIdListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_cameroon_secret_id_list
    private val vm: VegaCameroonSecretIdViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonSecretIdListBinding
    private var callBack: CallBack? = null
    private var secretIdList = mutableListOf<VegaCameroonSecretId>()
    private var sortList = mutableListOf<VegaCameroonSecretId>()
    private var mSearchList = mutableListOf<VegaCameroonSecretId>()

    interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaCameroonSecretId
        )
    }

    companion object {
        fun newInstance() = VegaCameroonSecretIdListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search Weighbridge ID"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonSecretIdListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("secretid/ui/VegaCameroonSecretIdListFragment")
            .title("Vega_Cameroon/SecretID").with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llSortBy, it, false)
        }
        vm.secretId.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.ivSortDownUp.setOnClickListener {
            if (secretIdList.isNotEmpty()) {
                secretIdList.let { sortList = it }
                sortList = sortList.asReversed()
                secretIdList = sortList
                setUpAdapter(secretIdList)
            }
        }
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
                    formatDate(binding.tvFromDate.text.toString()),
                    formatDate(binding.tvToDate.text.toString()),
                    false
                )
            else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }
        vm.getSecretIdList(
            formatDate(binding.tvFromDate.text.toString()),
            formatDate(binding.tvToDate.text.toString()),
            false
        )
    }

    private fun getDatePickerDialog(date: String, label: String) {
        val cal = Calendar.getInstance()
        if (date != "") {
            val dateTxt = date.split("/")
            cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
        }
        val dateFormat = "MM/dd/yyyy"
        val utc = "UTC"

        context?.let {
            val datePicker = DatePickerDialog(
                it,
                com.olam.warehouse.presentation.R.style.DatePickerTheme,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, monthOfYear)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    val sdf = SimpleDateFormat(dateFormat, LocaleHelper.getLocale(it))
                    sdf.timeZone = TimeZone.getTimeZone(utc)
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
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            when (label) {

                "To" -> {
                    var fromDate = binding.tvFromDate.text.toString()
                    var maxToDate = addingDaysToDate(fromDate, 4)
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

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaCameroonSecretId>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            secretIdList.clear()
                            secretIdList.addAll(it.data!!.data)
                            if(secretIdList.size >0) {
                                var gateEntryWbids: List<String> =
                                    secretIdList[0].wsItemSetDetails.map { it.weighBridgeId }
                                var offloadedwbids: List<String> =
                                    secretIdList[0].qcRecordSetDetails.filter { it.qcFlag == "" }
                                        .map { it.weighBridgeId }
                                var list = mutableListOf<String>()
                                list.addAll(gateEntryWbids)
                                list.addAll(offloadedwbids)
                                println("Roshna => ${list}")
                                secretIdList =
                                    secretIdList.filter { it.weighBridgeId in list } as MutableList<VegaCameroonSecretId>
//                            secretIdList = secretIdList.sortByDescending { getFormated(it.erdat) }
                            }
                            setUpAdapter(secretIdList)

                        }
                        else -> showErrorDialogWithFAQLink(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    setUpAdapter(secretIdList)
                    showErrorDialogWithFAQLink(requireContext(), "${it.error}")
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_secretidcameroon_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint =
                SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(secretIdList)
                        } else {
                            mSearchList.clear()
                            secretIdList.forEach { lot ->
                                newText?.let { text ->
                                    if (lot.weighBridgeId.contains(text)) {
                                        mSearchList.add(lot)
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

    private fun setUpAdapter(lotList: MutableList<VegaCameroonSecretId>) {
        if (lotList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUpAdapter(
            lotList,
            R.layout.item_vega_cameroon_secretid_details,
            ItemVegaCameroonSecretidDetailsBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvWbId.text = it.weighBridgeId
                bindItem.tvBatchNo.text = it.vehNo
                bindItem.tvMaterialName.text = it.tempBagCount
//            tvMaterialName.text = it.bagCount
//            tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                bindItem.tvDateTxt.text = getFormatedDate(it.erdat)

            },
            {
                val item = this
                callBack?.replaceQualityFragment(PARAMS_FRAG, item)
            })
    }
}
