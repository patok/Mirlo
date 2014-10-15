package ar.edu.ips.aus.android;

import android.app.Activity;
import android.os.Bundle;
import ar.edu.ips.android.R;
import ar.edu.ips.aus.android.TestTwitter4jLib.TwitterTest;

public class HomeActivity extends Activity {

	private TwitterTest twitterTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
	}
	

}
