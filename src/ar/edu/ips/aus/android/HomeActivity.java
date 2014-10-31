package ar.edu.ips.aus.android;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ar.edu.ips.aus.android.TestTwitter4jLib.TwitterTest;

public class HomeActivity extends Activity {

	private TwitterTest twitterTest;
	private LruCache<String, Bitmap> imageMemoryCache;
	private DBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		dbHelper = new DBHelper(this);

		twitterTest = new TestTwitter4jLib.TwitterTest();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		twitterTest.init(prefs.getString("OAuthConsumerKey", ""),
				prefs.getString("OAuthConsumerSecret", ""),
				prefs.getString("OAuthAccessToken", ""),
				prefs.getString("OAuthAccessTokenSecret", ""));

		//
		// LruCache sample code from
		// http://developer.android.com/intl/es/training/displaying-bitmaps/cache-bitmap.html
		//
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;

		imageMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

		new RetrieveTweets().execute();

	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			imageMemoryCache.put(key, bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key) {
		return imageMemoryCache.get(key);
	}

	class RetrieveTweets extends AsyncTask<Void, Void, List<Status>> {

		private static final String TAG = "TwitterTests";

		@Override
		protected List<twitter4j.Status> doInBackground(Void... params) {
			try {
				return twitterTest.getHomeTimeLine();
				// TODO insert tweets data into db
			} catch (TwitterException e) {
				Log.e(TAG, e.getErrorMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<twitter4j.Status> result) {
			super.onPostExecute(result);

			ListView listView = (ListView) findViewById(R.id.listView1);

			SQLiteDatabase db = dbHelper.getReadableDatabase();
			db.close();

			// TODO use CursorAdapter/Cursor to read status data from db and into the listView 
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
				reusableView = getLayoutInflater().inflate(R.layout.list_item_layout, null);
			}
			Status status = data.get(position);

			TextView textView = (TextView) (reusableView.findViewById(R.id.text1));
			textView.setText(status.getUser().getScreenName() + " :: " + status.getText());

			ImageView imageView = (ImageView) reusableView.findViewById(R.id.imageView1);
			final Bitmap bitmap = getBitmapFromMemCache(status.getUser().getMiniProfileImageURL());
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
				task.execute(status.getUser().getMiniProfileImageURL());
			}
			return reusableView;
		}

	}

	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			String bitmapUrl = urls[0];
			try {
				URL url = new URL(bitmapUrl);
				Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				addBitmapToMemoryCache(bitmapUrl, bitmap);
				return bitmap;
			} catch (IOException e) {
				Log.e("Bitmap Downloader", "Error trying to download image from " + bitmapUrl);
				return null;
			}
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	class DBHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "tweets.db";
		private static final int VERSION = 1;
		public static final String TABLE_NAME = "tweet";
		public static final String ID = "_id";
		public static final String USER_NAME = "user_name";
		public static final String TWEET_TEXT = "tweet_text";
		private static final String IMAGE_PROFILE_URL = "image_profile_url";

		public DBHelper(Context context) {
			super(context, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String createDbSql = "CREATE TABLE " + TABLE_NAME + " (" 
					+ ID + " int primary key, " + USER_NAME + " text," 
					+ TWEET_TEXT + " text, " + IMAGE_PROFILE_URL + " text )";
			db.execSQL(createDbSql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

	}
}
