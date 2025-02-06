package com.olam.warehouse.odreceiving.ui.weigh

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.olam.warehouse.master.dorigin.entity.DOPackageMaterial
import com.olam.warehouse.master.dorigin.entity.DOReceiving
import com.olam.warehouse.master.dorigin.entity.DOReceivingLineItem
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentDoReceivingWeighScalePalletAddBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.odreceiving.utils.RECEIVING_DATA
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

/**
 * Created by Baskaran Kannan on 1/3/2020.
 */
class DOReceivingWeighScalePalletAddFragment : BaseFragment() {

    private val vm: DOReceivingViewModel by viewModel()
    private var callBack: CallBack? = null
    private var receivingData = DOReceiving()
    private var packageMaterial: DOPackageMaterial? = null
    private var packageMaterials = arrayListOf<DOPackageMaterial>()
    private lateinit var binding: FragmentDoReceivingWeighScalePalletAddBinding
    override val layoutResourceId = R.layout.fragment_do_receiving_weigh_scale_pallet_add

    interface CallBack {
        fun replaceFragment(receivingData: DOReceiving)
    }

    companion object {
        fun newInstance(data: DOReceiving, postData: ArrayList<DOReceivingLineItem>?) =
            DOReceivingWeighScalePalletAddFragment().putArgs {
                putParcelable(RECEIVING_DATA, data)
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDoReceivingWeighScalePalletAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/weigh/DOReceivingWeighScalePalletAddFragment").title("OD/Receiving")
            .with(tracker)
        initUI()
    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
            getActionBtnChangedView(binding.btnNext, it, true)
        }
        receivingData = arguments?.getParcelable(RECEIVING_DATA)!!

        vm.material.observe(viewLifecycleOwner, Observer {
            packageMaterials.clear()
            packageMaterials.addAll(it)
            binding.tvTypeOfPallet.setOnClickListener { showPalletTypeDialog() }
        })
        vm.getMaterials()

        binding.btnNext.setOnClickListener {
            moveToWeighingPage()
        }
    }

    private fun showPalletTypeDialog() {
        val types = packageMaterials.map { data -> data.bagType }
        MaterialDialog(requireContext()).show {
            title(R.string.select_pallet_type)
            listItemsSingleChoice(items = types) { _, index, _ ->
                packageMaterial = packageMaterials[index]
                updateUI()
            }
            positiveButton(text = UIUtils.getSpannedText(getString(R.string.ok), true))
        }
    }

    private fun updateUI() {
        binding.tvTypeOfPallet.text = packageMaterial?.bagType
        binding.etWeight.setText(packageMaterial?.tareWeight)
    }

    private fun moveToWeighingPage() {
        packageMaterial?.let {
            val weight = binding.etWeight.text.toString()
            val noOfPallet = binding.etNoOfPallet.text.toString()
            when {
                noOfPallet.isEmpty() -> {
                    binding.etNoOfPallet.requestFocus()
                    binding.etNoOfPallet.error = getString(R.string.error_valid_pallet_number)
                }
                weight.isEmpty() -> {
                    binding.etWeight.requestFocus()
                    binding.etWeight.error = getString(R.string.error_weight)
                }
                else -> {
                    val tareWeight = (binding.etWeight.text.toString().toDouble()) * noOfPallet.toInt()
                    receivingData.palletCount = noOfPallet
                    receivingData.palletType = it.bagType
                    receivingData.palletWeight = tareWeight
                    callBack?.replaceFragment(receivingData)
                }
            }

        } ?: run { requireActivity().toast(getString(R.string.message_valid_pallet)) }
    }
}
