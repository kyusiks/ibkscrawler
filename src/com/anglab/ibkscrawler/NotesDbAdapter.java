package com.anglab.ibkscrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

@SuppressLint("NewApi")
public class NotesDbAdapter {
   public static final String KEY_TITLE = "title";
   public static final String KEY_BODY = "body";
   public static final String KEY_ROWID = "_id";

   private DatabaseHelper mDbHelper;
   private SQLiteDatabase mDb;

   /**
    * Database creation sql statement
    * 
    * TB_LC000	���������
    * TB_LC001	�������
    * TB_LC002	����Ʈ����
    * TB_LC003	��ȭȸ��
    * TB_LC004	��������
    */
   private static final String DATABASE_CREATE = "create table notes (_id integer primary key autoincrement, "
                                               + "title text not null, body text not null);";
   private static final String gv_create_TB_LC000 /* ���������_ */ = "CREATE TABLE TB_LC000 (ID_SEQ TEXT PRIMARY KEY, LST_VIEW_NO TEXT, SORT TEXT);";
   private static final String gv_create_TB_LC001 /* ��������__ */ = "CREATE TABLE TB_LC001 (ID_SEQ TEXT PRIMARY KEY, CID TEXT, MAX_NO TEXT, SITE TEXT, NAME TEXT, COMP_YN TEXT, THUMB_NAIL TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";
   private static final String gv_create_TB_LC002 /* ����Ʈ����_ */ = "CREATE TABLE TB_LC002 (SITE TEXT PRIMARY KEY, NAME TEXT, SORT TEXT, IMG_VIEWER TEXT, THUMB_COMN TEXT, USE_YN TEXT, LST_UPD_DH TEXT, LOCAL_LST_UPD_DH TEXT);"; //TOON_CNT���� ks20141228. �������� ��⿣ �÷��� ���������������� �Ⱦ��� ����
   private static final String gv_create_TB_LC003 /* ����ȸ������ */ = "CREATE TABLE TB_LC003 (ID_SEQ TEXT NOT NULL, LINK_CODE TEXT NOT NULL, TITLE TEXT, THUMB_NAIL TEXT, SORT TEXT, USE_YN TEXT, LST_UPD_DH TEXT, PRIMARY KEY(ID_SEQ, LINK_CODE));";
   private static final String gv_create_TB_LC004 /* ��������___*/ = "CREATE TABLE TB_LC004 (SET_ID TEXT PRIMARY KEY, SET_NM TEXT NOT NULL, SEL_MODE TEXT NOT NULL, SET_VALUE TEXT, SET_CONT TEXT, SORT TEXT, USE_YN TEXT, LST_UPD_DH TEXT);";

   private static final String DATABASE_NAME = "data";
   private static final String DATABASE_TABLE = "notes";
   private static final int DATABASE_VERSION = 16;
   private final Context mCtx;

   private static class DatabaseHelper extends SQLiteOpenHelper {
       DatabaseHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL(DATABASE_CREATE);
           db.execSQL(gv_create_TB_LC000);
           db.execSQL(gv_create_TB_LC001);
           db.execSQL(gv_create_TB_LC002);
           db.execSQL(gv_create_TB_LC003);
           db.execSQL(gv_create_TB_LC004);
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w("NotesDbAdapter", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
           whenChgDb(db, "TB_LC002", new String[] {"LOCAL_LST_UPD_DH TEXT"});

           /*
           db.execSQL("DROP TABLE IF EXISTS notes");
           db.execSQL("DROP TABLE IF EXISTS TB_LC000");
           db.execSQL("DROP TABLE IF EXISTS TB_LC001");
           db.execSQL("DROP TABLE IF EXISTS TB_LC002");
           db.execSQL("DROP TABLE IF EXISTS TB_LC003");
           db.execSQL("DROP TABLE IF EXISTS TB_LC004");
           onCreate(db);*/ // ������ ������ ���ƺ�����..
       }
       public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w("NotesDbAdapter", "Downgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
           whenChgDb(db, "TB_LC002", new String[] {"LOCAL_LST_UPD_DH TEXT"});
       }

