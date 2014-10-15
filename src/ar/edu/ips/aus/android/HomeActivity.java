package ar.edu.ips.aus.android;

import java.util.LinkedList;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

	class RetrieveTweets extends AsyncTask<Void, Void, List<Status>> {

		private static final String TAG = "TwitterTests";

		@Override
		protected List<twitter4j.Status> doInBackground(Void... params) {
			try {
				return twitterTest.getHomeTimeLine();
			} catch (TwitterException e) {
				Log.e(TAG, e.getErrorMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> result) {
			super.onPostExecute(result);

			List<String> data = new LinkedList<String>();
			for (twitter4j.Status status : result) {
				data.add(status.getUser().getScreenName() + " :: "
						+ status.getText());
			}

			ListView listView = (ListView) findViewById(R.id.listView1);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					HomeActivity.this, R.layout.list_item_layout, data);

			listView.setAdapter(adapter);
		}

	}

}
