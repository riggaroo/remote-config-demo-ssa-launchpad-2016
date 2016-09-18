package za.co.riggaroo.remoteconfigdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long CACHE_TIME_SECONDS = 0;
    private static final String EXPERIMENT_A = "Variant_A";

    private static final String EXPERIMENT_B = "Variant_B";

    private FirebaseRemoteConfig remoteConfig;

    private TextView greetingTextView, videoEnabledTextView, inSouthAfricaTextView, apiTokenTextView;
    private Button buttonCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        greetingTextView = (TextView) findViewById(R.id.textViewHello);
        inSouthAfricaTextView = (TextView) findViewById(R.id.textViewInSouthAfrica);
        videoEnabledTextView = (TextView) findViewById(R.id.textViewVideoPlaybackEnabled);
        apiTokenTextView = (TextView) findViewById(R.id.textViewApiToken);
        buttonCheckout = (Button) findViewById(R.id.buttonCheckout);

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        remoteConfig.setConfigSettings(configSettings);
        remoteConfig.setDefaults(R.xml.firebase_config_defaults);

        remoteConfig.fetch(CACHE_TIME_SECONDS).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Fetch Succeeded");
                    remoteConfig.activateFetched();
                } else {
                    Log.d(TAG, "Fetch Failed");
                }
                displayWelcomeMessage();
                displayExperiment();
            }


        });
        Log.d(TAG, "Last fetch status:" + remoteConfig.getInfo()
                .getLastFetchStatus() + ". Fetch Time millis:" + remoteConfig.getInfo().getFetchTimeMillis());

        buttonCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.d(TAG, "checkout clicked");
                FirebaseAnalytics.getInstance(getApplicationContext())
                        .logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, new Bundle());
            }
        });
    }

    private void displayWelcomeMessage() {
        String helloMessage = remoteConfig.getString("welcome_message");
        greetingTextView.setText(helloMessage);
    }

    private void displayExperiment() {
        String experiment = remoteConfig.getString("experiment_variant");
        Log.d(TAG, "Running experiment: " + experiment);
        FirebaseAnalytics.getInstance(getApplicationContext()).setUserProperty("Experiment", experiment);
        buttonCheckout.setVisibility(View.VISIBLE);
        if (experiment.equals(EXPERIMENT_A)) {
            buttonCheckout.setText(getString(R.string.checkout));
            buttonCheckout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else if (experiment.equals(EXPERIMENT_B)) {
            buttonCheckout.setText(getString(R.string.cart));
            buttonCheckout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            buttonCheckout.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            buttonCheckout.setText(getString(R.string.finished));

        }
        stagedRollout();
    }

    private void stagedRollout() {
        boolean featureVideoPlaybackSupported = remoteConfig.getBoolean("feature_add_friends");
        buttonCheckout.setVisibility(featureVideoPlaybackSupported ? View.VISIBLE : View.GONE);

    }
   /* private void displayIsAppSupported() {
        boolean featureVideoPlaybackSupported = remoteConfig.getBoolean("feature_videoplayback_enabled");
        boolean inSouthAfrica = remoteConfig.getBoolean("in_south_africa");
        String helloMessage = remoteConfig.getString("welcome_message");
        String token = remoteConfig.getString("fake_api_token");

        greetingTextView.setText(helloMessage);
        videoEnabledTextView.setText(getString(R.string.video_enabled, featureVideoPlaybackSupported));
        inSouthAfricaTextView.setText(getString(R.string.in_south_africa, inSouthAfrica));
        apiTokenTextView.setText(getString(R.string.api_token, token));
    }

    private void loadAppSupported() {
        boolean appSupported = remoteConfig.getBoolean("app_supported");
        Log.d(TAG, "Version supported:" + BuildConfig.VERSION_NAME + " - " + appSupported);

    }*/
}
