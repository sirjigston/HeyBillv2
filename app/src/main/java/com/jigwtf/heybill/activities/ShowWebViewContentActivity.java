package com.jigwtf.heybill.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.jigwtf.heybill.Config;
import com.jigwtf.heybill.R;

public class ShowWebViewContentActivity extends AppCompatActivity {
    private String contentTitle;
    private String contentCached;
    private String contentUrl;
    private String contentOrientation;
    private String contentActionBar;
    private WebView showContentWebView;
    public ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_show_webview_content);
        // Makes Progress bar Visible
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        // Prevent to turn off the screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Set to RTL if true
        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } else {
            Log.d("Log", "Working in Normal Mode, RTL Mode is Disabled");
        }

        //Get item url
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("contentActionBar")) {
                contentTitle = extras.getString("contentTitle");
                contentCached = extras.getString("contentCached");
                contentUrl = extras.getString("contentUrl");
                contentOrientation = extras.getString("contentOrientation");
                contentActionBar = extras.getString("contentActionBar");

            }else if (extras.containsKey("contentActionBar")) {
                contentTitle = extras.getString("contentTitle");
                contentCached = extras.getString("contentCached");
                contentUrl = extras.getString("contentUrl");
                contentOrientation = extras.getString("contentOrientation");
            }
        }

        //Set Activity Orientation
        if(contentOrientation.equals("1")) { // It does not matter
            //Not change
        }else if(contentOrientation.equals("2")) { // Portrait
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }else if(contentOrientation.equals("3")) {  // Landscape
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if(contentActionBar.equals("1")) {
            getSupportActionBar().show(); //show the title bar
        }


        showContentWebView = (WebView) findViewById(R.id.wv_show_video);
        showContentWebView.setWebViewClient(new MyWebViewClient());
        WebSettings settingWebView = showContentWebView.getSettings();
        settingWebView.setJavaScriptEnabled(true);
        settingWebView.setAllowFileAccess(true);
        settingWebView.setDomStorageEnabled(true);

        //HTML Cashe
        if(contentCached.equals("1"))
            enableHTML5AppCache();

        // -------------------- LOADER ------------------------
        pd = new ProgressDialog(ShowWebViewContentActivity.this);
        pd.setMessage(ShowWebViewContentActivity.this.getResources().getString(R.string.txt_loading));
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.show();

        showContentWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                setTitle(R.string.txt_loading);
                setProgress(progress * 100); //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if(progress == 100)
                    setTitle(contentTitle);
            }
        });
        showContentWebView.loadUrl(contentUrl);
    }

    //==========================================================================//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.common, menu);
        return true;
    }


    //==========================================================================//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            if (showContentWebView.canGoBack()) {
                showContentWebView.goBack();
            } else {
                showContentWebView.stopLoading();
                showContentWebView.loadUrl("");
                showContentWebView.reload();
                super.onBackPressed();
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    //==========================================================================//
    public void onBackPressed() {
        if (showContentWebView.canGoBack()) {
            showContentWebView.goBack();
        } else {
            showContentWebView.stopLoading();
            showContentWebView.loadUrl("");
            showContentWebView.reload();
            super.onBackPressed();
            finish();
        }
    }

    //==========================================================================//
    private void enableHTML5AppCache() {
        showContentWebView.getSettings().setDomStorageEnabled(true);
        showContentWebView.getSettings().setAppCachePath("/data/data/" + ShowWebViewContentActivity.this.getPackageName() + "/cache");
        showContentWebView.getSettings().setAppCacheEnabled(true);
        showContentWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    //==========================================================================//
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            if (!pd.isShowing()) {
                pd.show();
            }

            // Start intent for "tel:" links
            if (url != null && url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ShowWebViewContentActivity.this, R.string.txt_this_function_is_not_support_here, Toast.LENGTH_SHORT).show();
                }
                view.reload();
                return true;
            }

            // Start intent for "sms:" links
            if (url != null && url.startsWith("sms:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ShowWebViewContentActivity.this, R.string.txt_this_function_is_not_support_here, Toast.LENGTH_SHORT).show();
                }
                view.reload();
                return true;
            }

            // Start intent for "mailto:" links
            if (url != null && url.startsWith("mailto:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(ShowWebViewContentActivity.this, R.string.txt_this_function_is_not_support_here, Toast.LENGTH_SHORT).show();
                }
                view.reload();
                return true;
            }

            // Start intent for "https://www.instagram.com/YourUsername" links
            if (url != null && url.contains("instagram.com")) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.instagram.android");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
                view.reload();
                return true;
            }

            // Start intent for "https://www.t.me/YourUsername" links
            if (url != null && url.contains("t.me")) {
                Uri uri = Uri.parse(url);
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("org.telegram.messenger");
                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
                view.reload();
                return true;
            }

            if (url != null && url.startsWith("external:http")) {
                url = url.replace("external:", "");
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }

            if (url != null && url.startsWith("file:///android_asset/external:http")) {
                url = url.replace("file:///android_asset/external:", "");
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                view.loadUrl(url);
            }

            return true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            showContentWebView.loadUrl("file:///android_asset/error.html");
        }
    }
}
