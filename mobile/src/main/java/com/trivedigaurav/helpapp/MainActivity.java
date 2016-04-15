package com.trivedigaurav.helpapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import watch.nudge.phonegesturelibrary.AbstractPhoneGestureActivity;

public class MainActivity extends AbstractPhoneGestureActivity implements MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "HelpAppMobile";
    private static final String TIMER_SELECTED_PATH = "/timer_selected";
    private static final String TIMER_FINISHED_PATH = "/timer_finished";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

//Override these functions to make your app respond to gestures.

    @Override
    public void onSnap() { }

    @Override
    public void onFlick() { }

    @Override
    public void onTwist() {
        Toast.makeText(this,"Received twist",Toast.LENGTH_LONG).show();
    }

//These functions won't be called until you subscribe to the appropriate gestures
//in a class that extends AbstractGestureClientActivity in a wear app.

    @Override
    public void onTiltX(float x) {
//        throw new IllegalStateException("This function should not be called unless subscribed to TILT_X.");
    }

    @Override
    public void onTilt(float x, float y, float z) {
//        throw new IllegalStateException("This function should not be called unless subscribed to TILT.");
    }

    @Override
    public void onWindowClosed() {
        Log.e("MainActivity","This function should not be called unless windowed gesture detection is enabled.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equals(TIMER_SELECTED_PATH)) {
                    Toast.makeText(getApplicationContext(), "Call cancelled",
                            Toast.LENGTH_SHORT).show();
                } else if (messageEvent.getPath().equals(TIMER_FINISHED_PATH)) {
                    Toast.makeText(getApplicationContext(), "Make call now",
                            Toast.LENGTH_SHORT).show();
                    //        Runnable r = new Runnable() {
                    //            @Override
                    //            public void run(){
                    //                SmsManager smsManager = SmsManager.getDefault();
                    //                smsManager.sendTextMessage("<number>", null, "HELP App: <name> needs you to give them a call ASAP!", null, null);
                    //            }
                    //        };

                    //        Handler h = new Handler();
                    //        h.postDelayed(r, 5000);
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client with error code "
                + connectionResult.getErrorCode());
    }
}
