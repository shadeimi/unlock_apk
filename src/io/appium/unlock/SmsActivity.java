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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class SmsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		
		JSONArray json_list = new JSONArray();
	    List<Sms> l = getAllSms();
	    for (Iterator<Sms> iterator = l.iterator(); iterator.hasNext();) {
			Sms my_sms = (Sms) iterator.next();
			if ( my_sms.isInbox() ) {
				json_list.add(""+standardize_purchase_json(my_sms));
			}
		}
	    
	    
	    StringWriter out = new StringWriter();
	    try {
			json_list.writeJSONString(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    String jsonText = out.toString();
	    //Log.i("", "jsonText=" + jsonText);
	    
	    String fname = getFullPath() + "/" + getFileName();
	    WriteTXTFile(fname, jsonText, false);
	    //Log.i("", "wrote file " + fname);
	    System.out.println("wrote file " + fname);
	    
	    finish();
	}

	
	public String getFullPath () {
		return Environment.getExternalStorageDirectory().toString();
	}
	
	private String getFileName () {
		return getPackageName() + ".txt";
	}
	
	private static String standardize_purchase_json (Sms sms) {
		String buff = "";
		buff = "{\"originator\":\"" + sms.getAddress() + "\"," +
				"\"text\":\"" + sms.getMsg() + "\"," +
				"\"id\":\"" + sms.getId() + "\"," +
				"\"time\":" + sms.getTime() + "," +
				"\"readstate\":" + sms.getReadState() + "," +
				"\"keytest\":\"" + "true" + "\"" +
			    "}";
		return buff;
	}
	
	public List<Sms> getAllSms() {
	    List<Sms> lstSms = new ArrayList<Sms>();
	    Sms objSms = new Sms();
	    Uri message = Uri.parse("content://sms/");
	    ContentResolver cr = getContentResolver();

	    Cursor c = cr.query(message, null, null, null, null);
	    startManagingCursor(c);
	    int totalSMS = c.getCount();

	    if (c.moveToFirst()) {
	        for (int i = 0; i < totalSMS; i++) {

	            objSms = new Sms();
	            objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
	            objSms.setAddress(c.getString(c
	                    .getColumnIndexOrThrow("address")));
	            objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
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
