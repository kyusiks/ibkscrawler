package com.anglab.ibkscrawler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class aUtil  extends Activity {
	static HashMap<String, Long> gv_timeChk = new HashMap<String, Long>(); // 시간 체크 전용 해시맵
	// true면 진행. false면 막아.
	// chkTimer("ggg", 120); // 지금부터 120초 전에 ggg를 호출했으면 true
	public static boolean chkTimer(String pTimerId, int pMinuteTerm ) {
		if ( gv_timeChk.containsKey(pTimerId) ) {
			Long vChkCal = System.currentTimeMillis() - (pMinuteTerm * 1000); // 초단위변환 후 계산
			Log.d("chkTimer", pTimerId + " : " + vChkCal + " / " +  gv_timeChk.get(pTimerId) + " : " + (vChkCal > gv_timeChk.get(pTimerId)));
			if ( vChkCal <= gv_timeChk.get(pTimerId) ) return true;
		}
		gv_timeChk.put(pTimerId, System.currentTimeMillis());
		return false;
	}
	
	public static int delXmlTimer() {
		ArrayList<String> vArrayList = new ArrayList<String>();
		for ( Entry<String, Long> entry : gv_timeChk.entrySet() ) {
	        String key = entry.getKey();
	        if ( key.indexOf("getXmldata/") == 0 ) vArrayList.add(key);
    		//Log.d("delXmlTimer", key);
	        //Long value = entry.getValue();
		}

		for ( String vRemovekey : vArrayList ) {
			gv_timeChk.remove(vRemovekey);
    		//Log.d("gv_timeChk.remove", vRemovekey);
		}
		return vArrayList.size();
	}

	// 24시간전 일시를 리턴(is_new 판단에 사용)
	public static String getDataCal() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss"); 
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1); 
		return fmt.format(calendar.getTime());
	}

    public static String getVersionName(Context context) {
        try {
            PackageInfo pi= context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static String[] vValueBefore = { "A"  , "B"      , "C"     , "D"         , "E"        , "F"         , "G"     , "H"   , "I"       
			                              , "J"       , "K"     , "L"        , "M"   , "N"   , "O"         , "P"         , "Q"     , "R"  };
    public static String[] vValueAfter  = { "CID", "COMP_YN", "ID_SEQ", "IMG_VIEWER", "LINK_CODE", "LST_UPD_DH", "MAX_NO", "NAME", "SEL_MODE"
			                              , "SET_CONT", "SET_ID", "SET_VALUE", "SITE", "SORT", "THUMB_COMN", "THUMB_NAIL", "USE_YN", "CNT"};
	// 트레픽을 줄이기위해 변수를 약어로 바꿨다. 치환해주는 역활
    public static String sectionFind(String pTagname) {
		for ( int i = 0; i < vValueBefore.length; i++ ) {
			if ( pTagname.equals(vValueBefore[i]) ) return vValueAfter[i];
		}
		return pTagname;
	}
}
