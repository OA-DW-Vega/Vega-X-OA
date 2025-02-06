package com.olam.warehouse.odreceiving.ui.weigh

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.olam.warehouse.master.dorigin.entity.DOPackageMaterial
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.data.domain.model.DOWeighScale
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingWeighScalePalletBinding
import com.olam.warehouse.odreceiving.databinding.ItemDoWeighScalePalletBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.ui.summary.DOReceivingSummaryActivity
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.odreceiving.utils.RECEIVING_POST_DATA
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.format
import com.olam.warehouse.presentation.utils.extension.hideKeyboard
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingWeighScalePalletFragment : BaseFragment() {

    private val vm: DOReceivingViewModel by viewModel()
    private var packageMaterial: DOPackageMaterial? = null
    private var receivingData = DOReceiving()
    private val mReceiving = mutableListOf<DOReceiving>()
    private val mWeighs = mutableListOf<DOWeighScale>()

    private lateinit var binding: FragmentDoReceivingWeighScalePalletBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_weigh_scale_pallet

    companion object {
        fun newInstance(data: DOReceiving) = DOReceivingWeighScalePalletFragment().putArgs {
            putParcelable(RECEIVING_DATA, data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingWeighScalePalletBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/weigh/DOReceivingWeighScalePalletFragment").title("OD/Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnConfirm, it, true)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!
        setHeaderView()
        vm.material.observe(viewLifecycleOwner, Observer {
            val material = it.map { data -> data.bagType }
            val materialAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, material)
            binding.spMaterial.adapter = materialAdapter
            binding.spMaterial.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        packageMaterial = it[position]
                    }
                }
        })
        vm.getMaterials()

        binding.btnAdd.setOnClickListener { addItemToAdapter() }
        binding.btnConfirm.setOnClickListener { moveToSummaryPage() }
    }

    private fun moveToSummaryPage() {
        if (mReceiving.isEmpty()) {
            requireContext().toast(getString(R.string.message_weight_to_proceed))
            return
        }
        if (receivingData.mtnCode.isNullOrEmpty()) {
            receivingData.bagType = packageMaterial?.bagType
            val intent = Intent(requireContext(), DOReceivingSummaryActivity::class.java)
            intent.putExtra(RECEIVING_DATA, receivingData)
            mReceiving.forEachIndexed { index, doReceiving -> doReceiving.item = index.inc().toString() }
            intent.putParcelableArrayListExtra(RECEIVING_POST_DATA, ArrayList(mReceiving))
            startActivity(intent)
        } else {
            mReceiving.forEachIndexed { index, doReceiving -> doReceiving.item = index.inc().toString() }
            val intent = Intent().putParcelableArrayListExtra(RECEIVING_POST_DATA, ArrayList(mReceiving))
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }
    }

    private fun addItemToAdapter() {
        val totalWeight = binding.etTotalWeight.text.toString()
        val noOfBags = binding.etNoOfBags.text.toString()
        when {
            noOfBags.isEmpty() || noOfBags.toInt() <= 0 -> {
                binding.etNoOfBags.requestFocus()
                binding.etNoOfBags.error = getString(R.string.error_bag)
            }
            totalWeight.isEmpty() || totalWeight.toDouble() <= 0 -> {
                binding.etTotalWeight.requestFocus()
                binding.etTotalWeight.error = getString(R.string.error_weight)
            }
            else -> {
                mWeighs.add(DOWeighScale(receivingData.wsGate, "${noOfBags.toInt()}", totalWeight))
                addReceiving(totalWeight.toDouble(), noOfBags)
                setHeaderView()
                setUpRecyclerView()
                binding.etNoOfBags.text.clear()
                binding.etTotalWeight.text.clear()
                binding.etTotalWeight.hideKeyboard()
            }
        }
    }

    private fun addReceiving(weight: Double, noOfBags: String) {
        val receiving = receivingData.copy()
        val tareWeight = noOfBags.toInt() * (packageMaterial?.tareWeight?.toDouble() ?: 0.0)
        receiving.bagCount = noOfBags
        receiving.bagType = packageMaterial?.bagType
        receiving.tareWeight = tareWeight.toString()
        receiving.bagWeight = tareWeight
        receiving.grossWeight = weight
        receiving.netWeight = weight - tareWeight
        mReceiving.add(receiving)
    }

    private fun setHeaderView() {
        binding.tvTotal.text =
            mWeighs.map { it.name.toInt() }.sum().toString().plus(" ").plus(getString(R.string.bags_total))
                .plus(" ").plus(mWeighs.map { it.weight.toDouble() }.sum().format()).plus(" kg")
    }

    @SuppressLint("SetTextI18n")
    private fun setUpRecyclerView() {
        var count = -1
        binding.rvWeight.setUpAdapter(
            mWeighs,
            R.layout.item_do_weigh_scale_pallet,
            ItemDoWeighScalePalletBinding::inflate,
            { it, pos, bindingItem ->
                count++
                val weigh = it
                bindingItem.tvBag.text = weigh.name
                bindingItem.tvWeight.text = weigh.weight.format().plus(" kg")
                bindingItem.ivDelete.tag = count
                bindingItem.ivDelete.setOnClickListener {
                    mWeighs.remove(weigh)
                    mReceiving.removeAt(bindingItem.ivDelete.tag.toString().toInt())
                    setHeaderView()
                    setUpRecyclerView()
                }
            })
    }
}
