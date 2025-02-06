package com.olam.wharhouse.vegax.transactionhistory.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsFgrnConsignmentBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_FGRN
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaTransHistoryFgrnConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_fgrn_consignment
    private lateinit var binding: FragmentHistoryTransactionsFgrnConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel
 by viewModel()
    private var historyData = VegaFGRNHistoryTransactions()


    companion object{
        fun newInstance(historyTransactionsFGRN: VegaFGRNHistoryTransactions) = VegaTransHistoryFgrnConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_FGRN, historyTransactionsFGRN)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsFgrnConsignmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/fgrn/VegaHistoryTransactionsGGrnListFragment").title("History Transactions - FGRN").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_FGRN)!!
        binding.tvprocessValue.text = historyData.processingType
        binding.tvDateValue.text =  historyData.date.toString()
        binding.tvQuantityValue.text= historyData.quantity
        binding.tvDrylossValue.text = historyData.dryingLoss
        binding.tvponumberValue.text=historyData.poNumber
        binding.tvuomValue.text=historyData.unitsOfMeasure
        binding.tvPlantValue.text=historyData.storageLocation?.plant
        binding.tvStLocValue.text=historyData.storageLocation?.storageLocationCode.plus("-").plus(historyData.storageLocation?.storageLocationName)


        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
