package ar.edu.ips.aus.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MirloService extends Service {

	private String TAG = MirloService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent arg0) {
		// just return null since we are not binding to service
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG , "Mirlo Service created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG , "Mirlo Service destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG , "Mirlo Service started");

		return START_STICKY;
	}

}
