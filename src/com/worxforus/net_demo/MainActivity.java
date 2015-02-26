package com.worxforus.net_demo;


import java.io.IOException;
import java.util.List;



import jodd.jerry.Jerry;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import com.worxforus.Utils;
import com.worxforus.net.NetHandler;
import com.worxforus.net.NetResult;
import com.worxforus_net_demo.R;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void singleThreadWorxForUs(View v) {
		updateDisplay("Getting page...");
		WorxForUsAction action = new WorxForUsAction();
		action.execute(this, null, null);
		Toast toast = Toast.makeText(this, "Running WorxForUs net access - single thread", Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * This is the async task that does the work of making the network request.
	 * A task is used so that the main thread is not blocked while waiting a long time for the network to respond
	 * @author sbossen
	 *
	 */
	private class WorxForUsAction extends AsyncTask<Context, Void, Void> {
		String message = "";
		
		@Override
		protected Void doInBackground(Context... params) {
			//not passing params here
			String url = "http://www.righthandedmonkey.com/";
//			String url = "https://www.kickstarter.com/projects/1445624543/anne";
			List<NameValuePair> paramsToSend = null;
			NetResult netResult = NetHandler.handlePostWithRetry(url, paramsToSend, NetHandler.NETWORK_DEFAULT_RETRY_ATTEMPTS);
			//save the result and close network stream
	        String consume_str= "";
			try {
				consume_str = Utils.removeUTF8BOM(EntityUtils.toString(netResult.net_response_entity, Utils.CHARSET));
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        netResult.object = consume_str;
	        netResult.closeNetResult();
			this.message = consume_str;
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			updateDisplay("Retrieved item on page = "+parseData(this.message));
		}
	}
	
	/**
	 * Parse the HTML like you would using jQuery by use of the Jerry library (based on Jodd)
	 * @param data
	 * @return parsed data string
	 */
	public String parseData(String data) {
		//for documentation on Jerry see:
		//	http://jodd.org/doc/jerry/
		Jerry doc = Jerry.jerry(data);
		//return doc.$("#updates_nav").text();
		//select the HTML object that has the 'title' class
		return doc.$(".title").text();
	}
	
	/**
	 * This function copies the data from the database into the view on the activity.
	 * Note: this must be run from the UI thread.
	 */
	public void updateDisplay(String data) {
		TextView textView = (TextView) this.findViewById(R.id.textMultiLine);
		textView.setText(data);
	}
}
