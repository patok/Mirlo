package ar.edu.ips.aus.android;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

public class MirloApplication extends Application {

	private static final String TAG = MirloApplication.class.getSimpleName();
	private static final String CONSUMER_KEY = "RYur1bWtcK187eci5c5X13dNe";
	private static final String CONSUMER_SECRET = "1uMhV4ap3xJ7gj0mF7eFrQIxbo2uqHKtNv37lOrXjLDZPWzOhN";
    private static final String PREFS_FILENAME = "oauth";
    private static final String OAUTH_USER_TOKEN = "oauth_user_token";
    private static final String OAUTH_USER_TOKEN_SECRET = "oauth_user_token_secret";

    private Twitter twitter;
    private RequestToken oauthRequestToken = null;
    private AccessToken userAccessToken = null;

	private SharedPreferences prefs;

	private LruCache<String, Bitmap> imageMemoryCache;
	private DBHelper dbHelper;

    @Override
	public void onCreate() {
		super.onCreate();

		prefs = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE);

        initAndCheckTwitter();
//        restoreUserAccessToken();

        this.initLruCache();

		dbHelper = new DBHelper(this);

		Log.d(TAG, "Creating app instance");
	}

    public Twitter getTwitter() {
        return this.twitter;
    }

    public String getAuthUrl() {
        if (initAndCheckTwitter())
            return oauthRequestToken.getAuthorizationURL();
        else
            return null;
    }

    public void retrieveUserAccessToken(String pin) {
        new RetrieveTwitterAccessTokenTask().execute(pin);
    }

    public LruCache<String, Bitmap> getImageMemoryCache() {
		return this.imageMemoryCache;
	}

	public DBHelper getDbHelper() {
		return this.dbHelper;
	}

    private void restoreUserAccessToken() {
        String oAuthAccessToken = prefs.getString(OAUTH_USER_TOKEN, null);
        String oAuthAccessTokenSecret = prefs.getString(OAUTH_USER_TOKEN_SECRET, null);
        setUserAccessToken(oAuthAccessToken, oAuthAccessTokenSecret);
    }

    private void setUserAccessToken(String oAuthAccessToken, String oAuthAccessTokenSecret) {
        if (oAuthAccessToken != null && oAuthAccessTokenSecret != null) {
            this.userAccessToken = new AccessToken(oAuthAccessToken, oAuthAccessTokenSecret);
            this.twitter.setOAuthAccessToken(userAccessToken);
        }
    }

    private boolean initAndCheckTwitter() {
        if (twitter == null) {
            this.twitter = TwitterFactory.getSingleton();
            this.twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        }
        if (this.userAccessToken == null
                && this.oauthRequestToken == null) {
                new RetrieveTwitterRequestTokenTask().execute();
        }
        return twitter != null && oauthRequestToken != null;
    }

    private class RetrieveTwitterRequestTokenTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                oauthRequestToken = twitter.getOAuthRequestToken();
                restoreUserAccessToken();
            } catch (TwitterException e) {
                oauthRequestToken = null;
                Log.e(TAG, "Error when trying to retrieve Twitter request token!.");
            }
            return null;
        }
    }

    private void saveTwitterUserAccessToken(String oAuthAccessToken,
                                            String oAuthAccessTokenSecret) {
        setUserAccessToken(oAuthAccessToken, oAuthAccessTokenSecret);
        storeTwitterUserAccessToken();
    }

    private void storeTwitterUserAccessToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(OAUTH_USER_TOKEN, userAccessToken.getToken());
        editor.putString(OAUTH_USER_TOKEN_SECRET, userAccessToken.getTokenSecret());
        editor.commit();
    }

    private RequestToken getOAuthRequestToken() {
        return this.oauthRequestToken;
    }

    private class RetrieveTwitterAccessTokenTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... pins) {
            String thePin = pins[0];
            try {
                if (initAndCheckTwitter()){
                    RequestToken requestToken = MirloApplication.this.getOAuthRequestToken();
                    userAccessToken = twitter.getOAuthAccessToken(requestToken, thePin);
                    saveTwitterUserAccessToken(userAccessToken.getToken(), userAccessToken.getTokenSecret());
                }
            } catch (TwitterException e) {
                Log.e(MirloApplication.TAG, "Error retrieving twitter access token!");
            }
            return null;
        }
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
