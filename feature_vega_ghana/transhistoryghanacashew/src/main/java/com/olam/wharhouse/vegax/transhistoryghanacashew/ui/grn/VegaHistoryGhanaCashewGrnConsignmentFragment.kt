package com.olam.warehouse.vegax.transhistoryghanacashew.ui.grn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vegaghana.entity.VegaGhanaCashewGRNHistoryTransactions
import com.olam.warehouse.presentation.enums.UserRoles
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.wharhouse.vegax.transhistoryghanacashew.R
import com.olam.wharhouse.vegax.transhistoryghanacashew.databinding.FragmentHistoryTransactionsGhanaCashewGrnConsignmentBinding
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaGhanaCashewTransHisViewModel
import com.olam.wharhouse.vegax.transhistoryghanacashew.ui.VegaHistoryTransactionsReplaceFragmentCallback
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_DATA_GRN
import com.olam.wharhouse.vegax.transhistoryghanacashew.utils.HISTORY_TRANSACTIONS_QUALITY_PARAM
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaHistoryGhanaCashewGrnConsignmentFragment : BaseFragment() {

    override val layoutResourceId: Int = R.layout.fragment_history_transactions_ghana_cashew_grn_consignment
    private lateinit var binding: FragmentHistoryTransactionsGhanaCashewGrnConsignmentBinding
    private var callBack: VegaHistoryTransactionsReplaceFragmentCallback? = null
    private val vm: VegaGhanaCashewTransHisViewModel by viewModel()
    private var historyData = VegaGhanaCashewGRNHistoryTransactions()
    private var isGrn = false
    private var isUnit = false


    companion object{
        fun newInstance(historyTransactionsGRN: VegaGhanaCashewGRNHistoryTransactions) = VegaHistoryGhanaCashewGrnConsignmentFragment().putArgs{
            putParcelable(HISTORY_DATA_GRN, historyTransactionsGRN)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaHistoryTransactionsReplaceFragmentCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentHistoryTransactionsGhanaCashewGrnConsignmentBinding.inflate(inflater)
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
        binding.tvPriceUnitValue.text = historyData.pricePerUnit
        binding.tvTotalPriceValue.text = historyData.totalPrice
        binding.tvStorageLocationValue.text = historyData.storageLocationCode
        binding.tvUnitIdValue.text = historyData.username

        binding.btnQuality.setOnClickListener {
            callBack?.replaceFragment(HISTORY_TRANSACTIONS_QUALITY_PARAM, historyData)
        }
        binding.btnOk.setOnClickListener {
            activity?.onBackPressed()
        }

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

                UserRoles.GRN -> {
                isGrn= true
                  //  binding.llHistoryTrans.visibility = View.VISIBLE
                }
                UserRoles.PCH -> {
                    isUnit= true
                    //  binding.llHistoryTrans.visibility = View.VISIBLE
                }
                else -> {}
            }
        }

        if (isGrn || isUnit){
            binding.llPriceunit.visible()
            binding.llTotalPrice.visible()
            binding.llGRN.visible()
            binding.lluserid.gone()
            binding.llStorageLocation.visible()
        }else{
            binding.llPriceunit.gone()
            binding.llTotalPrice.gone()
            binding.llGRN.gone()
            binding.lluserid.visible()
            binding.llStorageLocation.gone()
        }
    }

}
