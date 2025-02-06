package com.olam.wharhouse.vegax.transhistoryghanacashew.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vegaghana.entity.*
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
//import com.olam.warehouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewTypeBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewTypeBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.*


class VegaHistoryTransactionsGhanaCashewTypeFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_history_transactions_ghana_cashew_type
    private lateinit var binding: FragmentHistoryTransactionsGhanaCashewTypeBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private var historyMtnt = VegaGhanaCashewMtntHistoryTransactions()
    private var historyMtnr = VegaGhanaCashewMtnrHistoryTransactions()
    private var historyGrn = VegaGhanaCashewGRNHistoryTransactions()

    private var historyFgrn = VegaGhanaCashewFGRNHistoryTransactions()
    private var historyRmin = VegaGhanaCashewRMINHistoryTransactions()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    companion object {
        fun newInstance() = VegaHistoryTransactionsGhanaCashewTypeFragment().putArgs {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHistoryTransactionsGhanaCashewTypeBinding.inflate(layoutInflater)
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
        binding.llRmin.setOnClickListener {
            moveToMtnt()
        }
        binding.llFgrn.setOnClickListener {
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
        roleData.forEach { rol ->
            when (UserRoles.valueOfEnum(rol.roleName.trim())) {

            UserRoles.GRN ->{
               // binding.llHistoryTrans.visible()
            }

            UserRoles.MTNT -> {
                binding.llRmin.visible()
            }
            UserRoles.DISPATCH -> {
                binding.llRmin.visible()
            }
            UserRoles.QUALITY-> {
                binding.llFgrn.visible()
            }
            UserRoles.MTNR-> {
                binding.llFgrn.visible()
            }

            UserRoles.PROCESSING -> {
                binding.llHistoryTransfgrn.visible()
                binding.llHistoryTransrmin.visible()
            }
            else -> {}
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
