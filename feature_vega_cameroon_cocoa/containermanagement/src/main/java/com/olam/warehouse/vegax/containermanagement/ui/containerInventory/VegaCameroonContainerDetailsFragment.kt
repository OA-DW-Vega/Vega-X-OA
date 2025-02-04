package com.olam.warehouse.vegax.containermanagement.ui.containerInventory

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
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
import com.olam.warehouse.vegax.containermanagement.data.domain.model.*
import com.olam.warehouse.vegax.containermanagement.databinding.FragmentVegaCameroonContainerDetailsBinding
import com.olam.warehouse.vegax.containermanagement.ui.VegaCameroonContainerManagementViewModel
import com.olam.warehouse.vegax.containermanagement.utils.CONTAINER_DETAILS
import kotlinx.android.synthetic.main.fragment_vega_cameroon_container_details.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaCameroonContainerDetailsFragment : BaseFragment() {
    private lateinit var containerDetails: ContainerInventory
    private lateinit var binding: FragmentVegaCameroonContainerDetailsBinding
    private val vm: VegaCameroonContainerManagementViewModel by viewModel()
    private lateinit var mListener: onStuffingDetailsNavigateListner
    private var postDeleteContainer = VegaCameroonAddContainer()

    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_cameroon_container_details

    interface onStuffingDetailsNavigateListner{
        fun navigateToStuffingDetails(bundle: Bundle)
        fun navigateToEditContainer(bundle:Bundle)
        fun onDeleteContainer()
        fun onNavigateBack()
    }

    companion object {
        fun newInstance(bundle: Bundle) =
            VegaCameroonContainerDetailsFragment().apply {
                putArgs {
                    putBundle("BUNDLE_DATA", bundle)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVegaCameroonContainerDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/containerInventory/VegaCameroonContainerDetailsFragment")
            .title("Vega_Cameroon/Containermanagement").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as onStuffingDetailsNavigateListner
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
            getActionBtnChangedView(binding.btProceed, it, true)
        }
        val bundle = arguments?.getBundle("BUNDLE_DATA")
        containerDetails = bundle?.getSerializable(CONTAINER_DETAILS) as ContainerInventory
        vm.container.observe(viewLifecycleOwner, Observer {
            handleDeleteContainer(it)
        })

        binding.tvContainerNoValue.text = containerDetails.containerNum
        binding.tvContainerSizeValue.text = containerDetails.containerSize
        binding.tvContainerWeightValue.text = containerDetails.containerWeight.plus(" ").plus("MT")
        binding.tvShippingLineValue.text = containerDetails.shippingLine
        binding.tvDaysStandingValue.text = containerDetails.entryDate.split(" ")[0]
        binding.tvDaysValue.text = DateUtils.getNoOfDaysStanding(containerDetails.entryDate, App.getAppContext()).toString().plus(" Days")
        binding.tvStatusValue.text = containerDetails.status

        binding.btProceed.setOnClickListener {
            mListener.onNavigateBack()
        }
        if(containerDetails.status.contains("NEW",true) ){
            tv_edit.visibility = View.VISIBLE
        }
        else
            tv_edit.visibility = View.GONE

        btn_edit.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable(CONTAINER_DETAILS,containerDetails )
            }
            mListener.navigateToEditContainer(bundle)
        }
        btn_delete.setOnClickListener {
            showConfirmDialog()
        }
        binding.btStuffingDetails.setOnClickListener{
            val bundle = Bundle().apply {
                putSerializable(CONTAINER_DETAILS,containerDetails )
            }
            mListener.navigateToStuffingDetails(bundle)
        }

    }

    private fun postDeleteContainer(){
        if (AppUtils.isOnline()) {
            postDeleteContainer = VegaCameroonAddContainer(
                containerNum = containerDetails.containerNum,
                containerSize = containerDetails.containerSize,
                containerWeight = containerDetails.containerWeight,
                entryDate = containerDetails.entryDate,
                uom =  "MT",
                status = "Deleted",
                shippingLine = containerDetails.shippingLine
            )


            vm.deleteContainerData(VegaCameroonAddContainerPost(
                containerDto = VegaCameroonContainer(
                    id = containerDetails.id,
                    containerNum = containerDetails.containerNum,
                    containerWeight = containerDetails.containerWeight,
                    containerSize = containerDetails.containerSize,
                    shippingLine = containerDetails.shippingLine,
                    status = "Deleted",
                    uom = "MT",
                    entryDate = containerDetails.entryDate,
                    plantDto =  getPlantDetails()
                ),
                editFlag = true,
                editedContainerNum = "",
                key = getCurrentKey()
            ))
        }
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.delete_message)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                { postDeleteContainer() },
                { dismiss() })
        }
    }

    private fun handleDeleteContainer(data: Resource<GenericReqAndResp<VegaCameroonAddContainerResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    MaterialDialog(requireContext()).apply {
                        cancelOnTouchOutside(false)
                    }.show {
                        message(R.string.delete_success)
                        getMetirialCustomView(
                            this,
                            getString(R.string.ok),
                            "",
                            { activity?.onBackPressed() },
                            { })
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
