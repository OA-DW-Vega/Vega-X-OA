package com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewFGRNHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewFgrnConsignmentBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHisViewModel
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_FGRN
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_GRN
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryGhanaCashewFgrnConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cashew_fgrn_consignment
    private lateinit var binding: FragmentHistoryTransactionsGhanaCashewFgrnConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaGhanaCashewTransHisViewModel
 by viewModel()
    private var historyData = VegaGhanaCashewFGRNHistoryTransactions()


    companion object{
        fun newInstance(historyTransactionsFGRN: VegaGhanaCashewFGRNHistoryTransactions) = VegaHistoryGhanaCashewFgrnConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_FGRN, historyTransactionsFGRN)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCashewFgrnConsignmentBinding.inflate(inflater)
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

        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
