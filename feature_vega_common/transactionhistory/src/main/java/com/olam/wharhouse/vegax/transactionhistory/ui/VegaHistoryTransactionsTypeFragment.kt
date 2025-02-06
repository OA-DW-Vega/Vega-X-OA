package com.olam.wharhouse.vegax.transactionhistory.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.model.VegaFGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaGRNHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtnrHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaMtntHistoryTransactions
import com.olam.warehouse.master.vega.model.VegaRMINHistoryTransactions
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transactionhistory.R
import com.olam.wharhouse.vegax.transactionhistory.databinding.FragmentHistoryTransactionsTypeBinding
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_FGRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_GRN
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_MTNR
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_MTNT
import com.olam.wharhouse.vegax.transactionhistory.utils.HISTORY_TRANSACTIONS_RMIN


class VegaHistoryTransactionsTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_history_transactions_type
    private lateinit var binding: FragmentHistoryTransactionsTypeBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private var historyMtnt = VegaMtntHistoryTransactions()
    private var historyMtnr = VegaMtnrHistoryTransactions()
    private var historyGrn = VegaGRNHistoryTransactions()

    private var historyFgrn = VegaFGRNHistoryTransactions()
    private var historyRmin = VegaRMINHistoryTransactions()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaHistoryTransactionsTypeFragment().putArgs {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryTransactionsTypeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("historytransactions/ui/VegaHistoryTransactionsGhanaCashewTypeFragment").title("History Transactions")
            .with(tracker)
    }

    private fun initUI() {
        DisplayItems()
        binding.llMtnt.setOnClickListener {
            moveToMtnt()
        }
        binding.llMtnr.setOnClickListener {
            moveToMtnr()
        }
        binding.llHistoryTrans.setOnClickListener {
            moveToGrn()
        }

        binding.llHistoryTransfgrn.setOnClickListener {
            moveToFgrn()
        }

        binding.llHistoryTransrmin.setOnClickListener {
            moveToRmin()
        }
    }

    private fun DisplayItems()
    {
        val roleData =
            Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
                .filter { key ->
                    key.roleKey.equals(
                        PreferenceHelper.get(
                            Constants.CURRENT_KEY,
                            ""
                        )
                    )
                }
        var roleNames = roleData.map { it-> it.roleName }


        if(roleNames.contains(UserRoles.ROLE_ADMIN.toString())){
            PreferenceHelper.save(Constants.ADMIN_USER,true)
            binding.llHistoryTrans.visible()
            binding.llMtnt.visible()
            binding.llMtnr.visible()
            binding.llHistoryTransfgrn.visible()
            binding.llHistoryTransrmin.visible()
        }else{
            roleData.forEach { rol ->
                when (UserRoles.valueOfEnum(rol.roleName.trim())) {


                    UserRoles.GRN ->{
                        binding.llHistoryTrans.visible()
                    }

                    UserRoles.MTNT -> {
                        binding.llMtnt.visible()
                    }
                    UserRoles.DISPATCH -> {
                        binding.llMtnt.visible()
                    }
                    UserRoles.QUALITY-> {
                        binding.llMtnr.visible()
                    }
                    UserRoles.MTNR-> {
                        binding.llMtnr.visible()
                    }

                    UserRoles.PROCESSING -> {
                        binding.llHistoryTransfgrn.visible()
                        binding.llHistoryTransrmin.visible()
                    }
                    else -> {}
                }
            }
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

    private fun moveToFgrn() {
        callBack?.replaceFragment(
            HISTORY_TRANSACTIONS_FGRN, historyFgrn
        )
    }

    private fun moveToRmin() {
        callBack?.replaceFragment(
            HISTORY_TRANSACTIONS_RMIN, historyRmin
        )
    }


}
