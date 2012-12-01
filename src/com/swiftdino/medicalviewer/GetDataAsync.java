package com.swiftdino.medicalviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class GetDataAsync extends AsyncTask<Integer, Integer, String> {
	private String TAG = "GetDataAsync";

	@Override
	protected String doInBackground(Integer... params) {
		Log.d(TAG, "doing in background");
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		int patientID = params[0];
		int fromIndex = params[1];
		int toIndex = params[2];
		HttpGet httpGet = new HttpGet("http://128.61.123.28:8080/SDesign/ecg/"
				+ patientID + "/" + fromIndex + "/" + toIndex + "");
		try {
			HttpResponse response = client.execute(httpGet);
			Log.d(TAG, "HTTP Response:");
			StatusLine statusLine = response.getStatusLine();
			Log.d(TAG, "Status Line");
			int statusCode = statusLine.getStatusCode();
			Log.d(TAG, "Status Code:" + statusCode);
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(GetDataAsync.class.toString(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();

	}

	protected void onProgressUpdate(Integer... progress) {
		// setProgressPercent(progress[0]);
	}

	protected void onPostExecute(String results) {
		if (results != null) {
			// EditText et = (EditText) findViewById(R.id.my_edit);
			// ///et.setText(results);
		}
		// Button b = (Button) findViewById(R.id.my_button);
		// b.setClickable(true);
	}

}
