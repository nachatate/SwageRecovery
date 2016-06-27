package su.swage.recovery.tasks.restore;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import su.swage.recovery.MainActivity;
import su.swage.recovery.R;
import su.swage.recovery.RestoreActivity;

public class CallLogsImport extends AsyncTask<Void, Integer, Void> {
    private RestoreActivity restoreActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private JSONArray data;

    public CallLogsImport(RestoreActivity restoreActivity, JSONArray data) {
        this.restoreActivity = restoreActivity;
        this.data = data;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.pText = (TextView) this.restoreActivity.findViewById(R.id.progressText);
        this.pBar = (ProgressWheel) this.restoreActivity.findViewById(R.id.progressBar);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(2000);
            if (ActivityCompat.checkSelfPermission(restoreActivity, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    values.put(CallLog.Calls.NUMBER, row.getString("number"));
                    values.put(CallLog.Calls.DATE, row.getString("date"));
                    values.put(CallLog.Calls.DURATION, row.getString("duration"));
                    values.put(CallLog.Calls.TYPE, row.getString("type"));
                    restoreActivity.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
                    values.clear();
                    publishProgress((int) ((i + 1) * 100 / data.length()));
                }
                publishProgress(100);
            }
            Thread.sleep(4000);
        } catch (InterruptedException | JSONException e) {
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
        Intent intent = new Intent(restoreActivity, MainActivity.class);
        restoreActivity.startActivity(intent);
    }
}