package com.olam.wharhouse.vegax.transactionhistory.ui.mtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsMtntConsignmentBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_MTNT
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryMtntConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_mtnt_consignment
    private lateinit var binding: FragmentHistoryTransactionsMtntConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel by viewModel()
    private var historyData = VegaMtntHistoryTransactions()


    companion object{
        fun newInstance(historyTransactions: VegaMtntHistoryTransactions) = VegaHistoryMtntConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_MTNT, historyTransactions)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsMtntConsignmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsMtntDetailsFragment").title("History Transactions - MTNT").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_MTNT)!!
        binding.tvDeliveryValue.text = historyData.deliveryNumber
        binding.tvWbIdValue.text = historyData.wbId
        binding.tvDateValue.text =  DateUtils.getFormatedDate(historyData.postingDate.toString())
        binding.tvNoOfBagsValue.text = historyData.quantity
        binding.tvUOMValue.text = historyData.uom
        binding.tvWaybillValue.text = historyData.wayBillNumber
        binding.tvMaterialCodeValue.text = historyData.materialCode
        binding.tvDestinationWHValue.text = historyData.destinationLocation
        binding.tvEvacuationValue.text = historyData.evacuationCertificate
        binding.tvDriverNameValue.text = historyData.driverName
        binding.tvDriverLicenseValue.text = historyData.driverLicenseNumber
        binding.tvDriverPhoneValue.text = historyData.driverContactNumber
        binding.tvTruckNoValue.text = historyData.truckNumber
        binding.tvTransporterValue.text = historyData.transporterName
        binding.tvVendorNameValue.text = historyData.vendorName
        binding.tvSTOValue.text = historyData.stoNumber
        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
