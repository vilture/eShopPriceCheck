package vilture.ru.eshoppricecheck

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var url = "https://eshop-prices.com"

    // выполняем переключения валют
    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        val i = url.indexOf("?currency")
        val sb = StringBuilder(url)
        if (i > 0) {
            sb.delete(i, 100)
            url = sb.toString()
        }

        when (item.itemId) {
            R.id.navigation_local -> {
                webView.loadUrl("$url?currency=LOCAL")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rub -> {
                webView.loadUrl("$url?currency=RUB")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_usd -> {
                webView.loadUrl("$url?currency=USD")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_eur -> {
                webView.loadUrl("$url?currency=EUR")
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // определяем строку прогресса
        progressBar.progress = 0
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.selectedItemId = R.id.navigation_local
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // определяем webview
        // загружаем страничку с ценами
        webView.webViewClient = MyWebViewClient()
        webView.loadUrl("$url/prices?currency=LOCAL")

        // определяем параметр настроек webview
        // включаем javascript
        // включаем кэширование
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.setAppCachePath(cacheDir.path)
        settings.allowFileAccess = true
        settings.setAppCacheEnabled(true)

        // доступность сети
        if (!isNetworkAvailable())
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        else
            settings.cacheMode = WebSettings.LOAD_DEFAULT

    }

    private inner class MyWebViewClient : WebViewClient() {
        @TargetApi(Build.VERSION_CODES.N)

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            view.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

            view.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE

            // убираем заголовок сайта
            view.loadUrl(
                "javascript:(function() { " +
                        "document.getElementsByClassName('header')[0].style.display='none'; })()"
            )

            // убираем подвал сайта
            view.loadUrl(
                "javascript:(function() { " +
                        "document.getElementsByClassName('footer')[0].style.display='none'; })()"
            )

            // убираем рекламу
            view.loadUrl(
                "javascript:(function() { " +
                        "document.getElementsByClassName('adsbygoogle')[0].style.display='none'; })()"
            )

        }

        // обрабатываем все ссылки внутри приложения
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            url = request.url.toString()
            return true
        }
    }

    // при потери сети будем грузить сохраненный кеш предыдущих сессий
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    // кнопку назад отрабатываем правильно
    override fun onBackPressed() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        val mWebBackForwardList = webView.copyBackForwardList()
        var undoUrl = ""

        if (url.contains("https://eshop-prices.com/games/")) {
            val i = webView.url.indexOf("?currency")
            val sb = StringBuilder(webView.url)

            if (i > 0) {
                sb.delete(0, i)
                webView.loadUrl("https://eshop-prices.com/prices" + sb.toString())
            } else {
                sb.clear()
                webView.loadUrl("https://eshop-prices.com/prices?currency=LOCAL")
            }
            url = "https://eshop-prices.com/prices"
            return
        }

        if (webView.canGoBack()) {
            webView.goBack()

            if (mWebBackForwardList.currentIndex > 0)
                undoUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url

            if (undoUrl.contains("=LOCAL"))
                bottomNavigation.selectedItemId = R.id.navigation_local
            if (undoUrl.contains("=RUB"))
                bottomNavigation.selectedItemId = R.id.navigation_rub
            if (undoUrl.contains("=USD"))
                bottomNavigation.selectedItemId = R.id.navigation_usd
            if (undoUrl.contains("=EUR"))
                bottomNavigation.selectedItemId = R.id.navigation_eur
        } else {
            super.onBackPressed()
        }
    }
}

