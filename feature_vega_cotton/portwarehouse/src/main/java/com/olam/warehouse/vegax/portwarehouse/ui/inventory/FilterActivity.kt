package com.olam.warehouse.vegax.portwarehouse.ui.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleMark
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.CropYears
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.Grade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PileModel
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import com.olam.warehouse.vegax.portwarehouse.databinding.FilterActivityBinding
import com.olam.warehouse.vegax.portwarehouse.databinding.GradeRowItemBinding
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.CROP_YEARS
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_BALE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_GRADE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_MARK
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_PILES
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.FILTER_YEAR
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.GRADE
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.MARK_LIST
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.vegax.portwarehouse.utils.PortWHUtil.SELECTED_GRADE
import com.olam.warehouse.vegax.portwarehouse.utils.enums.BaleStatus
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper

class FilterActivity : HomeBaseActivity() {

    private var typeOfBale: String? = ""
    private var mGrades = arrayListOf<Grade>()
    private var mBaleMarks = arrayListOf<BaleMark>()
    private var mCropYears = arrayListOf<CropYears>()
    private var mPileList = arrayListOf<PileModel>()
    private var selectedGradeList = ArrayList<String>()
    var mFilterAdapter = FilterAdapter({ selectedGrade(it) }, this)

    private fun selectedGrade(position: Int) {
        when {
            mGrades[position].isSelect!! -> {
                mGrades[position].isSelect = false
            }
            else -> {
                mGrades[position].isSelect = true
            }
        }
        mFilterAdapter.addItems(mGrades)

    }

