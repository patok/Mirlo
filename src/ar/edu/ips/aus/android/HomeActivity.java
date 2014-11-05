package ar.edu.ips.aus.android;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			// calling PrefsActivity explicitly!
			Intent prefsIntent = new Intent(this, PrefsActivity.class);
			startActivity(prefsIntent);
			return true;
		case R.id.menu_refresh:
			// refresh data
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			imageMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return imageMemoryCache.get(key);
	}

	class RetrieveTweets extends AsyncTask<Void, Void, Void> {

		private static final String TAG = "TwitterTests";

		@Override
		protected Void doInBackground(Void... params) {
			try {
				List<twitter4j.Status> result = twitterTest.getHomeTimeLine();
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				for (twitter4j.Status status : result) {
					values.put(dbHelper.ID, status.getId());
					values.put(dbHelper.USER_NAME, status.getUser().getName());
					values.put(dbHelper.TWEET_TEXT, status.getText());
					values.put(dbHelper.IMAGE_PROFILE_URL, status.getUser().getMiniProfileImageURL());
					db.insert(dbHelper.TABLE_NAME, null, values);
				}
				db.close();
			} catch (TwitterException e) {
				Log.e(TAG, e.getErrorMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ListView listView = (ListView) findViewById(R.id.listView1);

			SQLiteDatabase db = dbHelper.getReadableDatabase();
			
			Cursor cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);
			startManagingCursor(cursor);
			String[] from = new String[]{DBHelper.TWEET_TEXT};
			int[] to = new int[]{R.id.text1};
			StatusAdapter adapter = new StatusAdapter(HomeActivity.this, R.layout.list_item_layout, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			listView.setAdapter(adapter);
		}

	}

	class StatusAdapter extends SimpleCursorAdapter implements ListAdapter {

		private Cursor cursor;

		public StatusAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags) {
			super(context, layout, cursor, from, to, flags);
			this.cursor = cursor;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			
			TextView textView = (TextView) (view.findViewById(R.id.text1));
			textView.setText(cursor.getString(cursor.getColumnIndex(DBHelper.USER_NAME))
					+ " :: " + cursor.getString(cursor.getColumnIndex(DBHelper.TWEET_TEXT)));

			ImageView imageView = (ImageView) view.findViewById(R.id.imageView1);
			String profileImageUrl = cursor.getString(cursor.getColumnIndex(dbHelper.IMAGE_PROFILE_URL));
			final Bitmap bitmap = getBitmapFromMemCache(profileImageUrl);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
				task.execute(profileImageUrl);
			}
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
