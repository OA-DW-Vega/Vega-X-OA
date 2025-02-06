package com.olam.warehouse.vegax.processingsesame.ui.fgrn

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.master.vega.entity.VegaProcessingStage
import com.olam.warehouse.master.vegacoffee.dao.VegaCoffeeFgrnItems
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.processingsesame.R
import com.olam.warehouse.vegax.processingsesame.databinding.FragmentVegaSesameFgrnPendingBinding
import com.olam.warehouse.vegax.processingsesame.databinding.ItemVegaSesameFgrnPendingBinding
import com.olam.warehouse.vegax.processingsesame.utils.FRAG_GRADES
import com.olam.warehouse.vegax.processingsesame.utils.FRAG_ITEM
import com.olam.warehouse.vegax.processingsesame.utils.VEGA_STAGE
import com.olam.warehouse.vegax.processingsesame.utils.getColor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaSesameFgrnPendingFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_sesame_fgrn_pending
    private lateinit var binding: FragmentVegaSesameFgrnPendingBinding
    private val vm: VegaSesameFgrnViewModel by viewModel()
    private var callBack: CallBack? = null
    private var fgrnItem = VegaCoffeeFgrnItems()
    private var offlineFgrn = mutableListOf<VegaCoffeeFgrnItems>()
    private val mSearchList = arrayListOf<VegaCoffeeFgrnItems>()
    private var vegaStage = VegaProcessingStage()

    interface CallBack {
        fun replaceFgrnFragment(
            fragment: String,
            fgrnItem: VegaCoffeeFgrnItems,
            fgrnId: String,
            vegaStage: VegaProcessingStage
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    companion object {
        fun newInstance(model: VegaCoffeeFgrnItems, vegaStage: VegaProcessingStage) = VegaSesameFgrnPendingFragment().putArgs {
            putParcelable(FRAG_ITEM, model)
            putParcelable(VEGA_STAGE, vegaStage)
        }

        const val SEARCH_HINT_TEXT = "Search Fgrn Item"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaSesameFgrnPendingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("processingsesame/ui/fgrn/VegaSesameFgrnPendingFragment").title("Processing Cocoa")
            .with(tracker)
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.tvHeadPoDetail, it, false)
        }
        fgrnItem = arguments?.getParcelable(FRAG_ITEM) ?: VegaCoffeeFgrnItems()
        vegaStage = arguments?.getParcelable(VEGA_STAGE) ?: VegaProcessingStage()
        binding.tvPoNo.text = fgrnItem.processOrderNo
        binding.tvMaterialName.text = fgrnItem.materialName
        binding.tvWeight.text = fgrnItem.weight.plus(" ").plus("KG")
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
            offlineFgrn = it as MutableList<VegaCoffeeFgrnItems>
            updateItems(it)
        })
        vm.fetchOfflineFgrnList(fgrnItem.processOrderNo)
        binding.tvCreateNewFgrn.setOnClickListener {
            fgrnItem.fgrnId = ""
            callBack?.replaceFgrnFragment(
                FRAG_GRADES,
                fgrnItem,
                fgrnItem.fgrnId,vegaStage
            )
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
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = SEARCH_HINT_TEXT
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            updateItems(offlineFgrn)
                        } else {
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

    private fun updateItems(pendingList: List<VegaCoffeeFgrnItems>) {
        binding.rvPendingPoList.setUpAdapter(
            pendingList as MutableList,
            R.layout.item_vega_sesame_fgrn_pending,
            ItemVegaSesameFgrnPendingBinding::inflate,
            { item, pos, bindItem ->
                bindItem.tvPoNo.text = item.fgrnId
                bindItem.tvMaterialName.text = item.materialName
                val times = item.startDate?.split('(', ')')
                bindItem.tvDate.text = times?.get(1).let { it1 ->
                    it1?.let { it2 ->
                        DateUtils.getUTCDateTime(
                            it2,
                            App.getAppContext()
                        )
                    }
                }
                bindItem.ivDelete.setOnClickListener {
                    showConfirmDialog(item.fgrnId)
                }
            }, {
                callBack?.replaceFgrnFragment(FRAG_GRADES, this, this.fgrnId, vegaStage)
            })
    }

    private fun showConfirmDialog(fgrnId: String) {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.presentation.R.string.delete_msg)
            UIUtils.getMetirialCustomView(
                this,
                getString(R.string.proceed),
                getString(R.string.cancel),
                {
                    vm.deleteItemInAllTable(fgrnId)
                },
                { dismiss() })
        }
    }
}
