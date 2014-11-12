package ar.edu.ips.aus.android;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.LruCache;

public class MirloApplication extends Application {

	private static final String TAG = MirloApplication.class.getSimpleName();
	private Twitter twitter;
	private SharedPreferences prefs;
	private LruCache<String, Bitmap> imageMemoryCache;
	private DBHelper dbHelper;

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// TODO register for prefs changes

		this.initTwitter(prefs.getString("OAuthConsumerKey", ""),
				prefs.getString("OAuthConsumerSecret", ""),
				prefs.getString("OAuthAccessToken", ""),
				prefs.getString("OAuthAccessTokenSecret", ""));

		this.initLruCache();

		dbHelper = new DBHelper(this);

		Log.d(TAG, "Creating app instance");
	}

	public Twitter getTwitter() {
		return this.twitter;
	}

	public LruCache<String, Bitmap> getImageMemoryCache() {
		return this.imageMemoryCache;
	}

	public DBHelper getDbHelper() {
		return this.dbHelper;
	}

	private void initTwitter(String oAuthConsumerKey,
			String oAuthConsumerSecret, String oAuthAccessToken,
			String oAuthAccessTokenSecret) {
		ConfigurationBuilder cb = new ConfigurationBuilder();

		cb.setOAuthConsumerKey(oAuthConsumerKey)
				.setOAuthConsumerSecret(oAuthConsumerSecret)
				.setOAuthAccessToken(oAuthAccessToken)
				.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);

		this.twitter = new TwitterFactory(cb.build()).getInstance();
	}

	private void initLruCache() {
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
	}

	class DBHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "tweets.db";
		private static final int VERSION = 1;
		public static final String TABLE_NAME = "tweet";
		public static final String ID = "_id";
		public static final String USER_NAME = "user_name";
		public static final String TWEET_TEXT = "tweet_text";
		public static final String IMAGE_PROFILE_URL = "image_profile_url";

		public DBHelper(Context context) {
			super(context, DB_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String createDbSql = "CREATE TABLE " + TABLE_NAME + " (" + ID
					+ " int primary key, " + USER_NAME + " text," + TWEET_TEXT
					+ " text, " + IMAGE_PROFILE_URL + " text )";
			db.execSQL(createDbSql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

	}

}
