package com.olam.warehouse.vegax.reports.ui

import android.os.Bundle
import android.util.Log
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import com.google.gson.JsonObject
import com.olam.warehouse.login.ui.home.HomeBaseActivity
import com.olam.warehouse.presentation.data.domain.model.Auth
import com.olam.warehouse.presentation.data.remote.Resource
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ACCESS_TOKEN
import com.olam.warehouse.presentation.utils.PreferenceHelper
import com.olam.warehouse.presentation.utils.extension.toast
import com.olam.warehouse.vegax.reports.R
import com.olam.warehouse.vegax.reports.data.domain.IdentityModel
import com.olam.warehouse.vegax.reports.data.domain.ReportDataSetModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenModel
import com.olam.warehouse.vegax.reports.data.domain.VegaCommonReportDataSetTokenRequest
import com.olam.warehouse.vegax.reports.databinding.ActivityReportsBinding
import com.olam.warehouse.vegax.reports.di.injectCommonReportFeature
import com.olam.warehouse.vegax.reports.utils.reportId
import com.olam.warehouse.vegax.reports.vm.VegaCommonReportViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReportsActivity : HomeBaseActivity() {


    private lateinit var webView: WebView
    override val layoutResourceId: Int = R.layout.activity_reports
    private lateinit var binding:ActivityReportsBinding
    private var embedUrl: String? = ""
    private val vm: VegaCommonReportViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigationView()
        injectCommonReportFeature()
        initUi(savedInstanceState)
    }

    fun initUi(savedInstanceState: Bundle?) {
        webView = findViewById(R.id.web_view)
        //loadWebView(savedInstanceState)
        vm.auth.observe(this, Observer { updateToken(it) })
        vm.dataSet.observe(this, Observer { updateDataSetUrl(it) })
        vm.dataSetToken.observe(this, Observer { updateDataSetToken(it) })
        vm.getADToken()
    }

    private fun updateDataSetToken(data: Resource<VegaCommonReportDataSetTokenModel>?) {
        data?.let { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val tokenId = response.data?.tokenId
                    val token = response.data?.token
                    //tokenId?.let { toast(it) }
                    loadPowerBiDataInWebView(token)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun updateDataSetUrl(data: Resource<VegaCommonReportDataSetModel>?) {
        data?.let { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    val datasetId = response.data?.datasetId
                    val targetWorkspacesId = response.data?.datasetWorkspaceId
                    embedUrl = response.data?.embedUrl
                   // datasetId?.let { toast(it) }
                    val model = VegaCommonReportDataSetTokenRequest()
                    val reports:ArrayList<ReportDataSetModel> = arrayListOf()
                    var dataModel = ReportDataSetModel()
                    dataModel.id = reportId
                    reports.add(dataModel)

                    val targetWorkspaces:ArrayList<ReportDataSetModel> = arrayListOf()
                    dataModel = ReportDataSetModel()
                    dataModel.id = targetWorkspacesId
                    targetWorkspaces.add(dataModel)

                    val datasets:ArrayList<ReportDataSetModel> = arrayListOf()
                    dataModel = ReportDataSetModel()
                    dataModel.id = datasetId
                    datasets.add(dataModel)

                    val identities:ArrayList<IdentityModel> = arrayListOf()
                    var identity = IdentityModel()
                    identity.datasets = arrayListOf(datasetId.toString())
                    identities.add(identity)

                    model.reports = reports
                    model.targetWorkspaces = targetWorkspaces
                    model.datasets = datasets
                    model.identities = identities
                    vm.getDataSetToken(model)
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun updateToken(data: Resource<Auth>?) {
        data?.let { response ->
            when (response.status) {
                Resource.Status.SUCCESS -> {
                    hideLoading()
                    PreferenceHelper.save(Constants.DO_BEARER_TOKEN, "")
                    val token = response.data?.access_token
                    //token?.let { toast(it) }
                    token?.let { PreferenceHelper.save(Constants.DO_BEARER_TOKEN, it) }
                    token?.let { vm.getDataSet(it) }
                }
                Resource.Status.LOADING -> showLoading()
                Resource.Status.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    fun loadWebView(savedInstanceState : Bundle?)
    {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.allowFileAccess = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.supportMultipleWindows()
        settings.userAgentString = "userAgent"

        webView.webViewClient = mWebClient
        webView.webChromeClient = mWebChromeClient
        if (savedInstanceState == null) {
            webView.loadUrl("https://vegax-uat.ofi.com/#/insight?token="+ PreferenceHelper.get(ACCESS_TOKEN, ""))
        }
        Log.d("SAMPLETOKEN", "loadWebView: "+PreferenceHelper.get(ACCESS_TOKEN, ""))
    }

    private val mWebChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            this@ReportsActivity.setProgress(newProgress * (Window.PROGRESS_END / 100))
        }
    }

    private val mWebClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return false
        }
    }

    private fun loadPowerBiDataInWebView(token: String?) {

        var jsonObject = JsonObject()
        jsonObject.addProperty("type", "report")
        jsonObject.addProperty("tokenType", 1)
        jsonObject.addProperty("accessToken", token)
        jsonObject.addProperty("embedUrl", embedUrl)
        jsonObject.addProperty("id", reportId)
        jsonObject.addProperty("permissions", 7)
        var jsonObject1 = JsonObject()
        jsonObject1.addProperty("filterPaneEnabled", false)
        jsonObject1.addProperty("navContentPaneEnabled", false)
        jsonObject1.addProperty("layoutType", 2)
        jsonObject.add("settings", jsonObject1)
        val configuration = jsonObject

        val webData = "<html><head><meta http-equiv=\"content-type\" content=\"text/html;  charset=\"utf-8\" />" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"+
                "<script src=\"https://cdn.jsdelivr.net/npm/powerbi-client@2.8.0/dist/powerbi.min.js\"></script>"+
                "<style>" +
                "html, " +
                "body, " +
                "reportContainer {" +
                "                width: 100%;" +
                "                height: 100%;" +
                "                margin: 0;" +
                "                background-color: 'white';" +
               // "                -webkit-overflow-scrolling: touch;" +
                "            }" +
                "iframe {" +
                "                border: 0px" +
                "            }" +
                "</style></head>" +
                "<body>" +
                "<div id=\"reportContainer\"></div>" +
                "        <script>" +
                "        var models = window['powerbi-client'].models;" +
                "        var config = ${configuration};" +
                "        var reportContainer = document.getElementById('reportContainer');" +
                "        var report = powerbi.embed(reportContainer, config);" +
                "        </script>" +
                "</body></html>"

        val settings = webView.settings
        settings.javaScriptEnabled = true
//        settings.allowFileAccess = true
//        settings.domStorageEnabled = true
//        settings.javaScriptCanOpenWindowsAutomatically = true
//        settings.supportMultipleWindows()
//        settings.userAgentString = "userAgent"

        webView.webViewClient = mWebClient
        webView.webChromeClient = mWebChromeClient
        webView.loadDataWithBaseURL(null, webData, "text/html", "UTF-8", null)



    }

}
