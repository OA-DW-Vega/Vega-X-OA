package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.grn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.historytransactionsghanacocoa.R
import com.olam.warehouse.vegax.historytransactionsghanacocoa.databinding.FragmentHistoryTransactionsGhanaCocoaGrnConsignmentBinding
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionGhanaCocoaViewModel
import com.olam.warehouse.vegax.historytransactionsghanacocoa.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.HISTORY_DATA_GRN
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryGhanaCocoaGrnConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cocoa_grn_consignment
    private lateinit var binding: FragmentHistoryTransactionsGhanaCocoaGrnConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaHistoryTransactionGhanaCocoaViewModel by viewModel()
    private var historyData = VegaGhanaCocoaGRNHistoryTransactions()


    companion object{
        fun newInstance(historyTransactionsGRN: VegaGhanaCocoaGRNHistoryTransactions) = VegaHistoryGhanaCocoaGrnConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_GRN, historyTransactionsGRN)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCocoaGrnConsignmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/grn/VegaHistoryTransactionsGrnListFragment").title("History Transactions - GRN").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_GRN)!!
        binding.tvWbIdValue.text = historyData.wbId
        binding.tvDateValue.text =  DateUtils.getFormatedDate(historyData.postingDate.toString())
        binding.tvNoOfBagsValue.text = historyData.quantity
        binding.tvUOMValue.text = historyData.uom
        binding.tvWHReceiptNoValue.text = historyData.whReceiptNumber
        binding.tvGRNValue.text = historyData.grnNumber
        binding.tvVendorCodeValue.text = historyData.vendorCode
        binding.tvVendorNameValue.text = historyData.vendorName
        binding.btnOk.setOnClickListener {
            requireActivity().finish()
        }
    }

}
