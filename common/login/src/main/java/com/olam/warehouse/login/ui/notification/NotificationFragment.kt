package com.olam.warehouse.login.ui.notification

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.olam.warehouse.login.R
import com.olam.warehouse.login.databinding.FragmentNotificationBinding
import com.olam.warehouse.login.databinding.ItemNotificationBinding
import com.olam.warehouse.login.ui.notification.navigation.WorkFlowNavigation
import com.olam.warehouse.master.common.model.MessageModel
import com.olam.warehouse.master.common.model.NotificationModel
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.common.utils.getCurrentUserName
import com.olam.warehouse.master.common.utils.getPlantDetails
import com.olam.warehouse.presentation.adapter.setUpAdapter
import com.olam.warehouse.presentation.ui.BaseFragment
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.GsonUtils
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.gone
import com.olam.warehouse.presentation.utils.extension.visible
import com.olam.warehouse.presentation.utils.fromJson

/**
 * Created by Baskaran Kannan on 1/27/2022.
 */
class NotificationFragment : BaseFragment() {
    override val layoutResourceId = R.layout.fragment_notification
    private lateinit var binding: FragmentNotificationBinding
    val TAG = "NotificationFragment"
    private var notifiList = arrayListOf<NotificationModel>()
    val gson = GsonUtils()
    private var isReadTab = false
    private var isUnRead = false
    private var callBack: CallBack? = null

