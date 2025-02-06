package com.olam.warehouse.vegax.notificationconfig.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import com.olam.warehouse.login.utils.showErrorDialogWithFAQLink
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.data.domain.model.GenericReqAndResp
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.putArgs
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.vegax.notificationconfig.R
import com.olam.warehouse.vegax.notificationconfig.data.domain.model.VegaNotifyConfigUserDetails
import com.olam.warehouse.vegax.notificationconfig.databinding.FragmentVegaNotifyConfigUserListBinding
import com.olam.warehouse.vegax.notificationconfig.databinding.ItemUserListBinding
import com.olam.warehouse.vegax.notificationconfig.ui.callback.VegaNotificationConfigCallbackListener
import com.olam.warehouse.vegax.notificationconfig.utils.MODULE_LIST_FRAGMENT
import com.olam.warehouse.vegax.notificationconfig.utils.getColor
import com.olam.warehouse.vegax.notificationconfig.vm.VegaNotificationConfigViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class VegaNotificationConfigUserListFragment : BaseFragment() {

    override val layoutResourceId = R.layout.fragment_vega_notify_config_user_list
    private lateinit var binding: FragmentVegaNotifyConfigUserListBinding
    private val vm: VegaNotificationConfigViewModel by viewModel()
    private var callBack: VegaNotificationConfigCallbackListener? = null
    private var userList = arrayListOf<VegaNotifyConfigUserDetails>()
    private var searchUserList = arrayListOf<VegaNotifyConfigUserDetails>()

    companion object {
        fun newInstance() = VegaNotificationConfigUserListFragment().putArgs {
//            putBundle(BUNDLE_DATA, bundle)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as? VegaNotificationConfigCallbackListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentVegaNotifyConfigUserListBinding.inflate(layoutInflater)
        initUI()
        return binding.root
    }

    private fun initUI() {
        observer()
        binding.tvTitle.setOnClickListener { callBack?.replaceFragment(MODULE_LIST_FRAGMENT, "") }
    }

    private fun observer() {
        vm.getUserList()
        vm.userList.observe(viewLifecycleOwner, Observer { updateUserList(it) })
    }

    private fun updateUserList(response: Resource<GenericReqAndResp<List<VegaNotifyConfigUserDetails>>>) {
        response.let {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    response.data?.data?.toMutableList()?.let { it1 ->
                        userList = it1 as ArrayList<VegaNotifyConfigUserDetails>
                        if(userList.isNotEmpty()) {
                            setAdapter(userList)
                        } else {
                          enableEmptyMsg()
                        }
                    }
                }

                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                    enableEmptyMsg()
                    showErrorDialogWithFAQLink(requireContext(), it.error.toString())
                }
            }
        }
    }

    private fun enableEmptyMsg(){
        binding.tvEmpty.visible()
        binding.rvUserList.gone()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(com.olam.warehouse.presentation.R.menu.search_menu, menu)
        try {
            val search = menu.findItem(com.olam.warehouse.presentation.R.id.search)
            val searchView: SearchView =
                search?.actionView as SearchView
            searchView.setBackgroundColor(getColor(com.olam.warehouse.presentation.R.color.colorPrimaryOfi))
            searchView.queryHint = "Search by user"
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText.let {
                        if (newText?.isEmpty() == true) {
                           setAdapter(userList)
                        } else {
                            searchUserList.clear()
                            if(userList.isNotEmpty()){
                                for(item in userList){
                                    if(newText?.let { it1 -> item.userName.contains(it1) || item.id.contains(it1) } == true){
                                        searchUserList.add(item)
                                    }
                                }
                            }
                            setAdapter(searchUserList)
                        }
                    }
                    return true
                }
            })
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun setAdapter(list: MutableList<VegaNotifyConfigUserDetails>) {
        binding.rvUserList.setUpAdapter(
            list,
            R.layout.item_user_list,
            ItemUserListBinding::inflate,
            { it, pos, bindingItem ->
                val item = list.get(pos)
                bindingItem.tvUserName.setText(item.userName)
                bindingItem.tvUserId.setText(item.id)

            }, itemClick = {
                callBack?.replaceFragment(MODULE_LIST_FRAGMENT, this)
            })
    }

}
