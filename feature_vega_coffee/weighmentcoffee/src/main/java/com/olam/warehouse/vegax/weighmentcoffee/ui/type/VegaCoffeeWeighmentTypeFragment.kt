package com.olam.warehouse.vegax.weighmentcoffee.ui.type

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
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.weighmentcoffee.R
import com.olam.warehouse.vegax.weighmentcoffee.databinding.FragmentVegaCoffeeWeighmentTypeBinding
import com.olam.warehouse.vegax.weighmentcoffee.ui.VegaCoffeeReplaceCallback
import com.olam.warehouse.vegax.weighmentcoffee.utils.*
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


/**
 * Created by Baskaran Kannan on 1/27/2020.
 */
class VegaCoffeeWeighmentTypeFragment : BaseFragment() {

    private val mTAG = VegaCoffeeWeighmentTypeFragment::class.java.canonicalName
    private lateinit var binding: FragmentVegaCoffeeWeighmentTypeBinding
    private var callBack: VegaCoffeeReplaceCallback? = null
    private var receivingData = VegaReceiving()
    private var mtntData = VegaMtnt()
    private var mDirection: String? = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaCoffeeReplaceCallback
    }

    override val layoutResourceId = R.layout.fragment_vega_coffee_weighment_type

    companion object {
        fun newInstance() = VegaCoffeeWeighmentTypeFragment().putArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaCoffeeWeighmentTypeBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("weighmentcoffee/ui/type/VegaCoffeeWeighmentTypeFragment").title("Receiving")
            .with(tracker)
        initUI()
    }


    private fun initUI() {
        changeTruckInActivity()
        binding.llTruckIn.setOnClickListener { changeTruckInActivity() }
        binding.llTruckOut.setOnClickListener { changeTruckOutActivity() }
        binding.llSupplier.setOnClickListener { moveToSupplier() }
        binding.llMtnr.setOnClickListener { moveToMtnr() }
        binding.llMtnt.setOnClickListener { moveToMtnt() }
        binding.llSales.setOnClickListener { moveToSales() }
        var isMtnt = false
        val roleData = Gson().fromJson<List<UserRole>>(PreferenceHelper.get(Constants.USER_ROLES, ""))
            .filter { key -> key.roleKey.equals(PreferenceHelper.get(Constants.CURRENT_KEY, "")) }
        roleData.forEach { rol -> if (rol.roleName.equals("ROLE_DISPATCH_STO")) isMtnt = true }
        if (isMtnt) binding.llTransfer.visible() else binding.llTransfer.gone()
    }

    private fun moveToMtnr() {
        receivingData = VegaReceiving()
        receivingData.truckDirection = mDirection
        receivingData.weighBridgeType = STO
        callBack?.replaceMtntFragment(MTNR, receivingData.truckDirection.toString(), receivingData)
    }

    private fun moveToSupplier() {
        receivingData = VegaReceiving()
        receivingData.truckDirection = mDirection
        receivingData.weighBridgeType = PROCURE
        callBack?.replaceMtntFragment(SUPPLIER, mDirection ?: "", receivingData)
    }

    private fun changeTruckOutActivity() {
        mDirection = DIRECTIONOUT
        binding.ivTruckOut.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckout_select_ofi)
        binding.ivTruckIn.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckin_unselect)
    }

    private fun changeTruckInActivity() {
        mDirection = DIRECTIONIN
        binding.ivTruckOut.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckout_unselect)
        binding.ivTruckIn.setBackgroundResource(com.olam.warehouse.presentation.R.drawable.ic_truckin_select_ofi)

    }

    private fun moveToMtnt() {
        mtntData = VegaMtnt()
        mtntData.truckDirection = mDirection
        mtntData.weighBridgeType = STO
        callBack?.replaceMtntFragment("", mDirection.toString(), mtntData)
    }

    private fun moveToSales() {
        if (mDirection == DIRECTIONIN) {
            receivingData = VegaReceiving()
            receivingData.truckDirection = mDirection
            receivingData.weighBridgeType = SALES
            callBack?.replaceMtntFragment(SALES, mDirection ?: "", receivingData)
        } else {
            val vegaMtnt = VegaMtnt()
            vegaMtnt.truckDirection = mDirection
            vegaMtnt.weighBridgeType = SALES
            callBack?.replaceMtntFragment(SALES, mDirection ?: "", vegaMtnt)
        }
    }

}
