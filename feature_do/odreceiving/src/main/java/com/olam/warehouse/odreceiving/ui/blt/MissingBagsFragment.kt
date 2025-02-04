package com.olam.warehouse.odreceiving.ui.blt

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.olam.warehouse.master.dorigin.entity.Bag
import com.olam.warehouse.master.dorigin.entity.DOBag
import com.olam.warehouse.master.dorigin.entity.DOTxnDetail
import com.olam.warehouse.odreceiving.R
import com.olam.warehouse.odreceiving.databinding.FragmentMissingBagsBinding
import com.olam.warehouse.odreceiving.ui.DOReceivingViewModel
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getActionBtnChangedView
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import kotlinx.android.synthetic.main.bag_missing_list_item.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class MissingBagsFragment : BaseFragment() {

    private var bagMissedCount: Int = 0
    private lateinit var binding: FragmentMissingBagsBinding
    override val layoutResourceId = R.layout.fragment_missing_bags
    private val vm: DOReceivingViewModel by viewModel()
    private var mtransactionID: String? = null

    var bags: MutableList<Bag> = mutableListOf()

    private lateinit var doTxnDetail: DOTxnDetail

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMissingBagsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("odquality/ui/blt/MissingBagsFragment").title("OD/Receiving")
            .with(tracker)
    }

    private fun initExtras() {
        doTxnDetail = arguments?.getParcelable("doTxnDetail")!!
        mtransactionID = arguments?.getString("transactionid")!!
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
                if (binding.rvMissingBags.adapter != null) {
                    binding.rvMissingBags.adapter?.notifyDataSetChanged()
                }
            }
        })

//        binding.tvMissedBagValue.text = "Missed Bags : $bagMissedCount"
        binding.rvMissingBags.setUp(bags, R.layout.bag_missing_list_item, { bagItem, pos ->
            tvMissQr.text = bagItem.bagQrCode.toString()

            if (bagItem.bagMissed) {
                btnMarkMissingBag.text = requireContext().getString(R.string.bag_already_miss)
                btnMarkMissingBag.isEnabled = false
                btnMarkMissingBag?.setBackgroundColor(Color.DKGRAY)
            } else {
                btnMarkMissingBag.text = getString(R.string.mark_missing)
                btnMarkMissingBag.isEnabled = true
            }

            btnMarkMissingBag.setOnClickListener { view ->
                if (btnMarkMissingBag.text == getString(R.string.mark_missing)) {
                    showAlert(bagItem, view as Button)
                }
            }
        })



        /*if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.size > 0) {
            for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                val bagObj = doTxnDetail.transactionDetails!![0].bagList[i]
                if (bagObj.bagMissed) {
                    bagMissedCount += 1
                } else {
                    if (!bags.contains(bagObj))
                        bags.add(bagObj)
                }
            }


        }*/



        /*vm.getDOBags(doTxnDetail.lotTransactionId)
        vm.doBags.observeOnce(viewLifecycleOwner, Observer {
            if (doTxnDetail.transactionDetails != null && doTxnDetail.transactionDetails!!.size > 0) {

                if (it.isNullOrEmpty()) {
                    for (i in 0 until doTxnDetail.transactionDetails!![0].noOfBags!!) {
                        val bagObj = doTxnDetail.transactionDetails!![0].bagList[i]
                        if (bagObj.bagMissed) {
                            bagMissedCount += 1
                        } else {
                            if (!bags.contains(bagObj))
                                bags.add(bagObj)
                        }
                    }
                } else {
                    for (bag in it) {
                        if (bag.bagMissed) {
                            bagMissedCount += 1
                        } else {
                            val bagObj = Bag()
                            bagObj.bagMissed = true
                            bagObj.invalidQrCode = bag.invalidQrCode
                            bagObj.bagQrCode = bag.bagQrCode
                            bagObj.weight = bag.weight
                            if (!bags.contains(bagObj))
                                bags.add(bagObj)
                        }
                    }
                }

                binding.tvMissedBagValue.text = "Missed Bags : $bagMissedCount"

                binding.rvMissingBags.setUp(bags, R.layout.bag_missing_list_item, { bagItem, pos ->
                    tvMissQr.text = bagItem.bagQrCode.toString()

                    if (bagItem.bagMissed) {
                        btnMarkMissingBag.text = "Bag is marked as missing"
                        btnMarkMissingBag.isEnabled = false
                    } else {
                        btnMarkMissingBag.text = "Mark Missing"
                        btnMarkMissingBag.isEnabled = true
                    }

                    btnMarkMissingBag.setOnClickListener { view ->
                        if (!bagItem.bagMissed) {
                            showAlert(bagItem, view as Button)
                        }
                    }
                })
            }
        })*/
    }

    private fun getBags(doBags: List<DOBag>): MutableList<Bag> {
        val list = mutableListOf<Bag>()
        list.clear()
        doBags.filter { !it.isScanned && !it.isReplaced }.forEach {
            val bag = Bag()
            bag.bagMissed = it.bagMissed
            bag.bagQrCode = it.bagQrCode
            bag.invalidQrCode = it.invalidQrCode
            bag.weight = it.weight

            list.add(bag)
        }
        /*val filtered = doBags.filter { it.bagMissed }.count()
        if (filtered > 0) {
            doBags.filter { !it.bagMissed && !it.isScanned }.forEach {
                val bag = Bag()
                bag.bagMissed = it.bagMissed
                bag.bagQrCode = it.bagQrCode
                bag.invalidQrCode = it.invalidQrCode
                bag.weight = it.weight

                list.add(bag)
            }
        } else {

        }*/
        return list
    }

    private fun markMissing(bag: Bag) {
        val doBag = DOBag()
        doBag.bagMissed = true
        doBag.invalidQrCode = bag.invalidQrCode
        doBag.bagQrCode = bag.bagQrCode
        doBag.weight = bag.weight
        doBag.isScanned = false
        doBag.lotTransactionId = doTxnDetail.lotTransactionId
        doBag.transactionId = mtransactionID.toString()
        vm.saveBagDetail(bag = doBag)
        val pos = bags.indexOf(bag)
        bags.removeAt(pos)
        bags.add(pos, bag)
//        bagMissedCount += 1
//        binding.tvMissedBagValue.text = "Missed Bags : $bagMissedCount"
        binding.rvMissingBags.adapter?.notifyDataSetChanged()
    }

    private fun showAlert(
        bag: Bag,
        button: Button? = null) {
        val builder = AlertDialog.Builder(this.activity!!).apply {
            setMessage(getString(R.string.mark_as_missing).plus("\n").plus(getString(R.string.qr_code) + " : ").plus(bag.bagQrCode))
            setPositiveButton(getString(R.string.confirm)) { dialogInterface, i ->
                run {
                    dialogInterface.dismiss()
//                    bagMissedCount += 1
//                    binding.tvMissedBagValue.text = "Missed Bags : $bagMissedCount"
//                    button?.text = "Bag is marked as missing"
//                    button?.setBackgroundColor(Color.DKGRAY)
//                    button?.isEnabled = false
//                    adapter.remove(pos, bag)
                    markMissing(bag)
                }
            }
            setNegativeButton(getString(R.string.cancel)) { dialogInterface, i ->
                run {
                    dialogInterface.cancel()
                }
            }
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    companion object {
        fun newInstance(data: DOTxnDetail, id: String) = MissingBagsFragment().putArgs {
            putParcelable("doTxnDetail", data)
            putString("transactionid", id)
        }
    }
}
