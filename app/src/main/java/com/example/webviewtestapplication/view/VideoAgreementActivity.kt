package com.example.webviewtestapplication.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.webviewtestapplication.databinding.ActivityVideoAgreementBinding
import com.example.webviewtestapplication.model.AndroidGenericInterfaceJS
import com.example.webviewtestapplication.model.ModelVideoAgreement
import com.example.webviewtestapplication.model.TaskInService
import com.example.webviewtestapplication.viewmodel.VideoAgreementViewModel

/**
 * Class to display the web page of the video agreement.
 * @param modelVideoAgreement Credit data for video agreement
 * */
class VideoAgreementActivity(
    private val modelVideoAgreement: ModelVideoAgreement = ModelVideoAgreement(
        "25", "Juanito","Credifiel","3500","55%20Quincenas","Nomina"
    )
) : AppCompatActivity() {
    private val viewModel: VideoAgreementViewModel by viewModels()
    private lateinit var binding: ActivityVideoAgreementBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    /**
     * Permissions required for video agreement
     * */
    private var globalPermissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            viewModel.setNetworkState(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            viewModel.setNetworkState(false)
        }
    }

    //region Android Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        observers()
        initObjects()
    }

    /**
     * webView.clearCache(false)
     *  It is used to clear the cache of the WebView.
     *  The method has a boolean parameter set to false, indicate cache data in the persistent store,
     *  such as image files and resources, should also be cleared. Only cache RAM.
     * */
    override fun onPause() {
        super.onPause()
        binding.webView.clearCache(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearWebViewData()
        unregisterNetworCallback()
    }
    // endregion

    //region ON CREATE METHODS
    private fun initBinding() {
        binding = ActivityVideoAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initObjects() {
        definePermissionLauncher()
        validateConnection()
        showVideoAgreement()
        registerNetworkCallback()
    }

    private fun observers() {
        viewModel.url.observe(this) { url ->
            if (checkPermissions(false)) configureWebViewVideoCapture(url)
        }

        viewModel.networkState.observe(this) { state ->
            if (state) reloadWebView()
            else binding.viewNoConnection.visibility = View.VISIBLE
        }
    }

    /**
     * If use evaluateJavascript("javascript:window.location.reload(true)" refresh page and history is not affected
     * Add try catch if try update View and View not exist
     * */
    private fun reloadWebView() {
        binding.viewNoConnection.visibility = View.GONE
        binding.viewProgress.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                binding.viewProgress.visibility = View.GONE
                binding.webView.evaluateJavascript("javascript:window.location.reload(true)", null)
            } catch (exception: Exception) {
                println("No se pudo realizar la tarea definida al tener conexión")
            }
        }, 3000)
    }
    //endregion

    //region Settings WebView Component
    /**
     * Configure the WebView with the necessary settings for handling the video agreement.
     * Show a loading screen.
     * Enable JavaScript.
     * Multimedia content playback will not require user interaction to start playback.
     * Clear the cache completely.
     * Add a JS interface defined in the mobile app to communicate between the app and the web page.
     * Define a webChromeClient to request necessary permissions in the browser.
     * Use a webViewClient to hide the loading page when it is loaded.
     * Load url Web View
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebViewVideoCapture(url: String) {
        binding.viewProgress.visibility = View.VISIBLE
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            clearCache(true)
            addJavascriptInterface(getInterfaceService(), viewModel.nameInterface)
            webChromeClient = getWebChromeClientForVideoRecord()
            webViewClient = getWebClientForPageLoaded()
            loadUrl(url)
        }
    }

    /**
     * Method to update mobile flags or the status of the video agreement in the application.
     * */
    private fun getInterfaceService(): AndroidGenericInterfaceJS {
        return AndroidGenericInterfaceJS(object : TaskInService {
            override fun onSuccessService(vararg objects: Any) {
                println("AndroidGenericInterfaceJS Se guardo imagen desde interfaz")
            }

            override fun onFailService(vararg objects: Any) {
                runOnUiThread {
                    println("AndroidGenericInterfaceJS No se guardo imagen desde interfaz")
                }
            }
        })
    }

    /**
     * Override WebChromeClient.
     * Add permissions for audio and video.
     * There is a commented-out method to obtain information from alerts generated on the web page that can be useful for communication.
     */
    private fun getWebChromeClientForVideoRecord(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    val permissions = arrayOf(
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE
                    )
                    request.grant(permissions)
                }
            }

            /*override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                message?.let { message ->
                    println("AndroidGenericInterfaceJS message Alert: $message")
                    if (message.contentEquals("Vídeo Enviado"))
                        println("AndroidGenericInterfaceJS se guardo video acuerdo")
                }

                return super.onJsAlert(view, url, message, result)
            }*/
        }
    }

    /**
     * Detects if the page has already loaded to hide the loading view.
     */
    private fun getWebClientForPageLoaded(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding.viewProgress.visibility = View.GONE
            }
        }
    }

    /**
     * webView.clearCache(false)
     * Clear all WebView data
     * */
    private fun clearWebViewData() {
        binding.webView.apply {
            clearCache(true)
            clearFormData()
            clearHistory()
            clearSslPreferences()
            clearMatches()
            WebStorage.getInstance().deleteAllData()
        }
    }

    /**
     * Validate if you have the necessary permissions to capture the video;
     * if not, request them from the user.
     * */
    private fun showVideoAgreement() {
        if (!checkPermissions(verifyNoGranted = false)) checkPermissions(verifyNoGranted = true)
        else viewModel.loadUrlVideoAgreement(modelVideoAgreement)
    }
    //endregion

    /**
     * THIS IS ONLY TEMPORARY FOR THE TEST PROJECT. EXISTING PROJECT ELEMENTS WILL BE USED.
     * */
    //region Network Callback
    private fun validateConnection() {
        if (isNetworkAvailable(this)) {
            binding.viewNoConnection.visibility = View.GONE
        } else binding.viewNoConnection.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val network = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun unregisterNetworCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    //endregion

    //region Permissions
    /**
     * Add a listener to request permission validation if not already granted.
     * If any permission is not granted, show an alert for permission request.
     * If all permissions are granted, load the URL.
     * */
    private fun definePermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { status: Boolean ->
            if (status) {
                if (!checkPermissions(verifyNoGranted = false)) {
                    createDialog(
                        "Permisos necesarios", "No has otorgado todos los permisos",
                        "Solicitar de nuevo"
                    )
                } else viewModel.loadUrlVideoAgreement(modelVideoAgreement)
            }
        }
    }

    private fun checkPermissions(verifyNoGranted: Boolean): Boolean {
        var countPermissions = 0
        for (globalPermission in globalPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this, globalPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) countPermissions++
        }

        val permissions = arrayOfNulls<String>(countPermissions)
        countPermissions = 0

        for (globalPermission in globalPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this, globalPermission
                ) != PackageManager.PERMISSION_GRANTED
            ) permissions[countPermissions++] = globalPermission
        }

        if (countPermissions > 0) {
            if (verifyNoGranted) permissions.forEach { permission ->
                requestPermissionLauncher.launch(permission)
            }
            return false
        }

        return true
    }

    private fun createDialog(
        title: String, message: String, positiveButtonText: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(positiveButtonText) { _, _ -> checkPermissions(true) }
        builder.show()
    }
    //endregion
}