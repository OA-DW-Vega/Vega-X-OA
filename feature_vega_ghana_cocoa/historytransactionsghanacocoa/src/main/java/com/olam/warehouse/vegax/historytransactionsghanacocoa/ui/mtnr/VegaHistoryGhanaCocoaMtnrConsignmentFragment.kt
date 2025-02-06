package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.historytransactionsghanacocoa.R
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtnrHistoryTransactions
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.vegax.historytransactionsghanacocoa.databinding.FragmentHistoryTransactionsGhanaCocoaMtnrConsignmentBinding
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionGhanaCocoaViewModel
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.BAG
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.HISTORY_DATA_MTNR
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.MATERIAL_CODE
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.convertKgToBag
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryGhanaCocoaMtnrConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cocoa_mtnr_consignment
    private lateinit var binding: FragmentHistoryTransactionsGhanaCocoaMtnrConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaHistoryTransactionGhanaCocoaViewModel by viewModel()

    private var historyData = VegaGhanaCocoaMtnrHistoryTransactions()


    companion object{
        fun newInstance(historyTransactions: VegaGhanaCocoaMtnrHistoryTransactions) = VegaHistoryGhanaCocoaMtnrConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_MTNR, historyTransactions)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCocoaMtnrConsignmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/mtnr/VegaHistoryTransactionsMtnrListFragment").title("History Transactions - MTN-R").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_MTNR)!!
        binding.tvDeliveryValue.setText(historyData.deliveryNumber)
        binding.tvWbIdValue.setText(historyData.wbId)
        binding.tvDateValue.setText(DateUtils.getFormatedDate(historyData.postingDate.toString()))
        binding.tvUOMValue.setText(BAG)
        binding.tvWaybillValue.setText(historyData.wayBillNumber)
        binding.tvMaterialCodeValue.setText(historyData.materialCode)
        binding.tvSendingWHValue.setText(historyData.sendingLocation)
        binding.tvGRNValue.setText(historyData.grnNumber)
        binding.tvEvacuationValue.setText(historyData.evacuationCertificate)
        binding.tvDriverNameValue.setText(historyData.driverName)
        binding.tvDriverLicenseValue.setText(historyData.driverLicenseNumber)
        binding.tvDriverPhoneValue.setText(historyData.driverContactNumber)
        binding.tvTruckNoValue.setText(historyData.truckNumber)
        binding.tvTransporterValue.setText(historyData.transporterName)
        if(!historyData.quantity.isNullOrEmpty() && !historyData.materialCode.isNullOrEmpty() ) {
            val bagCount = convertKgToBag(historyData.materialCode.toString(), historyData.quantity.toString())
            binding.tvNoOfBagsValue.setText(bagCount.toString())
        }

        binding.btnOk.setOnClickListener {
            requireActivity().finish()
        }
    }
}
