package com.olam.warehouse.portwarehouse.ui.inventory

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.BaleMark
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.CropYears
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.Grade
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.model.PileModel
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.CROP_YEARS
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.FILTER_BALE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.FILTER_GRADE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.FILTER_MARK
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.FILTER_PILES
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.FILTER_YEAR
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.GRADE
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.MARK_LIST
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.PILE_LIST
import com.olam.warehouse.portwarehouse.utils.PortWHUtil.SELECTED_GRADE
import com.olam.warehouse.portwarehouse.utils.enums.BaleStatus
import com.olam.warehouse.presentation.adapter.setUp
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.App
import com.olam.warehouse.vegax.portwarehouse.R
import kotlinx.android.synthetic.main.filter_activity.*
import kotlinx.android.synthetic.main.grade_row_item.view.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        }
        setUpMarkAdapter(mBaleMarks)
        setUpCropYearAdapter(mCropYears)
        setUpPileAdapter(mPileList)
    }

    private fun InitUI() {
        rvGrades.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        rvGrades.adapter = mFilterAdapter

        rgType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbGood -> {
                    typeOfBale = "Good"
                    showDamageType(false)
                    cbSelectDamageType.isChecked = false
                    cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
                    cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
                    cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
                    cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
                    cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
                }
                R.id.rbDamaged -> {
                    showDamageType(true)
                    typeOfBale = "Damage"
                }
            }
        }


        cbSelectDamageType.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionDamageType(true)
                else -> setSelectionDamageType(false)
            }
        }

        cbSelectGrade.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionGrade(true)
                else -> setSelectionGrade(false)
            }
        }

        cbSelectMarks.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionMark(true)
                else -> setSelectionMark(false)
            }
        }

        cbSelectYears.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionYear(true)
                else -> setSelectionYear(false)
            }
        }

        cbSelectPiles.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> setSelectionPiles(true)
                else -> setSelectionPiles(false)
            }
        }

        cbDamageClearCotton.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        cbDamageDirtyCotton.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        cbBaleTieDamage.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
        cbWetBale.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> cbWetBale.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
        cbNoBaleTag.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
            }
        }

        rbGood.text = BaleStatus.Good.id.toString().plus(" - ").plus(BaleStatus.Good.status)
        cbDamageClearCotton.text =
            BaleStatus.CottonClean.id.toString().plus(" - ").plus(BaleStatus.CottonClean.status)
        cbDamageDirtyCotton.text =
            BaleStatus.CottonDirty.id.toString().plus(" - ").plus(BaleStatus.CottonDirty.status)
        cbBaleTieDamage.text =
            BaleStatus.TieDamage.id.toString().plus(" - ").plus(BaleStatus.TieDamage.status)
        cbWetBale.text =
            BaleStatus.WetBale.id.toString().plus(" - ").plus(BaleStatus.WetBale.status)
        cbNoBaleTag.text =
            BaleStatus.NoBaleTag.id.toString().plus(" - ").plus(BaleStatus.NoBaleTag.status)

        ivBack.setOnClickListener { moveToInventory() }
        tvClear.setOnClickListener { clearFilter() }
        tvApply.setOnClickListener { applyFilter() }

    }

    private fun moveToInventory() {
        finish()
    }

    private fun showDamageType(type: Boolean) {
        when (type) {
            true -> {
                llDamageType.visible()
                vDamageType.visible()
            }
            false -> {
                llDamageType.gone()
                vDamageType.gone()
            }
        }

    }

    private fun clearFilter() {
        rbGood.isChecked = false
        rbDamaged.isChecked = false
        cbDamageClearCotton.isChecked = false
        cbDamageDirtyCotton.isChecked = false
        cbBaleTieDamage.isChecked = false
        cbWetBale.isChecked = false
        cbNoBaleTag.isChecked = false
        cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
        cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
        cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
        cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
        cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
        cbSelectGrade.isChecked = false
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
        rvMarks.adapter?.notifyDataSetChanged()
    }

    private fun setSelectionYear(type: Boolean) {
        mCropYears.forEach { it.isChecked = type }
        rvYears.adapter?.notifyDataSetChanged()
    }

    private fun setSelectionPiles(type: Boolean) {
        mPileList.forEach { it.isChecked = type }
        rvPiles.adapter?.notifyDataSetChanged()
    }
    private fun setSelectionDamageType(type: Boolean) {
        when (type) {
            true -> {
                cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_green_port)
                cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_green_port)
                cbWetBale.setBackgroundResource(R.drawable.border_corner_green_port)
                cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_green_port)
            }
            false -> {
                cbDamageClearCotton.setBackgroundResource(R.drawable.border_corner_port)
                cbDamageDirtyCotton.setBackgroundResource(R.drawable.border_corner_port)
                cbBaleTieDamage.setBackgroundResource(R.drawable.border_corner_port)
                cbWetBale.setBackgroundResource(R.drawable.border_corner_port)
                cbNoBaleTag.setBackgroundResource(R.drawable.border_corner_port)
            }
        }
    }

    private fun applyFilter() {
        val baleTypeList = ArrayList<String>()
        val gradeList = ArrayList<String>()
        if (cbDamageClearCotton.isChecked) {
            baleTypeList.add(BaleStatus.CottonClean.status)
        }
        if(cbSelectDamageType.isChecked) {
            baleTypeList.add(BaleStatus.CottonClean.status)
            baleTypeList.add(BaleStatus.CottonDirty.status)
            baleTypeList.add(BaleStatus.TieDamage.status)
            baleTypeList.add(BaleStatus.WetBale.status)
            baleTypeList.add(BaleStatus.NoBaleTag.status)
        }
        if (cbDamageDirtyCotton.isChecked) {
            baleTypeList.add(BaleStatus.CottonDirty.status)
        }
        if (cbBaleTieDamage.isChecked) {
            baleTypeList.add(BaleStatus.TieDamage.status)
        }
        if (cbWetBale.isChecked) {
            baleTypeList.add(BaleStatus.WetBale.status)
        }
        if (cbNoBaleTag.isChecked) {
            baleTypeList.add(BaleStatus.NoBaleTag.status)
        }
        if (rbGood.isChecked) {
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
        rvMarks.setUp(markList, R.layout.grade_row_item, { it, pos ->
            tvRowItem.text = it.baleMark
            tvRowItem.setOnClickListener { selectedMarks(pos) }
            when {
                it.isChecked -> tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
            }

        }, {}, GridLayoutManager(this, 4))
    }

    private fun setUpCropYearAdapter(cropYears: ArrayList<CropYears>) {
        rvYears.setUp(cropYears, R.layout.grade_row_item, { it, pos ->
            tvRowItem.text = it.cropYear
            tvRowItem.setOnClickListener { selectedYears(pos) }
            when {
                it.isChecked -> tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
            }

        }, {}, GridLayoutManager(this, 3))
    }

    private fun setUpPileAdapter(piles: ArrayList<PileModel>) {
        rvPiles.setUp(piles, R.layout.grade_row_item, { it, pos ->
            tvRowItem.text = it.storageLocationCode.plus(" - ").plus(it.classification)
            tvRowItem.setOnClickListener { selectedPiles(pos) }
            when {
                it.isChecked -> tvRowItem.setBackgroundResource(R.drawable.border_corner_green_port)
                else -> tvRowItem.setBackgroundResource(R.drawable.border_corner_port)
            }

        }, {}, GridLayoutManager(this, 1))
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
        rvMarks.adapter?.notifyDataSetChanged()
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
        rvYears.adapter?.notifyDataSetChanged()
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
        rvPiles.adapter?.notifyDataSetChanged()
    }
}
