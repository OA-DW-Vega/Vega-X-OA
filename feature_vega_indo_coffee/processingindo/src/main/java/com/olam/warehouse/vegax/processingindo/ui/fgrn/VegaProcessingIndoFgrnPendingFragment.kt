package com.olam.warehouse.vegax.processingindo.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.common.utils.getCurrentOriginEntity
import com.olam.warehouse.master.vegacocoa.entity.VegaCocoaFgrnItems
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingindo.R
import com.olam.warehouse.vegax.processingindo.databinding.FragmentVegaProcessingIndoFgrnPendingBinding
import com.olam.warehouse.vegax.processingindo.utils.FRAG_GRADES
import com.olam.warehouse.vegax.processingindo.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingindo.utils.getColor
import kotlinx.android.synthetic.main.item_vega_processing_indo_fgrn_pending.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 6/2/2020.
 */
class VegaProcessingIndoFgrnPendingFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_processing_indo_fgrn_pending
    private lateinit var binding: FragmentVegaProcessingIndoFgrnPendingBinding
    private val vm: VegaProcessingIndoFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCocoaFgrnItems()
    private var offlineFgrn = mutableListOf<VegaCocoaFgrnItems>()
    private val mSearchList = arrayListOf<VegaCocoaFgrnItems>()

    interface CallBack {
        fun replaceFgrnFragment(fragment: String,
            fgrnItem: VegaCocoaFgrnItems,
            fgrnId: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCocoaFgrnItems) = VegaProcessingIndoFgrnPendingFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
        }

        const val SEARCH_HINT_TEXT = "Search Fgrn Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaProcessingIndoFgrnPendingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingcocoa/ui/fgrn/VegaCocoaFgrnPendingFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCocoaFgrnItems()

        binding.tvPoNo.text = fgrnItem.processOrderNo
        binding.tvMaterialName.text = fgrnItem.materialName
        binding.tvWeight.text = fgrnItem.weight.plus(" ").plus(fgrnItem.unitsOfMeasure)
        if ("null" != fgrnItem.startDate) {
            val times = fgrnItem.startDate?.split('(', ')')
            binding.tvDate.text = times?.get(1)?.let { it1 ->
                DateUtils.getUTCDateTime(
                    it1,
                    App.getAppContext()
                )
            }
        }

        vm.offlineFgrn.observe(viewLifecycleOwner, Observer {
            it.forEach { item ->
                item.processName = fgrnItem.processName
                item.rminList = fgrnItem.rminList
                item.rfgrnList = fgrnItem.rfgrnList
            }
            offlineFgrn = it as MutableList<VegaCocoaFgrnItems>
            updateItems(it)
        })
        vm.fetchOfflineFgrnList(fgrnItem.processOrderNo)
        binding.tvCreateNewFgrn.setOnClickListener {
            fgrnItem.fgrnId = ""
            callBack?.replaceFgrnFragment(FRAG_GRADES,
                fgrnItem,
                fgrnItem.fgrnId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        activity?.menuInflater?.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(if(getCurrentOriginEntity().contains("OFI"))com.olam.warehouse.presentation.R.color.colorPrimaryOfi else com.olam.warehouse.presentation.R.color.green))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            updateItems(offlineFgrn)
                        }
                        else {
                            mSearchList.clear()
                            offlineFgrn.forEach { po ->
                                newText?.let { text ->
                                    if (po.fgrnId.contains(text, true)) {
                                        mSearchList.add(po)
                                    }
                                }
                            }
                            updateItems(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun updateItems(pendingList: List<VegaCocoaFgrnItems>) {
        binding.rvPendingPoList.setUp(pendingList as MutableList, R.layout.item_vega_processing_indo_fgrn_pending, { item, pos ->
            tvPoNo.text = item.fgrnId
            tv_material_name.text = item.materialName
            val times = item.startDate?.split('(', ')')
            tvDate.text = times?.get(1)?.let { it1 ->
                it1.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
            ivDelete.setOnClickListener {
                showConfirmDialog(item.fgrnId)
            }
        }, {
            callBack?.replaceFgrnFragment(FRAG_GRADES, this, this.fgrnId)
        })
    }

    private fun showConfirmDialog(fgrnId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
                {
                    vm.deleteItemInAllTable(fgrnId)
                },
                { dismiss() })
        }
    }
}
