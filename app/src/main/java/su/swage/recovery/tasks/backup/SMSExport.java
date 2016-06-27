package su.swage.recovery.tasks.backup;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.R;
import su.swage.recovery.tasks.FilesUtils;

public class SMSExport extends AsyncTask<Void, Integer, Void> {
    private BackupActivity backupActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private FilesUtils filesUtils;

    public SMSExport(BackupActivity backupActivity, FilesUtils filesUtils) {
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
            Uri message = Uri.parse("content://sms/");
            Cursor c = backupActivity.getContentResolver().query(message, null, null, null, null);
            JSONArray ja = new JSONArray();
            if (c != null && c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("address", c.getString(c.getColumnIndexOrThrow("address")));
                        jo.put("body", "" + c.getString(c.getColumnIndexOrThrow("body")));
                        jo.put("read", c.getInt(c.getColumnIndex("read")));
                        jo.put("date", c.getString(c.getColumnIndexOrThrow("date")));
                        jo.put("type", c.getInt(c.getColumnIndexOrThrow("type")));
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
            backupActivity.backupData[1] = ja.toString();
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
        backupActivity.nextTask(3);
    }
}