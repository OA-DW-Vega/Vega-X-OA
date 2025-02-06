package com.olam.warehouse.vegax.ginningwarehouse.ui.ui.incominglots.incomingmtn

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.MtnBales
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.ginningwarehouse.R
import com.olam.warehouse.vegax.ginningwarehouse.databinding.ActivityGinningIncomingBaleStatusBinding
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.BALE
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.PLANT
import com.olam.warehouse.vegax.ginningwarehouse.ui.utils.enums.BaleStatus
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.emptyParametersHolder
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

@Suppress("DEPRECATION")
class GinningIncomingBaleStatusActivity : HomeBaseActivity() {

    private val vm: GinningIncomingBaleStatusViewModel by viewModel { emptyParametersHolder() }

    override val layoutResourceId: Int = R.layout.activity_ginning_incoming_bale_status

    private var baleStatus: BaleStatus? = null
    private var bale: MtnBales? = null
    private var plantName: String? = ""
    private lateinit var binding: ActivityGinningIncomingBaleStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGinningIncomingBaleStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // injectGinningIncomingLOTsIncomingMtnFeature()
        initExtras()
        initNavigationView()
        initUI()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track()
            .screen("ginningwarehouse/ui/incomingmtn/GinningIncomingBaleStatusActivity")
            .title("Ginningwarehouse").with(tracker)
    }

    private fun initExtras() {
        bale = intent.getParcelableExtra(BALE)
        plantName = intent.getStringExtra(PLANT)
        Log.d("BALE", bale.toString())
    }

    private fun initUI() {

        binding.tvPlant.text = plantName
        binding.llDamaged.setOnClickListener {
            checkDamageRadioButton()
            enableUpdateButton()
            showDamageTypeDialog()
        }

        binding.llGood.setOnClickListener {
            checkGoodRadioButton()
            enableUpdateButton()
            binding.llDamageTypeContainer.visibility = View.INVISIBLE
            baleStatus = BaleStatus.Good
        }

        binding.btnUpdateStatus.setOnClickListener {
            vm.updateMtnBale(bale, baleStatus)
            finish()
        }

        updateUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        bale?.let {
            binding.tvReceivedFrom.text = it.mtnNumber
            binding.tvBaleGrade.text = it.grade

            if (it.delyNetQty != null && it.delyNetQty?.isNotEmpty() == true) {
                binding.tvBaleWeight.text = it.delyNetQty + " " + it.unitOfMeasurement
            } else {
                binding.tvBaleWeight.text = it.netWeight + " " + it.unitOfMeasurement
            }

            binding.tvBaleNumber.text = it.baleId

            if (it.isVerified == 1) {
                enableUpdateButton()
                it.baleStatus?.let { status ->
                    baleStatus = BaleStatus.from(status)
                    when (baleStatus) {
                        BaleStatus.Good -> {
                            binding.rbGood.isChecked = true
                            hideDamageType()
                        }
                        BaleStatus.CottonClean -> {
                            checkDamageRadioButton()
                            showDamageType(BaleStatus.CottonClean.status)
                        }
                        BaleStatus.CottonDirty -> {
                            checkDamageRadioButton()
                            showDamageType(BaleStatus.CottonDirty.status)
                        }
                        BaleStatus.TieDamage -> {
                            checkDamageRadioButton()
                            showDamageType(BaleStatus.TieDamage.status)
                        }
                        BaleStatus.WetBale -> {
                            checkDamageRadioButton()
                            showDamageType(BaleStatus.WetBale.status)
                        }
                        BaleStatus.NoBaleTag -> {
                            checkDamageRadioButton()
                            showDamageType(BaleStatus.NoBaleTag.status)
                        }
                        else -> {}
                    }
                }

            }
        }
    }



    private fun showDamageTypeDialog() {
        val dialog = MaterialDialog(this)
            .customView(
                R.layout.ginning_dialog_damage_type,
                scrollable = false,
                noVerticalPadding = true
            )
        val dialogView = dialog.getCustomView()
        val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)

        val llCottonClean = dialogView.findViewById<LinearLayout>(R.id.llCottonClean)
        val llCottonDirty = dialogView.findViewById<LinearLayout>(R.id.llCottonDirty)
        val llTieDamage = dialogView.findViewById<LinearLayout>(R.id.llTieDamage)
        val llWetBale = dialogView.findViewById<LinearLayout>(R.id.llWetBale)
        val llNoBaleTag = dialogView.findViewById<LinearLayout>(R.id.llNoBaleTag)
        val flCottonClean = dialogView.findViewById<FrameLayout>(R.id.flCottonClean)
        val flCottonDirty = dialogView.findViewById<FrameLayout>(R.id.flCottonDirty)
        val flTieDamage = dialogView.findViewById<FrameLayout>(R.id.flTieDamage)
        val flWetBale = dialogView.findViewById<FrameLayout>(R.id.flWetBale)
        val flNoBaleTag = dialogView.findViewById<FrameLayout>(R.id.flNoBaleTag)

        when (baleStatus) {
            BaleStatus.CottonClean -> {
                llCottonClean.setBackgroundColor(resources.getColor(R.color.warm_grey))
                flCottonClean.visibility = View.VISIBLE
            }
            BaleStatus.CottonDirty -> {
                llCottonDirty.setBackgroundColor(resources.getColor(R.color.warm_grey))
                flCottonDirty.visibility = View.VISIBLE
            }
            BaleStatus.TieDamage -> {
                llTieDamage.setBackgroundColor(resources.getColor(R.color.warm_grey))
                flTieDamage.visibility = View.VISIBLE
            }
            BaleStatus.WetBale -> {
                llWetBale.setBackgroundColor(resources.getColor(R.color.warm_grey))
                flWetBale.visibility = View.VISIBLE
            }
            BaleStatus.NoBaleTag -> {
                llNoBaleTag.setBackgroundColor(resources.getColor(R.color.warm_grey))
                flNoBaleTag.visibility = View.VISIBLE
            }
            else -> {}
        }

        llCottonClean?.setOnClickListener {
            baleStatus = BaleStatus.CottonClean
            showDamageType(BaleStatus.CottonClean.status)
            dialog.dismiss()
        }

        llCottonDirty?.setOnClickListener {
            baleStatus = BaleStatus.CottonDirty
            showDamageType(BaleStatus.CottonDirty.status)
            dialog.dismiss()
        }

        llTieDamage?.setOnClickListener {
            baleStatus = BaleStatus.TieDamage
            showDamageType(BaleStatus.TieDamage.status)
            dialog.dismiss()
        }

        llWetBale?.setOnClickListener {
            baleStatus = BaleStatus.WetBale
            showDamageType(BaleStatus.WetBale.status)
            dialog.dismiss()
        }

        llNoBaleTag?.setOnClickListener {
            baleStatus = BaleStatus.NoBaleTag
            showDamageType(BaleStatus.NoBaleTag.status)
            dialog.dismiss()
        }

        ivClose?.setOnClickListener {
            if (baleStatus == BaleStatus.Good) {
                checkGoodRadioButton()
            } else if (baleStatus == null) {
                disableUpdateButton()
                unCheckAllRadioButton()
            }
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showDamageType(type: String) {
        binding.llDamageTypeContainer.visibility = View.VISIBLE
        binding.tvDamageType.text = type
    }

    private fun hideDamageType() {
        binding.llDamageTypeContainer.visibility = View.INVISIBLE
        binding.tvDamageType.text = ""
    }

    private fun enableUpdateButton(){
        binding.btnUpdateStatus.setBackgroundColor(
            ContextCompat.getColor(this, com.olam.warehouse.presentation.R.color.colorPrimaryOfi)
        )
        binding.btnUpdateStatus.isEnabled = true
    }

    private fun disableUpdateButton() {
        binding.btnUpdateStatus.setBackgroundColor(
            ContextCompat.getColor(this, R.color.light_grey)
        )
        binding.btnUpdateStatus.isEnabled = false
    }

    private fun unCheckAllRadioButton() {
        binding.rbDamaged.isChecked = false
        binding.rbGood.isChecked = false
    }

    private fun checkGoodRadioButton() {
        binding.rbDamaged.isChecked = false
        binding.rbGood.isChecked = true
    }

    private fun checkDamageRadioButton() {
        binding.rbDamaged.isChecked = true
        binding.rbGood.isChecked = false
    }

}
