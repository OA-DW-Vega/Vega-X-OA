package com.olam.warehouse.vegax.stockrecon.ui.reconreport

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.model.Plant
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.ui.widget.VegaCustomSingleSelectDialogWithSearch
import com.olam.warehouse.presentation.ui.widget.VegaSingleSelectListener
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.stockrecon.R
import com.olam.warehouse.vegax.stockrecon.data.domian.model.VegaReconReportBundleData
import com.olam.warehouse.vegax.stockrecon.databinding.FragmentReconReportDatePlantSelectionBinding
import com.olam.warehouse.vegax.stockrecon.ui.callback.VegaStockCallbackListener
import com.olam.warehouse.vegax.stockrecon.utils.RECON_REPORT_STATISTICS
import com.olam.warehouse.vegax.stockrecon.vm.VegaStockReconViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class VegaReconReportDatePlantSelectionFragement : BaseFragment(), VegaSingleSelectListener {
    override val layoutResourceId = R.layout.fragment_recon_report_date_plant_selection
    private lateinit var binding: FragmentReconReportDatePlantSelectionBinding
    private var callBack: VegaStockCallbackListener? = null
    private val vm: VegaStockReconViewModel by viewModel()
    private val fromToDateFormat = "dd-MM-yyyy"
    private val UTC = "UTC"

    /*As of now we allowed 90 days in calendar*/
    private val maxAllowedFromDate = 1000L * 60L * 60L * 24L * 90L

    var plantList = mutableListOf<Plant>()
    private var hyphen: String = "-"
    private var customDialog: VegaCustomSingleSelectDialogWithSearch? = null
    var bundleData = VegaReconReportBundleData()


    companion object {
        fun newInstance() = VegaReconReportDatePlantSelectionFragement().putArgs {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaStockCallbackListener
        plantList = Gson().fromJson<List<Plant>>(PreferenceHelper.get(Constants.PLANT_LIST, "")) as MutableList<Plant>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentReconReportDatePlantSelectionBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        clickListener()
    }

    private fun clickListener() {
        binding.tvFromDate.setOnClickListener { getDatePickerDialog("From") }
        binding.tvToDate.setOnClickListener {
            /*if "From" date is empty, we should not allow user to enter "TO" date*/
            if (binding.tvFromDate.text.toString().isEmpty()) {
                showSnack(getString(R.string.kindly_select_from_date))
            } else {
                getDatePickerDialog("To")
            }
        }
        binding.btnProceed.setOnClickListener { validateToProceed() }
        binding.tvPlant.setOnClickListener { showSingleSelectDialog(getString(R.string.select_plant)) }
    }


    private fun getDatePickerDialog(label: String) {
        val cal = Calendar.getInstance()
        val dateFormat = fromToDateFormat
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

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            when (label) {
                "From" -> {
                    /*Setting minimum allowed date for FROM date picker*/
                    datePicker.datePicker.minDate = System.currentTimeMillis() - maxAllowedFromDate
                }

                "To" -> {
                    /*In "TO" date picker dialog, we wrote logic to restrict user to select beyound "FROM" Date*/
                    var fromDate = binding.tvFromDate.text.toString()
                    val sdformat = SimpleDateFormat(fromToDateFormat)
                    val d1 = sdformat.parse(fromDate)
                    datePicker.datePicker.minDate = d1.time
                    datePicker.datePicker.maxDate = System.currentTimeMillis()
                }
            }
            datePicker.show()
            datePicker.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    private fun showSingleSelectDialog(title: String) {
        /*The below things to show plant list in dialog box*/
        var list = plantList.map { it.plantId.plus(hyphen).plus(it.plantName) } as ArrayList<String>
        customDialog =
            VegaCustomSingleSelectDialogWithSearch(
                title,
                false, false, false,
                list,
                requireActivity(),
                this, isOrigin = false, isDepartment = false
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    /*The below method, is to get the user clicked item data from dialog box*/
    override fun clickOnItem(data: String, isWh: Boolean, isVendor: Boolean, isGrade: Boolean) {
        customDialog?.dismiss()
        binding.tvPlant.text = data
    }

    private fun validateToProceed() {
        if (binding.tvPlant.text.toString().isEmpty()) {
            showSnack(getString(R.string.select_plant_storage_warning_msg))
            return
        }
        if (binding.tvFromDate.text.toString().isEmpty()) {
            showSnack(getString(R.string.kindly_select_from_date))
            return
        }
        if (binding.tvToDate.text.toString().isEmpty()) {
            showSnack(getString(R.string.kindly_select_to_date))
            return
        }
        bundleData.plantAndDate?.plant = binding.tvPlant.text.toString().trim()
        bundleData.plantAndDate?.fromDate = binding.tvFromDate.text.toString().trim()
        bundleData.plantAndDate?.toDate = binding.tvToDate.text.toString().trim()
        bundleData.selectedDaysCount = DateUtils.getDaysCountBwTwoDates(
            bundleData.plantAndDate?.fromDate!!,
            bundleData.plantAndDate?.toDate!!
        ).toString()

        moveToReconReportStatPage()
    }

    private fun moveToReconReportStatPage() {
        callBack?.replaceFragment(RECON_REPORT_STATISTICS, bundleData)
    }
}
