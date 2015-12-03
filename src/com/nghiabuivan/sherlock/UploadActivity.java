package com.nghiabuivan.sherlock;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.nghiabuivan.sherlock.db.DaoPerson;
import com.nghiabuivan.sherlock.db.SherlockDbHelper;

public class UploadActivity extends Activity {
	
	private static final String POST_KEY = "jsonpayload";
	private static final String USER_ID = "GT60538";
	
	private static final String CODE_ERROR = "ERROR";
	
	private String m_json;
	private String m_url;
	private boolean m_isUploading;
	
	private int m_numberOfPeople = 0;
	
	private EditText m_editTextUrl;
	private TextView m_textViewStatus;
	private Button m_buttonStart;
	private ProgressBar m_progressBar;
	private Button m_buttonClose;
	
	private UploadTask m_task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		setFinishOnTouchOutside(false);
		
		//
		m_editTextUrl = (EditText) findViewById(R.id.editTextUrl);
		m_textViewStatus = (TextView) findViewById(R.id.textViewUploadStatus);
		m_buttonStart = (Button) findViewById(R.id.buttonStartUpload);
		m_buttonClose = (Button) findViewById(R.id.buttonCloseUpload);
		m_progressBar = (ProgressBar) findViewById(R.id.progressBarUpload);
		
		//
		m_progressBar.setVisibility(View.GONE);
		m_textViewStatus.setVisibility(View.GONE);
		
		m_buttonStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				handleButtonStart();
			}
		});
		
		m_buttonClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				handleButtonClose();
			}
		});
		
		//
		m_isUploading = false;
		
		//
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			m_numberOfPeople = extras.getInt(Utils.KEY_PEOPLE_NUMBER);
		} else {
			m_numberOfPeople = 0;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.log("Upload onDestroy");
		if (m_isUploading) {
			m_task.cancel(true);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        if (m_isUploading)
	        	return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	private void handleButtonStart() {
		String url = m_editTextUrl.getText().toString().trim();
		if (url.length() == 0) {
			m_textViewStatus.setVisibility(View.VISIBLE);
			m_textViewStatus.setText(R.string.please_enter_url);
			return;
		}
		
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}
		
		if (m_numberOfPeople == 0) {
			m_textViewStatus.setVisibility(View.VISIBLE);
			m_textViewStatus.setText(R.string.cannot_upload);
			return;
		}
		
		//
		m_progressBar.setVisibility(View.VISIBLE);
		m_textViewStatus.setVisibility(View.VISIBLE);
		m_buttonStart.setVisibility(View.GONE);
		
		m_buttonClose.setText(R.string.cancel);
		m_textViewStatus.setText(getString(R.string.upload_status, m_numberOfPeople));
		
		//
		m_url = url;
			
		m_task = new UploadTask();
		m_task.execute();
		
		m_isUploading = true;
	}
	
	private void handleButtonClose() {
		if (m_isUploading) {
			m_task.cancel(true);
			
			m_progressBar.setVisibility(View.GONE);
			m_textViewStatus.setVisibility(View.GONE);
			m_buttonStart.setVisibility(View.VISIBLE);
			m_buttonClose.setText(getString(R.string.close));
			
			m_isUploading = false;
		} else {
			Utils.hideKeyboard(this, m_editTextUrl);
			finish();
		}
	}
	
	private void onFinishUpload(String code, String responseFromServer) {
		String message = "[" + code + "] " + responseFromServer;
		m_textViewStatus.setText(message);
		
		m_progressBar.setVisibility(View.GONE);
		m_buttonStart.setVisibility(View.VISIBLE);
		m_buttonClose.setText(R.string.close);
		
		m_isUploading = false;
	}
	
	private class UploadTask extends AsyncTask<Void, String, Void> {
		
		private String m_code;
		private String m_msg;
		
		private static final String UTF8 = "utf-8";

		@Override
		protected Void doInBackground(Void... params) {
			//
			SherlockDbHelper.getInstance().openReadDb();
			List<DaoPerson> personList = SherlockDbHelper.getInstance().getListPersonAll();
			SherlockDbHelper.getInstance().closeDb();
			
			//
			{
				JsonRequestObj jsonObj = new JsonRequestObj();
				jsonObj.personList = personList;
				
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				m_json = gson.toJson(jsonObj);
				
				Utils.log("JSON length to send: " + m_json.length());
				Utils.log("JSON content to send:");
				Utils.log(m_json);
			}
			
			//
			m_code = null;
			m_msg = null;
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(m_url);
			try {
				List<NameValuePair> list = new ArrayList<NameValuePair>(1);
				list.add(new BasicNameValuePair(POST_KEY, m_json));
				
				UrlEncodedFormEntity encoder = new UrlEncodedFormEntity(list, UTF8);
				
				post.setEntity(encoder);
				
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					m_code = CODE_ERROR;
					m_msg = getString(R.string.sth_wrong);
				} else {
					String resString = EntityUtils.toString(entity, UTF8);
					Utils.log("Got response from server:");
					Utils.log(resString);
					
					Gson gson = new Gson();
					try {
						JsonResponseObj obj = gson.fromJson(resString, JsonResponseObj.class);
						m_code = obj.uploadResponseCode;
						m_msg = obj.message;
					} catch (JsonSyntaxException ex) {
						m_code = CODE_ERROR;
						m_msg = getString(R.string.server_response_not_valid);
					}
				}
			} catch (Exception ex) {
				m_code = CODE_ERROR;
				m_msg = ex.getMessage();
			}
			
			publishProgress("hehe");
			
			return null;
		}
		
		protected void onProgressUpdate(String... p) {
			
		}
		
		protected void onPostExecute(Void result) {
			onFinishUpload(m_code, m_msg);
		}
		
	}
	
	private class JsonRequestObj {
		@Expose
		public String userId = USER_ID;
		
		@Expose
		public List<DaoPerson> personList;
	}
	
	private class JsonResponseObj {
		public String uploadResponseCode;
		public String userId;
		public int numberOfPeople;
		public String namesOfPeople;
		public String message;
	}

}
