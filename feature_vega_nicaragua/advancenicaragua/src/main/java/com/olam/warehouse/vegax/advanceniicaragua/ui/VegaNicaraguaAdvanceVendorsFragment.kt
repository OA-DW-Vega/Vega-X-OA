package com.olam.warehouse.vegax.advanceniicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaAdvanceTransactionDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.advancenicaragua.R
import com.olam.warehouse.vegax.advancenicaragua.databinding.FragmentVegaNicaraguaAdvanceVendorsBinding
import com.olam.warehouse.vegax.advancenicaragua.databinding.ItemVegaNicarguaAdvanceVendorBinding
import com.olam.warehouse.vegax.advanceniicaragua.utils.ADVANCE_DETAILS_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraguaAdvanceVendorsFragment : BaseFragment() {

    private var callBack: Callback? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var mSearchList = mutableListOf<VegaVendor>()
    private var receiveData = VegaNicaraguaAdvanceTransactionDetails()
    private lateinit var binding: FragmentVegaNicaraguaAdvanceVendorsBinding

    private val vm: VegaNicaraguaAdvanceViewModel by viewModel()


    companion object {
        fun newInstance() = VegaNicaraguaAdvanceVendorsFragment().putArgs {

        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_advance_vendors

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaAdvanceVendorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("advanceCreation/ui/VegaNicaraguaAdvanceVendorsFragment")
            .title("Advance Creation  vendors list")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        setSearchView()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList =
                it.toMutableList().filter { it.vendorCode.startsWith("1") } as ArrayList<VegaVendor>

            setUpAdapter(supplierList)

        })

        vm.getSuppliers("NI01")
    }

    private fun setSearchView()
    {
        binding.searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText.let {
                    if (newText?.isEmpty() == true) {
                        setUpAdapter(supplierList)
                    } else {
                        mSearchList.clear()
                        supplierList.forEach { vegaVendor ->
                            newText?.let { text ->
                                if (vegaVendor.vendorName?.toLowerCase()?.trim()
                                        ?.contains(text.toLowerCase())!! || vegaVendor.vendorCode.toLowerCase()
                                        .trim().contains(text.toLowerCase())
                                ) {
                                    mSearchList.add(vegaVendor)
                                }
                            }
                        }
                        setUpAdapter(mSearchList)
                    }
                }
                return true
            }
        })

    }
    private fun setUpAdapter(data: List<VegaVendor>?) {
        val weighBridgeList1 = data as MutableList<VegaVendor>

        var count = 0
        binding.vendorList.setUpAdapter(
            weighBridgeList1.asReversed(),
            R.layout.item_vega_nicargua_advance_vendor,
            ItemVegaNicarguaAdvanceVendorBinding::inflate,
            { it, pos, bindItem ->
                bindItem.vendorId.text = it.vendorCode
                bindItem.vendorName.text = it.vendorName
                count++
            },
            {
                val item = this
                receiveData.vendorCode = item.vendorCode
                receiveData.vendorName = item.vendorName
                var data = Bundle()
                data.putParcelable(UIUtils.RECEIVING_DATA, receiveData)

                callBack?.replaceFragment(
                    ADVANCE_DETAILS_FRAG, data
                )
            })
    }
}
