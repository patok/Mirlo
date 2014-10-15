package ar.edu.ips.aus.android;

import twitter4j.TwitterException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import ar.edu.ips.android.R;
import ar.edu.ips.aus.android.TestTwitter4jLib.TwitterTest;

public class HomeActivity extends Activity {

	private TwitterTest twitterTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		twitterTest = new TestTwitter4jLib.TwitterTest();
		twitterTest.init();
		
		new RetrieveTweets().execute();
	}
	
	class RetrieveTweets extends AsyncTask<Void, Void, String> {

		private static final String TAG = "TwitterTests";

		@Override
		protected String doInBackground(Void... params) {
	        try {
				return twitterTest.buildHomeTimelineOutput();
			} catch (TwitterException e) {
				Log.e(TAG, e.getErrorMessage());
				return e.getErrorMessage();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
	        TextView textView = (TextView) findViewById(R.id.homeText);
	        textView.setText(result);
		}

	}

}
