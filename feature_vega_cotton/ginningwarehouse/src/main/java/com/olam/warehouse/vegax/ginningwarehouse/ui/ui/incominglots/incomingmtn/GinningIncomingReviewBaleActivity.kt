package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.ui.succes.SuccessActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.Mtn
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnGrades
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningIncomingReviewBaleBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.GRADES
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.MTN
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.SUB_TITLE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.TITLE
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import java.util.*

class GinningIncomingReviewBaleActivity : HomeBaseActivity() {

    private val vm: GinningIncomingReviewViewModel by viewModel { emptyParametersHolder() }

    private var mtnNumber: String? = null
    private var mtn: Mtn? = null
    private var grades: ArrayList<MtnGrades>? = null

    override val layoutResourceId: Int =
        R.layout.activity_ginning_incoming_review_bale
    private lateinit var binding: ActivityGinningIncomingReviewBaleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //injectGinningIncomingLOTsIncomingMtnFeature()
        binding = ActivityGinningIncomingReviewBaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initExtras()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incomingmtn/GinningIncomingReviewBaleActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtras() {
        mtn = intent.getParcelableExtra(MTN)
        grades = intent.getParcelableArrayListExtra(GRADES)
        mtnNumber = mtn?.mtnNumber
    }

    private fun initUI() {

        binding.tvPlant.text = mtn?.suplierPlantDesc
        binding.tvWeight.text = mtn?.sourceNetWeight.plus(" ").plus(mtn?.uom)
        val grades = grades?.map { it.grade }
        binding.tvGrades.text = grades.toString().replace("[", "").replace("]", "")
        binding.btnStoreInInventory.setOnClickListener {


            //if (isOnline()) {
            val mtnModel = vm.getMtnModel()
            mtnModel?.let {
                showLoading()
                vm.postMtnWithBales(mtnModel)
            }

            /*} else {
                vm.updateOfflineMtnStatus(vm.getVerifiedBales())
                moveToSuccess("")
            }*/
        }
        vm.postMtnWithBales.observe(this, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {

                    it.data?.let {
                        it.data.let {
                            if (it.success) {
                                mtnNumber?.let { mtn ->
                                    vm.deleteMtnsByMtnId(mtn)
                                    it.message.let { message ->
                                        moveToSuccess(message.trim())
                                    }
                                }
                            } else {
                                showErrorDialogWithFAQLink(this, it.message.toString())

                            }
                        }
                    }

                    hideLoading()
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(this, it.error.toString())
                }
            }
        })

        vm.getMtnWithBalesOffline.observe(this, Observer {
            if (it != null) {
                binding.tvMtnNumber.text = mtnNumber
                vm.setMtnWithBales(it)
                vm.getTotalCount()?.let {
                    binding.tvBaleReceived.text = it
                }
                vm.getBaleGoodCount()?.let {
                    binding.tvBaleGood.text = it
                }
                vm.getBaleTieDamageCount()?.let {
                    binding.tvBaleTieDamage.text = it
                }

                vm.getWetBaleCount()?.let {
                    binding.tvWetBale.text = it
                }

                vm.getNoBaleTagCount()?.let {
                    binding.tvNoBaleTag.text = it
                }

                vm.getBaleCottonCleanCount()?.let {
                    binding.tvCottonClean.text = it
                }

                vm.getBaleCottonDirtyCount()?.let {
                    binding.tvBaleCottonDirty.text = it
                }

                vm.getVerifiedCount()?.let {
                    binding.tvBaleVerified.text = it
                }

                vm.getYetToVerifyCount()?.let {
                    binding.tvBaleYetToVerify.text = it
                }
            } else {
                toast("No MTN with Bales")
            }
        })


        mtnNumber?.let { mtnNumber ->
            vm.getMtnWithBales(mtnNumber)
        }
    }


    private fun moveToSuccess(materialDoc: String) {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(TITLE, getString(R.string.offloaded_successfully))
        intent.putExtra(
            SUB_TITLE,
            getString(R.string.mtn_no).plus(mtnNumber).plus("\n").plus(materialDoc)
        )
        startActivity(intent)
    }

}
