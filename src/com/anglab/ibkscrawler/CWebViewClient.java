package com.anglab.ibkscrawler;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CWebViewClient extends WebViewClient{
	Context ctx;
	MainActivity vMainActivity;
	public CWebViewClient(Context ctx) {
		this.ctx = ctx;
		this.vMainActivity = (MainActivity) ctx;
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("webview", "onPageStarted");
        //CDialog.showLoading(ctx);
        CDialog.showProgress(ctx);
		super.onPageStarted(view, url, favicon);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
        Log.d("webview", "onPageFinished");
		super.onPageFinished(view, url);
        //CDialog.hideLoading();
        CDialog.hideProgress();
        vMainActivity.fn_updLstViewNo(); // 최근 본 화 저장
	}

	@Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("webview", "shouldOverrideUrlLoading");
        view.loadUrl(url);
        return true;
    }

	@Override
	public void onReceivedError(WebView view, int errorCode,
			String description, String failingUrl) {
        Log.d("webview", "onReceivedError");
		super.onReceivedError(view, errorCode, description, failingUrl);
	}
}
