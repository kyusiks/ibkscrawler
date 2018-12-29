package com.anglab.ibkscrawler;

import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListViewer extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	static class ListAdapterWithButton<T> extends BaseAdapter {
		private final LayoutInflater mInflater;
		private final List<HashMap<String, String>> array;
	    private final Context mContext;

		public ListAdapterWithButton(final Context context, final List<HashMap<String, String>> array) {
			this.mContext = context;
			this.mInflater = LayoutInflater.from(context);
			this.array = array;
		}

		@Override
		public int getCount() { return array.size(); }

		@Override
		public String getItem(int position) { return (String)array.get(position).get("NAME"); }

		@Override
		public long getItemId(int position) { return position; }

		class ViewHolder {
			TextView label;
			Button btn_list_01;
			ImageView img_site;
			ImageView img_new;
			ImageView img_fin;
			RelativeLayout lay_mm;
			WebView web_thumb;
			CheckBox chk_list_02;
			TextView txt_section;
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			final MainActivity vMainActivity = (MainActivity) mContext;

			if ( convertView == null ) {
				convertView = mInflater.inflate(R.layout.list_one_row, null);
				holder = new ViewHolder();
				holder.label     = (TextView) convertView.findViewById(R.id.txt_title);
				holder.btn_list_01 = (Button) convertView.findViewById(R.id.btn_list_01);
				holder.img_site  = (ImageView) convertView.findViewById(R.id.img_site);
				holder.img_new   = (ImageView) convertView.findViewById(R.id.img_new);
				holder.img_fin   = (ImageView) convertView.findViewById(R.id.img_fin);
				holder.lay_mm    = (RelativeLayout) convertView.findViewById(R.id.lay_mm);
				holder.web_thumb = (WebView) convertView.findViewById(R.id.web_thumb);
				holder.chk_list_02 = (CheckBox) convertView.findViewById(R.id.chk_list_02);
				holder.txt_section = (TextView) convertView.findViewById(R.id.txt_section);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String vName = getList(position, "NAME");
			holder.label.setText(vName); // 리스트 제목

			// 자료가 없다 = 검색된 결과가 없습니다
			if ( vMainActivity.getResources().getString(R.string.str_noSearchData).equals(vName)
			  && array.size() == 1 ) {
				holder.label.setTextColor(Color.WHITE);
				holder.btn_list_01.setVisibility(View.GONE);
				holder.img_new.setVisibility(View.GONE); 
				holder.img_fin.setVisibility(View.GONE); 
				holder.img_site.setVisibility(View.GONE);
				holder.chk_list_02.setVisibility(View.GONE);
				holder.web_thumb.setVisibility(View.GONE);
				holder.txt_section.setVisibility(View.GONE);
				return convertView;
			}

			final String vMode = vMainActivity.fn_getMode();
			/********** 섹션처리 **********/
			if ( "2".equals(vMode) || "4".equals(vMode) ) {
				String vSectionWord = vMainActivity.fn_sectionWordsEmpty(position);
				if ( "".equals(vSectionWord) ) {
					holder.txt_section.setVisibility(View.GONE);
				} else {
					holder.txt_section.setText(vSectionWord);
					holder.txt_section.setVisibility(View.VISIBLE);
				}

			} else if ( "6".equals(vMode) ) { // 추천웹툰
				// 최근 2주간 클릭수를 기준으로한 랭킹. 가장 마지막 row는  추천 웹툰
				if ( position == getCount() - 1 ) {
					//TODO holder.txt_section.setText(R.string.str_ibkscrawlerFavo);
					holder.txt_section.setVisibility(View.VISIBLE);
				} else {
					holder.txt_section.setVisibility(View.GONE);
				}
			} else {
				holder.txt_section.setVisibility(View.GONE);
			}
			/********************/

			/********** 색칠처리, new 처리 **********/
			// vTF1:24시간 이내 업데이트 되었는가, vTF2:내가 안본 최신화가 있는가
			String vLstUpdDh = getList(position, "LST_UPD_DH");

			boolean vTF1 = !"".equals(vLstUpdDh) && ( vMainActivity.gv_isNewDate.compareTo(vLstUpdDh) < 0 );
			boolean vTF2 = ( !"".equals(getList(position, "LST_VIEW_NO")) && !"".equals(getList(position, "MAX_NO")) )
					    && !getList(position, "MAX_NO").equals(getList(position, "LST_VIEW_NO"));

			if ( vTF2 ) {
				//holder.lay_mm.setBackgroundColor(Color.WHITE);
				holder.label.setTextColor(0xFF009BD9);
				//holder.label.setTextColor(Color.WHITE);
			} else {
				//holder.lay_mm.setBackgroundColor(0xFFF4F4F4);
				//holder.label.setTextColor(0xFF333333);
				holder.label.setTextColor(Color.WHITE);
			}
			holder.img_new.setVisibility(vTF1? View.VISIBLE : View.GONE); // new image icon visible
			/********************/

			/********** 완료 아이콘 **********/
			if ( "Y".equals(getList(position, "COMP_YN")) ) {
				holder.img_fin.setVisibility(View.VISIBLE);
			} else {
				holder.img_fin.setVisibility(View.GONE);
			}
			/********************/

			/********** 사이트 썸네일 **********/
			String vSite = getList(position, "SITE");
			int vDrawable = -1;
			if ( "naver".equals(vSite) ) vDrawable = R.drawable.ic_naver;
			else if ( "nate".equals(vSite) ) vDrawable = R.drawable.ic_nate;
			else if ( "daum".equals(vSite) ) vDrawable = R.drawable.ic_daum;
			else if ( "naver_b".equals(vSite) ) vDrawable = R.drawable.ic_naver_b;
			else if ( "daum_l".equals(vSite)  ) vDrawable = R.drawable.ic_daum_l;
			else if ( "kakao".equals(vSite)  ) vDrawable = R.drawable.ic_kakao;
			else if ( "lezhin".equals(vSite)  ) vDrawable = R.drawable.ic_lezhin;
			else if ( "olleh".equals(vSite)  ) vDrawable = R.drawable.ic_olleh;
			else if ( "tstore".equals(vSite)  ) vDrawable = R.drawable.ic_tstore;
			else if ( "ttale".equals(vSite)  ) vDrawable = R.drawable.ic_ttale;

			if ( vDrawable == -1 ) {
				holder.img_site.setVisibility(View.GONE);				
			} else {
				holder.img_site.setVisibility(View.VISIBLE);
				holder.img_site.setBackgroundResource(vDrawable);
				//(holder.img_site.getBackground()).setAlpha(400); // 투명도 조절
			}
			/********************/

			/********** 버튼 비지블 처리 **********/
			boolean vThumbTF = true;

			holder.web_thumb.setVisibility(View.VISIBLE);
			holder.chk_list_02.setVisibility(View.GONE);
			
			if ( "0".equals(vMode) ) { // 내구독목록
				holder.btn_list_01.setBackgroundResource(R.drawable.ic_sub_b);
			} else if ( "1".equals(vMode) ) { // 사이트
				holder.btn_list_01.setVisibility(View.GONE);
				holder.img_site.setVisibility(View.GONE);
			} else if ( "2".equals(vMode) || "4".equals(vMode) || "6".equals(vMode) ) { // 웹툰목록 / 검색 / 추천웹툰
				if ( "Y".equals(getList(position, "MY_INQ_YN")) ) {
					holder.btn_list_01.setVisibility(View.GONE);
				} else { // 내 구독 리스트에 없으면 추가 버튼
					holder.btn_list_01.setVisibility(View.VISIBLE);
					holder.btn_list_01.setBackgroundResource(R.drawable.ic_add_b);
				}
			} else if ( "3".equals(vMode) ) { // 회차정보
				holder.btn_list_01.setVisibility(View.GONE);
			} else if ( "5".equals(vMode) ) { // 설정
				vThumbTF = false;
				holder.btn_list_01.setVisibility(View.GONE);
				holder.web_thumb.setVisibility(View.GONE);
				holder.img_site.setVisibility(View.GONE);
				holder.img_new.setVisibility(View.GONE);

				if ( "CheckBox".equals(getList(position, "SEL_MODE")) ) {
					holder.chk_list_02.setVisibility(View.VISIBLE);
					holder.chk_list_02.setChecked("Y".equals(getList(position, "SET_VALUE")));
					holder.chk_list_02.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
							vMainActivity.updSettingValue(getList(position, "SET_ID"), (arg1? "Y" : "N"));
						}
					});
				}
			}
			/****************************************/

			/********** 썸네일 처리 **********/
			if ( vThumbTF ) { // 썸네일 보기가 true이면

				String vThumb = "ic_noimage.png";
				if ( "Y".equals(vMainActivity.getSetting("THUMB_YN")) ) {
					if ( "1".equals(vMode) ) {
						vThumb = "ic_" + vSite + ".png";
					} else {
						vThumb = vMainActivity.fn_getUrl(position);
					}
				}

				String data = "<style>div {-webkit-clip-path: polygon(30% 0%,70% 0%,93% 50%,70% 100%,30% 100%,7% 50%);" 
				                                 +"clip-path: polygon(30% 0%,70% 0%,93% 50%,70% 100%,30% 100%,7% 50%);"
				                                 + "width:100%;height:100%;}"
						           + "img {width:100%;height:100%;}</style>";
				data += "<body align='center' style='margin:0 0 0 0;padding:7 14 8 14;'><div>";
				data += "<img src='" + vThumb + "'>"; 
				data += "</div></body>";

				holder.web_thumb.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "utf-8", null);
				holder.web_thumb.setBackgroundColor(0);
			}
			/********************/

			/*holder.web_thumb.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					switch (arg1.getAction()){
					case MotionEvent.ACTION_DOWN : 
						vMainActivity.fn_listOnClick(position);
						break;
					}
					return false;
				}
			}); 스크롤이 안되서 봉인*/ 

			holder.lay_mm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					vMainActivity.fn_listOnClick(position);
				}
			});

			holder.btn_list_01.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if ( "0".equals(vMode) ) { // 내구독목록
						vMainActivity.fn_delMyList(getList(position, "ID_SEQ"));
					} else {
						vMainActivity.fn_insMyList(getList(position, "ID_SEQ"));
					}
				}
			});

			holder.btn_list_01.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					vMainActivity.onTouch(v, event);
					return false;
				}
			});
			return convertView;
		}

		public String getList(int pPosition, String pTag) {
			if ( getCount() <= pPosition && 0 > pPosition ) return "";
			if ( !array.get(pPosition).containsKey(pTag) ) return "";
			String vReturn = (String) array.get(pPosition).get(pTag);
			if ( vReturn == null || vReturn.trim().length() == 0 ) vReturn = "";
			return vReturn;
		}
	}
}