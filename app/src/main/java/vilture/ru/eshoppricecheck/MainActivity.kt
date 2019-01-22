package vilture.ru.eshoppricecheck

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val web = webView
    var url = "https://eshop-prices.com/"

    // выполняем переключения валют
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_local -> {
                url = "https://eshop-prices.com/"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rub -> {
                url = "https://eshop-prices.com/prices?currency=RUB"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_usd -> {
                url = "https://eshop-prices.com/prices?currency=USD"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_eur -> {
                url = "https://eshop-prices.com/prices?currency=EUR"
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // определяем строку прогресса
        progressBar.progress = 0

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        //bottomNavigation.setSelectedItemId(R.id.navigation_local)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // определяем webview
        // загружаем страничку с ценами
        web.webViewClient = MyWebViewClient()

        web.loadUrl(url)

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
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