       public void whenChgDb(SQLiteDatabase db, String pTable, String[] pColumn) {
    	   Cursor result = db.rawQuery("SELECT COUNT(*) AS CNT FROM sqlite_master WHERE NAME = '" + pTable + "';", null); // ���̺��� �ִ°�
    	   result.moveToFirst();
    	   String vReturn = result.getString(0);
           result.close();

           if ( "0".equals(vReturn) ) { // ���̺� ��ü�� ������ �׳� create
               if ( "TB_LC000".equals(pTable) ) db.execSQL(gv_create_TB_LC000);
        	   else if ( "TB_LC001".equals(pTable) ) db.execSQL(gv_create_TB_LC001);
        	   else if ( "TB_LC002".equals(pTable) ) db.execSQL(gv_create_TB_LC002);
        	   else if ( "TB_LC003".equals(pTable) ) db.execSQL(gv_create_TB_LC003);
        	   else if ( "TB_LC004".equals(pTable) ) db.execSQL(gv_create_TB_LC004);
               return;
           }

           for ( int i = 0; i < pColumn.length; i++ ) {
        	   Cursor result1 = db.rawQuery("SELECT COUNT(*) AS CNT FROM sqlite_master WHERE NAME ='" + pTable + "' AND SQL LIKE '%" + pColumn[i]+ "%';", null); // ���̺��� �ִ°�
        	   result1.moveToFirst();
        	   String vReturn1 = result1.getString(0);
               result1.close();
               if ( "0".equals(vReturn1) ) {
                   db.execSQL("ALTER TABLE " + pTable + " ADD COLUMN " + pColumn[i] + "; ");
               }
           }
       }
   }

   public NotesDbAdapter(Context ctx) { this.mCtx = ctx; }

   public NotesDbAdapter open() throws SQLException {
       mDbHelper = new DatabaseHelper(mCtx);
       mDb = mDbHelper.getWritableDatabase();
       return this;
   }

   public void close() { mDbHelper.close(); }

   public long createNote(String title, String body) {
       ContentValues initialValues = new ContentValues();
       initialValues.put(KEY_TITLE, title);
       initialValues.put(KEY_BODY, body);
       return mDb.insert(DATABASE_TABLE, null, initialValues);
   }

   public boolean deleteNote(long rowId) {
       Log.i("Delete called", "value__" + rowId);
       return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
   }

