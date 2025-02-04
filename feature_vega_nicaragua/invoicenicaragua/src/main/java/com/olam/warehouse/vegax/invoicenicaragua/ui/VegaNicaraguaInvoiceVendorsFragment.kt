package com.olam.warehouse.vegax.invoicenicaragua.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.master.vega.entity.VegaVendor
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.grnecuador.utils.INVOICE_SELECT_GRN_FRAG
import com.olam.warehouse.vegax.invoicenicaragua.R
import com.olam.warehouse.vegax.invoicenicaragua.databinding.FragmentVegaNicaraguaInvoiceVendorsBinding
import kotlinx.android.synthetic.main.item_vega_nicargua_invoice_vendor.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class VegaNicaraguaInvoiceVendorsFragment : BaseFragment() {

    private var callBack: CallBack? = null
    private var supplierList = mutableListOf<VegaVendor>()
    private var mSearchList = mutableListOf<VegaVendor>()
    private lateinit var binding: FragmentVegaNicaraguaInvoiceVendorsBinding

    private val vm: VegaNicaraguaInvoiceViewModel by viewModel()

    interface CallBack {
        fun replaceFragment(moveFrag: String)
        fun replaceFragment(
            moveFrag: String,
            data: Bundle
        )
    }

    companion object {
        fun newInstance() = VegaNicaraguaInvoiceVendorsFragment().putArgs {

        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override val layoutResourceId = R.layout.fragment_vega_nicaragua_invoice_vendors

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVegaNicaraguaInvoiceVendorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("invoicenicaragua/ui/VegaNicarguaInvoiceVendorsFragment")
            .title("Invoice  vendors list")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        setSearchView()
        vm.supplier.observe(this, Observer {
            supplierList = it.filter { it.purchaseOrgType.equals("NI02") }.toMutableList()
            setUpAdapter(supplierList)

        })

        vm.getSuppliers()
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
        binding.vendorList.setUp(
            list.asReversed(),
            R.layout.item_vega_nicargua_invoice_vendor,
            { it, pos ->

                vendorId.text = it.vendorCode
                vendorName.text = it.vendorName

                count++
            },
            {
                val item = this

                var data=Bundle()
                data.putParcelable(UIUtils.VENDOR_DATA,item)

                callBack?.replaceFragment(
                    INVOICE_SELECT_GRN_FRAG,data
                )
            })
    }
}
