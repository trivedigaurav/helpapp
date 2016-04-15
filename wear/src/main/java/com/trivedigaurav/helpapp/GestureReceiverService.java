package com.trivedigaurav.helpapp;

import watch.nudge.gesturelibrary.AppControllerReceiverService;

/*
* GestureReceiverService
*
* Service which launches the wear app when the mobile app is launched.
 */

public class GestureReceiverService extends AppControllerReceiverService {
    @Override
    protected Class getWatchActivityClass() {
        return MainWatchActivity.class;
    }
}
