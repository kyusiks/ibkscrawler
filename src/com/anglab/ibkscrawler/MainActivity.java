package com.anglab.ibkscrawler;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.android.vending.billing.IInAppBillingService;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.anglab.ibkscrawler.R;
import com.anglab.ibkscrawler.ListViewer.ListAdapterWithButton;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// 진한파랑 0057A3
// 하늘색 009BD9
// 주횡색 F0520E

// 메인테마 33BB77 (글씨, 아이콘)
// 가장검정 333333 (글씨)
// 흰색배경 FFFFFF
// 흰색1 F4F4F4 (연한회색배경)
// 흰색2 E4E4E4 (선색)

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnTouchListener,OnClickListener  {
	WebView mWebView;
	Spinner cbo_no;
    private NotesDbAdapter dbAdapter;
    static int gv_currentViewId = -1; // 현재 화면 저장

	List<HashMap<String, String>> list = new ArrayList<>();
	List<HashMap<String, String>> gv_backList = new ArrayList<>(); // 뒤로가기 버튼 저장

	HashMap<String, String> gv_setting   = new HashMap<String, String>(); // 프로그램세팅정보 VER세팅버전, URL프로그램 URL,VIEW_FIRST_YN첫회부터 볼것인가 최종회부터 볼것인가, THUMB_YN 썸네일을 표시할것인가
	HashMap<String, String> gv_imgViewer = new HashMap<String, String>(); // 사이트별 웹툰 조회 주소
	HashMap<String, String> gv_thumbComn = new HashMap<String, String>(); // 사이트별 썸네일 조회 주소
	HashMap<String, int[]>  gv_buttons   = new HashMap<String, int[]>();  // 터치시 버튼 색을 바꿍기위한값을 저장
	static HashMap<String, String> gv_setView = new HashMap<String, String>(); // 웹툰보는데 필요한 정보. CID,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX

	String vMode = "0"; // 0내구독목록, 1사이트목록에서의클릭, 2웹툰목록에서의 클릭, 3회차정보조회(2웹톤목록에서의클릭직후실행된다)
	String gv_nav = ""; // 내비게이션 제목 저장 (보통 웹툰 제목)
	String gv_isNewDate = ""; // 년월일시분초 가 저장되며 이 시간 이후의 게시물은 NEW

	String gv_svrUrlOrg = "http://kyusiks.dothome.co.kr/ibkscrawler/"; // 기본중에 기본.
	String gv_pgmName = "a.php";
	String gv_svrUrl = gv_svrUrlOrg; // 서버 주소. 후에 setting에서 어레이로 읽어오는걸로 대체.
	ArrayList<String> gv_svrList = new ArrayList<String>(); // 서버연동 프로그램 주소
	int gv_svrUrlArrayIndex = 0; // 서버연동 주소 어레이 선택 인덱스

	IInAppBillingService mService;
	IabHelper mHelper;
	String[] gv_sectionG ,gv_sectionV; // 섹션관련 어레이
	Context gv_this = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading);

        dbAdapter = new NotesDbAdapter(this);
        dbAdapter.open();

        /**********************************************/
        if ( !"느낌표붙이면개발서버로".equals("no") ) { //TODO 개발서버를 돌릴때 true
            //dbAdapter.fn_dbClear(); // 디비 초기화. ONLY TEST!!
        	gv_svrUrlOrg = "http://anglab.dothome.co.kr/toontaku/";
        	gv_pgmName = "c.php";

        	//gv_svrUrlOrg = "http://anglab.url.ph/toontaku/";
        	//gv_pgmName = "a.php";
        }
        /**********************************************/

        // 결제모듈 시작
        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApsZz3yn0tSyZqQedUXj8U8aHvP3UniO+ugJk/10tqcgfIiMH5WVkWOdTR+ZW0/40qfiC3iToZMyJkanFPrsI9LUA2AiWU23Q2Gqg0YvvVhPVoEROKb1KFfydN5xlWsc367EF42MFWZmCqRtv3I4mimaHQ1lp0rS18rknTluc8EcI2SCx71GN4pD5LyRMaxBxJXZUwlpURAmmGKFYFlrARuzowrVrrCktIZR5tynDbBkMhSRV/HPYS/mRk4zg3UEWW//c4AvanqSry6ZYgr0c5aG218TiDOpciaz7ML3VB/Rvl9g2Tn8lMvOjUm2MMqhfzIm8r7G434SIe7F9+Db/jwIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                	// 구매오류처리 ( 토스트하나 띄우고 결제팝업 종료시키면 되겠습니다 )
                }
                AlreadyPurchaseItems();    // AlreadyPurchaseItems(); 메서드는 구매목록을 초기화하는 메서드입니다. v3으로 넘어오면서 구매기록이 모두 남게 되는데 재구매 가능한 상품( 게임에서는 코인같은아이템은 ) 구매후 삭제해주어야 합니다.  이 메서드는 상품 구매전 혹은 후에 반드시 호출해야합니다. ( 재구매가 불가능한 1회성 아이템의경우 호출하면 안됩니다 )
            }
        });
		// 결제모듈 끝
	    (new initLoadings()).execute();
	}

	private List<HashMap<String, String>> getXmldata(String pMode, String pParam) {
		List<HashMap<String, String>> vList = new ArrayList<>();
		try {
			String vParam = "";
			String vLstUpdDh = "";
			Long vLstUpdDhLong = (long)0;
			InputStream in = null;

			if ( "-1".equals(pParam) // 초기 실행이여서 로컬 파일 읽는다.
			  && ( "1-1".equals(pMode) || "2".equals(pMode) || "3".equals(pMode) || "4".equals(pMode) ) ) {
				in = getAssets().open("xml_list_" + pMode + ".xml");
			} else {

				vParam = pParam;
				// 파라메터 세팅
				if ( "2".equals(pMode) || "4".equals(pMode) || "V0".equals(pMode) // 예네는 pParam이 안들어온다.
				  || "3".equals(pMode) || "V1".equals(pMode) ) { // pParam = ID_SEQ // pParam = 사이트
					vLstUpdDh = inqXmlParam(pMode, vParam);
					if ( "-1".equals(vLstUpdDh) ) return vList;
					if ( "V0".equals(pMode) ) vParam = fn_inqMyListToString(); 

				} else if ( "V2".equals(pMode) ) { // 웹툰추가
					vLstUpdDh = getSetting("FST_INS_DH");
					if ( "".equals(vLstUpdDh) ) return vList;

				} else if ( "UPD_MYLIST".equals(pMode) ) { // 내구독목록 / 내가보는웹툰의 업데이트 확인
					vParam = fn_inqMyListToString(); 
					if ( "".equals(vParam) ) return vList;

				} else {
					vLstUpdDh = vParam; // pParam은 보통 lst_upd_dh를 가진다.
				}

				try {
					if ( Long.valueOf(vLstUpdDh) < Long.valueOf("20990101000000") ) vLstUpdDhLong = Long.valueOf(vLstUpdDh);
				} catch(Exception e) {
				} finally { }

				if ( aUtil.chkTimer("getXmldata/" + pMode + "/" + vParam, 10 * 60) ) { // ks20141130 10분 내에 호출한적있으면 리턴
					if ( "6".equals(pMode) ) { // 6번 인기웹툰은 매번 트래픽 일어나라. DB에 저장을 안해서니라.
					} else {
						return vList; // 매번의 트래픽 있는것을 회피하기 위함이니라.
					}
				}

				String vProgram = gv_pgmName;
				if ( "1".equals(pMode) || "1-1".equals(pMode) // 1웹툰리스트update 1-1웹툰리스트insert  -- 이제 안쓴다
				  || "4".equals(pMode) || "6".equals(pMode) || "2".equals(pMode)  // 4세팅 6추천웹툰 2사이트리스트
				  || "3".equals(pMode)  // 3카툰회차정보 웹툰회차 조회시는 선작업. 로컬에 저장된 버전을 전송하여 이후 리스트만 인서트한다. 트래픽 감소 유도
				  || "V0".equals(pMode) // V0내가보는웹툰만업데이트 LST_UPD_DH|ID_SEQ/ID_SEQ/ID_SEQ/...
				  || "V1".equals(pMode) // V1사이트별업데이트툰 LST_UPD_DH|SITE
				  || "V2".equals(pMode) ) {
				} else if ( "UPD".equals(pMode) || "UPD_MYLIST".equals(pMode) // UPD cid업데이트확인  / 내가보는웹툰 업데이트 
						 || "UPD_ALL".equals(pMode) ) { // 전체 업데이트
					vProgram = "updateBot.php";
				} else if ( "SITE_SEARCHER".equals(pMode) ) { // 신규웹툰탐색 ks20150119
					vProgram = "siteSearcher.php";
				} else { // err처리
					return vList;
				}
				URL url = new URL(gv_svrUrl + vProgram + "?pMode=" + pMode + "&pMyNm="+ getSetting("MY_NM") + "&pLstUpdDh=" + vLstUpdDh + "&pParam=" + vParam);
				Log.d("url", "url : " +url);
				in = url.openStream();
			}

			XmlPullParserFactory fatorry = XmlPullParserFactory.newInstance();
			fatorry.setNamespaceAware(true);
			XmlPullParser xpp = fatorry.newPullParser();
			xpp.setInput(in, "utf-8"); // xpp.setInput(in, "euc-kr");

			int eventType = xpp.getEventType();
			String tagname = "";
			String vFirstTagName = ""; // 최초 태그네임을 저장한다. list의 원활한 저장을 위해.
			HashMap<String, String> data = new HashMap<String, String>();

			while ( eventType != XmlPullParser.END_DOCUMENT ) {
				if ( eventType == XmlPullParser.START_TAG ) {
					tagname = xpp.getName();
				} else if ( eventType == XmlPullParser.TEXT ) { // 태그별로 저장
					/**** LMultiData 형식으로 저장하기위한 꼼수. 크게 신경쓸 필요 없다. ****/
					if ( vFirstTagName.equals("") ) vFirstTagName = tagname;
					if ( vFirstTagName.equals(tagname) ) {
						if ( !data.isEmpty() ) {
							if ( !data.isEmpty() && "3".equals(pMode) && !data.containsKey("ID_SEQ") ) data.put("ID_SEQ", vParam);
							vList.add(new HashMap<String, String>(data));
							data.clear();
						}
					}
					/********/
					if ( "F".equals(tagname) && !"-1".equals(pParam) ) { // F:LST_UPD_DH 인 경우 숫자가 압축되어있다. 압축 풀어서 입력 ks20140416
						data.put(aUtil.sectionFind(tagname), Long.valueOf(xpp.getText()) + vLstUpdDhLong + ""); // 이게 중요한거다.
					} else {
						data.put(aUtil.sectionFind(tagname), xpp.getText()); // 이게 중요한거다.
					}
				} else if ( eventType == XmlPullParser.END_TAG ) {
				}
				eventType = xpp.next();
			}
			if ( !data.isEmpty() && "3".equals(pMode) && !data.containsKey("ID_SEQ") ) data.put("ID_SEQ", vParam);
			if ( !data.isEmpty() ) vList.add(new HashMap<String, String>(data));
//Log.d("list", vList.toString());
			if ( "1".equals(pMode) || "1-1".equals(pMode)   // 1웹툰리스트 update / 1-1 insert
			  || "2".equals(pMode)                          // 2사이트 리스트  
			  || "3".equals(pMode) || "4".equals(pMode)     // 3카툰회차정보 / 4설정
			  || "V0".equals(pMode) || "V1".equals(pMode) 	 // 내구독목록만 업데이트 // 사이트별 업데이트
			  || "V2".equals(pMode) ) { // 신규웹툰업데이트
				dbAdapter.updList(pMode, vList);    // 로컬DB에 업데이트할게있으면 업뎃
			} else if ( "UPD".equals(pMode) ) { // 업데이트(특정웹툰)
				//if ( vList.size() < 0 && vList.get(0).containsKey("UPD") ) vRtn = (String) vList.get(0).get("UPD"); // 업데이트건이 있으면 Y. 아니면N
			}
		} catch (Exception e) {
			e.printStackTrace();
			gv_svrUrlArrayIndex++;
			if ( gv_svrUrlArrayIndex < gv_svrList.size() ) { // 한바퀴 다돌아서 에러면 끝
				gv_svrUrl = gv_svrList.get(gv_svrUrlArrayIndex); // 서버연동 프로그램 주소
				aUtil.delXmlTimer();
				return getXmldata(pMode, pParam);
			}
		}
		return vList;
	}

	// 웹툰을 보기위해 필요한 6가지 정보를 세팅한다.
	// CID,ID_SEQ,SITE,LST_VIEW_NO,MAX_NO,CBO_INDEX
	public void fn_setViewSetting(String pTag, String pValue) {
		gv_setView.put(pTag, pValue); // gv_setView 에 넣는 값을 좀 보기 좋을까 하고 이렇께 짜봤다.
	}

	private void fn_viewerSetting(final String pParam) {
		fn_saveBack("fn_viewerSetting", pParam);
		if ( !"3".equals(vMode) ) ((TextView)findViewById(R.id.txt_nav)).setText(gv_nav);

		fn_chgContentView(R.layout.view_mode);

		if ( "5".equals(vMode) ) { // 설정에서 호출한 링크 페이지 로딩용
			fn_menuSlide();
			if ( isMenuExpanded ) fn_menuSlide(); // 내비게이션바가 나와있으면 집어넣어.
			mWebView.loadUrl(pParam);
		} else {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fn_convArrayList("NAME"));
			cbo_no = (Spinner)findViewById(R.id.cbo_no);
			cbo_no.setAdapter(adapter);
			cbo_no.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					if ( gv_setView.containsKey("CBO_INDEX") && (arg2 + "").equals(gv_setView.get("CBO_INDEX")) ) return;
					fn_goSite(pParam, arg2);
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {}
			});

			int index = fn_findListRow("LINK_CODE", gv_setView.get("LST_VIEW_NO"));
			if ( index < 0 ) { // 검색된 결과가 없다 = LST_VIEW_NO 가 없다 = 첫화부터봅시다.
				index = list.size() - 1;
			} else if ( index > 0 ) {
				if ( !"3".equals(vMode) ) index = index - 1; // 업데이트된 화가 있으면, 최근본 그 다음 회차를 보여준다.
			}
			fn_goSite(pParam, index);
		}
	}

	// 회차를 pParam에 넣어 호출하면 gv_cid를 통해 해당 회의 웹툰을 보여준다.
	public void fn_goSite(String pIdSeq, int pIndex) {
		if ( pIndex < 0 ) { // 최근건에서 다음화 클릭. 업데이트 확인한다.
			if ( aUtil.chkTimer("업데이트중복"+pIdSeq, 10 * 60) ) { // 120분
				if ( aUtil.chkTimer("ToastClickTimer", 2) ) return; // 지난 뒤로 클릭보다 2초 이내면
				alert(R.string.str_atLast);
				return;
			}
			(new NewIsThere()).execute(pIdSeq); //ks20141123 업데이트 체크
			return;
		} else if ( pIndex >= cbo_no.getCount() ) { // 최초건에서 이전화 클릭
			alert(R.string.str_atFirst);
			return;
		}

		fn_setViewSetting("CBO_INDEX", pIndex + "");
		String vLinkCode = getList(pIndex, "LINK_CODE"); // 현재 no 글로벌 변수
		cbo_no.setSelection(pIndex);

		if ( "kakao".equals(gv_setView.get("SITE")) ) { // 카카오 웹툰은 5개씩밖에 못본다. 모두 보기위해선 쿠키를 지워줘야해.
			CookieManager.getInstance().removeAllCookie(); // ks20140627
		}

		mWebView.loadUrl(gv_imgViewer.get(gv_setView.get("SITE")).replace("$cid", gv_setView.get("CID")).replace("$no", vLinkCode));
		//Log.d("webview url", gv_imgViewer.get(gv_setView.get("SITE")).replace("$cid", gv_setView.get("CID")).replace("$no", vLinkCode));
		fn_setViewSetting("LST_VIEW_NO", vLinkCode); // 최근 조회한 LinkCode를 업데이트한다.
	}

	// pArrList 배열로 넘어온 값을 적당히 가공하여 리스트업 한다.
	// 내비게이션바에 pNav 으로 세팅한다. pTF 가 true면 새로쓰기 아니면 이어쓰기
	public void fn_listAdapter(String pMode) {
		fn_chgContentView(R.layout.activity_main);
		TextView txt_nav = (TextView)findViewById(R.id.txt_nav);

		findViewById(R.id.img_arrow).setVisibility(View.GONE);
		findViewById(R.id.txt_noMyList).setVisibility(View.GONE);
		findViewById(R.id.txt_section_main).setVisibility(View.GONE);
		if ( "0".equals(pMode) ) {
			findViewById(R.id.main_search).setVisibility(View.GONE);
			txt_nav.setText(getResources().getString(R.string.app_name) + " > " + getResources().getString(R.string.str_myList));

			if ( list.size() == 0 ) { // 내가 보는 웹툰 없으면 추가하라고 안내
				findViewById(R.id.img_arrow).setVisibility(View.VISIBLE);
				findViewById(R.id.txt_noMyList).setVisibility(View.VISIBLE);
			}
		} else if ( "1".equals(pMode) ) {
			findViewById(R.id.main_search).setVisibility(View.VISIBLE);
			txt_nav.setText(getResources().getString(R.string.app_name) + " > " + getResources().getString(R.string.str_search));
		} else if ( "2".equals(pMode) ) {
			findViewById(R.id.main_search).setVisibility(View.VISIBLE);
			txt_nav.setText(gv_nav);
			fn_setSection();
		} else if ( "3".equals(pMode) ) {
			findViewById(R.id.main_search).setVisibility(View.GONE);
			txt_nav.setText(gv_nav);
		} else if ( "4".equals(pMode) ) {
			findViewById(R.id.main_search).setVisibility(View.VISIBLE);
			txt_nav.setText(gv_nav);
			fn_setSection();
			if ( list.size() == 0 ) { // 검색했는데 결과가 없다!
				HashMap<String, String> data = new HashMap<String, String>();
				data.put("NAME", getResources().getString(R.string.str_noSearchData));
				list.add(0, data); // 검색된 결과가 없습니다 삽입
			}
		} else if ( "5".equals(pMode) ) { // 세팅
			findViewById(R.id.main_search).setVisibility(View.GONE);
			txt_nav.setText(getResources().getString(R.string.app_name) + " > " + getResources().getString(R.string.str_setting));

		} else if ( "6".equals(pMode) ) { // 추천웹툰 ks20141116
			findViewById(R.id.main_search).setVisibility(View.GONE);
			txt_nav.setText(getResources().getString(R.string.app_name) + " > " + getResources().getString(R.string.str_favo));
		}

		gv_isNewDate = aUtil.getDataCal(); // 업데이트 기준 일자 세팅. 보통 현재의 24시간전.
		ListAdapterWithButton<String> adapter = new ListAdapterWithButton<String>(this, list);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
	}

	/*****************************
	 * 멀티스레드 시작
	 *****************************/
    /**
     * 메뉴 멀티스레드
     */
    class BackgroundTask extends AsyncTask<String, String , String[]> {
        protected void onPreExecute() {
    		CDialog.showProgress(gv_this);
        }
 
        // vMode 0내가보는웹툰 1사이트 2사이트별웹툰목록 3웹툰회차목록 4검색 5세팅 6인기 
        protected String[] doInBackground(String ... values) {
        	String vMode  = values[0];
        	String vParam = values[1];

        	List<HashMap<String, String>> vList = new ArrayList<>();
        	if ( "3".equals(vMode) || "6".equals(vMode) ) { 
                publishProgress(getResources().getString(R.string.str_confirmUpdating));
  				vList = getXmldata(vMode, vParam);
        	} else if ( "2".equals(vMode) ) {
                publishProgress(getResources().getString(R.string.str_confirmUpdating));
  				vList = getXmldata("V1", vParam);
        	} else if ( "0".equals(vMode) ) {
                publishProgress(getResources().getString(R.string.str_confirmUpdating));
  				vList = getXmldata("V0", "");
      		} else {
      			//Log.d("서버에 안감", "서버에 안감");
      		}

            publishProgress("");
    		if ( "6".equals(vMode) && !vList.isEmpty() ) { // 인기웹툰
    			StringBuffer vStr = new StringBuffer();
    			for ( int i = 0; i < vList.size(); i++ ) {
    				vStr.append("SELECT '").append(vList.get(i).get("ID_SEQ")).append("' AS ID_SEQ, ").append(i).append(" AS SORT UNION ALL ");
    			}
        		list = dbAdapter.inqSql(vMode, vStr.toString()); 
    		} else {
        		list = dbAdapter.inqSql(vMode, vParam);
    		}
        	return values;
        }

        protected void onPostExecute(String[] values) {
			if ( gv_currentViewId != R.layout.activity_main ) fn_chgContentView(R.layout.activity_main);

        	String vMode1 = values[0];
        	String vParam = values[1];

    		if ( "0".equals(vMode) && "3".equals(vMode1) ) { // 내업무목록에서 뷰어화면을 호출
    			fn_viewerSetting(vParam); // 뷰어를 세팅한다.
    		} else {
    			fn_saveBack(vMode1, vParam); // 뒤로가기 버튼 정보 저장 끝
    			fn_listAdapter(vMode1);
    		}
    		if ( "1".equals(vMode1) ) ((EditText)findViewById(R.id.edt_search)).setText("");

    		vMode = vMode1;
    		if ( !gv_saveHist ) whenBackTouch(); // 뒤로가기 버튼으로 동작한 것이라면
    		CDialog.hideProgress();
        }

        protected void onProgressUpdate(String ... values) {
        	if ( "".equals(values[0]) ) {
        		CDialog.showProgress(gv_this);
        	} else {
        		CDialog.showProgress(gv_this, values[0]);
        	}
    	}
        protected void onCancelled() { }
    }

    /**
     * 최신화에서 다음버튼 눌렀을때 멀티스레드 ks20141201
     */
    class NewIsThere extends AsyncTask<String, String , String[]> {
        protected void onPreExecute() {
    		CDialog.showProgress(gv_this, getResources().getString(R.string.str_confirmUpdating));
        }
 
        protected String[] doInBackground(String ... values) {
        	String vIdSeq = values[0];
        	
        	List<HashMap<String, String>> vUpdYN = getXmldata("UPD", vIdSeq); // 업데이트 확인 한번 날려주시고
        	String vYN = NVL(vUpdYN.get(0).get("UPD"), "N");

			if ( "Y".equals(vYN) ) { // update 되었다면
				aUtil.delXmlTimer();
	        	getXmldata("3", vIdSeq); // 업데이트 리스트 다시 불러와주시고. 
				getXmldata("V1", gv_setView.get("SITE")); // ks20140905 리스트최신화
				list = dbAdapter.inqSql("3", vIdSeq); // 리로드
			}
        	return new String[] { vIdSeq, vYN };
        }
 
        protected void onPostExecute(String[] values) {
        	String vIdSeq = values[0];
        	String vYN = values[1];

        	if ( "Y".equals(vYN) ) {
    			ArrayAdapter<String> adapter = new ArrayAdapter<String>(gv_this, android.R.layout.simple_spinner_item, fn_convArrayList("NAME"));
    			cbo_no = (Spinner)findViewById(R.id.cbo_no);
    			cbo_no.setAdapter(adapter);
    			fn_goSite(vIdSeq, fn_findListRow("LINK_CODE", gv_setView.get("LST_VIEW_NO")) - 1);
                alert(R.string.str_confirmUpdated);

                // ks20141203 누군가 새로운 툰을 발견했을때 전체 업데이트 확인이 발동된다.
    			Thread thread = new Thread(null, doBackgroundThreadProcessing, "Background");
    			thread.start();

                // ks20150119 사이트서처를 돌린다. 사이트에 신규 웹툰이있나
    			Thread thread1 = new Thread(null, doBackgroundThreadProcessingSearcher, "Background");
    			thread1.start();
        	} else {
                alert(R.string.str_atLast);
        	}
    		CDialog.hideProgress();
        }

        protected void onProgressUpdate(String ... values) { }
        protected void onCancelled() { }
    }

    /**
     * 시스템 초기화 ks20141201
     */
    class initLoadings extends AsyncTask<String, String , String> {
        protected void onPreExecute() {
    		if ( android.os.Build.VERSION.SDK_INT > 9 ) {
    			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    			StrictMode.setThreadPolicy(policy);
    		}
    		fn_setSetting();
        }
 
        protected String doInBackground(String ... values) {
        	gv_sectionG = getResources().getStringArray(R.array.gv_sectionG);
        	gv_sectionV = getResources().getStringArray(R.array.gv_sectionV);
            gv_buttons.put(""+ R.id.btn_myList, new int[]{R.drawable.ic_mn_mylist, R.drawable.ic_mn_mylist_b});
            gv_buttons.put(""+ R.id.btn_search, new int[]{R.drawable.ic_mn_add, R.drawable.ic_mn_add_b});
            gv_buttons.put(""+ R.id.btn_menuSetting, new int[]{R.drawable.ic_mn_setting, R.drawable.ic_mn_setting_b});
            gv_buttons.put(""+ R.id.btn_bottomPrevMax, new int[]{R.drawable.ic_pre_max, R.drawable.ic_pre_max_b});
            gv_buttons.put(""+ R.id.btn_bottomPrev, new int[]{R.drawable.ic_pre, R.drawable.ic_pre_b});
            gv_buttons.put(""+ R.id.btn_bottomNextMax, new int[]{R.drawable.ic_next_max, R.drawable.ic_next_max_b});
            gv_buttons.put(""+ R.id.btn_nav_smp, new int[]{R.drawable.ic_next, R.drawable.ic_next_b});
            gv_buttons.put(""+ R.id.btn_bottomReload, new int[]{R.drawable.ic_reload, R.drawable.ic_reload_b});
            gv_buttons.put(""+ R.id.btn_nav_menu, new int[]{R.drawable.ic_menu, R.drawable.ic_menu_b});
            gv_buttons.put(""+ R.id.btn_bottomMyList, new int[]{R.drawable.ic_mn_mylist, R.drawable.ic_mn_mylist_b});
            gv_buttons.put(""+ R.id.btn_bottomSearch, new int[]{R.drawable.ic_mn_add, R.drawable.ic_mn_add_b});
            gv_buttons.put(""+ R.id.btn_favo        , new int[]{R.drawable.ic_mn_favo, R.drawable.ic_mn_favo_b}); // 추가 ks20141116
            gv_buttons.put(""+ R.id.btn_bottomAdd   , new int[]{R.drawable.ic_add, R.drawable.ic_add_b});

        	//Log.d("버전", getSetting("APP_VER") + " / " + aUtil.getVersionName(gv_this));

    		// 앱의 버전업이 일어났는가 "1".equals("1") || 
            if ( !getSetting("APP_VER").equals(aUtil.getVersionName(gv_this)) ) { // 어플 버전업이 되었다면?
    			//if ( "-1".equals(data.get("VER004")) ) { // 정말 쌩으로 최초
    	        // 로컬 파일로 입력 후 서버를 호출한다.
                publishProgress("1 / 3");
    			getXmldata("2", "-1"); // LC002사이트정보
    	        getXmldata("1-1", "-1"); // LC001웹툰목록 insert. 웹툰이 추가되는 경우가 많지 않아서 잘 없다.X
    	        getXmldata("4", "-1"); // LC004세팅항목
                publishProgress("2 / 3");
    			getXmldata("3", "-1"); // 회차목록. 인기있는, 양 많은 웹툰만 넣어뒀다.
                publishProgress("3 / 3");

    			updSettingValue("FST_INS_DH", inqXmlParam("1-1", "")); // 이 날짜를 기준으로 이후 웹툰 인서트
    			updSettingValue("APP_VER", aUtil.getVersionName(gv_this)); // APP_VER 로컬버전,  WEB_VER 최신버전(웹서버에 업데이트되어있다.)

            } else {
            	try {
					Thread.sleep(500);
	                publishProgress("1");
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}

            publishProgress("2");
        	List<HashMap<String, String>> vList = getXmldata("4", "");
        	if ( !vList.isEmpty() ) fn_setSetting();

            getXmldata("V0", ""); // ks20141030
            getXmldata("2" , "");

    		fn_setSiteInfo();  // 사이트 설정값 로드

    		list = dbAdapter.inqSql("0", "");
    		return list.size() + "";
        }
 
        protected void onProgressUpdate(String ... values) {
        	if ( "1".equals(values[0]) ) {
            	alert("Hi! " + getSetting("MY_NM") + "!"); //alert(R.string.app_name);
        	} else if ( "2".equals(values[0]) ) {
	    		CDialog.showProgress(gv_this);
        	} else {
	    		CDialog.showProgress(gv_this, getResources().getText(R.string.str_firstLoading) + "\n" + values[0]);
        	}
    	}

        protected void onPostExecute(String values) {
			//Thread thread = new Thread(null, doBackgroundThreadUpdChkMyList, "Background");
			//thread.start(); // ks20141225 내가보는웹툰을 업데이트 확인
			//Thread thread1 = new Thread(null, doBackgroundThreadUpdChkNewToons, "Background");
			//thread1.start(); // ks20150121 신규웹툰검색

        	if ( "0".equals(values) ) {
        		fn_menu("1", "");
        	} else {
        		fn_menu("0", "");
        	}

        	alert(""); // 토스트 지우려고.
    		CDialog.hideProgress();
        }
        protected void onCancelled() { }
    }

	// 내가보는웹툰리스트 업데이트 확인
	private Runnable doBackgroundThreadUpdChkMyList = new Runnable() {
		@Override
		public void run() {
        	List<HashMap<String, String>> vList = getXmldata("UPD_MYLIST", "");
        	if ( vList.isEmpty() ) return;
    		if ( !"0".equals(vList.get(0).get("UPD")) ) {
                // 새로운 툰이 발견되면 전체 업데이트
    			Thread thread = new Thread(null, doBackgroundThreadProcessing, "Background");
    			thread.start();
                // ks20150119 사이트서처를 돌린다. 사이트에 신규 웹툰이있나
    			Thread thread1 = new Thread(null, doBackgroundThreadProcessingSearcher, "Background");
    			thread1.start();
    		}
		}
	};

	// 백그라운드 처리 메서드를 실행하는 Runnable
	private Runnable doBackgroundThreadProcessing = new Runnable() {
		@Override
		public void run() {
        	List<HashMap<String, String>> vList = getXmldata("UPD_ALL", "");
        	if ( vList.isEmpty() ) return;
    		if ( !"0".equals(vList.get(0).get("UPD")) ) {
				aUtil.delXmlTimer(); // 위의 aUtil.delXmlTimer() 호출한 뒤 시간이 지난후여서 다시 지운다.
    	        getXmldata("V0", ""); // ks20141203 서버에 웹툰들이 업데이트되었으니 일단 내가보는웹툰 업데이트
    		}
		}
	};

	// ks20150111 새로운 웹툰 인서트(신규사이트 등록시 로드 못하는 문제 대응). 완결,use_yn도 업데이트된다.
	private Runnable doBackgroundThreadUpdChkNewToons = new Runnable() {
		@Override
		public void run() { getXmldata("V2", ""); }
	};

	// ks20150119 사이트서처 발동
	private Runnable doBackgroundThreadProcessingSearcher = new Runnable() {
		@Override
		public void run() { getXmldata("SITE_SEARCHER", ""); }
	};
	/*****************************
	 * 멀티스레드 끝
	 *****************************/

	/*****************************
	 * OnClick 시작
	 *****************************/
	// ListViewer 의 클릭 이벤트에서 온다.
	public void fn_listOnClick(int pPosition) {
		gv_nav = ((TextView) findViewById(R.id.txt_nav)).getText() + " > " + getList(pPosition, "NAME");
		if ( "0".equals(vMode) ) { // 0내구독목록
			gv_setView.clear();
			fn_setViewSetting("CID"   , getList(pPosition, "CID"   ));
			fn_setViewSetting("ID_SEQ", getList(pPosition, "ID_SEQ"));
			fn_setViewSetting("SITE"  , getList(pPosition, "SITE"  ));
			fn_setViewSetting("MAX_NO", getList(pPosition, "MAX_NO"));
			fn_setViewSetting("LST_VIEW_NO", getList(pPosition, "LST_VIEW_NO"));
			fn_menu("3", gv_setView.get("ID_SEQ"));

		} else if ( "1".equals(vMode) ) { // 1사이트목록에서의클릭
			gv_setView.clear();
			fn_setViewSetting("SITE", getList(pPosition, "SITE"));
			fn_menu("2", getList(pPosition, "SITE"));

		} else if ( "2".equals(vMode) || "4".equals(vMode) ) { // 2웹툰목록에서의 클릭 4검색 
			fn_setViewSetting("CID"   , getList(pPosition, "CID"   ));
			fn_setViewSetting("ID_SEQ", getList(pPosition, "ID_SEQ"));
			fn_setViewSetting("MAX_NO", getList(pPosition, "MAX_NO"));
			if ( "4".equals(vMode) ) fn_setViewSetting("SITE", getList(pPosition, "SITE"));
			fn_menu("3", gv_setView.get("ID_SEQ"));

		} else if ( "3".equals(vMode) ) { // 3회차리스트에서클릭
			fn_setViewSetting("LST_VIEW_NO", getList(pPosition, "LINK_CODE"));
			fn_viewerSetting(gv_setView.get("ID_SEQ"));

		} else if ( "5".equals(vMode) ) { // 설정
			String vSelMode = getList(pPosition, "SEL_MODE");
			// vSelMode 에는 LINK:인터넷페이지로이동 COMBO:콤보 SWITCH:스위치 등이 있다.
			if ( "LINK".equals(vSelMode) ) {
				if ( "REVIEW".equals(getList(pPosition, "SET_ID")) ) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.anglab.ibkscrawler"));
					startActivity(i);
				} else {
					fn_viewerSetting(getList(pPosition, "SET_VALUE"));
				}
			} else if ( "intent".equals(vSelMode) ) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getList(pPosition, "SET_VALUE")));
				startActivity(i);
			} else if ( "donation".equals(vSelMode) ) {
				fn_donation(getList(pPosition, "SET_CONT"));
			} else if ( "function".equals(vSelMode) ) {
				if ( "DB_CLEAR".equals(getList(pPosition, "SET_ID")) ) { // DB클리어
					fn_dbClear();
				}
			} else if ( "CheckBox".equals(vSelMode) ) {
				if ( !"".equals(getList(pPosition, "SET_CONT")) ) {
				    AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
				    alt_bld.setMessage(getList(pPosition, "SET_CONT"))
							.setCancelable(false)
							.setPositiveButton(R.string.str_yes,
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = alt_bld.create();
					alert.setTitle(R.string.str_alert);
					alert.show();
				}
			} else {
			}
		} else if ( "6".equals(vMode) ) { // 6추천 웹툰 ks20141116
			fn_setViewSetting("CID"   , getList(pPosition, "CID"   ));
			fn_setViewSetting("ID_SEQ", getList(pPosition, "ID_SEQ"));
			fn_setViewSetting("MAX_NO", getList(pPosition, "MAX_NO"));
			fn_setViewSetting("SITE"  , getList(pPosition, "SITE"));
			fn_menu("3", gv_setView.get("ID_SEQ"));

		} else { // 에러
		}
	}

	@Override
    public boolean onTouch(View v, MotionEvent event) {
		if ( gv_buttons.containsKey( "" + v.getId() ) ) { // 버튼 변하는 것들이면
			int[] vKeys = gv_buttons.get( "" + v.getId() );
	        if ( event.getAction() == MotionEvent.ACTION_DOWN ) { // 버튼을 누르고 있을 때
	        	findViewById(v.getId()).setBackgroundResource(vKeys[0]);
	        } else if ( event.getAction() == MotionEvent.ACTION_UP ) { //버튼에서 손을 떼었을 때 
	        	findViewById(v.getId()).setBackgroundResource(vKeys[1]);
	        }
		}
		return false;
	}

    @Override
	public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_bottomMyList:
        	fn_menu("0", ""); // 내 구독목록으로 이동
            break;
        case R.id.btn_bottomSearch:
        	fn_menu("1", "");
            break;
        case R.id.btn_bottomAdd:
        	fn_bottomAddSub(gv_setView.get("ID_SEQ"));
            break;
        case R.id.btn_bottomPrevMax:
			fn_goSite(gv_setView.get("ID_SEQ"), cbo_no.getCount() - 1);
            break;
        case R.id.btn_bottomPrev:
			fn_goSite(gv_setView.get("ID_SEQ"), cbo_no.getSelectedItemPosition() + 1);
            break;
        case R.id.btn_bottomNextMax:
			fn_goSite(gv_setView.get("ID_SEQ"), 0);
            break;
        case R.id.btn_myList:
        	//fn_menu("0", ""); // 내 구독목록으로 이동
        	fn_ddd();
            break;
        case R.id.btn_search:
        	fn_menu("1", "");
            break;
        case R.id.btn_favo:
        	fn_menu("6", ""); // 추천웹툰
            break;
        case R.id.btn_menuSetting:
        	fn_menu("5", "");
            break;
        case R.id.btn_nav_menu:
        	fn_menuSlide();
            break;
        case R.id.btn_nav_smp:
			fn_goSite(gv_setView.get("ID_SEQ"), cbo_no.getSelectedItemPosition() - 1);
            break;
        case R.id.btn_bottomReload:
			mWebView.reload();
            break;
	    }
    }
    
    public void fn_ddd() {
    	Intent intent = new Intent( Intent.ACTION_CALL );
    	intent.setData( Uri.parse( "tel:114" ) );
    	startActivity( intent );
    	
    	Intent intent1 = new Intent( Intent.ACTION_DIAL );
    	intent1.setData( Uri.parse( "tel:4444444" ) );
    	startActivity( intent1 );
    }
	/*****************************
	 * OnClick 끝
	 *****************************/

	/*****************************
	 * UTIL 시작
	 *****************************/
    String gv_searchWord = "";
	// 화면을 바꾼다.
	public void fn_chgContentView(int pLayoutResId) {
		if ( gv_currentViewId == pLayoutResId ) return; // 현재화면과 바뀔화면이 같다면 리턴.

		// 없어저야 할 창의 사전작업 시작 //
		String vNav = "";
		if ( gv_currentViewId == R.layout.activity_main ) {
			vNav = ((TextView)findViewById(R.id.txt_nav)).getText().toString();
		} else if ( gv_currentViewId == R.layout.view_mode ) {
		}
		// 없어저야 할 창의 사전작업 끝 //

		gv_currentViewId = pLayoutResId;
		setContentView(gv_currentViewId);

		// 새로 생성된 화면의 사전작업 시작 //
		if ( gv_currentViewId == R.layout.activity_main ) {

			findViewById(R.id.img_arrow).setVisibility(View.GONE);
			findViewById(R.id.txt_noMyList).setVisibility(View.GONE);
			
			findViewById(R.id.btn_myList).setOnTouchListener(this);
			findViewById(R.id.btn_search).setOnTouchListener(this);
			findViewById(R.id.btn_favo).setOnTouchListener(this);
			findViewById(R.id.btn_menuSetting).setOnTouchListener(this);

			findViewById(R.id.btn_myList).setOnClickListener(this);
			findViewById(R.id.btn_search).setOnClickListener(this);
			findViewById(R.id.btn_favo).setOnClickListener(this);
			findViewById(R.id.btn_menuSetting).setOnClickListener(this);

			EditText edt_search = (EditText)findViewById(R.id.edt_search);
			edt_search.setOnKeyListener(new OnKeyListener(){
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ( keyCode == KeyEvent.KEYCODE_ENTER ) { // 엔터누르면 검색 호출
						fn_goSearch();
						return true;
					} else {
						//글자 입력시마다 검색. 은 성능 고려 삭제 ks20141116
						//if ( gv_searchWord.equals(((EditText) findViewById(R.id.edt_search)).getText().toString()) ) {
						//	return false;
						//} else {
						//	fn_goSearch();
						//	return true;
						//}
						return false;
					}
				}
				} );

			ListView listView = (ListView) findViewById(R.id.list);
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					// 리스트 ㄱㄴㄷ 섹션 표시를 위함
					if ( !"2".equals(vMode) && !"4".equals(vMode) ) return;
					TextView txt_section_main = (TextView) findViewById(R.id.txt_section_main);
					String vSection = fn_sectionWords(firstVisibleItem);
					if ( !txt_section_main.getText().equals(vSection) ) {
						txt_section_main.setText(fn_sectionWords(firstVisibleItem));
					}
				}
			});

		} else if ( gv_currentViewId == R.layout.view_mode ) {
			((TextView)findViewById(R.id.txt_view_nav)).setText(vNav);

			if ( "Y".equals(getSetting("NAV_HIDDEN")) &&  isMenuExpanded ) fn_menuSlide(); // 내비게이션바가 나와있으면 집어넣어.
			if ( "N".equals(getSetting("NAV_HIDDEN")) && !isMenuExpanded ) fn_menuSlide(); // 내비게이션바가 나와있으면 집어넣어.

			mWebView = (WebView) findViewById(R.id.web_main);
			mWebView.getSettings().setJavaScriptEnabled(true); // 자바스크립트  on
			mWebView.setWebViewClient(new CWebViewClient(this));

	        findViewById(R.id.btn_bottomAdd    ).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomMyList ).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomSearch ).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomPrevMax).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomPrev   ).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomNextMax).setOnTouchListener(this);
	        findViewById(R.id.btn_bottomReload ).setOnTouchListener(this);
	        findViewById(R.id.btn_nav_menu     ).setOnTouchListener(this);
	        findViewById(R.id.btn_nav_smp      ).setOnTouchListener(this);

	        findViewById(R.id.btn_bottomAdd    ).setOnClickListener(this);
	        findViewById(R.id.btn_bottomMyList ).setOnClickListener(this);
	        findViewById(R.id.btn_bottomSearch ).setOnClickListener(this);
	        findViewById(R.id.btn_bottomPrevMax).setOnClickListener(this);
	        findViewById(R.id.btn_bottomPrev   ).setOnClickListener(this);
	        findViewById(R.id.btn_bottomNextMax).setOnClickListener(this);
	        findViewById(R.id.btn_bottomReload ).setOnClickListener(this);
	        findViewById(R.id.btn_nav_menu     ).setOnClickListener(this);
	        findViewById(R.id.btn_nav_smp      ).setOnClickListener(this);

	        if ( fn_isInMyList(gv_setView.get("ID_SEQ")) ) {
	        	gv_buttons.put("" + R.id.btn_bottomAdd, new int[]{R.drawable.ic_sub, R.drawable.ic_sub_b});
	        	findViewById(R.id.btn_bottomAdd).setBackgroundResource(R.drawable.ic_sub_b);
	        } else {
	        	gv_buttons.put("" + R.id.btn_bottomAdd, new int[]{R.drawable.ic_add, R.drawable.ic_add_b});
	        	findViewById(R.id.btn_bottomAdd).setBackgroundResource(R.drawable.ic_add_b);
	        }

	        if ( "5".equals(vMode) ) { // 설정의 링크는 내비게이션 버튼 제거
	        	findViewById(R.id.ll_menuBottom_smp).setVisibility(View.GONE);
	        } else {
	        	findViewById(R.id.ll_menuBottom_smp).setVisibility(View.VISIBLE);
	        }
		} else if ( gv_currentViewId == R.layout.loading ) {
		}
		// 새로 생성된 화면의 사전작업 끝 //
	}

	// pTagName 을 가진 list 부분을 ArrayList로 리턴한다.
	public ArrayList<String> fn_convArrayList(String pTagName) {
		ArrayList<String> vArrayList = new ArrayList<String>();
		for ( int i = 0; i < list.size(); i++ ) {
			vArrayList.add(getList(i, pTagName));
		}
		return vArrayList;
	}

	// list 의 pTagName의 값중 pValue의 값을 가지고 있는 row를 리턴한다.
	public int fn_findListRow(String pTagName, String pValue) {
		if ( pTagName == null || pValue == null ) return -1;
		for ( int i = 0; i < list.size(); i++ ) {
			if ( pValue.equals(getList(i, pTagName)) ) {
				return i;
			}
		}
		return -1;
	}

	// thumbNail url을 가지고온다.
	public String fn_getUrl(int pPosition) {
		String vSite = NVL(getList(pPosition, "SITE"), gv_setView.get("SITE")); // list에 SITE가 있으면 쓰고 없으면 글로벌셋에서 가져다 쓴다.
		String vCode = NVL(getList(pPosition, "MAX_NO"), getList(pPosition, "LINK_CODE")); // "3".equals(vMode)? getList(pPosition, "LINK_CODE") : getList(pPosition, "MAX_NO"); // 회차정보. 회차리스트면 LINK_CODE로, 내구독목록이면 MAX_NO를 가져온다.
		String vCid  = NVL(getList(pPosition, "CID"), gv_setView.get("CID"));
		String vThumbUrl = (( getList(pPosition, "THUMB_NAIL").indexOf("http") == 0 )? "" : gv_thumbComn.get(vSite) ) + getList(pPosition, "THUMB_NAIL");
		return (vThumbUrl.replace("$cid", vCid).replace("$no", vCode));
	}

	// list의 pPosition 줄의 pTagName 값을 반환
	public String getList(int pPosition, String pTagName) {
		return NVL((String) list.get(pPosition).get(pTagName));
	}
	public String NVL(String pStr1) { return NVL(pStr1, ""); }
	public String NVL(String pStr1, String pStr2) {
		if ( pStr1 == null || pStr1.trim().length() == 0 ) {
			if ( pStr2 == null || pStr2.trim().length() == 0 ) pStr2 = "";
			return pStr2;
		}
		return pStr1;
	}

	public String fn_getMode() { return vMode; }
	public String getSetting(String pParam) { return NVL(gv_setting.get(pParam), ""); }

    private void fn_bottomAddSub(String pIdSeq) {
        if ( fn_isInMyList(pIdSeq) ) {
        	fn_delMyList(pIdSeq); // 내구독목록에서 삭제
        } else {
        	fn_insMyList(pIdSeq);
        }
	}

	// 웹툰 검색
	public void fn_goSearch() {
		gv_searchWord = ((EditText) findViewById(R.id.edt_search)).getText().toString().trim();
		if ( "".equals(gv_searchWord) ) return;
		gv_nav = getResources().getString(R.string.app_name) + " > " + getResources().getString(R.string.str_search) + " > '" + ((EditText) findViewById(R.id.edt_search)).getText().toString() + "'" + getResources().getString(R.string.str_searchWords);
		fn_menu("4", gv_searchWord);
	}

    // 뒤로가기 버튼 동작
	boolean gv_saveHist = true; // 뒤로가기 이력을 쌓으면 true
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ( keyCode == KeyEvent.KEYCODE_BACK ) {
    		if ( gv_backList.isEmpty() || gv_backList.size() < 2 ) { // 최 상위 메뉴일때 뒤로버튼을 두번 클릭하면 앱이 종료된다.
    			if ( aUtil.chkTimer("BackButton", 2) ) { // 2초만에 클릭하면 시스템종료
    		        finish();
    		        System.exit(0);
    		        android.os.Process.killProcess(android.os.Process.myPid());
    		    } else {
    		        Toast.makeText(this, R.string.str_oneMoreFinish, Toast.LENGTH_SHORT).show();
    		    }
    		} else {
    			HashMap<String, String> data = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 2);
    			gv_saveHist = false;
    			fn_menu(NVL(data.get("vMode")), NVL(data.get("vParam")));
    		}
    	}
		return false;
	}

    public void whenBackTouch() {
		gv_saveHist = true;

		//HashMap<String, String> data1 = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 2); // vMode, vParam
		HashMap<String, String> data = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1); // vPos, vNav
		gv_backList.remove(gv_backList.size() - 1);

		int vPos = Integer.valueOf(data.get("vPos"));
		if ( vPos > -1 && list.size() > vPos ) ((ListView)findViewById(R.id.list)).setSelection(vPos);
		((TextView)findViewById(R.id.txt_nav)).setText(data.get("vNav"));
    }

    // 뒤로가기 버튼을 저장한다.
    public void fn_saveBack(String pMode, String pParam) {
    	if ( !gv_saveHist ) return;
		if ( "0".equals(pMode) || "1".equals(pMode) ) gv_backList.clear(); // 내구독리스트 일때는 백버튼 초기화(더이상 뒤로갈수없음)

		if ( gv_backList.size() > 0 ) {
			HashMap<String, String> dataCompare = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1);
			if ( dataCompare.get("vMode").equals(pMode) && dataCompare.get("vParam").equals(pParam) ) return; // 같은 메뉴 두번 클릭은 패스
		}

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("vMode" , pMode );
		data.put("vParam", pParam);
		data.put("vNav"  , ((TextView)findViewById(R.id.txt_nav)).getText().toString());
		data.put("vPos"  , ((ListView)findViewById(R.id.list)).getFirstVisiblePosition() + "");
		gv_backList.add(new HashMap<String, String>(data));
    }
    
	Toast mToast = null; // 메시지변경을 위해 전역으로 토스트 설정.
    public void alert(String pMsg) {
    	if ( "".equals(pMsg) ) {
    		if ( mToast != null ) mToast.cancel();
    	} else {
	    	if ( mToast != null ) {
				mToast.setText(pMsg);
			} else {
				mToast = Toast.makeText(gv_this, pMsg, Toast.LENGTH_SHORT);
			}
			mToast.show();
    	}
    }
    public void alert(int pMsg) {
		if ( mToast != null ) {
			mToast.setText(pMsg);
		} else {
			mToast = Toast.makeText(gv_this, pMsg, Toast.LENGTH_SHORT);
		}
		mToast.show();    	
    }
	/*****************************
	 * UTIL 끝
	 *****************************/

	/*****************************
	 * 무빙샷 시작
	 *****************************/
    private static boolean isMenuExpanded = true;
    private void fn_menuSlide() {
    	LinearLayout pLayout = (LinearLayout) findViewById(R.id.ll_menuBottom);
        FrameLayout.LayoutParams leftMenuLayoutPrams = (FrameLayout.LayoutParams) pLayout.getLayoutParams();
		enableDisableViewGroup((ViewGroup) findViewById(R.id.ll_menuBottomRelative), !isMenuExpanded);

        if ( isMenuExpanded ) {
            isMenuExpanded = false;
            new OpenAnimation( pLayout, 0, 0.0f, 0, 0.0f
		                     , Animation.INFINITE, 0
		                     , Animation.INFINITE, leftMenuLayoutPrams.height);
        } else {
            isMenuExpanded = true;
            new OpenAnimation( pLayout, 0, 0.0f, 0, 0.0f
                             , Animation.INFINITE, leftMenuLayoutPrams.height
                             , Animation.INFINITE, 0);
        }
    }

	/**
	 * 뷰의 동작을 제어한다. 하위 모든 뷰들이 enable 값으로 설정된다.
	 */
	public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
		int childCount = viewGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = viewGroup.getChildAt(i);
			view.setEnabled(enabled);
			if (view instanceof ViewGroup) {
				enableDisableViewGroup((ViewGroup) view, enabled);
			}
		}
	}
	/*****************************
	 * 무빙샷 끝
	 *****************************/

	/*****************************
	 * 리스트뷰 섹션 관련 시작
	 *****************************/
	ArrayList<Integer> gv_sections = new ArrayList<Integer>();
	public int fn_findSection(String pName) {
        for ( int i = 1; i < gv_sectionG.length; i++) {
            if ( pName.compareTo(gv_sectionG[i]) < 0 ) return i - 1;
        }
        return 0;
	}

	public void fn_setSection() {
		if ( list.size() == 0 ) { 
			findViewById(R.id.txt_section_main).setVisibility(View.GONE);
			return;
		}
		gv_sections.clear();
		findViewById(R.id.txt_section_main).setVisibility(View.VISIBLE);
		for ( int i = 0; i < list.size(); i++ ) {
			gv_sections.add(fn_findSection(getList(i, "NAME")));
		}
	}

	public String fn_sectionWords(int pPosition) {
		return gv_sectionV[(Integer) gv_sections.get(pPosition)];
	}

	public String fn_sectionWordsEmpty(int pPosition) {
		String vRtn = fn_sectionWords(pPosition);
		if ( pPosition == 0 ) return vRtn;
		return vRtn.equals(fn_sectionWords(pPosition - 1))? "" : vRtn;
	}
	/*****************************
	 * 리스트뷰 섹션 관련 끝
	 *****************************/

	/*****************************
	 * SQLite DB 컨트롤 영역 시작
	 *****************************/
	/** 각 메뉴별 쿼리조회 */
	public void fn_menu(String pMode, String pParam) {
	    (new BackgroundTask()).execute(new String[] {pMode, pParam});
	}

	/** 프로그램 기본정보 로드 */
	public void fn_setSetting() {
        List<HashMap<String, String>> vList = dbAdapter.inqSql("TB_LC004", "");
        gv_svrList.clear();
        gv_svrUrlArrayIndex = 0;
    	boolean vIsSvrOrnTF = false;

        for ( HashMap<String, String> data : vList ) {
    		if ( "svrList".equals(data.get("SEL_MODE")) ) { // 백업서버 리스트
	   			 /* http://kyusiks.dothome.co.kr/toontaku/
			        http://anglab.dothome.co.kr/toontaku/
			        http://anglab.url.ph/toontaku/ 
			                     현재 3개의 미러링 사이트로 운영됨 */
    			gv_svrList.add(data.get("SET_VALUE")); // SET_NM 가 없어서 리스트엔 표시가 안된다.
        		if ( gv_svrUrlOrg.equals(data.get("SET_VALUE")) ) vIsSvrOrnTF = true; // 오리지널 서버가 있으면 true
    		} else {
        		gv_setting.put(data.get("SET_ID"), data.get("SET_VALUE"));
    		}
        }
        //Log.d("MY_NM", getSetting("MY_NM"));

        // 백업서버 리스트에 공을 들인다. 서버 리스트가 없거나 오리지널 서버가 포함되어있지 않다면 add.
        if ( gv_svrList.size() == 0 || !vIsSvrOrnTF ) gv_svrList.add(gv_svrUrlOrg);
		gv_svrUrl = gv_svrList.get(gv_svrUrlArrayIndex); // 최초 로드시에는 0번째 row가 기본.
	}

	/** 사이트별 정보 로드 */
	public void fn_setSiteInfo() {
        List<HashMap<String, String>> vList = dbAdapter.inqSql("TB_LC002", "");
        if ( vList.size() <= 0 ) return;
        for ( HashMap<String, String> data : vList ) {
    		gv_imgViewer.put(data.get("SITE"), data.get("IMG_VIEWER"));
	    	gv_thumbComn.put(data.get("SITE"), data.get("THUMB_COMN"));
        }
	}

	// 구독 목록에 추가
	public void fn_insMyList(final String pIdSeq) {
        String vName = "";
		int index = fn_findListRow("ID_SEQ", pIdSeq);
        if ( index >= 0 && index < list.size() ) {
        	vName = getList(index, "NAME");
        	if ( !"".equals(vName) ) vName = "[" + vName +"]\n\n";
        }

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.str_rusure);
        dialogBuilder.setMessage(vName + getResources().getString(R.string.str_gudokAdd)); // 구독추가하시겠습니까
        dialogBuilder.setNegativeButton(R.string.str_no, null);
        dialogBuilder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
        		if ( gv_currentViewId == R.layout.activity_main ) { // 리스트에서 클릭
            		dbAdapter.insLC000(pIdSeq, fn_inqSort("MAX"), "-1");

            		// 아이콘 재배치를 위해 화면 리로드
        			HashMap<String, String> data = (HashMap<String, String>) gv_backList.get(gv_backList.size() - 1);
        			data.put("vPos", ((ListView)findViewById(R.id.list)).getFirstVisiblePosition() + "");
        			data.put("vNav", ((TextView)findViewById(R.id.txt_nav)).getText().toString());
        			gv_backList.add(new HashMap<String, String>(data));
        			gv_saveHist = false;
        			fn_menu(NVL(data.get("vMode")), NVL(data.get("vParam")));

        		} else { // 뷰모드에서 클릭
            		dbAdapter.insLC000(pIdSeq, fn_inqSort("MAX"), gv_setView.get("LST_VIEW_NO"));
    	        	gv_buttons.put("" + R.id.btn_bottomAdd, new int[]{R.drawable.ic_sub, R.drawable.ic_sub_b});
    	        	findViewById(R.id.btn_bottomAdd).setBackgroundResource(R.drawable.ic_sub_b);
        		}
            }
        });
        dialogBuilder.show();
	}

	// 구독 목록에 해당건 삭제
	public void fn_delMyList(final String pIdSeq) {
        String vName = "";
		int index = fn_findListRow("ID_SEQ", pIdSeq);
        if ( index >= 0 && index < list.size() ) {
        	vName = getList(index, "NAME");
        	if ( !"".equals(vName) ) vName = "[" + vName +"]\n\n";
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.str_rusure);
        dialogBuilder.setMessage(vName + getResources().getString(R.string.str_gudokCancle));
        dialogBuilder.setNegativeButton(R.string.str_no, null);
        dialogBuilder.setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
        		dbAdapter.delLC000(pIdSeq);

        		if ( gv_currentViewId == R.layout.activity_main ) { // 리스트에서 클릭
            		// 아이콘 재배치를 위해 화면 리로드
            		int vTempPos = ((ListView)findViewById(R.id.list)).getFirstVisiblePosition();
            		fn_menu("0", ""); // 내 구독목록으로 이동
    				((ListView)findViewById(R.id.list)).setSelection(vTempPos);
        		} else { // 뷰모드에서 클릭
    	        	gv_buttons.put("" + R.id.btn_bottomAdd, new int[]{R.drawable.ic_add, R.drawable.ic_add_b});
    	        	findViewById(R.id.btn_bottomAdd).setBackgroundResource(R.drawable.ic_add_b);
        		}
            }
        });
        dialogBuilder.show();
	}

	// 최근 본 화.
	public void fn_updLstViewNo() {
		dbAdapter.updLstViewNo(gv_setView.get("ID_SEQ"), gv_setView.get("LST_VIEW_NO"));
	}

	/**TODO 뭐지? 프로그램 기본정보 로드 */
	public String fn_inqSort(String pMinMax) {
		HashMap<String, String> data = dbAdapter.inqSql("TB_LC000_SORT_MAX", "").get(0);
        String vReturn = "MAX".equals(pMinMax)? data.get("SORT_MAX") : data.get("SORT_MIN");
        return vReturn; // 소트 맥스 : 소트 민
	}

	/** MyList의 IdSeq를 '/' 구분자로. 텍스트형태로. ks20141101 */
	public String fn_inqMyListToString() {
        List<HashMap<String, String>> vList = dbAdapter.inqSql("0", "");
        if ( vList.size() <= 0 ) return "";

        StringBuffer rtn = new StringBuffer();
        for ( HashMap<String, String> data : vList ) {
        	rtn.append(data.get("ID_SEQ")).append("/");
        }
        return rtn.toString();
	}

	public String inqXmlParam(String pMode, String pParam) {
		List<HashMap<String, String>> vList = dbAdapter.inqSql("S_" + pMode, pParam);
		if ( vList.isEmpty() ) return "";
		return vList.get(0).get("COL1");
	}

	public boolean fn_isInMyList(String pIdSeq) { // ID_SEQ로 myList에 있나? 있으면 true
		return !"0".equals(inqXmlParam("fn_isInMyList", pIdSeq)); // 내 구독 리스트에 있으면 true 없으면(0) false
	}

	/** 설정값 저장 */
	public void updSettingValue(String pSetId, String pSetValue) {
		dbAdapter.updSettingValue(pSetId, pSetValue);
		fn_setSetting();
	}

	/** DB클리어 */
	private void fn_dbClear() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(R.string.str_dbClear)
				.setCancelable(false)
				.setPositiveButton(R.string.str_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dbAdapter.delLC000All();
							}
						})
				.setNegativeButton(R.string.str_no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(R.string.str_rusure);
		alert.show();
	}
	/*****************************
	 * SQLite DB 컨트롤 영역 끝
	 *****************************/

	/*****************************
	 * 결제관련 영역 시작
	 *****************************/
	ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
 
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( mServiceConn != null ) {
            unbindService(mServiceConn);
        }
    }

    public void AlreadyPurchaseItems() {
        try {
            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems
                        .getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");
                    // 여기서 tokens를 모두 컨슘 해주기
                    mService.consumePurchase(3, getPackageName(), tokens[i]);
                }
            }
            // 토큰을 모두 컨슘했으니 구매 메서드 처리
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Buy(String id_item) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), id_item, "inapp", "test");
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
 
            if ( pendingIntent != null ) {
            	startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                //mHelper.launchPurchaseFlow(this, getPackageName(), 1001,  mPurchaseFinishedListener, "test");
            	// 위에 두줄 결제호출이 2가지가 있는데 위에것을 사용하면 결과가 onActivityResult 메서드로 가고, 밑에것을 사용하면 OnIabPurchaseFinishedListener 메서드로 갑니다.  (참고하세요!)
            } else {
                // 결제가 막혔다면
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	   if ( requestCode == 1001 ) {           
	      //int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
	      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
	      //String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
	 
	      if ( resultCode == RESULT_OK ) {
	         try {
	            JSONObject jo = new JSONObject(purchaseData);
	            String sku = jo.getString("productId");
	            Log.d("ff", "You have bought the " + sku + ". Excellent choice, adventurer!");
	          } catch (JSONException e) {
				Log.d("ff", "Failed to parse purchase data.");
				e.printStackTrace();
	          }
	      }
	   }
	}

	private void fn_donation(String pContext) {
		pContext = pContext.replace("\\n", "\n");
	    AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
	    alt_bld.setMessage(pContext)
				.setCancelable(false)
				.setPositiveButton(R.string.str_yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								fn_donationGo();
							}
						})
				.setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alt_bld.create();
		alert.setTitle(R.string.str_donTitle);
		alert.show();
	}

	int gv_donItem = -1;
	private void fn_donationGo() {
		final String items[]  = getResources().getStringArray(R.array.itm_donName);
		final String itemId[] = getResources().getStringArray(R.array.itm_donId);

		gv_donItem = 0;
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.str_donTitle);
		ab.setSingleChoiceItems(items, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) { // 각 리스트를 선택했을때
						gv_donItem = whichButton;
					}
				})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
						if ( gv_donItem >= 0 && gv_donItem < itemId.length) {
							Buy(itemId[gv_donItem]);
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Cancel 버튼 클릭시
							}
						});
		ab.show();
	}
	/*****************************
	 * 결제관련 영역 끝
	 *****************************/
}