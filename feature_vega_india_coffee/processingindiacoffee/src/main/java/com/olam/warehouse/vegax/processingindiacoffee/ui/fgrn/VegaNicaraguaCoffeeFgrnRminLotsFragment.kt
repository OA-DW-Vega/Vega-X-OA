package com.olam.warehouse.vegax.processingindiacoffee.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.master.vegacocoa.entity.VegaProcessingList
import com.olam.warehouse.master.vegacoffee.entity.VegaCoffeeRminLots
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.formatThreeDigits
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindiacoffee.R
import com.olam.warehouse.vegax.processingindiacoffee.databinding.FragmentVegaNicaraguaFgrnLocalRminLotsBinding
import com.olam.warehouse.vegax.processingindiacoffee.utils.*
import kotlinx.android.synthetic.main.item_lot_summary.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraguaCoffeeFgrnRminLotsFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_vega_nicaragua_fgrn_local_rmin_lots
    private lateinit var binding: FragmentVegaNicaraguaFgrnLocalRminLotsBinding
    private val vm: VegaIndiaCoffeeFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var model = VegaCocoaFgrnItems()
    private var fgrnId: String = ""
    private var dispatchLotsList = mutableListOf<VegaCoffeeRminLots>()
    private var dispatchFgrnLotsList = mutableListOf<VegaCoffeeRminLots>()
    private var isMultipleAdd = false
    private val tickets = mutableListOf<VegaProcessingList>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String, model: VegaCocoaFgrnItems, id: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems, id: String) =
            VegaNicaraguaCoffeeFgrnRminLotsFragment().putArgs {
                putParcelable(FRAG_LOTS, model)
                putString(FRAG_ID, id)
            }

        const val SEARCH_HINT_TEXT = "Search Po Item"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNicaraguaFgrnLocalRminLotsBinding.inflate(layoutInflater)
        binding.tvCreateNewFgrn.isEnabled = false
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnLotsDetailsFragment")
            .title("Processing Coffee")
            .with(tracker)
    }

    private fun initUI() {
        model = arguments?.getParcelable(FRAG_LOTS) ?: VegaCocoaFgrnItems()
        fgrnId = arguments?.getString(FRAG_ID) ?: ""
        binding.tvMaterialValue.text = model.stageFevor
        binding.tvStageValue.text = model.materialName.toString().trim()
        //binding.tvProceesValue.text = model.weight
        binding.tvPoNoValue.text = model.processOrderNo
        binding.tvCreateNewFgrn.setOnClickListener { moveToNext() }
        vm.getOfflineRminLots(model.processOrderNo)
        vm.offlineRminLotsLocal.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                //Toast.makeText(context, "inside", Toast.LENGTH_SHORT).show()
                it.forEach {
                    var lot = VegaCoffeeRminLots()
                    lot.isAdded = false
                    lot.batchNumber = it.batchNumber
                    lot.materialName = it.materialName
                    lot.poNumber = it.poNo.toString()
                    lot.storageLocationCode = it.storageLocationCode
                    lot.weight = it.weight
                    lot.remarks = it.lotId //lot id
                    dispatchLotsList.add(lot)

                }
                updateFgrnAdapter(dispatchLotsList)
            } else {
                binding.tvNoData.visible()
                binding.rvPoList.visibility = View.GONE
            }
        })

    }

    private fun moveToNext() {
        when (getCurrentFragment()) {
            is VegaNicaraguaCoffeeFgrnRminLotsFragment -> {
                //callBack?.replaceFgrnFragment(FRAG_ADD_WEIGHT, model, TICKET)
                TICKET = true
                callBack?.replaceFgrnFragment(FRAG_GRADES, model, "")
            }
        }

    }


    fun getCurrentFragment(): Fragment {
        return activity?.supportFragmentManager?.findFragmentById(R.id.flProcessing)!!
    }

    private fun updateFgrnAdapter(poList: MutableList<VegaCoffeeRminLots>) {
        if (poList.isNotEmpty()) {
            when (poList.size > 0) {
                true -> {
                    binding.rvPoList.visible()
                    binding.tvNoData.gone()
                }
                else -> {
                    binding.rvPoList.gone()
                    binding.tvNoData.visible()
                }
            }
        } else {
            binding.rvPoList.gone()
            binding.tvNoData.visible()
        }
        binding.rvPoList.setUp(poList, R.layout.item_lot_summary, { item, pos ->
            tvLotId.text = item.batchNumber
            tvGradeValue.text = item.materialName
            tvWeightValue.text = item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus("Kg")
            tvStLocationValue.text = item.storageLocationCode
            ivSelect.isChecked = item.isAdded ?: false
            llLotItem.setOnClickListener { view ->
                item.isAdded = !item.isAdded!!
                ivSelect.isChecked = item.isAdded!!
                enableDisableBtn(item.isAdded!!)
                binding.tvProceesValue.text =
                    item.weight?.toDouble()?.formatThreeDigits().plus(" ").plus("Kg")
                dispatchFgrnLotsList.clear()
                dispatchLotsList.forEach { lot ->
                    if (lot.remarks.equals(item.remarks)) {         //comparing batch_number
                        lot.isAdded = item.isAdded
                    }
                }
                dispatchFgrnLotsList =
                    dispatchLotsList.filter { it.isAdded == true } as MutableList<VegaCoffeeRminLots>
                /* binding.tvCreateNewFgrn.isEnabled = true
                 binding.tvCreateNewFgrn.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))*/
                model.message = item.remarks    //lot id
                dispatchFgrnLotsList.forEach {
                    model.rminList = prepareTicketData(it)
                }
                if (!isMultipleAdd) {
                    if (item.isAdded!!)
                        removeChecked(pos, poList)
                } else
                    binding.rvPoList.adapter?.notifyItemChanged(pos)
            }

        }, itemClick = {

        })
    }

    private fun removeChecked(item: Int, list: MutableList<VegaCoffeeRminLots>) {
        list.forEach { it.isAdded = false }
        list[item].isAdded = true
        updateFgrnAdapter(list)
    }

    fun prepareTicketData(ticket: VegaCoffeeRminLots): List<VegaProcessingList> {
        tickets.clear()
        val lot = VegaProcessingList()
        lot.batchNumber = ticket.batchNumber
        lot.materialCode = model.materialCode
        lot.materialName = ticket.materialName
        lot.netWeight = ticket.weight
        lot.storageLocationCode = ticket.storageLocationCode
        lot.processOrderNo = ticket.poNumber
        lot.unitsOfMeasure = ticket.unitOfMeasure
        tickets.add(lot)
        return tickets
    }

    private fun enableDisableBtn(enable: Boolean) {
        if (enable) {
            binding.tvCreateNewFgrn.isEnabled = true
            binding.tvCreateNewFgrn.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.green))

        } else {
            binding.tvCreateNewFgrn.isEnabled = false
            binding.tvCreateNewFgrn.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.light_grey))
        }
    }
}

