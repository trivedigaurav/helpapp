package com.trivedigaurav.helpapp;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import watch.nudge.gesturelibrary.AbstractGestureClientActivity;
import watch.nudge.gesturelibrary.GestureConstants;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainWatchActivity extends AbstractGestureClientActivity
        implements DelayedConfirmationView.DelayedConfirmationListener, GoogleApiClient.OnConnectionFailedListener,
                GoogleApiClient.ConnectionCallbacks{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private TextView mClockView;
    Node mNode;

    private GoogleApiClient mGoogleApiClient;
    private DelayedConfirmationView delayedConfirmationView;

    private static final int NUM_SECONDS = 5;
    private boolean timerOn = false;

    private static final String TAG = "HELPApp";

    private static final String TIMER_SELECTED_PATH = "/timer_selected";
    private static final String TIMER_FINISHED_PATH = "/timer_finished";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_watch);
        mClockView = (TextView) findViewById(R.id.clock);

        mClockView.setVisibility(View.VISIBLE);
        mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));

        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.timer);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
    }

    //Subscribe to gestures.

    @Override
    public ArrayList<GestureConstants.SubscriptionGesture> getGestureSubscpitionList() {
        ArrayList<GestureConstants.SubscriptionGesture> gestures = new ArrayList<GestureConstants.SubscriptionGesture>();
        gestures.add(GestureConstants.SubscriptionGesture.FLICK);
        gestures.add(GestureConstants.SubscriptionGesture.SNAP);
        gestures.add(GestureConstants.SubscriptionGesture.TWIST);
        return gestures;
    }

    //State whether your app will automatically deliver gesture events to the phone.

    @Override
    public boolean sendsGestureToPhone() { return false; }

    @Override
    public void onSnap() {}

    @Override
    public void onFlick() {}

    @Override
    public void onTwist() {
        onStartTimer(null);
    }

    @Override
    public void onGestureWindowClosed() {
        Toast.makeText(this,"Gesture window closed.", Toast.LENGTH_LONG).show();
    }

    //These functions won't be called until you subscribe to the appropriate gestures.
    @Override
    public void onTiltX(float v) {
        throw new IllegalStateException("This function should not be called unless subscribed to TILT_X.");
    }

    @Override
    public void onTilt(float v, float v1, float v2) {
        throw new IllegalStateException("This function should not be called unless subscribed to TILT.");
    }

    public void onStartTimer(View view) {
        if(!timerOn) {

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] vibrationPattern = {0, 500, 50, 300};
            //-1 - don't repeat
            final int indexInPatternToRepeat = -1;
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            
            delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
            delayedConfirmationView.setListener(this);
            delayedConfirmationView.start();

            View cancel = findViewById(R.id.cancelView);
            cancel.setVisibility(View.VISIBLE);

            View call = findViewById(R.id.callButton);
            call.setVisibility(View.INVISIBLE);

            timerOn = true;
        }
    }

    public void showMessage(View view) {
//        Toast.makeText(this,"Button Pressed",Toast.LENGTH_LONG).show();
        onStartTimer(view);
    }

    @Override
    public void onTimerFinished(View v){
//        Toast.makeText(this,"onTimerFinished is called.",Toast.LENGTH_LONG).show();
        fixViews();
        delayedConfirmationView.reset();
        Toast.makeText(this, "Send to phone", Toast.LENGTH_SHORT).show();
        sendMessageToCompanion(TIMER_FINISHED_PATH);
    }

    @Override
    public void onTimerSelected(View v) {
//        Toast.makeText(this,"onTimerSelected is called.",Toast.LENGTH_LONG).show();
        fixViews();
        delayedConfirmationView.setListener(null);
        delayedConfirmationView.reset();
        sendMessageToCompanion(TIMER_SELECTED_PATH);
    }

    public void fixViews(){
        View cancel = findViewById(R.id.cancelView);
        cancel.setVisibility(View.INVISIBLE);

        View call = findViewById(R.id.callButton);
        call.setVisibility(View.VISIBLE);

        timerOn = false;
    }

    private void sendMessageToCompanion(final String path) {

        if (mNode != null && mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), path, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }else{
            Log.e(TAG, "sendMessageToCompanion() found no companion");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO
    }

}
