package com.olam.wharhouse.vegax.transactionhistory.ui.rmin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsRminConsignmentBinding
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaTransHisViewModel
import com.olam.wharhouse.vegax.transactionhistory.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_DATA_RMIN
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
class VegaHistoryRminConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_rmin_consignment
    private lateinit var binding: FragmentHistoryTransactionsRminConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaTransHisViewModel by viewModel()
    private var historyData = VegaRMINHistoryTransactions()
    private var isAdminUser= false


    companion object{
        fun newInstance(historyTransactions: VegaRMINHistoryTransactions) = VegaHistoryRminConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_RMIN, historyTransactions)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsRminConsignmentBinding.inflate(inflater)
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
        binding.tvPlantIdvalue.text=historyData.storageLocation?.plant
        binding.tvProcessingtypevalue.text=historyData.processingType
        binding.tvStorageLocationValue.text=historyData.storageLocation?.storageLocationName
        if(PreferenceHelper.get(Constants.ADMIN_USER,false)) isAdminUser= true
        if (isAdminUser){
            binding.tvusertypeValue.text = getString(R.string.admin_user)
        }
        else {
            binding.tvusertypeValue.text= getString(R.string.warehouse_user)
        }

        


        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }
    }

}
