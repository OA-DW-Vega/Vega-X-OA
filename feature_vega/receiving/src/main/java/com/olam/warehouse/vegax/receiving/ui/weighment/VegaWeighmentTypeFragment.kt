package com.olam.warehouse.vegax.receiving.ui.weighment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.master.vega.entity.VegaMtnt
import com.olam.warehouse.master.vega.entity.VegaReceiving
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.receiving.R
import com.olam.warehouse.vegax.receiving.databinding.FragmentVegaWeighmentTypeBinding
import com.olam.warehouse.vegax.receiving.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Baskaran Kannan on 1/27/2020.
 */
class VegaWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaWeighmentTypeBinding
    private var callBack: CallBack? = null
    private var receivingData = VegaReceiving()
    private var mtntData = VegaMtnt()
    private var mDirection: String? = ""

    interface CallBack {
        fun replaceFragment(
            receivingType: String,
            direction: String,
            receivingData: VegaReceiving
        )

        fun replaceMtntFragment(direction: String, mtntData: VegaMtnt)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_weighment_type

    companion object {
        fun newInstance() = VegaWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("receiving/ui/weighment/VegaWeighmentTypeFragment").title("Receiving").with(tracker)
        initUI()
    }


    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvTitle, it, false)
        }
        changeTruckInActivity()
        binding.llTruckIn.setOnClickListener { changeTruckInActivity() }
        binding.llTruckOut.setOnClickListener { changeTruckOutActivity() }
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
        binding.llMtnt.setOnClickListener { moveToMtnt() }

        var isMtnt = false
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
        roleData.forEach { rol -> if (rol.roleName.equals("ROLE_DISPATCH_STO")) isMtnt = true }
        if (isMtnt) binding.llTransfer.visible() else binding.llTransfer.gone()
    }

    private fun moveToMtnr() {
        receivingData = VegaReceiving()
        receivingData.truckDirection = mDirection
        receivingData.weighBridgeType = STO
        callBack?.replaceFragment(MTNR, receivingData.truckDirection.toString(), receivingData)
    }

    private fun moveToSupplier() {
        receivingData = VegaReceiving()
        receivingData.truckDirection = mDirection
        receivingData.weighBridgeType = PROCURE
        callBack?.replaceFragment(SUPPLIER, receivingData.truckDirection.toString(), receivingData)
    }

    private fun changeTruckOutActivity() {
        mDirection = DIRECTIONOUT
        binding.ivTruckOut.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckout_select)
        binding.ivTruckIn.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckin_unselect)
    }

    private fun changeTruckInActivity() {
        mDirection = DIRECTIONIN
        binding.ivTruckOut.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckout_unselect)
        binding.ivTruckIn.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckin_select)

    }

    private fun moveToMtnt() {
        mtntData = VegaMtnt()
        mtntData.truckDirection = mDirection
        mtntData.weighBridgeType = STO
        callBack?.replaceMtntFragment(mDirection.toString(), mtntData)
    }


}
