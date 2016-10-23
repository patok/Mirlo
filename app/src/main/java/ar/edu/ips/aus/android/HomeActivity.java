package ar.edu.ips.aus.android;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import twitter4j.TwitterException;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import ar.edu.ips.aus.android.MirloApplication.DBHelper;

public class HomeActivity extends Activity {

	private MirloApplication app;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		this.app = (MirloApplication) getApplication();
		db = app.getDbHelper().getReadableDatabase();
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		Cursor cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);
		startManagingCursor(cursor);
		String[] from = new String[]{DBHelper.TWEET_TEXT};
		int[] to = new int[]{R.id.text1};
		StatusAdapter adapter = new StatusAdapter(HomeActivity.this, R.layout.list_item_layout, cursor,
				from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(adapter);
	}

	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
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
		case R.id.menu_service_start:
			startService(new Intent(this, MirloService.class));
			return true;
		case R.id.menu_service_stop:
			stopService(new Intent(this, MirloService.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			app.getImageMemoryCache().put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromMemCache(String key) {
		return app.getImageMemoryCache().get(key);
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
			String profileImageUrl = cursor.getString(cursor.getColumnIndex(DBHelper.IMAGE_PROFILE_URL));
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

}
