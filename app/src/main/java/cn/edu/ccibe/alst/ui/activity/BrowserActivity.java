package cn.edu.ccibe.alst.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.ccibe.alst.databinding.ActivityBrowserBinding;
import cn.edu.ccibe.alst.net.NetHeaderUtil;
import cn.edu.ccibe.alst.ui.base.BaseActivity;
import cn.edu.ccibe.alst.ui.utils.DisplayUtil;
import cn.edu.ccibe.alst.ui.view.SimpleDialogView;

public class BrowserActivity extends BaseActivity {
    private ActivityBrowserBinding binding;
    private ValueCallback<Uri[]> updateMessage;

    private static final int REQUEST_CODE_FILE_CHOOSER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
        loadUrl();
    }

    private void initView() {
        WebSettings webSettings = binding.browserWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        //webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setUserAgentString(NetHeaderUtil.UserAgents.PhoneWeChat_8_0_11);

        binding.browserWebView.setBackground(new ColorDrawable(0));
        binding.browserWebView.setBackgroundColor(0);
        binding.browserWebView.getBackground().setAlpha(0);

        binding.browserWebView.setWebChromeClient(new GWebChromeClient());
        binding.browserWebView.setWebViewClient(new GWebViewClient());
        binding.browserWebView.setDownloadListener(new GDownloadListener());

        binding.browserWebView.setOnLongClickListener(v -> onHelper());
    }

    private void loadUrl() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        String url = bundle.getString("url");
        String cookie = bundle.getString("cookie");

        if (!cookie.contains("path")) cookie += ";path=/"; // 切记这里的 ";path=/"

        // 设置 Cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);

        binding.browserWebView.loadUrl(url);

        // 仅作为上传文件测试
        //binding.browserWebView.loadUrl("https://www.wjx.cn/jq/27265670.aspx");
    }

    // WebView辅助(弹层方式)
    private boolean onHelper() {
        GWebViewHelper webViewHelper = new GWebViewHelper(binding.browserWebView);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        String cookie = bundle.getString("cookie");

        if (webViewHelper.isImage()) {
            ImageView imageView = new ImageView(this);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(this, 160f), DisplayUtil.dip2px(this, 200f));
            imageView.setLayoutParams(layoutParams);

            LazyHeaders lazyHeaders = new LazyHeaders.Builder().addHeader("Cookie", cookie).build();
            GlideUrl glideUrl = new GlideUrl(webViewHelper.imageUrl(), lazyHeaders);
            Glide.with(imageView)
                    .load(glideUrl)
                    .centerCrop()
                    .into(imageView);

            SimpleDialogView simpleDialogView = new SimpleDialogView();
            simpleDialogView.setTitle("图片下载提示");
            simpleDialogView.setDialogContentView(imageView);
            simpleDialogView.setAgreeText("保存该图片");
            simpleDialogView.setAgreeOnClickListener(dialog -> {
                String url = webViewHelper.imageUrl();
                new Thread(() -> {
                    try {
                        webViewHelper.downloadImage(url, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
                        runOnUiThread(() -> showToast("已经保存至相册。"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> showToast("保存失败，错误信息：" + e.getMessage()));
                    }
                }).start();
                dialog.dismiss();
            });
            simpleDialogView.show(getSupportFragmentManager(), "ShowImageViewDialog");
        }
        return false;
    }

    // 文件上传选择
    private void showFileChooser() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), REQUEST_CODE_FILE_CHOOSER);
    }

    // 清除 WebView
    private void clearWebView() {
        binding.browserWebView.loadUrl("");
        binding.browserWebView.destroy();
        binding.browserWebView.clearHistory();
        binding.browserWebView.clearFormData();
        binding.browserWebView.setWebViewClient(null);
        binding.browserWebView.setWebChromeClient(null);
        binding.browserWebView.setDownloadListener(null);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        WebStorage.getInstance().deleteAllData();
    }

    @Override
    protected void onDestroy() {
        clearWebView();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        /*if (binding.browserWebView.canGoBack()) {
            binding.browserWebView.goBack();
            return;
        }*/
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // 处理文件上传
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    String dataString = intent.getDataString();
                    ClipData clipData = intent.getClipData();
                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }
                    if (dataString != null) results = new Uri[]{Uri.parse(dataString)};
                }
            }

            if (results == null) showToast("未选择文件");
            // 清理文件上传消息
            updateMessage.onReceiveValue(results);
            updateMessage = null;
        }
    }

    /**
     * 辅助接口
     */
    private class GWebViewHelper {
        private WebView webView;

        public GWebViewHelper(WebView webView) {
            this.webView = webView;
        }

        public boolean isImage() {
            return webView.getHitTestResult().getType() == WebView.HitTestResult.IMAGE_TYPE;
        }

        public String imageUrl() {
            return webView.getHitTestResult().getExtra();
        }

        public void downloadImage(String url, File file) throws IOException {
            this.downloadImage(url, file.getAbsolutePath());
        }

        public void downloadImage(String url, String path) throws IOException {
            File file = new File(path, "img_" + System.currentTimeMillis() + ".jpg");

            // 网络请求
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestProperty("Cookie", CookieManager.getInstance().getCookie(url));
            BufferedInputStream buffIn = new BufferedInputStream(connection.getInputStream());
            BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(file));

            // 文件流写入
            byte[] bytes = new byte[1024 * 5];
            int len;
            while ((len = buffIn.read(bytes)) != -1) {
                buffOut.write(bytes, 0, len);
            }
            buffIn.close();
            buffOut.close();
        }

    }

    private class GWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            SimpleDialogView simpleDialogView = new SimpleDialogView();
            simpleDialogView.setCancelable(false);
            //simpleDialogView.setTitle(view.getTitle());
            simpleDialogView.setTitle("提示");
            simpleDialogView.setContentText(message);
            simpleDialogView.setAgreeText("确定");
            simpleDialogView.setAgreeOnClickListener(dialog -> {
                result.confirm();
                dialog.dismiss();
            });
            simpleDialogView.show(getSupportFragmentManager(), "WebViewAlertDialog");
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            SimpleDialogView simpleDialogView = new SimpleDialogView();
            simpleDialogView.setCancelable(false);
            //simpleDialogView.setTitle(view.getTitle()); // 网页标题
            simpleDialogView.setTitle("提示");
            simpleDialogView.setContentText(message);
            simpleDialogView.setAgreeText("确定");
            simpleDialogView.setCancelText("取消");
            simpleDialogView.setAgreeOnClickListener(dialog -> {
                result.confirm();
                dialog.dismiss();
            });
            simpleDialogView.setCancelCOnClickListener(dialog -> dialog.dismiss());
            simpleDialogView.show(getSupportFragmentManager(), "WebViewConfirmDialog");
            return true;
        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            updateMessage = filePathCallback;
            showFileChooser();
            return true;
        }
    }

    private class GWebViewClient extends WebViewClient {
        //隐藏公告中的返回按钮。
        String hideBackTable = "var tables = document.getElementsByTagName('table'); tables[tables.length-1].style.display = 'none';";

        //隐藏其他页中的，回首页按钮
        String hideHomeTr = "document.getElementsByName('_ctl0')[0].parentElement.parentElement.style.display = 'none';";

        @Override
        public void onPageFinished(WebView view, String url) {
            //隐藏公告
            if (url.contains("cxyxs.aspx")) {
                view.evaluateJavascript("javascript:" + hideBackTable, value -> {
                    Log.d("GLogWebView", "onPageFinished: hideBackTable - " + value);
                });
            } else {
                //隐藏回首页按钮
                view.evaluateJavascript("javascript:" + hideHomeTr, value -> {
                    Log.d("GLogWebView", "onPageFinished: hideHomeTr - " + value);
                });
            }
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("first.aspx")) {
                onBackPressed();
                return true;
            }
            view.loadUrl(url);
            return true;
        }
    }

    private class GDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            SimpleDialogView simpleDialogView = new SimpleDialogView();
            simpleDialogView
                    .setTitle("温馨提示")
                    .setContentText("未着重处理下载逻辑，可能会出现下载失败的情况。")
                    .setAgreeText("我已了解")
                    .setAgreeOnClickListener((dialog) -> {
                        dialog.dismiss();
                        //跳转浏览器，进行下载
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    });
            simpleDialogView.show(getSupportFragmentManager(), "DownloadDialog");
        }
    }

}