package mx.mariner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class Help extends Activity {

    private WebView mWebView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.webview);
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setSupportZoom(true);
        mWebView.loadUrl("file:///android_asset/help.html");
    }
    
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MapActivity.class));
        finish();
        return;
    }
}