   public Cursor fetchAllNotes() {
       return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_BODY }, null, null, null, null, null);
   }

   public Cursor fetchNote(long rowId) throws SQLException {
       Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TITLE, KEY_BODY }, KEY_ROWID
               + "=" + rowId, null, null, null, null, null);
       if (mCursor != null) {
           mCursor.moveToFirst();
       }
       return mCursor;
   }

   public boolean updateNote(long rowId, String title, String body) {
       ContentValues args = new ContentValues();
       args.put(KEY_TITLE, title);
       args.put(KEY_BODY, body);
       return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
   }

   public List<HashMap<String, String>> inqSql(String pMode, String pParam) {
	   String vTemp = "";
	   if ( pMode == null || "".equals(pMode) ) {
	   } else if ( "0".equals(pMode) ) { // ���������
		   vTemp = "SELECT A.ID_SEQ, B.CID, B.NAME, A.LST_VIEW_NO, B.SITE, A.SORT "
				 + "     , B.MAX_NO, B.LST_UPD_DH, B.THUMB_NAIL, B.COMP_YN"
		   		 + "  FROM TB_LC000 A INNER JOIN TB_LC001 B"
		         + " WHERE A.ID_SEQ = B.ID_SEQ AND B.USE_YN != 'N'";

		   if ( "Y".equals(((MainActivity) mCtx).getSetting("COMP_SORT_YN")) ) { // �ϰ�� �ڷ�
			   vTemp += " ORDER BY B.COMP_YN, B.NAME";
		   } else {
			   vTemp += " ORDER BY B.NAME"; // ��������� A.SORT, 
		   }

	   } else if ( "1".equals(pMode) ) { // ����Ʈ
		   vTemp = "SELECT LC002.SITE, LC002.NAME || '(' || COUNT(1) || ')' AS NAME " //|| (SELECT COUNT(1) FROM TB_LC001 WHERE SITE=LC002.SITE)
				 + "  FROM TB_LC002 LC002 LEFT OUTER JOIN TB_LC001 LC001 ON LC001.SITE=LC002.SITE AND LC001.USE_YN !='N' "
				 + " WHERE LC002.USE_YN != 'N' AND LC002.NAME != '' "
				 + " GROUP BY LC002.SITE, LC002.NAME "
				 + " ORDER BY LC002.SORT, LC002.NAME ";

	   } else if ( "2".equals(pMode) ) { // ����Ʈ�� ��������Ʈ
		   vTemp = "SELECT ID_SEQ, CID, MAX_NO, SITE, NAME, COMP_YN, THUMB_NAIL, LST_UPD_DH "
				 + "     , IFNULL((SELECT 'Y' FROM TB_LC000 WHERE ID_SEQ = A.ID_SEQ), 'N') AS MY_INQ_YN "
				 + "  FROM TB_LC001 A WHERE SITE = '" + pParam + "' AND A.USE_YN != 'N'";

		   if ( "Y".equals(((MainActivity) mCtx).getSetting("COMP_SORT_YN")) ) {
			   vTemp += " ORDER BY COMP_YN, NAME";
		   } else {
			   vTemp += " ORDER BY NAME"; // ��������� A.SORT, 
		   }

	   } else if ( "3".equals(pMode) ) { // ������ ȸ������Ʈ
		   vTemp = "SELECT LINK_CODE, TITLE AS NAME, THUMB_NAIL, SORT, LST_UPD_DH "
				 + "  FROM TB_LC003 WHERE ID_SEQ = '" + pParam + "'"
				 + "   AND USE_YN != 'N' AND TITLE != ''"
				 + "  ORDER BY CASE LINK_CODE WHEN LINK_CODE + 0 THEN LINK_CODE + 0 ELSE LINK_CODE END DESC ";
		   // LINK_CODE �� �����϶��� LINK_CODE + 0 ������ �Ǿ����� ����(tstore)�������� �������� �߰�.
		   
	   } else if ( "4".equals(pMode) ) { // �˻� - �˻���ߵ�
		   vTemp = "SELECT ID_SEQ, CID, MAX_NO, SITE, NAME, COMP_YN, LST_UPD_DH, THUMB_NAIL"
				 + "     , IFNULL((SELECT 'Y' FROM TB_LC000 WHERE ID_SEQ = A.ID_SEQ), 'N') AS MY_INQ_YN "
	             +  " FROM TB_LC001 A"
				 + " WHERE NAME LIKE '%" + pParam + "%' AND USE_YN != 'N' AND A.NAME != '' ";

		   if ( "Y".equals(((MainActivity) mCtx).getSetting("COMP_SORT_YN")) ) {
			   vTemp += " ORDER BY COMP_YN, NAME";
		   } else {
			   vTemp += " ORDER BY NAME"; // ��������� A.SORT, 
		   }

	   } else if ( "5".equals(pMode) ) { // ����
		   vTemp = "SELECT SET_ID, SET_NM AS NAME, SEL_MODE, SET_VALUE, SET_CONT, SORT, LST_UPD_DH"
				 + "  FROM TB_LC004 WHERE USE_YN != 'N' AND SET_NM != '' ORDER BY SORT"; // ���� �� �⺻��������

	   } else if ( "6".equals(pMode) ) { // ��õ���� ks20141116 pParam�� UNION �� ������ �´�. ID_SEQ,SORT
		   vTemp = "SELECT A.ID_SEQ, CID, MAX_NO, SITE, NAME, COMP_YN, LST_UPD_DH, THUMB_NAIL"
				 + "     , IFNULL((SELECT 'Y' FROM TB_LC000 WHERE ID_SEQ = A.ID_SEQ), 'N') AS MY_INQ_YN "
	             + "  FROM TB_LC001 A, ( " + pParam + " SELECT '' AS ID_SEQ, 99999 AS SORT ) B"
				 + " WHERE A.ID_SEQ = B.ID_SEQ AND USE_YN != 'N' AND A.NAME != '' ORDER BY B.SORT";

	   } else if ( "TB_LC000_SORT_MAX".equals(pMode) ) {
		   vTemp = "SELECT MAX(IFNULL(SORT,'0')) + 1 AS SORT_MAX, MIN(IFNULL(SORT,'0')) - 1 AS SORT_MIN FROM TB_LC000"; // ��Ʈ

	   } else if ( "TB_LC002".equals(pMode) ) {
		   vTemp = "SELECT SITE, IMG_VIEWER, THUMB_COMN FROM TB_LC002 WHERE USE_YN != 'N'"; // �⺻���� �ε�

	   } else if ( "TB_LC004".equals(pMode) ) {
		   vTemp = "SELECT SET_ID, SET_NM, SEL_MODE, SET_VALUE, SET_CONT, SORT, LST_UPD_DH "
				 + "  FROM TB_LC004 WHERE USE_YN != 'N' "; // ���� �� �⺻��������
		   
		   
		   
	   } else if ( "S_2".equals(pMode) ) { // ����Ʈ�� ��������Ʈ
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC002 WHERE USE_YN != 'N'";
	   } else if ( "S_3".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '0') AS COL1 FROM TB_LC003 WHERE USE_YN != 'N' AND ID_SEQ = '" + pParam + "'";
	   } else if ( "S_4".equals(pMode) ) { // �˻� - �˻���ߵ�
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC004 WHERE USE_YN != 'N'";
	   } else if ( "S_V0".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(MAX(B.LST_UPD_DH), '-1') AS COL1 FROM TB_LC000 A INNER JOIN TB_LC001 B WHERE A.ID_SEQ = B.ID_SEQ AND B.USE_YN != 'N'";
	   } else if ( "S_V1".equals(pMode) ) {
		   vTemp = "SELECT IFNULL(LOCAL_LST_UPD_DH, '-1') AS COL1 FROM TB_LC002 WHERE SITE = '" + pParam + "'"; // ����Ʈ�� ������Ʈ�Ͻ�
	   } else if ( "S_1-1".equals(pMode) ) { // �ű������� �б�����
		   vTemp = "SELECT IFNULL(MAX(LST_UPD_DH), '-1') AS COL1 FROM TB_LC001";
	   } else if ( "S_fn_isInMyList".equals(pMode) ) {  // CID�� ����������Ʈ ���� ���� �Ǵ�
		   vTemp = "SELECT COUNT(ID_SEQ) AS COL1 FROM TB_LC000 WHERE ID_SEQ = '" + pParam + "'"; // ���������
   	   } else {
   		   return null;
	   }

	   Cursor result = mDb.rawQuery(vTemp, null);

	   List<HashMap<String, String>> vList = new ArrayList<>();
       if ( result.getCount() > 0 ) {
	       result.moveToFirst(); // ����Ʈ�� Ű�� �̹������, ����� �ּҰ� ����ȴ�.
	       HashMap<String, String> data = new HashMap<String, String>();
	       while ( !result.isAfterLast() ) {
				data.clear();
				for ( int i = 0; i < result.getColumnCount(); i++ ) {
					if ( "NAME".equals(result.getColumnName(i))  // Ư������ ��ġ
					  && result.getString(i).indexOf("&") >= 0 ) {
						data.put(result.getColumnName(i), result.getString(i).replace("&#039;", "'").replace("&lt;", "<").replace("&gt;", ">"));
					} else {
						data.put(result.getColumnName(i), result.getString(i));	
					}
				}
				vList.add(new HashMap<String, String>(data));
				result.moveToNext();
	       }
       }
       result.close();
	   return vList;
   }

   // ��������Ʈ ���� �� ������Ʈ
   public void updList(String pMode, List<HashMap<String, String>> pList) {
	   Log.d("updList", "mode : " + pMode + " / " + pList.size() + " row update");
	   if ( pList.isEmpty() ) return;

	   String vSql = "";
	   if ( "1".equals(pMode) ) {
		   //vSql = "INSERT OR REPLACE INTO TB_LC001 (ID_SEQ, MAX_NO, COMP_YN, THUMB_NAIL, LST_UPD_DH) VALUES (?, ?, ?, ?, ?)";
		   vSql = "UPDATE TB_LC001 SET MAX_NO = ?, THUMB_NAIL = ?, LST_UPD_DH = ? WHERE ID_SEQ = ?";
	   } else if ( "1-1".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC001 (ID_SEQ, CID, SITE, NAME, THUMB_NAIL, MAX_NO, COMP_YN, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "2".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC002 (SITE, NAME, SORT, IMG_VIEWER, THUMB_COMN, USE_YN, LST_UPD_DH, LOCAL_LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "3".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC003 (ID_SEQ, LINK_CODE, TITLE, THUMB_NAIL, SORT, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "4".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC004 (SET_ID, SET_NM, SEL_MODE, SET_VALUE, SET_CONT, SORT, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "V2".equals(pMode) ) {
		   vSql = "INSERT OR REPLACE INTO TB_LC001 (ID_SEQ, CID, SITE, NAME, THUMB_NAIL, MAX_NO, COMP_YN, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	   } else if ( "V1".equals(pMode) ) {
		   vSql = "UPDATE TB_LC001 SET MAX_NO = ?, THUMB_NAIL = ?, LST_UPD_DH = ? WHERE ID_SEQ = ?";
	   } else if ( "V0".equals(pMode) ) {
		   vSql = "UPDATE TB_LC001 SET MAX_NO = ?, THUMB_NAIL = ?, LST_UPD_DH = ? WHERE ID_SEQ = ?";
	   }

	   mDb.beginTransaction();
	   SQLiteStatement insert = mDb.compileStatement(vSql);

	   if ( "V2".equals(pMode) ) {
		   String vLstUpdDhMax = "";
		   for ( int i = 0; i < pList.size(); i++ ) {
			   if ( vLstUpdDhMax.compareTo(fn_getList(pList.get(i), "LST_UPD_DH")) < 0 ) vLstUpdDhMax = fn_getList(pList.get(i), "LST_UPD_DH");
			   insert.bindString(1, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(2, fn_getList(pList.get(i), "CID"));
			   insert.bindString(3, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(4, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(5, fn_getList(pList.get(i), "THUMB_NAIL"));
			   insert.bindString(6, fn_getList(pList.get(i), "MAX_NO"));
			   insert.bindString(7, fn_getList(pList.get(i), "COMP_YN"));
			   insert.bindString(8, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(9, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
		   if ( !"".equals(vLstUpdDhMax) ) ((MainActivity) mCtx).updSettingValue("FST_INS_DH", vLstUpdDhMax);

	   } else if ( "V1".equals(pMode) ) {
		   // �� ���� �ΰ��� ������ ������ �ʿ��ؼ�
		   SQLiteStatement insert1 = mDb.compileStatement("INSERT OR REPLACE INTO TB_LC001 (ID_SEQ, CID, SITE, NAME, THUMB_NAIL, MAX_NO, COMP_YN, USE_YN, LST_UPD_DH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		   String vLstUpdDhMax = "";
		   String vSite = MainActivity.gv_setView.get("SITE");

		   for ( int i = 0; i < pList.size(); i++ ) {
			   if ( vLstUpdDhMax.compareTo(fn_getList(pList.get(i), "LST_UPD_DH")) < 0 ) vLstUpdDhMax = fn_getList(pList.get(i), "LST_UPD_DH");
			   if ( "".equals(fn_getList(pList.get(i), "CID")) ) { // CID�� ���°� ������Ʈ��
				   insert.bindString(1, fn_getList(pList.get(i), "MAX_NO"));
				   insert.bindString(2, fn_getList(pList.get(i), "THUMB_NAIL"));
				   insert.bindString(3, fn_getList(pList.get(i), "LST_UPD_DH"));
				   insert.bindString(4, fn_getList(pList.get(i), "ID_SEQ"));
				   insert.execute();
			   } else { // CID�� �ִٸ� �߰�/�����
				   insert1.bindString(1, fn_getList(pList.get(i), "ID_SEQ"));
				   insert1.bindString(2, fn_getList(pList.get(i), "CID"));
				   insert1.bindString(3, vSite);
				   insert1.bindString(4, fn_getList(pList.get(i), "NAME"));
				   insert1.bindString(5, fn_getList(pList.get(i), "THUMB_NAIL"));
				   insert1.bindString(6, fn_getList(pList.get(i), "MAX_NO"));
				   insert1.bindString(7, fn_getList(pList.get(i), "COMP_YN"));
				   insert1.bindString(8, fn_getList(pList.get(i), "USE_YN"));
				   insert1.bindString(9, fn_getList(pList.get(i), "LST_UPD_DH"));
				   insert1.execute();
			   }
		   }

		   if ( !"".equals(vLstUpdDhMax) ) { // ���� ������Ʈ �Ͻ� ����. ����Ʈ��
			   SQLiteStatement insert2 = mDb.compileStatement("UPDATE TB_LC002 SET LOCAL_LST_UPD_DH = ? WHERE SITE = ?");
			   insert2.bindString(1, vLstUpdDhMax);
			   insert2.bindString(2, vSite);
			   insert2.execute();
		   }

	   } else if ( "V0".equals(pMode) ) { // ����Ʈ�� ������Ʈ
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "MAX_NO"));
			   insert.bindString(2, fn_getList(pList.get(i), "THUMB_NAIL"));
			   insert.bindString(3, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.bindString(4, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.execute();
		   }
	   }

	   if ( "1".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "MAX_NO"));
			   insert.bindString(2, fn_getList(pList.get(i), "THUMB_NAIL"));
			   insert.bindString(3, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.bindString(4, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.execute();
		   }
	   } else if ( "1-1".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(2, fn_getList(pList.get(i), "CID"));
			   insert.bindString(3, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(4, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(5, fn_getList(pList.get(i), "THUMB_NAIL"));
			   insert.bindString(6, fn_getList(pList.get(i), "MAX_NO"));
			   insert.bindString(7, fn_getList(pList.get(i), "COMP_YN"));
			   insert.bindString(8, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(9, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
	   } else if ( "2".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "SITE"));
			   insert.bindString(2, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(3, fn_getList(pList.get(i), "SORT"));
			   insert.bindString(4, fn_getList(pList.get(i), "IMG_VIEWER"));
			   insert.bindString(5, fn_getList(pList.get(i), "THUMB_COMN"));
			   insert.bindString(6, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(7, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.bindString(8, fn_getList(pList.get(i), "LOCAL_LST_UPD_DH")); // ks20141113 �߰�
			   insert.execute();
		   }
	   } else if ( "3".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "ID_SEQ"));
			   insert.bindString(2, fn_getList(pList.get(i), "LINK_CODE"));
			   insert.bindString(3, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(4, fn_getList(pList.get(i), "THUMB_NAIL"));
			   insert.bindString(5, fn_getList(pList.get(i), "SORT"));
			   insert.bindString(6, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(7, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
	   } else if ( "4".equals(pMode) ) {
		   for ( int i = 0; i < pList.size(); i++ ) {
			   insert.bindString(1, fn_getList(pList.get(i), "SET_ID"));
			   insert.bindString(2, fn_getList(pList.get(i), "NAME"));
			   insert.bindString(3, fn_getList(pList.get(i), "SEL_MODE"));
			   insert.bindString(4, fn_getList(pList.get(i), "SET_VALUE"));
			   insert.bindString(5, fn_getList(pList.get(i), "SET_CONT"));
			   insert.bindString(6, fn_getList(pList.get(i), "SORT"));
			   insert.bindString(7, fn_getList(pList.get(i), "USE_YN"));
			   insert.bindString(8, fn_getList(pList.get(i), "LST_UPD_DH"));
			   insert.execute();
		   }
	   }
	   mDb.setTransactionSuccessful();
	   mDb.endTransaction();
   }

   public String fn_getList(HashMap<String, String> map, String pTagname) {
	   if ( map.containsKey(pTagname) ) {
		   return (String) map.get(pTagname);
	   } else {
		   return "";
	   }
   }

   /**** �������� ****/
   public void fn_dbClear() {
	   // �ʱ�ȭ����
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC001"); mDb.execSQL(gv_create_TB_LC001);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC002"); mDb.execSQL(gv_create_TB_LC002);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC003"); mDb.execSQL(gv_create_TB_LC003);
	   mDb.execSQL("DROP TABLE IF EXISTS TB_LC004"); mDb.execSQL(gv_create_TB_LC004);
   }

   public void updSettingValue(String pSetId, String pSetValue) {
       ContentValues initialValues = new ContentValues();
       initialValues.put("SET_VALUE", pSetValue);
       int i = mDb.update("TB_LC004", initialValues, "SET_ID = '" + pSetId + "'", null);
       if ( i == 0 ) {
           initialValues.put("SET_ID", pSetId);
           initialValues.put("SET_NM", "");
           initialValues.put("SEL_MODE", "");
           initialValues.put("SET_CONT", "");
           initialValues.put("SORT", "");
           initialValues.put("USE_YN", "");
           initialValues.put("LST_UPD_DH", "");
           initialValues.put("SET_NM", "");

    	   mDb.insert("TB_LC004", null, initialValues);
       }
   }
   /********/

   /**** ��������� ���� ****/
   public void insLC000(String pIdSeq, String pSort, String pLstViewNo) {
       ContentValues initialValues = new ContentValues();
       initialValues.put("ID_SEQ", pIdSeq);
       initialValues.put("LST_VIEW_NO", pLstViewNo); // ó�� ������ ������ -1�� ����. ������������ ���߿� ����
       initialValues.put("SORT", pSort);
       mDb.insert("TB_LC000", null, initialValues);
   }

   public boolean updLstViewNo(String pIdSeq, String pLstViewNo) {
       ContentValues args = new ContentValues();
       args.put("LST_VIEW_NO", pLstViewNo);
       return mDb.update("TB_LC000", args, "ID_SEQ='" + pIdSeq + "'", null) > 0;
   }

   public boolean delLC000(String pIdSeq) {
       return mDb.delete("TB_LC000", "ID_SEQ='" + pIdSeq + "'", null) > 0;
   }

   public boolean delLC000All() {
       return mDb.delete("TB_LC000", "1=1", null) > 0;
   }
   /********/
}