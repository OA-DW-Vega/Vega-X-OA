package com.olam.warehouse.vegax.dummyquality.ui

import VegaDummySampleListAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.UIUtils
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.vegax.dummyquality.R
import com.olam.warehouse.vegax.dummyquality.data.domain.model.ResponseDummySampleList
import com.olam.warehouse.vegax.dummyquality.databinding.FragmentDummySampleListBinding
import com.olam.warehouse.vegax.dummyquality.utils.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaCommonDummySampleListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_dummy_sample_list
    private lateinit var binding: FragmentDummySampleListBinding
    private val viewModel: VegaCommonDummyQualityViewModel by viewModel()
    private var callBack: CallBack? = null
    private lateinit var materialName: String
    private lateinit var materialCode: String
    private lateinit var supplierName: String
    private lateinit var supplierCode: String
    private lateinit var id: String
    private lateinit var mAdapter: VegaDummySampleListAdapter
    private var cardId: Int = 0
    private var dummySampleList: ArrayList<ResponseDummySampleList> = ArrayList()
    private var position: Int = -1

    companion object {
        fun newInstance(
            supplierCode: String, supplierName: String, materialName: String, materialCode: String, id: String
        ) = VegaCommonDummySampleListFragment().putArgs {
            putString(SUPPLIER_CODE, supplierCode)
            putString(SUPPLIER_NAME, supplierName)
            putString(MATERIAL_NAME, materialName)
            putString(MATERIAL_CODE, materialCode)
            putString(MATERIAL_ID, id)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDummySampleListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        supplierName = requireArguments().getString(SUPPLIER_NAME).toString()
        supplierCode = requireArguments().getString(SUPPLIER_CODE).toString()
        materialName = requireArguments().getString(MATERIAL_NAME).toString()
        materialCode = requireArguments().getString(MATERIAL_CODE).toString()
        id = requireArguments().getString(MATERIAL_ID).toString()

        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())

        viewModel.getDummySampleList(
            getCurrentKey(), getPlantDetails().plantId, "000000" + materialCode, supplierCode, true
        )

        viewModel.dummySampleList.observe(viewLifecycleOwner) { it ->
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.let { response ->
                        dummySampleList = response.data as ArrayList<ResponseDummySampleList>
                        if (dummySampleList.isNullOrEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                            mAdapter = VegaDummySampleListAdapter(dummySampleList)
                            binding.recyclerview.adapter = mAdapter
                            mAdapter.onItemClick = { item, option, position, view ->
                                if (option == 1) {
                                    item.id
                                    cardId = item.id!!
                                    IS_VIEW_QUALITY = true
                                    callBack?.replaceFragment(
                                        DUMMY_QUALITY_CAPTURE_ACCEPT,
                                        supplierCode,
                                        supplierName,
                                        materialName,
                                        materialCode,
                                        item.id.toString()
                                    )
                                } else {
                                    showConformationDialog(position, view, item.id.toString())
                                }
                            }
                        }
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
                else -> {
                }
            }
        }

        viewModel.dummySampleDeleteList.observe(viewLifecycleOwner) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    if(it.data?.success == true) {
//                        moveToSuccessPage(it.data?.message, model.transactionNumber)
                        dummySampleList.removeAt(position)
                    mAdapter.notifyDataSetChanged()
                        if(mAdapter.itemCount == 0) {
                            binding.tvNoData.visibility = View.VISIBLE
                        }else
                        {
                            binding.tvNoData.visibility = View.GONE
                        }
                    }else {
                        showErrorDialogWithFAQLink(requireContext(),it.data?.message.toString())
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()
                    showErrorDialogWithFAQLink(requireContext(),it.error.toString())
                }
                else -> {
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? CallBack
    }

    interface CallBack {
        fun replaceFragment(
            moveFrag: String,
            supplierCode: String,
            supplierName: String,
            materialName: String,
            materialId: String,
            id: String
        )
    }

    private fun showConformationDialog(positions: Int, view: View, id: String) {
        MaterialDialog(view.context).show {
            message((R.string.conform_remove))
            UIUtils.getMetirialCustomView(this,
                view.context.getString(com.olam.warehouse.presentation.R.string.confirm),
                view.context.getString(R.string.cancel_remove),
                {
                    position = positions
                    viewModel.deleteDummySample(id)
//
                },
                { dismiss() })
        }
    }


}
