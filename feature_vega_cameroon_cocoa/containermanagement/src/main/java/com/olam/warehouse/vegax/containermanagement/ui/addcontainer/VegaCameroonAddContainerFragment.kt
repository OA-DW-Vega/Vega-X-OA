package com.olam.warehouse.vegax.containermanagement.ui.addcontainer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonContainerSize
import com.olam.warehouse.master.vegacameroon.entity.VegaCameroonShippingLine
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeSalesLots
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.AppUtils
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagement.R
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainer
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerPost
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonAddContainerResponse
import com.olam.warehouse.vegax.containermanagement.data.domain.model.VegaCameroonContainer
import com.olam.warehouse.vegax.containermanagement.databinding.FragmentVegaCameroonAddContainerBinding
import com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerCustomSingleSelectDialog
import com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.text.SimpleDateFormat
import java.util.*

class VegaCameroonAddContainerFragment : BaseFragment(),
    VegaCameroonContainerCustomSingleSelectListener {

    private val vm: VegaCameroonContainerManagementViewModel by viewModel()
    private lateinit var binding: FragmentVegaCameroonAddContainerBinding
    override val layoutResourceId = R.layout.fragment_vega_cameroon_add_container

    private var containerData = VegaCameroonAddContainer()
    private var postData = VegaCameroonAddContainer()
    private var containerSizeList = mutableListOf<VegaCameroonContainerSize>()
    private var shippingLineList = mutableListOf<VegaCameroonShippingLine>()
    private var customDialog: VegaCameroonContainerCustomSingleSelectDialog? = null

    companion object {
        fun newInstance() = VegaCameroonAddContainerFragment().putArgs {
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaCameroonAddContainerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/addcontainer/VegaCameroonAddContainerFragment").title("Vega_Cameroon/Containermanagement")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        updateMandatory()
        vm.getContainerSizeList()
        vm.containerSize.observe(
            viewLifecycleOwner,
            Observer { containerSizeList = it.toMutableList() })
        vm.getShippingLineList()
        vm.shippingLine.observe(
            viewLifecycleOwner,
            Observer { shippingLineList = it.toMutableList() })
        vm.container.observe(viewLifecycleOwner, Observer { updateUI(it) })


        binding.tvDate.text =
            DateUtils.getUTCDateTime(System.currentTimeMillis().toString(), App.getAppContext())
        binding.tvContainerSizeValue.setOnClickListener {
            showCustomDialogBox(true, false)
        }
        binding.tvShippingLineValue.setOnClickListener {
            showCustomDialogBox(false, true)
        }
        binding.btnConfirm.setOnClickListener { validateInputs() }

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
            VegaCameroonContainerCustomSingleSelectDialog(
                getString(R.string.select_container_size),
                isContainerSize,
                isShippingLine,
                list,
                requireActivity(),
                this
            )
        customDialog?.show()
        customDialog?.setCanceledOnTouchOutside(false)
    }


    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_message)
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { postAddContainer() },
                { dismiss() })
        }
    }

     fun postAddContainer() {
         containerData.containerNum = binding.tvContainerNoValue.text.toString()
         containerData.containerSize = binding.tvContainerSizeValue.text.toString()
         containerData.containerWeight = binding.tvContainerWeightValue.text.toString()
         containerData.shippingLine = binding.tvShippingLineValue.text.toString().split(":")[1]
         containerData.status = "New Container"
         containerData.uom = "MT"
         containerData.entryDate = binding.tvDate.text.toString()
        postData = containerData

        if (AppUtils.isOnline()) {



            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val newFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            var date = sdf.parse(containerData.entryDate)
            var d = newFormat.format(date)

            vm.postContainerData(
                VegaCameroonAddContainerPost(
                    containerDto = VegaCameroonContainer(
                        id = 0,
                        containerNum = containerData.containerNum,
                        containerWeight = containerData.containerWeight,
                        containerSize = containerData.containerSize,
                        shippingLine = containerData.shippingLine,
                        status = containerData.status,
                        uom = containerData.uom,
                        entryDate = newFormat.format(date),
                        plantDto =  getPlantDetails()
                        ),
                    editFlag = false,
                    editedContainerNum = "",
                    key = getCurrentKey()
                )
            )

        }
    }

     private fun updateUI(data: Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    when (it.data?.success) {
                        true -> {
                            prepareSuccessData(containerData.containerNum)
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

    private fun prepareSuccessData(wbId: String?) {
        hideLoading()
        moveToSuccessPage(wbId)
    }

    private fun moveToSuccessPage(containerID: String?) {
        val intent = Intent(requireContext(), SuccessActivity::class.java)
        intent.putExtra(AppUtils.TITLE, getString((R.string.add_container_success_message)))

        intent.putExtra(
            AppUtils.SUB_TITLE,
            getString((R.string.container_id)).plus(": ")
                .plus(containerID)
        )

        val lotlist = ArrayList<VegaCoffeeSalesLots>()
        lotlist.add(
            VegaCoffeeSalesLots(
                "",
                containerData.containerNum,
                "",
                containerData.containerSize,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                binding.tvShippingLineValue.text.toString()
            )
        )

        intent.putExtra(UIUtils.FROM_CAMEROON_COCOA_ADD_CONTAINER, true)
        intent.putParcelableArrayListExtra(AppUtils.LOT_CARD, lotlist)
        intent.putExtra(AppUtils.PRINT_ENABLE, true)

        startActivity(intent)
        requireActivity().finish()
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
}


