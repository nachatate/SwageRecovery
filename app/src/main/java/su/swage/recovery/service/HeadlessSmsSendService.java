package su.swage.recovery.service;

import android.app.IntentService;
import android.content.Intent;

public class HeadlessSmsSendService extends IntentService {
    public HeadlessSmsSendService() {
        super("HeadlessSmsSendService");
    }

    protected void onHandleIntent(Intent intent) {
    }
}
