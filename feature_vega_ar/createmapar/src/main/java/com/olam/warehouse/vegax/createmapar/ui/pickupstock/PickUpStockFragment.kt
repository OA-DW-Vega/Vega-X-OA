package com.olam.warehouse.vegax.createmapar.ui.pickupstock

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.DateUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.createmapar.R
import com.olam.warehouse.vegax.createmapar.data.domain.model.ArLotDetails
import com.olam.warehouse.vegax.createmapar.databinding.FragmentPickupStockBinding
import com.olam.warehouse.vegax.createmapar.ui.ArViewModel
import com.olam.warehouse.vegax.createmapar.ui.arcore.ArCoreViewActivity
import com.olam.warehouse.vegax.createmapar.utils.*
import kotlinx.android.synthetic.main.item_pickup_stock.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by Baskaran Kannan on 6/15/2021.
 */
class PickUpStockFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_pickup_stock
    private lateinit var binding: FragmentPickupStockBinding
    private val vm: ArViewModel by viewModel()
    private var lotList = arrayListOf<ArLotDetails>()

    companion object {
        fun newInstance() = PickUpStockFragment().putArgs {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPickupStockBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        val lotList =
            Gson().fromJson<List<String>>(PreferenceHelper.get(LOT_LIST, "")) as ArrayList<String>?
                ?: ArrayList()
        //setUpAdapter(lotList)
        vm.getLotDeatils.observe(viewLifecycleOwner, Observer { updateUI(it) })
        vm.getLotDetails()
    }

    private fun updateUI(response: Resource<List<ArLotDetails>>?) {
        response?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if (it.data?.isNotEmpty() == true) {
                        val lotitems = it.data?.filter { it.arReq.anchorIds.isNotEmpty() }
                        if (lotitems?.size ?: 0 > 0) {
                            lotList = lotitems as ArrayList<ArLotDetails>
                            binding.tvNoData.gone()
                            binding.rvLots.visible()
                        } else {
                            binding.tvNoData.visible()
                            binding.rvLots.gone()
                        }
                        setUpAdapter(lotList)
                    } else {

                    }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    activity?.toast(it.error.toString())
                    hideLoading()
                }
            }
        }
    }

    private fun setUpAdapter(lotList: ArrayList<ArLotDetails>) {
        binding.tvTruckIDNo.text = getString(R.string.total_lots).plus(" ").plus(lotList.size)
        binding.rvLots.setUp(lotList, R.layout.item_pickup_stock, { it, pos ->
            tv_truck_no.text = it.lotId
            tvStLocation.text = it.storageLocationCode
//            tvMaterialName.text = it.materialName
//            tvWbId.text = it.weighBridgeId
            val times = DateUtils.getCurrentTimeInMills().toString()
            tvDate.text = times.let { it1 ->
                it1.let { it2 ->
                    DateUtils.getUTCDateTime(
                        it2,
                        App.getAppContext()
                    )
                }
            }
        }, {
            val item = this
            val anchoridList = arrayListOf<String>()
            anchoridList.addAll(item.arReq.anchorIds.map { it.arValue.toString() })
            val intent = Intent(activity, ArCoreViewActivity::class.java)
            intent.putExtra(MODE, RESOLVE_MAP.toString())
            intent.putExtra(LOT_ID, item.lotId)
            intent.putStringArrayListExtra(ANCHOR_IDS, anchoridList)
            startActivity(intent)
        })
    }
}