    interface CallBack {
        fun updateTabStatus(isReadTab: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBack = context as CallBack
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        notifiList.clear()
        val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
        if (oldNotifiList.isNotEmpty()) {
            val item = Gson().fromJson<List<NotificationModel>>(oldNotifiList)
            item.forEach {
                val bodyData = Gson().fromJson<MessageModel>(it.notification)
                val splitItem = bodyData.flag?.split(",")
                when {
                    splitItem?.any { it.equals(getCurrentKey(), true)} == true && getCurrentKey().isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(getPlantDetails().plantId, true)} == true && getPlantDetails().plantId.isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(getCurrentUserName(), true)} == true && getCurrentUserName().isNotEmpty() -> notifiList.add(
                        it
                    )
                    splitItem?.any { it.equals(Constants.ALL, true)} == true -> notifiList.add(
                        it
                    )
                }
            }
        }
        moveToUnReadNotification()
        binding.tvUnRead.setOnClickListener { moveToUnReadNotification() }
        binding.tvRead.setOnClickListener { moveToReadNotification() }
        //setupAdapter(notifiList)

    }

    private fun moveToUnReadNotification(){
        isReadTab = false
        callBack?.updateTabStatus(isReadTab)
        binding.tvUnRead.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvUnRead.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        binding.tvRead.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvRead.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        setupAdapter(notifiList.filter { !it.isViewed })
    }

    private fun moveToReadNotification(){
        isReadTab = true
        callBack?.updateTabStatus(isReadTab)
        binding.tvUnRead.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvUnRead.context,
                com.olam.warehouse.presentation.R.color.grey_light
            )
        )
        binding.tvRead.setBackgroundColor(
            ContextCompat.getColor(
                binding.tvRead.context,
                com.olam.warehouse.presentation.R.color.colorPrimaryOfi1
            )
        )
        setupAdapter(notifiList.filter { it.isViewed })
    }

    private fun setupAdapter(notifiList: List<NotificationModel>) {
        /*val isReadUnReadMsg = notifiList.any { it.isViewed } && notifiList.any { !it.isViewed }
        val unReadCount = notifiList.filter { !it.isViewed }.size
        val readCount = notifiList.filter { it.isViewed }.size*/
        if(notifiList.isNotEmpty()) {
            binding.tvNoData.gone()
            binding.rvNotification.visible()
            binding.rvNotification.setUpAdapter(
                notifiList.asReversed().toMutableList(),
                R.layout.item_notification,
                ItemNotificationBinding::inflate,
                { it, pos, bindingItem ->
                    val bodyData = Gson().fromJson<MessageModel>(it.notification)
                    bindingItem.tvNotificationTitle.text = bodyData.title
                    bindingItem.tvNotificationTitle.gone()
                    bindingItem.tvNotificationDes.text = bodyData.message
                    bindingItem.tvNotificationDate.text = it.Date
                    /* if (it.isViewed && !notifiList.get(pos).isRead && isReadUnReadMsg) {
                    notifiList.forEach { it.isRead = true }
                    bindingItem.tvRead.visible()
                    bindingItem.tvRead.text =
                        if (readCount == 1) context.getString(R.string.got_read_msg) else readCount.toString()
                            .plus(" ").plus(getString(R.string.got_read_messages))
                }
                if (!it.isViewed && !notifiList.get(pos).isUnRead && isReadUnReadMsg) {
                    notifiList.forEach { it.isUnRead = true }
                    bindingItem.tvUnRead.visible()
                    bindingItem.tvUnRead.text =
                        if (unReadCount == 1) context.getString(R.string.one_unread_msg) else unReadCount.toString()
                            .plus(" ").plus(getString(R.string.unread_messages))
                }*/
                    /*if(it.isViewed){
                    tvNotificationTitle.setTextColor(ContextCompat.getColor(tvNotificationTitle.context,com.olam.warehouse.presentation.R.color.grey))
                    tvNotificationDes.setTextColor(ContextCompat.getColor(tvNotificationTitle.context,com.olam.warehouse.presentation.R.color.grey_border))
                }*/
                },
                {
                    val bodyData = Gson().fromJson<MessageModel>(this.notification)
                    makeSingleMessageToReadState(bodyData.transactionId)
                    WorkFlowNavigation().navigationProcess(bodyData.navigationId, bodyData.transactionId, requireContext())
                })
        }else{
            binding.tvNoData.visible()
            binding.rvNotification.gone()
        }

    }

    override fun onResume() {
        super.onResume()
        registerNetworkListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        //context?.sendBroadcast(Intent(Constants.NOTIFICATION_RECEIVED))
        context?.sendBroadcast(
            Intent(Constants.NOTIFICATION_RECEIVED).apply {
                setPackage(context?.packageName)
            }
        )
        context?.unregisterReceiver(notificationReceiver)
    }

    /*Register broadcast receiver */
    private fun registerNetworkListener() {
        val intentFilterNotification = IntentFilter(Constants.NOTIFICATION_RECEIVED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context?.registerReceiver(notificationReceiver, intentFilterNotification, Activity.RECEIVER_EXPORTED)
        }else{
            context?.registerReceiver(notificationReceiver, intentFilterNotification)
        }

    }

    fun makeAllMessageToReadState() {
        val currentNotifiList = arrayListOf<NotificationModel>()
        val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
        if (oldNotifiList.isNotEmpty()) {
            val item = Gson().fromJson<List<NotificationModel>>(oldNotifiList)
            item.forEach {
                val bodyData = Gson().fromJson<MessageModel>(it.notification)
                val splitItem = bodyData.flag?.split(",")
                when {
                    splitItem?.any { it.equals(getCurrentKey(), true)} == true && getCurrentKey().isNotEmpty() -> it.isViewed =
                        true
                    splitItem?.any { it.equals(getPlantDetails().plantId, true)} == true && getPlantDetails().plantId.isNotEmpty() -> it.isViewed =
                        true
                    splitItem?.any { it.equals(getCurrentUserName(), true)} == true && getCurrentUserName().isNotEmpty() -> it.isViewed =
                        true
                    splitItem?.any { it.equals(Constants.ALL, true)} == true -> it.isViewed = true
                }
                currentNotifiList.add(it)
            }
           /* notifiList = currentNotifiList
            setupAdapter(notifiList.filter { !it.isViewed })*/
            PreferenceHelper.save(Constants.NOTIFICATION_LIST, gson.toJson(currentNotifiList))
            initUI()
        }
    }

    private fun makeSingleMessageToReadState(transactionId: String?) {
        val currentNotifiList = arrayListOf<NotificationModel>()
        val oldNotifiList = PreferenceHelper.get(Constants.NOTIFICATION_LIST, "")
        if (oldNotifiList.isNotEmpty()) {
            val item = Gson().fromJson<List<NotificationModel>>(oldNotifiList)
            item.forEach {
                val bodyData = Gson().fromJson<MessageModel>(it.notification)
                val splitItem = bodyData.flag?.split(",")
                if(bodyData.transactionId.equals(transactionId)){
                        when {
                            splitItem?.any { it.equals(getCurrentKey(), true)} == true && getCurrentKey().isNotEmpty() -> it.isViewed =
                                true
                            splitItem?.any { it.equals(getPlantDetails().plantId, true)} == true && getPlantDetails().plantId.isNotEmpty() -> it.isViewed =
                                true
                            splitItem?.any { it.equals(getCurrentUserName(), true)} == true && getCurrentUserName().isNotEmpty() -> it.isViewed =
                                true
                            splitItem?.any { it.equals(Constants.ALL, true)} == true -> it.isViewed = true
                        }
                }
                currentNotifiList.add(it)
            }
            /*notifiList = currentNotifiList
            setupAdapter(notifiList.filter { !it.isViewed })*/
            PreferenceHelper.save(Constants.NOTIFICATION_LIST, gson.toJson(currentNotifiList))
            initUI()
        }
    }

    fun clearNotification(){
        if(isReadTab){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notifiList.removeIf { it.isViewed  }
                setupAdapter(notifiList.filter { it.isViewed })
                PreferenceHelper.save(Constants.NOTIFICATION_LIST, gson.toJson(notifiList))
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notifiList.removeIf { !it.isViewed  }
                setupAdapter(notifiList.filter { !it.isViewed })
                PreferenceHelper.save(Constants.NOTIFICATION_LIST, gson.toJson(notifiList))
            }
        }
    }

    //Notification Listener
    val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            when (intent?.action) {
                Constants.NOTIFICATION_RECEIVED -> {
                    initUI()
                }
            }
        }
    }
}
