package com.olam.warehouse.vegax.forwardponicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.master.veganicaragua.entity.VegaNicaraguaForwardPODetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.forwardponicaragua.R
import com.olam.warehouse.vegax.forwardponicaragua.databinding.FragmentVegaNicaraguaForwardPoVendorsBinding
import com.olam.warehouse.vegax.forwardponicaragua.databinding.ItemVegaNicarguaForwardPoVendorBinding
import com.olam.warehouse.vegax.forwardponicaragua.utils.FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper


class VegaNicaraguaForwardPOVendorsFragment : BaseFragment() {

    private var callBack: Callback? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var mSearchList = mutableListOf<VegaVendor>()
    private lateinit var binding: FragmentVegaNicaraguaForwardPoVendorsBinding
    private var receivingData = VegaNicaraguaForwardPODetails()
    private val vm: VegaNicaraguaForwardPOViewModel by viewModel()


    companion object {
        fun newInstance() = VegaNicaraguaForwardPOVendorsFragment().putArgs {

        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? Callback
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_forward_po_vendors

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaForwardPoVendorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("forwordPOCreation/ui/VegaNicaraguaForwordPOVendorsFragment")
            .title("ForwordPO  vendors list")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        setSearchView()
        vm.supplier.observe(viewLifecycleOwner, Observer {
            supplierList = it.toMutableList()
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
        val list=weighBridgeList1.filter { it.vendorCode.startsWith("1") } as ArrayList<VegaVendor>
        var count = 0
        binding.vendorList.setUpAdapter(
            list.asReversed(),
            R.layout.item_vega_nicargua_forward_po_vendor,
            ItemVegaNicarguaForwardPoVendorBinding::inflate,
            { it, pos, bindItem ->
                bindItem.vendorId.text = it.vendorCode
                bindItem.vendorName.text = it.vendorName
                count++
            },
            {
                val item = this
                var data = Bundle()
                receivingData.vendorName = item.vendorName
                receivingData.vendorCode = item.vendorCode
                receivingData.taxNumber = item.taxNumber
                receivingData.vendorAddress = item.vendorAddress
                data.putParcelable(UIUtils.RECEIVING_DATA, receivingData)

                callBack?.replaceFragment(
                    FORWARD_PO_CREATION_TRANSACTION_DETAILS_FRAG, data
                )
            })
    }
}
