package ar.edu.ips.aus.android;

import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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

			ListView listView = (ListView) findViewById(R.id.listView1);
			StatusAdapter adapter = new StatusAdapter(result);

			listView.setAdapter(adapter);
		}

	}

	class StatusAdapter extends BaseAdapter implements ListAdapter {

		private final List<Status> data;

		public StatusAdapter(List<Status> data) {
			this.data = data;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return data.get(position).getId();
		}

		@Override
		public View getView(int position, View reusableView,
				ViewGroup parentView) {
			if (reusableView == null) {
				reusableView = getLayoutInflater().inflate(
						R.layout.list_item_layout, null);
			}
			TextView textView = (TextView) reusableView;
			textView.setText(data.get(position).getText());
			return textView;
		}

	}

}
