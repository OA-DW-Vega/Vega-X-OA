package com.olam.warehouse.vegax.secretidnigeria.ui

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.common.utils.getMultiPlantList
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.DateUtils.addingDaysToDate
import com.olam.warehouse.presentation.utils.DateUtils.formatDate
import com.olam.warehouse.presentation.utils.DateUtils.getFormatedDate
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.secretidnigeria.R
import com.olam.warehouse.vegax.secretidnigeria.data.domain.model.VegaNigeriaSecretId
import com.olam.warehouse.vegax.secretidnigeria.databinding.FragmentVegaNigeriaSecretIdListBinding
import com.olam.warehouse.vegax.secretidnigeria.utils.PARAMS_FRAG
import com.olam.warehouse.vegax.secretidnigeria.utils.getColor
import kotlinx.android.synthetic.main.item_vega_nigeria_secretid_details.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Baskaran Kannan on 8/5/2020.
 */

class VegaNigeriaSecretIdListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_nigeria_secret_id_list
    private val vm: VegaNigeriaSecretIdViewModel by viewModel()
    private lateinit var binding: FragmentVegaNigeriaSecretIdListBinding
    private var callBack: CallBack? = null
    private var secretIdList = mutableListOf<VegaNigeriaSecretId>()
    private var sortList = mutableListOf<VegaNigeriaSecretId>()
    private var mSearchList = mutableListOf<VegaNigeriaSecretId>()
    private var selectedPlantId = ""
    private var selectedPlantList = mutableListOf<String>()
    private var plantList = mutableListOf<Plant>()
    private var ids = ArrayList<String>()

    interface CallBack {
        fun replaceQualityFragment(
            paramsFrag: String,
            item: VegaNigeriaSecretId
        )
    }

    companion object {
        fun newInstance() = VegaNigeriaSecretIdListFragment().putArgs {
        }

        const val SEARCH_HINT_TEXT = "Search LOT ID"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaSecretIdListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("secretid/ui/ppq/VegaNigeriaPpqLotListFragment")
            .title("PPQ").with(tracker)
    }

    private fun initUI() {
        //vm.secretId.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.wbMultiPlant.observe(viewLifecycleOwner, Observer { updateUI(it) })
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
        plantList = getMultiPlantList() as MutableList<Plant>
        ids = this.plantList.map { it.plantId } as ArrayList<String>

        updatePlantListUI(ids)
        binding.btnGo.setOnClickListener {
            if (!(binding.tvFromDate.text.toString().isEmpty() || binding.tvToDate.text.toString()
                    .isEmpty())
            )
                vm.getfetchWBListforMultiPlants(
                    false,
                    formatDate(binding.tvFromDate.text.toString()),
                    formatDate(binding.tvToDate.text.toString()), selectedPlantList
                )
            else {
                Toast.makeText(context, "Please enter Date", Toast.LENGTH_SHORT).show()
            }
        }
        /*vm.getSecretIdList(
            formatDate(binding.tvFromDate.text.toString()),
            formatDate(binding.tvToDate.text.toString()),
            false
        )*/
        /* var currentDate = DateUtils.formatDate(DateUtils.getDate())

     }*/
        //val allPlantId = plantList.map { it.plantId }
        /*vm.getfetchWBListforMultiPlants(false,   formatDate(binding.tvFromDate.text.toString()),
                formatDate(binding.tvToDate.text.toString()),allPlantId)*/

        /* vm.getfetchWBListforMultiPlants(false,   formatDate(binding.tvFromDate.text.toString()),
             formatDate(binding.tvToDate.text.toString()),plantList)*/
    }

    private fun updatePlantListUI(plantList: ArrayList<String>) {
        binding.spPlantSelection.isEnabled = true
        val plantIdList = ArrayList<String>()
        plantIdList.add(getString(R.string.select_plant_id))
        plantIdList.addAll(plantList)
        val stageAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.item_vega_nigeria_sercretid_plant_select,
                plantIdList
            )
        stageAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spPlantSelection.adapter = stageAdapter
        binding.spPlantSelection.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position > 0) {
                        selectedPlantId = plantIdList[position]
                        selectedPlantList.add(selectedPlantId)
//                    validateLot(selectedPlantId)
                    }
