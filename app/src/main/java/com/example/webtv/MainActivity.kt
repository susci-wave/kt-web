package com.example.webtv


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream
import android.widget.Toast
import com.example.webtv.ui.theme.WebTVTheme

var webViewInstance: WebView? = null
var appContext:MainActivity ?= null;
var lastBackPress = 0L;
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        appContext = this;

        setContent {
            WebTVTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(sharedPreferences) { webView ->
                        webViewInstance = webView

                        toast("V 2.1");
                    }
                }
            }
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Toast.makeText(this, "press " + keyCode, Toast.LENGTH_SHORT).show();
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                webViewInstance?.evaluateJavascript("doAction('up');", null)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                webViewInstance?.evaluateJavascript("doAction('down');", null)
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                webViewInstance?.evaluateJavascript("doAction('center');", null)
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                webViewInstance?.evaluateJavascript("doAction('menu');", null)
                return true
            }
            KeyEvent.KEYCODE_O -> {
                webViewInstance?.evaluateJavascript("doAction('center');", null)
                return true
            }
            KeyEvent.KEYCODE_M -> {
                webViewInstance?.evaluateJavascript("doAction('menu');", null)
                return true
            }
            KeyEvent.KEYCODE_B -> {
                webViewInstance?.evaluateJavascript("doAction('back');", null)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onBackPressed() {
        if(System.currentTimeMillis() - lastBackPress > 1000){
            lastBackPress = System.currentTimeMillis()
            webViewInstance?.evaluateJavascript("doAction('back');", null)
        }else{
            finish()
        }
    }
    override fun onDestroy() {
        webViewInstance?.let {
            it.parent?.let {
                    parent -> (parent as android.view.ViewGroup).removeView(it)
            }
            it.destroy()
        }
        super.onDestroy()
    }
}

fun toast(msg: String){
    Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
}

@Composable
fun Greeting(sharedPreferences: SharedPreferences, modifier: Modifier = Modifier, onWebViewCreated: (WebView) -> Unit = {}) {
    val context = LocalContext.current
    AndroidView(
        factory = {
                ctx ->
            val webView = WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): android.webkit.WebResourceResponse? {
                        val url = request.url.toString()
                        //Log.d("WebViewRequests", "请求链接: ${url}")
                        if(url.indexOf("cctvh5-trace.min.js") > -1
                            || url.indexOf("vconsole.min.js") > -1
                            || url.indexOf("three.js") > -1
                            || url.indexOf("VR.js") > -1){
                            return WebResourceResponse(
                                "application/javascript",
                                "UTF-8",
                                ByteArrayInputStream("console.log('blocking ${url}')".toByteArray())
                            )
                        }
                        if(url.indexOf("/index.html") > -1){
                            try {
                                val inputStream = context.assets.open("index.html")
                                return WebResourceResponse("text/html", "UTF-8", inputStream)
                            } catch (e: Exception) {
                                Log.e("web-view", "错误: " + e.message);
                                e.printStackTrace()
                                return WebResourceResponse(
                                    "application/javascript",
                                    "UTF-8",
                                    ByteArrayInputStream("console.log('no found')".toByteArray())
                                )
                            }
                        }
                        if(url.indexOf("wasm/hls") > -1 && url.indexOf(".js") > -1 ){
                            try {
                                val inputStream = context.assets.open("hls.js")
                                return WebResourceResponse("application/javascript", "UTF-8", inputStream)
                            } catch (e: Exception) {
                                Log.e("web-view", "错误: " + e.message);
                                e.printStackTrace()
                                return WebResourceResponse(
                                    "application/javascript",
                                    "UTF-8",
                                    ByteArrayInputStream("console.log('no found')".toByteArray())
                                )
                            }
                        }
                        if(url.indexOf("sync-pid?pid=") > -1){
                            Log.d("WebViewRequests", "请求链接: ${url}")
                            var pid = url.substring(url.indexOf("sync-pid?pid=")+13);
                            val editor = sharedPreferences.edit()
                            editor.putString("pid", pid.toString())
                            editor.apply()
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                ByteArrayInputStream("console.log('no found')".toByteArray())
                            )
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onReceivedError(
                        view: WebView,
                        errorCode: Int,
                        description: String,
                        failingUrl: String
                    ) {
                        Log.e("my-log", "Error: $errorCode - $description - $failingUrl")
                    }
                }

                isFocusable = false
                isFocusableInTouchMode = false
                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false  // 允许自动播放
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
                }
                val pid = sharedPreferences.getString("pid", "600001859");
                toast("view " + pid);
                val host = getHost();
//                loadUrl(host + "/index.html?pid="+pid);
                loadUrl(host + "/tv/home?pid="+pid)
            }
            onWebViewCreated(webView)
            return@AndroidView webView
        },
        modifier = modifier
    )
}

fun getHost(): String {
    return "iuuqt;00xxx/zbohtijqjo/do".map { char ->
        (char.code - 1).toChar()
    }.joinToString("")
}