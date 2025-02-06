package com.olam.warehouse.odreceiving.ui.blt

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.olam.warehouse.master.dorigin.entity.Bag
import com.olam.warehouse.master.dorigin.entity.DOBag
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.AttachNewQrListItemBinding
import com.olam.warehouse.odreceiving.databinding.FragmentAttachNewQrBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class AttachNewQrFragment : BaseFragment() {

    private lateinit var selectedBag: Bag
    private var bagMissedCount: Int = 0
    private lateinit var binding: FragmentAttachNewQrBinding
    override val layoutResourceId = R.layout.fragment_attach_new_qr
    private val vm: DOReceivingViewModel by viewModel()
    private var mtransactionID: String? = null

    private lateinit var doTxnDetail: DOTxnDetail
    var bags: MutableList<Bag> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttachNewQrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtras()
        initUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odreceiving/ui/blt/AttachNewQrFragment").title("OD/Receiving")
            .with(tracker)

    }

    private fun initUI() {
        context?.let {
            getActionBtnChangedView(binding.llTitle, it, false)
        }
        vm.getDOBags(mtransactionID, true)
        vm.doBags.observe(viewLifecycleOwner, Observer { doBags ->
            if (!doBags?.isNullOrEmpty()!!) {
                val list = doBags.filter { it.bagMissed }
                val count = list.count()
                bagMissedCount = count
                binding.tvMissedBagValue.text =
                    "${getString(R.string.missing_bags)} : $bagMissedCount"

                bags.clear()
                bags.addAll(getBags(doBags))
                if (binding.rvAttachNewQr.adapter != null) {
                    binding.rvAttachNewQr.adapter?.notifyDataSetChanged()
                }
            }
        })

        binding.rvAttachNewQr.setUpAdapter(
            bags,
            R.layout.attach_new_qr_list_item,
            AttachNewQrListItemBinding::inflate,
            { bagItem, pos, bindingItem ->
                bindingItem.tvMissQr.text = bagItem.bagQrCode.toString()
                if (bagItem.isQrCodeReplaced) {
                    bindingItem.oldQrCode.text = getString(R.string.old_qr_code)
                    bindingItem.newQrCode.visibility = View.VISIBLE
                    bindingItem.tvNewMissQr.visibility = View.VISIBLE
                    bindingItem.tvNewMissQr.text = bagItem.newQrCode.toString()
                    bindingItem.btnAttachQrCode.isEnabled = false
                    bindingItem.btnAttachQrCode.setBackgroundColor(Color.DKGRAY)
                } else {
                    bindingItem.oldQrCode.text = getString(R.string.qr_code)
                    bindingItem.newQrCode.visibility = View.GONE
                    bindingItem.tvNewMissQr.visibility = View.GONE
                    bindingItem.btnAttachQrCode.isEnabled = true
                }
                bindingItem.btnAttachQrCode.setOnClickListener { view ->
                    selectedBag = bagItem
                    moveToScannerActivity()
                }
            })

        /*if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.size > 0) {

            for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                if (doTxnDetail.transactionDetails!![0].bagList[i].bagMissed) {
                    bagMissedCount += 1
                } else {
                    bags.add(doTxnDetail.transactionDetails!![0].bagList[i])
                }
            }

            binding.tvMissedBagValue.text = "Missed Bags : $bagMissedCount"

            binding.rvAttachNewQr.setUp(bags, R.layout.attach_new_qr_list_item, { bagItem, pos ->
                tvMissQr.text = bagItem.bagQrCode.toString()
                btnAttachQrCode.setOnClickListener { view ->
                    selectedBag = bagItem
                    moveToScannerActivity()
                }
            })
        }*/
    }

    private fun getBags(doBags: List<DOBag>): MutableList<Bag> {
        val list = mutableListOf<Bag>()
        list.clear()
        doBags.filter { !it.isScanned && !it.bagMissed }.forEach {
            val bag = Bag()
            bag.bagMissed = it.bagMissed
            bag.bagQrCode = it.bagQrCode
            bag.invalidQrCode = it.invalidQrCode
            bag.weight = it.weight
            bag.isQrCodeReplaced = it.isReplaced
            bag.newQrCode = it.newQrCode

            list.add(bag)
        }
        return list
    }

    private fun moveToScannerActivity() {
        val intent = Intent(requireContext(), ScannerActivityBlt::class.java)
//        intent.putExtra("isFromAttachNewQrCodePage", true)
        startActivityForResult(intent, Constants.SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SCAN_QR) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.getString(Constants.SCANNED_ID)?.trim()?.let {
                    validateTnxId(it, it.length, isFromScan = true)
                }
            }
        }
    }

    private fun validateTnxId(newQrCode: String, count: Int, isFromScan: Boolean) {
        when {
            count != 0 -> attachNewQrToBag(newQrCode)
            isFromScan -> requireContext().toast(getString(R.string.scan_valid_qr_code))
            else -> requireContext().toast(getString(R.string.enter_qr_code))
        }
    }

    private fun attachNewQrToBag(newQrCode: String) {
        val doBag = DOBag()
        doBag.bagMissed = false
        doBag.invalidQrCode = selectedBag.invalidQrCode
        doBag.oldQrCode = selectedBag.bagQrCode
        doBag.newQrCode = newQrCode.toInt()
        doBag.lotTransactionId = doTxnDetail.lotTransactionId
        doBag.transactionId = mtransactionID.toString()
        doBag.bagQrCode = selectedBag.bagQrCode
        doBag.weight = selectedBag.weight
        doBag.isScanned = false
        doBag.isReplaced = true
        vm.deleteAndSaveBagDetail(doBag)
//        vm.deleteBagDetail(doBag)
//        vm.saveBagDetail(bag = doBag)

//        bags.remove(selectedBag)
//        rvAttachNewQr.adapter?.notifyDataSetChanged()
    }

    private fun initExtras() {
        doTxnDetail = arguments?.getParcelable("doTxnDetail")!!
        mtransactionID = arguments?.getString("transactionid")!!
    }

    companion object {
        fun newInstance(data: DOTxnDetail, id: String) = AttachNewQrFragment().putArgs {
            putParcelable("doTxnDetail", data)
            putString("transactionid", id)
        }
    }
}