    override val layoutResourceId = R.layout.filter_activity
    private lateinit var binding: FilterActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FilterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InitUI()
        InitExtra()
        val tracker: Tracker? = App.getTracker()
        TrackHelper.track().screen("portwarehouse/ui/inventory/FilterActivity")
            .title("Portwarehouse").with(tracker)
    }

    private fun InitExtra() {
        mGrades = intent?.getParcelableArrayListExtra<Grade>(GRADE) as ArrayList<Grade>
        mBaleMarks = intent?.getParcelableArrayListExtra<BaleMark>(MARK_LIST) as ArrayList<BaleMark>
        mCropYears =
            intent?.getParcelableArrayListExtra<CropYears>(CROP_YEARS) as ArrayList<CropYears>
        mPileList =
            intent?.getParcelableArrayListExtra<PileModel>(PILE_LIST) as ArrayList<PileModel>
        selectedGradeList = intent?.getStringArrayListExtra(SELECTED_GRADE) as ArrayList<String>
        mGrades.forEach { item -> if (selectedGradeList.contains(item.grade)) item.isSelect = true }
        when (mGrades != null) {
            true -> mFilterAdapter.addItems(mGrades)
            else -> {}
        }
        setUpMarkAdapter(mBaleMarks)
        setUpCropYearAdapter(mCropYears)
        setUpPileAdapter(mPileList)
    }

    private fun InitUI() {
        binding.rvGrades.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.rvGrades.adapter = mFilterAdapter

        binding.rgType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGood -> {
                    typeOfBale = "Good"
                    showDamageType(false)
                    binding.cbSelectDamageType.isChecked = false
                    binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
                    binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
                    binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
                    binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
                    binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
                }
                R.id.rbDamaged -> {
                    showDamageType(true)
                    typeOfBale = "Damage"
                }
            }
        }


        binding.cbSelectDamageType.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionDamageType(true)
                else -> setSelectionDamageType(false)
            }
        }

        binding.cbSelectGrade.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionGrade(true)
                else -> setSelectionGrade(false)
            }
        }

        binding.cbSelectMarks.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionMark(true)
                else -> setSelectionMark(false)
            }
        }

        binding.cbSelectYears.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionYear(true)
                else -> setSelectionYear(false)
            }
        }

        binding.cbSelectPiles.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionPiles(true)
                else -> setSelectionPiles(false)
            }
        }

        binding.cbDamageClearCotton.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        binding.cbDamageDirtyCotton.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        binding.cbBaleTieDamage.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
        binding.cbWetBale.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
        binding.cbNoBaleTag.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        binding.rbGood.text = if(getCurrentKey().split("_")[2].contains("COTTPORT")){
            PortWHUtil.getStorageID().plus(" - ").plus(BaleStatus.Good.status)}else{BaleStatus.Good.id.toString().plus(" - ").plus(BaleStatus.Good.status)}
        binding.cbDamageClearCotton.text =
            BaleStatus.CottonClean.id.toString().plus(" - ").plus(BaleStatus.CottonClean.status)
        binding.cbDamageDirtyCotton.text =
            BaleStatus.CottonDirty.id.toString().plus(" - ").plus(BaleStatus.CottonDirty.status)
        binding.cbBaleTieDamage.text =
            BaleStatus.TieDamage.id.toString().plus(" - ").plus(BaleStatus.TieDamage.status)
        binding.cbWetBale.text =
            BaleStatus.WetBale.id.toString().plus(" - ").plus(BaleStatus.WetBale.status)
        binding.cbNoBaleTag.text =
            BaleStatus.NoBaleTag.id.toString().plus(" - ").plus(BaleStatus.NoBaleTag.status)

        binding.ivBack.setOnClickListener { moveToInventory() }
        binding.tvClear.setOnClickListener { clearFilter() }
        binding.tvApply.setOnClickListener { applyFilter() }

    }

    private fun moveToInventory() {
        finish()
    }

    private fun showDamageType(type: Boolean) {
        when (type) {
            true -> {
                binding.llDamageType.visible()
                binding.vDamageType.visible()
            }
            false -> {
                binding.llDamageType.gone()
                binding.vDamageType.gone()
            }
        }

    }

    private fun clearFilter() {
        binding.rbGood.isChecked = false
        binding.rbDamaged.isChecked = false
        binding.cbDamageClearCotton.isChecked = false
        binding.cbDamageDirtyCotton.isChecked = false
        binding.cbBaleTieDamage.isChecked = false
        binding.cbWetBale.isChecked = false
        binding.cbNoBaleTag.isChecked = false
        binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
        binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
        binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
        binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
        binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
        binding.cbSelectGrade.isChecked = false
        showDamageType(false)
        setSelectionGrade(false)
        setSelectionMark(false)
        setSelectionYear(false)
        setSelectionPiles(false)

    }

    private fun setSelectionGrade(type: Boolean) {
        mGrades.forEach { it.isSelect = type }
        mFilterAdapter.addItems(mGrades)
    }

    private fun setSelectionMark(type: Boolean) {
        mBaleMarks.forEach { it.isChecked = type }
        binding.rvMarks.adapter?.notifyDataSetChanged()
    }

    private fun setSelectionYear(type: Boolean) {
        mCropYears.forEach { it.isChecked = type }
        binding.rvYears.adapter?.notifyDataSetChanged()
    }

    private fun setSelectionPiles(type: Boolean) {
        mPileList.forEach { it.isChecked = type }
        binding.rvPiles.adapter?.notifyDataSetChanged()
    }
    private fun setSelectionDamageType(type: Boolean) {
        when (type) {
            true -> {
                binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_green_port)
                binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_green_port)
                binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_green_port)
            }
            false -> {
                binding.cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
                binding.cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
                binding.cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
                binding.cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
                binding.cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
    }

    private fun applyFilter() {
        val baleTypeList = ArrayList<String>()
        val gradeList = ArrayList<String>()
        if (binding.cbDamageClearCotton.isChecked) {
            baleTypeList.add(BaleStatus.CottonClean.status)
        }
        if (binding.cbSelectDamageType.isChecked) {
            baleTypeList.add(BaleStatus.CottonClean.status)
            baleTypeList.add(BaleStatus.CottonDirty.status)
            baleTypeList.add(BaleStatus.TieDamage.status)
            baleTypeList.add(BaleStatus.WetBale.status)
            baleTypeList.add(BaleStatus.NoBaleTag.status)
        }
        if (binding.cbDamageDirtyCotton.isChecked) {
            baleTypeList.add(BaleStatus.CottonDirty.status)
        }
        if (binding.cbBaleTieDamage.isChecked) {
            baleTypeList.add(BaleStatus.TieDamage.status)
        }
        if (binding.cbWetBale.isChecked) {
            baleTypeList.add(BaleStatus.WetBale.status)
        }
        if (binding.cbNoBaleTag.isChecked) {
            baleTypeList.add(BaleStatus.NoBaleTag.status)
        }
        if (binding.rbGood.isChecked) {
            baleTypeList.add(BaleStatus.Good.status)
        }

        mGrades.forEach {
            when {
                it.isSelect!! -> {
                    gradeList.add(it.grade!!)
                }
            }
        }

        val intent = Intent()
        intent.putExtra(FILTER_BALE, baleTypeList)
        intent.putExtra(FILTER_GRADE, gradeList)
        intent.putExtra(FILTER_MARK, mBaleMarks)
        intent.putExtra(FILTER_YEAR, mCropYears)
        intent.putExtra(FILTER_PILES, mPileList)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setUpMarkAdapter(markList: ArrayList<BaleMark>) {
        binding.rvMarks.setUpAdapter(
            markList,
            R.layout.grade_row_item,
            GradeRowItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvRowItem.text = it.baleMark
                bindItem.tvRowItem.setOnClickListener { selectedMarks(pos) }
                when {
                    it.isChecked -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                    else -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
                }

            },
            {},
            GridLayoutManager(this, 4)
        )
    }

    private fun setUpCropYearAdapter(cropYears: ArrayList<CropYears>) {
        binding.rvYears.setUpAdapter(
            cropYears,
            R.layout.grade_row_item,
            GradeRowItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvRowItem.text = it.cropYear
                bindItem.tvRowItem.setOnClickListener { selectedYears(pos) }
                when {
                    it.isChecked -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                    else -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
                }

            },
            {},
            GridLayoutManager(this, 3)
        )
    }

    private fun setUpPileAdapter(piles: ArrayList<PileModel>) {
        binding.rvPiles.setUpAdapter(
            piles,
            R.layout.grade_row_item,
            GradeRowItemBinding::inflate,
            { it, pos, bindItem ->
                bindItem.tvRowItem.text = it.storageLocationCode.plus(" - ").plus(it.classification)
                bindItem.tvRowItem.setOnClickListener { selectedPiles(pos) }
                when {
                    it.isChecked -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                    else -> bindItem.tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
                }

            },
            {},
            GridLayoutManager(this, 1)
        )
    }

    private fun selectedMarks(position: Int) {
        when {
            mBaleMarks[position].isChecked -> {
                mBaleMarks[position].isChecked = false
            }
            else -> {
                mBaleMarks[position].isChecked = true
            }
        }
        binding.rvMarks.adapter?.notifyDataSetChanged()
    }

    private fun selectedYears(position: Int) {
        when {
            mCropYears[position].isChecked -> {
                mCropYears[position].isChecked = false
            }
            else -> {
                mCropYears[position].isChecked = true
            }
        }
        binding.rvYears.adapter?.notifyDataSetChanged()
    }

    private fun selectedPiles(position: Int) {
        when {
            mPileList[position].isChecked -> {
                mPileList[position].isChecked = false
            }
            else -> {
                mPileList[position].isChecked = true
            }
        }
        binding.rvPiles.adapter?.notifyDataSetChanged()
    }
}
