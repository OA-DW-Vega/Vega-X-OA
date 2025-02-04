package com.olam.warehouse.vegax.containermanagementnigeria.ui.containerInventory

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
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.containermanagementnigeria.R
import com.olam.warehouse.vegax.containermanagementnigeria.data.domain.model.*
import com.olam.warehouse.vegax.containermanagementnigeria.databinding.FragmentVegaNigeriaContainerDetailsBinding
import com.olam.warehouse.vegax.containermanagementnigeria.ui.VegaNigeriaContainerManagementViewModel
import com.olam.warehouse.vegax.containermanagementnigeria.utils.CONTAINER_DETAILS
import kotlinx.android.synthetic.main.fragment_vega_nigeria_container_details.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaNigeriaContainerDetailsFragment : BaseFragment() {
    private lateinit var containerDetails: ContainerInventory
    private lateinit var binding: FragmentVegaNigeriaContainerDetailsBinding
    private val vm: VegaNigeriaContainerManagementViewModel by viewModel()
    private lateinit var mListener: onStuffingDetailsNavigateListner
    private var stuffingDetails = ArrayList<VegaNigeriaContainerStuffingDetails>()
    private var postDeleteContainer = VegaNigeriaAddContainer()

    override val layoutResourceId: Int
        get() = R.layout.fragment_vega_nigeria_container_details

    interface onStuffingDetailsNavigateListner{
        fun navigateToStuffingDetails(bundle: Bundle)
        fun navigateToEditContainer(bundle:Bundle)
        fun onDeleteContainer()
        fun onNavigateBack()
    }

    companion object {
        fun newInstance(bundle: Bundle) =
            VegaNigeriaContainerDetailsFragment().apply {
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
        binding = FragmentVegaNigeriaContainerDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("containermanagement/ui/containerInventory/VegaNigeriaContainerDetailsFragment")
            .title("Vega_Nigeria/Containermanagement").with(tracker)
        initUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as onStuffingDetailsNavigateListner
    }

    private fun initUI() {
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
//            btStuffingDetails.visibility = View.GONE
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

        updateUI()
    }

    private fun postDeleteContainer(){
        if (AppUtils.isOnline()) {
            postDeleteContainer = VegaNigeriaAddContainer(
                containerNum = containerDetails.containerNum,
                containerSize = containerDetails.containerSize,
                containerWeight = containerDetails.containerWeight,
                entryDate = containerDetails.entryDate,
                uom =  "MT",
                status = "Deleted",
                shippingLine = containerDetails.shippingLine
            )
            /*vm.deleteContainerData(VegaNigeriaAddContainerPost(
                key = getCurrentKey(),
                plantDto =  getPlantDetails(),
                containerNum = containerDetails.containerNum,
                containerSize = containerDetails.containerSize,
                containerWeight = containerDetails.containerWeight,
                entryDate = containerDetails.entryDate,
                status = "Deleted",
                uom = "MT",
                shippingLine = containerDetails.shippingLine))*/

            /*println("Roshna => ${containerDetails.entryDate}")
            val sdf = SimpleDateFormat("MM/dd/yyyy")
            val newFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            var date = sdf.parse(containerDetails.entryDate)
            var d = newFormat.format(date)*/

            vm.deleteContainerData(VegaNigeriaAddContainerPost(
                containerDto = VegaNigeriaContainer(
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
                {
                    postDeleteContainer()
                },
                { dismiss() })
        }
    }

    private fun handleDeleteContainer(data: Resource<GenericReqAndResp<VegaNigeriaAddContainerResponse>>?) {
        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideCustomLoading()
                    MaterialDialog(requireContext()).apply{
                        cancelOnTouchOutside(false)
                    }.show {
                        message(R.string.delete_success)
                        positiveButton(text = UIUtils.getSpannedText("OK", isPositive = true)) {
//                            mListener?.onDeleteContainer()
                            activity?.onBackPressed()
                        }
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

    private fun updateUI() {
        println("Roshna => inside Vega Container Details Fragment")
    }


}
