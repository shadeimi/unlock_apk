package io.appium.unlock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.*;



import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SmsActivity extends Activity {
	
	private static String TAG = "";
	private static String VERBOSE_MODE = "verbose";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		JSONArray json_list = getAllSmsJsonArr(this);
	    //Log.i("", "jsonText=" + convert2String(json_list));
	    
	    String fname = getFullPath() + "/" + getFileName();
	    WriteTXTFile(fname, convert2String(json_list), false);
	    Log.i(TAG, "wrote file " + fname);
	    //System.out.println("wrote file " + fname);
	    
	    if ( (getMyUri() != null) && (getMyUri().getSchemeSpecificPart() != null) && 
	    		getMyUri().getSchemeSpecificPart().contains(VERBOSE_MODE) ) {
	    	Toast.makeText(this, getClass().getSimpleName() + " success!\n" + fname, Toast.LENGTH_SHORT).show();
	    }
	    
	    finish();
	}
	
	/* while testing with selenium chromedriver, this is the right way to invoke
	 * the functionality provided by this activity.
	 * 
	public static void getSMS (WebDriver driver){
  		String script = "var a=document.createElement("a");a.href='intent://ok/#Intent;scheme=io.appium.unlock.querysms;package=io.appium.unlock;end';document.body.appendChild(a);a.click();";
  		((JavascriptExecutor) driver).executeScript(script);
	}
	*/

	
	private Uri getMyUri () {
		final Intent intent = getIntent();
		if (intent == null) {
			Log.w(TAG, "Intent is null");
			return null;
		}
		
		Uri myURI=intent.getData();
		return myURI;
	}
	
	public String getFullPath () {
		return Environment.getExternalStorageDirectory().toString();
	}
	
	private String getFileName () {
		return getPackageName() + ".txt";
	}
	
	private static String standardize_purchase_json (Sms sms) {
		String buff = "{\"originator\":\"" + sms.getAddress() + "\"," +
				"\"text\":\"" + sms.getMsg() + "\"," +
				"\"id\":\"" + sms.getId() + "\"," +
				"\"time\":" + sms.getTime() + "," +
				"\"readstate\":" + sms.getReadState() +
			    "}";
		return buff;
	}
	
	public static String convert2String (JSONArray json_list) {
		StringWriter out = new StringWriter();
	    try {
			json_list.writeJSONString(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    String jsonText = out.toString();
	    return jsonText;
	}
	
	public static JSONArray getAllSmsJsonArr (Context context) {
		JSONArray json_list = new JSONArray();
	    List<Sms> l = getAllSms(context);
	    for (Iterator<Sms> iterator = l.iterator(); iterator.hasNext();) {
			Sms my_sms = (Sms) iterator.next();
			if ( my_sms.isInbox() ) {
				json_list.add(""+standardize_purchase_json(my_sms));
			}
		}
	    return json_list;
	}
	
	private static List<Sms> getAllSms (Context context) {
	    List<Sms> lstSms = new ArrayList<Sms>();
	    Sms objSms = new Sms();
	    Uri message = Uri.parse("content://sms/");
	    ContentResolver cr = context.getContentResolver();

	    Cursor c = cr.query(message, null, null, null, null);
	    //context.startManagingCursor(c);
	    int totalSMS = c.getCount();

	    if (c.moveToFirst()) {
	        for (int i = 0; i < totalSMS; i++) {
	            objSms = new Sms();
	            objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
	            objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
	            
	            String text_message = c.getString(c.getColumnIndexOrThrow("body"));
	            text_message = JSONObject.escape(text_message);
	            objSms.setMsg(text_message);
	            objSms.setReadState(c.getString(c.getColumnIndex("read")));
	            objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
	            if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
	                objSms.setFolderName("inbox");
	            } else {
	                objSms.setFolderName("sent");
	            }

	            lstSms.add(objSms);
	            c.moveToNext();
	        }
	    }
	    // else {
	    // throw new RuntimeException("You have no SMS");
	    // }
	    c.close();

	    return lstSms;
	}
	
	
	private boolean WriteTXTFile (String fname, String text, Boolean AppendMode) {
    	boolean ret = false;
    	if (fname.length()<1) {
    		//DebugTools.E(getClass().getSimpleName() + "::WriteTXTFile:: ERROR >>> invalid filename: " + fname);
    		return ret;
    	}
    	if (text.length()<1) {
    		//DebugTools.E(getClass().getSimpleName() + "::WriteTXTFile:: ERROR >>> not text to save! [" + text + "]");
    		return ret;
    	}
    	try {
	        FileWriter fstream = new FileWriter(fname, AppendMode);  // Create file 
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(text);  //Close the output stream
            out.close();
            ret = true;
        }catch (Exception e){//Catch exception if any
          e.printStackTrace();
          ret = false;
        }
        return ret;
    }
}
