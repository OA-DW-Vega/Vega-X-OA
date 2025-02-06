package com.olam.wharhouse.vegax.transactionhistory.ui.mtnr

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.model.VegaMtnrHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsMtnrConsignmentBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_MTNR


class VegaHistoryMtnrConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_mtnr_consignment
    private lateinit var binding: FragmentHistoryTransactionsMtnrConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel by viewModel()

    private var historyData = VegaMtnrHistoryTransactions()


    companion object{
        fun newInstance(historyTransactions: VegaMtnrHistoryTransactions) = VegaHistoryMtnrConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_MTNR, historyTransactions)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsMtnrConsignmentBinding.inflate(inflater)
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
        binding.tvUOMValue.setText(historyData.uom)
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
        binding.tvSTOValue.setText(historyData.stoNumber)
        binding.tvTransitLossValue.setText(historyData.transitLoss)
        if(!historyData.quantity.isNullOrEmpty() && !historyData.materialCode.isNullOrEmpty() ) {
           // val bagCount = convertKgToBag(historyData.materialCode.toString(), historyData.quantity.toString())
            binding.tvNoOfBagsValue.setText(historyData.quantity.toString())
        }

        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
