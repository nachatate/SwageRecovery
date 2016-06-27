package su.swage.recovery.tasks.restore;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import su.swage.recovery.R;
import su.swage.recovery.RestoreActivity;

public class SMSImport extends AsyncTask<Void, Integer, Void> {
    private RestoreActivity restoreActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private JSONArray data;

    public SMSImport(RestoreActivity restoreActivity, JSONArray data) {
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
            for (int i = 0; i < data.length(); i++) {
                JSONObject row = data.getJSONObject(i);
                Uri uri = Uri.parse("content://sms/");
                ContentValues cv2 = new ContentValues();
                cv2.put("address", row.getString("address"));
                cv2.put("date", row.getString("date"));
                cv2.put("read", row.getInt("read"));
                cv2.put("type", row.getInt("type"));
                cv2.put("body", row.getString("body"));
                restoreActivity.getContentResolver().insert(uri, cv2);
                cv2.clear();
                publishProgress((int) ((i + 1) * 100 / data.length()));
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
        restoreActivity.startSMSActivity();
    }
}
