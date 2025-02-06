package com.olam.warehouse.vegax.historytransactionsghanacocoa.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.historytransactionsghanacocoa.R
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaGRNHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtnrHistoryTransactions
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCocoaMtntHistoryTransactions
import com.olam.warehouse.vegax.historytransactionsghanacocoa.databinding.FragmentHistoryTransactionsGhanaCocoaTypeBinding
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.HISTORY_TRANSACTIONS_GRN
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.HISTORY_TRANSACTIONS_MTNR
import com.olam.warehouse.vegax.historytransactionsghanacocoa.utils.HISTORY_TRANSACTIONS_MTNT
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryTransactionsGhanaCocoaTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_history_transactions_ghana_cocoa_type
    private lateinit var binding: FragmentHistoryTransactionsGhanaCocoaTypeBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private var historyMtnt = VegaGhanaCocoaMtntHistoryTransactions()
    private var historyMtnr = VegaGhanaCocoaMtnrHistoryTransactions()
    private var historyGrn = VegaGhanaCocoaGRNHistoryTransactions()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaHistoryTransactionsGhanaCocoaTypeFragment().putArgs {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryTransactionsGhanaCocoaTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsGhanaCocoaTypeFragment").title("History Transactions")
            .with(tracker)
    }

    private fun initUI() {
        binding.llRmin.setOnClickListener {
            moveToMtnt()
        }
        binding.llFgrn.setOnClickListener {
            moveToMtnr()
        }
        binding.llHistoryTrans.setOnClickListener {
            moveToGrn()
        }
    }

    private fun moveToMtnt() {
        callBack?.replaceFragment(
            HISTORY_TRANSACTIONS_MTNT, historyMtnt
        )
    }

    private fun moveToMtnr() {
        callBack?.replaceFragment(
            HISTORY_TRANSACTIONS_MTNR, historyMtnr
        )
    }

    private fun moveToGrn() {
        callBack?.replaceFragment(
            HISTORY_TRANSACTIONS_GRN, historyGrn
        )
    }
}
