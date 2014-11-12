package ar.edu.ips.aus.android;

import java.util.List;

import twitter4j.TwitterException;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import ar.edu.ips.aus.android.MirloApplication.DBHelper;

public class MirloService extends Service {

	private String TAG = MirloService.class.getSimpleName();
	private UpdateTweets updateTask;
	private boolean running = false;

	@Override
	public IBinder onBind(Intent arg0) {
		// just return null since we are not binding to service
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG , "Mirlo Service created");
		
		this.running  = true;
		this.updateTask = new UpdateTweets();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		updateTask.interrupt();
		this.running = false;
		updateTask = null;
		
		Log.d(TAG , "Mirlo Service destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG , "Mirlo Service started");

		updateTask.start();
		
		return START_STICKY;
	}

	private class UpdateTweets extends Thread {

		private static final long DELAY = 60000;

		public UpdateTweets() {
			super("MirloService - UpdateTweets");
		}

		@Override
		public void run() {
			while (MirloService.this.running) {
				SQLiteDatabase db = null;
				try {
					MirloApplication app = (MirloApplication) getApplication();
					List<twitter4j.Status> result = app.getTwitter().getHomeTimeline();
					db = app.getDbHelper().getWritableDatabase();
					ContentValues values = new ContentValues();
					for (twitter4j.Status status : result) {
						values.put(DBHelper.ID, status.getId());
						values.put(DBHelper.USER_NAME, status.getUser().getName());
						values.put(DBHelper.TWEET_TEXT, status.getText());
						values.put(DBHelper.IMAGE_PROFILE_URL, status.getUser().getMiniProfileImageURL());
						try {
							db.insertOrThrow(DBHelper.TABLE_NAME, null, values);
						} catch (SQLException e) {
							// log and do nothing else
							Log.d(TAG, "An sql error happened", e);
						}
					}
					Log.d(TAG, "Updated tweets from service");
				} catch (TwitterException e) {
					Log.e(TAG, "Error trying to retrieve tweets");
				}
				
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					MirloService.this.running = false;
					Log.d(TAG, "interrupted...");
				}
			}
		}

	}
}


