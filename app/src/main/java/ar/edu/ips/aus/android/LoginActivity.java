package ar.edu.ips.aus.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PIN_LENGTH = 7;
    private EditText pintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MirloApplication app = (MirloApplication) getApplication();

        TextView urlView = (TextView) findViewById(R.id.authURL);
        final String authUrl = app.getAuthUrl();
        urlView.setText(authUrl);

        Button launchButton = (Button) findViewById(R.id.auth_launch_button);
        launchButton.setEnabled(true);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent authIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
                startActivity(authIntent);
            }
        });

        final Button pinSaveButton = (Button) findViewById(R.id.pin_button);
        pinSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pinView = (EditText) findViewById(R.id.pin);
                MirloApplication app = (MirloApplication) getApplication();
                String pin = pinView.getText().toString();
                app.retrieveUserAccessToken(pin);
            }
        });

        pintText = (EditText) findViewById(R.id.pin);
        pintText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // intentionally left blank
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (pintText.getText().length() >= PIN_LENGTH) {
                    pinSaveButton.setEnabled(true);
                }
            }
        });


   }
}