//                binding.spPlantSelection.setSelection(0)
                }
            }
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
            //datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            when (label) {
                "From" -> {
                    //empty block
                }
                "To" -> {
                    var fromDate = binding.tvFromDate.text.toString()
                    var maxToDate = addingDaysToDate(fromDate, 5)
                    val sdformat = SimpleDateFormat("MM/dd/yyyy")
                    val d1 = sdformat.parse(fromDate)
                    datePicker.datePicker.minDate = d1.time
                    val currentDate = sdformat.parse(
                        DateUtils.getUTCDateTimeCameroon(
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

    private fun updateUI(response: Resource<GenericReqAndResp<List<VegaNigeriaSecretId>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            secretIdList.clear()
                            secretIdList.addAll(it.data!!.data)
                            if (secretIdList.size > 0) {
                                secretIdList =
                                    secretIdList.filter { it.qcFlag == "" && it.wtype == "PROCURE" } as MutableList<VegaNigeriaSecretId>
                                var fromDate = binding.tvFromDate.text.split("/")
                                var ToDate = binding.tvToDate.text.split("/")
                                secretIdList = secretIdList.filter {
                                    it.erdat.substring(0, 4).toInt() >= fromDate.get(2).trim()
                                        .toInt()
                                            && ((it.erdat.substring(4, 6).toInt() == fromDate.get(0)
                                        .trim().toInt()
                                            && it.erdat.substring(6, 8).toInt() >= fromDate.get(1)
                                        .trim().toInt()) || it.erdat.substring(4, 6)
                                        .toInt() > fromDate.get(0).trim().toInt()) &&
                                            it.erdat.substring(0, 4).toInt() <= ToDate.get(2).trim()
                                        .toInt()
                                            && ((it.erdat.substring(4, 6).toInt() == ToDate.get(0)
                                        .trim().toInt()
                                            && it.erdat.substring(6, 8).toInt() <= ToDate.get(1)
                                        .trim().toInt()) || it.erdat.substring(4, 6)
                                        .toInt() < ToDate.get(0).trim().toInt())
                                } as MutableList<VegaNigeriaSecretId>

                            }
                            /* var gateEntryWbids: List<String>? =
                                 secretIdList[0]?.wsItemSetDetails?.map { it.weighBridgeId }
                             var offloadedwbids: List<String>? =
                                 secretIdList[0]?.qcRecordSetDetails?.filter { it.qcFlag == "" }
                                     ?.map { it.weighBridgeId }*/
                            //  var list = ArrayList<String>()
                            /*if (gateEntryWbids != null) {
                                list.addAll(gateEntryWbids)
                            }
                            if (offloadedwbids != null) {
                                list.addAll(offloadedwbids)
                            }*/
                            // println("Roshna => ${list}")
                            //secretIdList =
                            // secretIdList.filter { it.weighBridgeId in list } as MutableList<VegaNigeriaSecretId>

                            //secretIdList = secretIdList.sortByDescending { getFormated(it.erdat) }
                            // }
                            //secretIdList =  secretIdList.filter {it.qcFlag == "" && it.wtype == "PROCURE"} as MutableList<VegaNigeriaSecretId>
                            //println("Roshna => ${secretIdList}")
                            setUpAdapter(secretIdList)

                        }
                        else -> UIUtils.showErrorDialog(requireContext(), "${it.data?.message}")
                    }
                }
                Resource.Status.LOADING -> showCustomLoading()
                Resource.Status.ERROR -> {
                    hideCustomLoading()
                    setUpAdapter(secretIdList)
                    UIUtils.showErrorDialog(requireContext(), "${it.error}")
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(R.menu.search_vega_secretidnigeria_menu, menu)
        //search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
        //searchView = search?.actionView as SearchView?
        try {
            val search = menu.findItem(R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))
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
                                    if (lot.chargeNum.contains(text) == true) {
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

    private fun setUpAdapter(lotList: MutableList<VegaNigeriaSecretId>) {
        if (lotList.size > 0) {
            binding.rvInspectiontLots.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvInspectiontLots.gone()
            binding.tvNoData.visible()
        }
        binding.rvInspectiontLots.setUp(
            lotList,
            R.layout.item_vega_nigeria_secretid_details,
            { it, pos ->
                tvWbId.text = it.weighBridgeId
                tvBatchNo.text = it.vehNo
                tvMaterialName.text = it.bagCount
//            tvMaterialName.text = it.bagCount
//            tvWeight.text = it.weight.plus(" ").plus(it.unitOfMeasure)
                tvDateTxt.text = getFormatedDate(it.erdat.toString())

            },
            {
                val item = this
                callBack?.replaceQualityFragment(PARAMS_FRAG, item)
            })
    }
}
