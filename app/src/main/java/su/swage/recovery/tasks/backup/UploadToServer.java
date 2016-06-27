package su.swage.recovery.tasks.backup;

import android.os.AsyncTask;

import java.util.Hashtable;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.utils.CompressString;
import su.swage.recovery.utils.RequestAPI;
import su.swage.recovery.utils.SessionManager;

public class UploadToServer extends AsyncTask<Void, Integer, Hashtable<String, String>> {
    private BackupActivity backupActivity;

    public UploadToServer(BackupActivity backupActivity) {
        this.backupActivity = backupActivity;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Hashtable<String, String> doInBackground(Void... params) {
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("token", SessionManager.ApiKey);
        hashtable.put("smslist", CompressString.encodeString(backupActivity.backupData[1], "somekey"));
        hashtable.put("calls", CompressString.encodeString(backupActivity.backupData[2], "somekey"));
        hashtable.put("contacts", CompressString.encodeString(backupActivity.backupData[0], "somekey"));
        hashtable.put("isAuto", "0");
        return hashtable;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(Hashtable<String, String> result) {
        super.onPostExecute(result);
        RequestAPI rAPI = new RequestAPI(backupActivity, backupActivity);
        rAPI.setMethod("/archive/save");
        rAPI.execute(result);
    }
}