package com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.LocaleHelper
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.*
import com.olam.warehouse.vegax.containermanagementnigeria.databinding.FragmentVegaNigeriaContainerEditBinding
import com.olam.warehouse.vegax.containermanagementnigeria.ui.VegaNigeriaContainerCustomSingleSelectDialog
import com.olam.warehouse.vegax.containermanagementnigeria.ui.VegaNigeriaContainerManagementViewModel
import com.olam.warehouse.vegax.containermanagementnigeria.ui.addcontainer.VegaNigeriaContainerCustomSingleSelectListener
import com.olam.warehouse.vegax.containermanagementnigeria.utils.CONTAINER_DETAILS
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*


class VegaNigeriaContainerEditFragment : BaseFragment(),
    VegaNigeriaContainerCustomSingleSelectListener {

    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_container_edit
    private lateinit var binding: FragmentVegaNigeriaContainerEditBinding

    private lateinit var containerDetails: ContainerInventory
    private val vm: VegaNigeriaContainerManagementViewModel by viewModel()
    private var postData = VegaNigeriaAddContainer()
    private var containerData = VegaNigeriaAddContainer()
    private var callBack: VegaContainerEditCallback? = null
    private var containerSizeList = mutableListOf<VegaCameroonContainerSize>()
    private var shippingLineList = mutableListOf<VegaCameroonShippingLine>()
    private var customDialog: VegaNigeriaContainerCustomSingleSelectDialog? = null

    interface VegaContainerEditCallback{
        fun onEditContainerSuccess()
    }
    companion object {
        fun newInstance(bundle:Bundle) =
            VegaNigeriaContainerEditFragment().apply {
                putArgs {
                    putBundle("BUNDLE_DATA", bundle)
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaContainerEditCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNigeriaContainerEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/containerInventory/VegaNigeriaContainerEditFragment")
            .title("Vega_Nigeria/Containermanagement").with(tracker)
        initUI()
    }

    private fun initUI() {
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        containerDetails = bundle?.getSerializable(CONTAINER_DETAILS) as ContainerInventory

        updateMandatory()

        vm.getContainerSizeList()
        vm.containerSize.observe(viewLifecycleOwner, Observer { containerSizeList = it.toMutableList() })
        vm.getShippingLineList()
        vm.shippingLine.observe(viewLifecycleOwner, Observer { shippingLineList = it.toMutableList()})

//        binding.tvDate.setOnClickListener { getDatePickerDialog() }
        binding.tvContainerSizeValue.setOnClickListener {
            showCustomDialogBox(true,false)
        }
        binding.tvShippingLineValue.setOnClickListener {
            showCustomDialogBox(false,true)
        }
        var date = containerDetails.entryDate

        var spf = SimpleDateFormat("yyyy-MM-dd")
        val newDate = spf.parse(date)
        spf = SimpleDateFormat("MM/dd/yyyy")
        date = spf.format(newDate)

        binding.tvDate.text = date
        binding.tvContainerNoValue.setText(containerDetails.containerNum)
        binding.tvContainerSizeValue.text = containerDetails.containerSize
        binding.tvContainerWeightValue.setText(containerDetails.containerWeight)
        binding.tvShippingLineValue.text = containerDetails.shippingLine

        vm.container.observe(viewLifecycleOwner, Observer { updateUI(it) })

        binding.btnSave.setOnClickListener{
            //API to save the container details.
            validateInputs()
        }
    }

    private fun updateMandatory() {
        binding.tvContainerNoLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.container_no)) { mandatoryStars() } }
        binding.tvContainerSizeLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.container_size)) { mandatoryStars() } }
        binding.tvContainerWeightLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.container_weight)) { mandatoryStars() } }
        binding.tvShippingLineLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.shipping_line)) { mandatoryStars() } }
        binding.tvContainerDateLabel.text =
            with(UIUtils) { with(requireContext().resources.getString(R.string.date_of_entry)) { mandatoryStars() } }

    }

    private fun showCustomDialogBox(isContainerSize:Boolean,isShippingLine: Boolean) {
        var list: ArrayList<String> = ArrayList()
        if (isContainerSize) {
            list = containerSizeList.map{data -> data.containerSize} as  ArrayList<String>
        } else if (isShippingLine) {
            list = shippingLineList.map{data -> data.shippingLineNm.plus(":").plus(data.shippingLineDesc)} as  ArrayList<String>
        }

        customDialog =
            VegaNigeriaContainerCustomSingleSelectDialog(
                getString(R.string.select_container_size),
                isContainerSize,
                isShippingLine,
                list,
                activity!!,
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }

    private fun getDatePickerDialog() {
        val cal = Calendar.getInstance()
        val dateTxt = binding.tvDate.text.split("/")
        cal.set(dateTxt[2].toInt(), dateTxt[0].toInt() - 1, dateTxt[1].toInt())
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
                    binding.tvDate.text = sdf.format(cal.time)
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

    private fun updateUI(data: Resource<GenericReqAndResp<VegaNigeriaAddContainerResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            MaterialDialog(requireContext()).apply{
                                cancelOnTouchOutside(false)
                            }.show {
                                message(R.string.save_success)
                                positiveButton(text = UIUtils.getSpannedText("OK", isPositive = true)) {
//                            callBack?.onEditContainerSuccess()
                                    activity?.onBackPressed()
                                }
                            }
                        }
                        else -> {
                            showErrorDialogWithFAQLink(requireContext(), "${it.data?.data?.message}")
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

    private fun validateInputs() {
        when {
            binding.tvDate.text.isNullOrEmpty() -> showSnack(getString(R.string.enter_valid_date))
            binding.tvContainerNoValue.text.isNullOrEmpty() -> showSnack(getString(R.string.valid_container_no))
            binding.tvContainerSizeValue.text.isNullOrEmpty() -> showSnack(getString(R.string.valid_container_size))
            binding.tvContainerWeightValue.text.isNullOrEmpty() -> showSnack(getString(R.string.valid_container_weight))
            binding.tvShippingLineValue.text.isNullOrEmpty() -> showSnack(getString(R.string.valid_shipping_line))
            else -> showConfirmDialog()

        }
    }

    override fun clickOnItem(data: String, isContainerSize: Boolean,isShippingLine: Boolean) {
        customDialog?.dismiss()
        if (isContainerSize) {
            binding.tvContainerSizeValue.text = data
        }else if (isShippingLine){
            binding.tvShippingLineValue.text = data
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.save_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    postSaveContainer()
                },
                { dismiss() })
        }
    }

    private fun postSaveContainer() {
        containerData.containerNum = binding.tvContainerNoValue.text.toString()
        containerData.containerSize = binding.tvContainerSizeValue.text.toString()
        containerData.containerWeight = binding.tvContainerWeightValue.text.toString()
//        containerData.shippingLine = binding.tvShippingLineValue.text.toString().split(":")[1]
        if(!binding.tvShippingLineValue.text.toString().contains(":")){
            containerData.shippingLine = binding.tvShippingLineValue.text.toString()
        }
        else
            containerData.shippingLine = binding.tvShippingLineValue.text.toString().split(":")[1]
        containerData.entryDate = binding.tvDate.text.toString()
        containerData.status = "New Container"
        containerData.uom = "MT"
        postData = containerData

        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val newFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
        var date = sdf.parse(containerData.entryDate)
        var d = newFormat.format(date)

        if (AppUtils.isOnline()) {
           /* vm.saveContainerData(VegaNigeriaAddContainerPost(
                key = getCurrentKey(),
                plantDto = getPlantDetails(),
                containerNum = containerData.containerNum,
                containerSize = containerData.containerSize,
                containerWeight = containerData.containerWeight,
                entryDate = containerData.entryDate,
                status = containerData.status,
                uom = "MT",
                shippingLine = containerData.shippingLine))*/
            var editedcontainerNum = ""
            if(containerData.containerNum != containerDetails.containerNum)
                editedcontainerNum = containerData.containerNum

            vm.saveContainerData(VegaNigeriaAddContainerPost(
                containerDto = VegaNigeriaContainer(
                    id = containerDetails.id,
                    containerNum = containerDetails.containerNum,
                    containerWeight = containerData.containerWeight,
                    containerSize = containerData.containerSize,
                    shippingLine = containerData.shippingLine,
                    status = containerData.status,
                    uom = containerData.uom,
                    entryDate = newFormat.format(date),
                    plantDto =  getPlantDetails()
                ),
                editFlag = true,
                editedContainerNum = editedcontainerNum,
                key = getCurrentKey()
            ))
        }
    }
}
