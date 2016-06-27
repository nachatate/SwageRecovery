package su.swage.recovery.tasks.backup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.R;
import su.swage.recovery.tasks.FilesUtils;
import su.swage.recovery.utils.InterfaceAPI;

public class CallLogsExport extends AsyncTask<Void, Integer, Void> implements InterfaceAPI {
    private BackupActivity backupActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private FilesUtils filesUtils;

    public CallLogsExport(BackupActivity backupActivity, FilesUtils filesUtils) {
        this.backupActivity = backupActivity;
        this.filesUtils = filesUtils;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.pText = (TextView) this.backupActivity.findViewById(R.id.progressText);
        this.pBar = (ProgressWheel) this.backupActivity.findViewById(R.id.progressBar);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(2000);
            if (ActivityCompat.checkSelfPermission(backupActivity, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                Cursor c = backupActivity.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
                JSONArray ja = new JSONArray();
                if (c != null && c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("number", c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)));
                            jo.put("type", c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));
                            jo.put("date", c.getString(c.getColumnIndex(CallLog.Calls.DATE)));
                            jo.put("duration", c.getString(c.getColumnIndex(CallLog.Calls.DURATION)));
                            ja.put(jo);
                            publishProgress((int) ((i + 1) * 100 / c.getCount()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        c.moveToNext();
                    }
                    publishProgress(100);
                } else {
                    publishProgress(100);
                }
                c.close();
                backupActivity.backupData[2] = ja.toString();
            }
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate();
        float del = progress[0] / 5f;
        if ((del - (int) del) == 0) {
            pBar.setProgress(progress[0] / 100f);
            String setText = progress[0] + "%";
            pText.setText(setText);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        backupActivity.nextTask(5);
    }

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        return null;
    }
}