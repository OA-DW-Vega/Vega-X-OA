package com.olam.warehouse.vegax.portwarehouse.ui.pile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileBale
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.PortPileStorageLocationModel
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.FragmentPortPileConfirmBinding
import com.olam.warehouse.vegax.portwarehouse.ui.success.PortSuccessActivity
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.CONFIRM_PILE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.PILE_ID
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SELECTED_BALE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SELECTED_GRADE_ITEM
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SUB_TITLE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.TITLE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/16/2020.
 */
class PortPileConfirmFragment : BaseFragment() {

    private var pileId: String? = ""
    var mCurrentPiles = PortPileStorageLocationModel()
    private lateinit var mAdapter: PortPileConfirmAdapter
    private var balesData = listOf<PortPileBale>()

    private val vm: PortPileAddBaleViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_port_pile_confirm
    private lateinit var binding: FragmentPortPileConfirmBinding

    companion object {
        fun newInstance(pileId: PortPileStorageLocationModel) =
            PortPileConfirmFragment().putArgs {
                putParcelable(PILE_ID, pileId)
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPortPileConfirmBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/pile/PortPileConfirmFragment")
            .title("Portwarehouse").with(tracker)
    }

    private fun intiExtras() {
        mCurrentPiles = arguments?.getParcelable(PILE_ID)!!
        pileId = mCurrentPiles.storageLocationCode
    }

    fun initUI() {
        mAdapter = PortPileConfirmAdapter { moveBaleDetail(it) }
        binding.rvPileConfirm.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvPileConfirm.adapter = mAdapter

        pileId?.let {
            binding.tvPileId.text = pileId.plus(" - ").plus(mCurrentPiles.classification)
            vm.getBaleListByStorageId(it)
        }
        vm.getBaleListByStorageId.observe(viewLifecycleOwner, Observer {

            balesData = it
            binding.tvWeight.text =
                balesData.sumByDouble { it.netWeight ?: 0.0 }.formatTwoDigits().toString().plus(" ")
                    .plus("KG")
            binding.tvBaleCount.text = balesData.size.toString()
            balesData.forEach {
                if (vm.baleHashMap.containsKey(it.grade)) {
                    vm.baleHashMap[it.grade.toString()] =
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }
                } else {
                    vm.baleHashMap.put(
                        it.grade.toString(),
                        balesData.filter { it1 -> it1.grade.equals(it.grade) })
                }
            }
            setUpAdapter(vm.baleHashMap)
        })

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }

        vm.postPile
            .observe(viewLifecycleOwner, Observer {
                hideLoading()
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            it.data.let {
                                mCurrentPiles.storageLocationCode.let { vm.deleteBalesDB(it) }
                                moveToSuccess(it.MessageV1)
                            }
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                    }
                }
            })

    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<PortPileBale>>) {
        mAdapter.updateData(baleHashMap)
    }

    private fun moveBaleDetail(baleList: List<PortPileBale>?) {
        val intent = Intent(requireContext(), PortPileBaleInfoActivity::class.java)
        intent.putParcelableArrayListExtra(PILE_LIST, baleList as ArrayList<PortPileBale>)
        intent.putExtra(CONFIRM_PILE, true)
        startActivityForResult(intent, SELECTED_GRADE_ITEM)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(com.olam.warehouse.vegax.portwarehouse.R.string.confirm_pile)
            try {
                var msg = this.findViewById<TextView>(android.R.id.message)
                msg.gravity = Gravity.CENTER
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.yes),
                getString(R.string.cancel),
                {
                    postDispatch()
                },
                { dismiss() })
        }
    }

    private fun postDispatch() {
        showLoading()
        mCurrentPiles.bales = balesData
        vm.postPile(mCurrentPiles)

    }

    private fun moveToSuccess(messageV1: String) {
        val intent = Intent(context, PortSuccessActivity::class.java)
        intent.putExtra(TITLE, getString(R.string.pile_completed_success))
        intent.putExtra(
            SUB_TITLE,
            getString(R.string.pile_no_is).plus(mCurrentPiles.storageLocationCode).plus("\n").plus(
                messageV1
            )
        )
        context?.startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == SELECTED_GRADE_ITEM && resultCode == Activity.RESULT_OK -> {
                data?.extras?.getParcelable<PortPileBale>(SELECTED_BALE)?.let {
                    val baleList = arrayListOf<PortPileBale>()
                    val dat = vm.baleHashMap[it.grade]
                    dat?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.baleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)
                    vm.baleHashMap[it.grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()

                    binding.tvWeight.text =
                        baleList.sumByDouble { it.netWeight ?: 0.0 }.formatTwoDigits().toString()
                            .plus(" ")
                            .plus("KG")
                    binding.tvBaleCount.text = baleList.size.toString()
                }
            }
        }
    }

}
