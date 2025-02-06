package com.olam.warehouse.login.ui

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.widget.doOnTextChanged
import com.olam.warehouse.login.R
import com.olam.warehouse.login.data.domain.model.FaqModel
import com.olam.warehouse.login.databinding.ActivityFaqBinding
import com.olam.warehouse.login.databinding.ItemFaqBinding
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.login.vm.LoginViewModel
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import org.koin.androidx.viewmodel.ext.android.viewModel


class FrequentlyAskedQActivity : HomeBaseActivity() {

    private lateinit var binding: ActivityFaqBinding
    private val vm: LoginViewModel by viewModel()

    private var allList = mutableListOf<FaqModel>()
    private var mSearchList = mutableListOf<FaqModel>()
    private var isDirectError = false
    private var errorMsg = ""
    private var isExactValue = true

    override val layoutResourceId = R.layout.activity_faq


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationView()
        initExtra()
        /*val ivBack = findViewById<ImageView>(com.olam.warehouse.master.R.id.iv_back)
        ivBack.visible()

        ivBack.setOnClickListener {
            finish()
        }*/
        vm.getFaq()
        vm.faqData.observe(this) { updateUi(it) }


        editTextSearch()
    }

    private fun editTextSearch(){
        binding.etSearch.doOnTextChanged { newText, start, count, after ->
            newText?.let {text->
                if (newText.isEmpty()) {
                    setUpAdapter(allList)
                } else {
                    mSearchList.clear()
                    searchData(text.toString())
                    setUpAdapter(mSearchList)

                }
            }
        }

    }

    private fun searchData(text:String){
        if(isDirectError) isExactValue = allList.any { it.question.equals(text) }
        run mainloop@{
            allList.forEach { data ->
                val containTextArray = text.trim().split(" ")
                if (containTextArray.size > 1 && isDirectError) {
                    containTextArray.forEachIndexed { index, s ->
                        val remainArray = containTextArray.take(containTextArray.size - (index + 1))
                        val remainText = remainArray.joinToString { it }.replace(",", "")
                        if (data.question.filterNot { it.isWhitespace() }.contains(remainText.filterNot { it.isWhitespace() }, true) && remainText.isNotEmpty() && remainArray.size > 2) {
                            mSearchList.add(data)
                            mSearchList.distinctBy { it.question }
                            return@mainloop
                        }
                    }
                } else {
                    isDirectError = false
                    if (data.question.contains(text, true)) {
                        mSearchList.add(data)
                    }
                }
            }
        }
        if(isDirectError && mSearchList.isEmpty()) mSearchList.addAll(allList.filter { it.question.contains(text) })
    }

    private fun initExtra() {
        if (intent?.hasExtra(Constants.ERROR_MSG) == true) {
            isDirectError = true
            errorMsg = intent?.getStringExtra(Constants.ERROR_MSG) ?: ""
        }
    }

    private fun updateUi(data: Resource<GenericReqAndResp<List<FaqModel>>>?) {

        data?.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    it.data?.data?.let {
                        allList.addAll(it)
                        setUpAdapter(allList)
                        if (errorMsg.isNotEmpty()) binding.etSearch.setText(errorMsg)
                    }
                }
                Resource.Status.LOADING -> {
                    showLoading()
                }
                Resource.Status.ERROR -> {
                    hideLoading()

                    showErrorDialogWithFAQLink(this, it.error.toString(),"FAQ")
                }
            }
        }
    }

    private fun setUpAdapter(list: MutableList<FaqModel>) {

        if (list.isNotEmpty()) {
            binding.rvFaq.visible()
            binding.tvNoData.gone()
        } else {
            binding.rvFaq.gone()
            binding.tvNoData.visible()
            if (isDirectError) binding.tvNoData.text = getString(R.string.no_data_found_in_faq)
        }
        if (!isExactValue && list.isNotEmpty())binding.tvRelevantMsg.visible() else binding.tvRelevantMsg.gone()

        binding.rvFaq.setUpAdapter(
            list,
            R.layout.item_faq,
            ItemFaqBinding::inflate,
            { it, pos, binding ->
                binding.tvQue.text = it.question
                binding.tvAns.text = it.answer

                binding.llContainer.setOnClickListener {
                    if (binding.tvAns.visibility == View.VISIBLE) {
                        binding.tvAns.gone()
                        binding.tvQue.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_arrow_down_black_24dp,
                            0
                        )
                    } else {
                        binding.tvAns.visible()
                        binding.tvQue.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.ic_arrow_up,
                            0
                        )
                    }
                }
            }
        )
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            }
            searchView.queryHint = getString(com.olam.warehouse.presentation.R.string.search_by_lot)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                            setUpAdapter(allList)
                        } else {
                            mSearchList.clear()
                            allList.forEach { data ->
                                newText?.let { text ->
                                    if (data.question.contains(text)) {
                                        mSearchList.add(data)
                                    }
                                }
                            }
                            setUpAdapter(mSearchList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
        return super.onCreateOptionsMenu(menu)
    }

}
