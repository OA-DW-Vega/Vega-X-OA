package com.olam.warehouse.vegax.transhistoryghanacashew.ui.mtnt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewRMINHistoryTransactions
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewRminConsignmentBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHisViewModel
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_FGRN
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_MTNT
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_RMIN
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryGhanaCashewRminConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cashew_rmin_consignment
    private lateinit var binding: FragmentHistoryTransactionsGhanaCashewRminConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaGhanaCashewTransHisViewModel by viewModel()
    private var historyData = VegaGhanaCashewRMINHistoryTransactions()


    companion object{
        fun newInstance(historyTransactions: VegaGhanaCashewRMINHistoryTransactions) = VegaHistoryGhanaCashewRminConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_RMIN, historyTransactions)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCashewRminConsignmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsRminDetailsFragment").title("History Transactions - RMIN").with(tracker)
        initUI()
    }

    private fun initUI() {
        historyData = arguments?.getParcelable(HISTORY_DATA_RMIN)!!
        binding.tvprocessValue.text = historyData.processingType
        binding.tvDateValue.text = historyData.date.toString()
        binding.tvQuantityValue.text= historyData.quantity
        binding.tvDrylossValue.text = historyData.dryingLoss
        binding.tvponumberValue.text=historyData.poNumber
        binding.tvuomValue.text=historyData.unitsOfMeasure


        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
