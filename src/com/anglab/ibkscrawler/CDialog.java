package com.anglab.ibkscrawler;

import com.anglab.ibkscrawler.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class CDialog extends Activity {

	private static Dialog m_loadingDialog = null;
	private static ProgressDialog progressDialog = null;

	public static void showLoading(Context context) {
		if ( m_loadingDialog == null ) {
			m_loadingDialog = new Dialog(context, R.style.TransDialog);
			ProgressBar pb = new ProgressBar(context);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			m_loadingDialog.addContentView(pb, params);
			m_loadingDialog.setCancelable(true);
			m_loadingDialog.setCanceledOnTouchOutside(true);
		}
		m_loadingDialog.show();
	}

	public static void hideLoading() {
		if ( m_loadingDialog != null ) {
			m_loadingDialog.dismiss();
			m_loadingDialog = null;
		}
	}

	public static void showProgress(Context context) { showProgress(context, "Loading"); }
	
	public static void showProgress(Context context, String pMsg) {
		if ( progressDialog == null ) {
			progressDialog = new ProgressDialog(context, R.style.NewDialog);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			//progressDialog.setMessage(Html.fromHtml("<FONT Color='White'>Data Loading...</FONT>"));
			progressDialog.setIcon(3);
		}

		if ( "Loading".equals(pMsg) ) { // Loading 이면 취소 가능
			progressDialog.setCancelable(true);
			progressDialog.setCanceledOnTouchOutside(true);
		} else {
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.setMessage(pMsg);
		progressDialog.show();
	}

	public static void hideProgress() {
		if ( progressDialog != null ) {
			progressDialog.dismiss(); //Handler 에서 프로그레스바 종료
			progressDialog = null;
		}
	}
}