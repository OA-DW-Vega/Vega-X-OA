package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.pile

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
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.GinningPileStorageLocationModel
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.PileBale
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils.getMetirialCustomView
import com.olam.warehouse.presentation.utils.extension.formatTwoDigits
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.FragmentGinningPileConfirmBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

/**
 * Created by Baskaran Kannan on 4/16/2020.
 */
class GinningPileConfirmFragment : BaseFragment() {

    private var pileId: String? = ""
    var mCurrentPiles = GinningPileStorageLocationModel()
    private lateinit var mAdapter: GinningPileConfirmAdapter
    private var balesData = listOf<PileBale>()

    private val vm: GinningPileAddBaleViewModel by viewModel()
    override val layoutResourceId = R.layout.fragment_ginning_pile_confirm
    private lateinit var binding: FragmentGinningPileConfirmBinding

    companion object {
        fun newInstance(pileId: GinningPileStorageLocationModel) =
            GinningPileConfirmFragment().putArgs {
                putParcelable(PILE_ID, pileId)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGinningPileConfirmBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intiExtras()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("ginningwarehouse/ui/pile/GinningPileConfirmFragment")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun intiExtras() {
        mCurrentPiles = arguments?.getParcelable<GinningPileStorageLocationModel>(PILE_ID)!!
        pileId = mCurrentPiles.storageLocationCode
    }

    fun initUI() {
        mAdapter = GinningPileConfirmAdapter { moveBaleDetail(it) }
        binding.rvPileConfirm.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvPileConfirm.adapter = mAdapter

        pileId?.let {
            binding.tvPileId.text = pileId.plus(" - ").plus(mCurrentPiles.classification)
            balesData = runBlocking {
                withContext(Dispatchers.IO)
                {
                    vm.getBaleListByStorageId(it)
                }
            }


            binding.tvWeight.text =
                balesData.sumByDouble { it.netWeight?.toDouble() ?: 0.0 }.formatTwoDigits()
                    .toString().plus(" ")
                    .plus("KG")
            binding.tvBaleCount.text = balesData.size.toString()
            balesData.forEach {
                if (vm.mBaleHashMap.containsKey(it.grade)) {
                    vm.mBaleHashMap[it.grade.toString()] =
                        balesData.filter { it1 -> it1.grade.equals(it.grade) }
                } else {
                    vm.mBaleHashMap.put(
                        it.grade.toString(),
                        balesData.filter { it1 -> it1.grade.equals(it.grade) })
                }
            }




            setUpAdapter(vm.mBaleHashMap)
        }

        binding.btnConfirm.setOnClickListener { showConfirmDialog() }


        vm.postPile.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let {
                            if (it.success) {
                                mCurrentPiles.storageLocationCode.let { vm.deleteBalesDB(it) }
                                moveToSuccess(it.data.MessageV1)
                            } else
                            {
                                showErrorDialogWithFAQLink(requireContext(), it.data.message)
                            }
                        }

                        hideLoading()
                    }
                    Resource.Status.LOADING -> showLoading()
                    Resource.Status.ERROR -> {
                        hideLoading()
                        showErrorDialogWithFAQLink(requireContext(), it.error ?: "Something went wrong")
                    }
                }
            }

        })
    }

    private fun setUpAdapter(baleHashMap: HashMap<String, List<PileBale>>) {
        mAdapter.updateData(baleHashMap)
    }

    private fun moveBaleDetail(baleList: List<PileBale>?) {
        val intent = Intent(requireContext(), GinningPileBaleInfoActivity::class.java)
        intent.putParcelableArrayListExtra(PILE_LIST, baleList as ArrayList<PileBale>)
        intent.putExtra(CONFIRM_PILE, true)
        startActivityForResult(intent, SELECTED_GRADE_ITEM)
    }

    private fun showConfirmDialog() {
        MaterialDialog(requireContext()).show {
            message(R.string.confirm_pile)
            try {
                var msg = this.findViewById<TextView>(android.R.id.message)
                msg.gravity = Gravity.CENTER
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            getMetirialCustomView(
                this,
                getString(com.olam.warehouse.presentation.R.string.confirm),
                getString(com.olam.warehouse.presentation.R.string.cancel),
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
        val intent = Intent(context, SuccessActivity::class.java)
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
                data?.extras?.getParcelable<PileBale>(SELECTED_BALE)?.let {
                    val baleList = arrayListOf<PileBale>()
                    val dat = vm.mBaleHashMap[it.grade]
                    dat?.let { it1 -> baleList.addAll(it1) }
                    val bale =
                        vm.mBaleHashMap[it.grade]?.find { it1 -> it1.baleID.equals(it.baleID) }
                    baleList.remove(bale)
                    vm.mBaleHashMap[it.grade.toString()] = baleList
                    mAdapter.notifyDataSetChanged()

                    binding.tvWeight.text =
                        baleList.sumByDouble { it.netWeight?.toDouble() ?: 0.0 }.formatTwoDigits()
                            .toString()
                            .plus(" ")
                            .plus("KG")
                    binding.tvBaleCount.text = baleList.size.toString()
                }
            }
        }
    }

}
