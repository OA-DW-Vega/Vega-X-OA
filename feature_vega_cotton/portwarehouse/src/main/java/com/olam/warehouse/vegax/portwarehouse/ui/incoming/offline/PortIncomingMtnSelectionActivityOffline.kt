package com.olam.warehouse.vegax.portwarehouse.ui.incoming.offline

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortMtn
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.TRANS_OUTPUT_DATA
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.data.model.Deliverylist
import com.olam.warehouse.vegax.portwarehouse.data.model.Deliverypostlist
import com.olam.warehouse.vegax.portwarehouse.databinding.SelectingIncomingMtnRowItemBinding
import com.olam.warehouse.vegax.portwarehouse.databinding.SelectionIncomingMtnOfflineActivityBinding
import com.olam.warehouse.vegax.portwarehouse.di.injectPortIncomingFeaturee
import com.olam.warehouse.vegax.portwarehouse.ui.incoming.PortIncomingMtnViewModel
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class PortIncomingMtnSelectionActivityOffline : HomeBaseActivity() {
    override val layoutResourceId = R.layout.selection_incoming_mtn_offline_activity

    private lateinit var binding: SelectionIncomingMtnOfflineActivityBinding
    private val vm: PortIncomingMtnViewModel by viewModel { emptyParametersHolder() }
    private var mlist: MutableList<PortMtn> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectionIncomingMtnOfflineActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        injectPortIncomingFeaturee()
        initNavigationView()
        initExtra()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("portwarehouse/ui/incoming/offline/PortIncomingMtnSelectionActivityOffline")
            .title("Portwarehouse").with(tracker)

    }

    private fun initExtra() {
        mlist = Gson().fromJson<List<PortMtn>>(PreferenceHelper.get(TRANS_OUTPUT_DATA, "")).toMutableList()

    }

    private fun initUI() {
        setStorageLocation()
        binding.btnProceed.setOnClickListener {
            val selectedGrades = mlist.filter { it.isChecked }
            val postedLot = arrayListOf<Deliverypostlist>()
            selectedGrades.forEach {
                val lot = Deliverypostlist()
                lot.deliveryNumber = it.mtnNumber
                postedLot.add(lot)
            }
            vm.getMtns(Deliverylist(postedLot))
        }

        setUpAdpater(mlist)

        vm.listMtns.observe(this, androidx.lifecycle.Observer {

            updateMtnDetails(it)

        })
    }

    private fun setStorageLocation() {
        vm.getStorageLocationList()
        vm.getStorageLocationList
            .observe(this, Observer {
                hideLoading()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let { outerData ->
                            outerData.data?.find { item ->
                                item.classification == "Good"
                            }?.let { matchingItem ->
                                PortWHUtil.STORAGEID = matchingItem.storageLocationCode
                                PreferenceHelper.save(Constants.STORAGEID, PortWHUtil.STORAGEID)


                            }
                        }
                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(this, it.error.toString())
                    }
                }
            })
    }

    private fun updateMtnDetails(response: Resource<GenericReqAndResp<List<PortMtn>>>) {
        hideLoading()
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val mtns = runBlocking {
                        withContext(Dispatchers.IO)
                        {
                            vm.getMtnsOffline()
                        }
                    }
                    it.data?.data.let { data ->
                        if (mtns.isEmpty()) {

                            data?.forEach { mtn ->
                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        vm.insertMtn(mtn)
                                    }
                                }
                            }

                        } else {
                            mtns.forEach { mtn ->

                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        vm.deleteMtnsByMtnId(mtn.mtnNumber)
                                    }
                                }

                            }
                            data?.forEach { serverMtn ->
                                runBlocking {
                                    withContext(Dispatchers.IO) {
                                        vm.insertMtn(serverMtn)
                                    }
                                }
                            }

                        }
                    }

                    toast("Successfully Downloaded")
                    finish()

                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(
                        this, it.error.toString()
                    )
                }
            }
        }
    }

    private fun setUpAdpater(mlist: MutableList<PortMtn>) {
        binding.incomingRecyclerView.setUpAdapter(
            mlist,
            R.layout.selecting_incoming_mtn_row_item,
            SelectingIncomingMtnRowItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvMtnNum.text = it.mtnNumber
                bindItem.tvSupplyPlantIdd.text = it.suplierPlantId
                bindItem.tvTruckNumberr.text = it.truckNumber
                bindItem.tvGradess.text = it.grade
                bindItem.rbMtnn.isChecked = it.isChecked
                bindItem.rbMtnn.setOnClickListener {
                    changeSelection(pos)
                }

            })
    }

    private fun changeSelection(position: Int) {

        val grade = mlist[position]
        grade.isChecked = !grade.isChecked
        onGradeCheck()
        binding.incomingRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun onGradeCheck() {
        val selectedGrades = mlist.filter { it.isChecked }

        if (selectedGrades.isNotEmpty()) {
            enableButton()
        } else {
            disableButton()
        }
    }

    private fun disableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(context, R.color.light_grey)
            )
            isEnabled = false
        }
    }

    private fun enableButton() {
        binding.btnProceed.apply {
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    com.olam.warehouse.presentation.R.color.colorPrimaryOfi
                )
            )
            isEnabled = true
        }
    }
}
