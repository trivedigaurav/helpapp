package com.trivedigaurav.helpapp.DefaultActivities;

import watch.nudge.gesturelibrary.AppControllerReceiverService;

/**
 * Created by davidjay on 8/7/15.
 */
public class DefaultWindowedReceiverService extends AppControllerReceiverService {
    @Override
    protected Class getWatchActivityClass() {
        return DefaultWindowedGestureActivity.class;
    }
}
