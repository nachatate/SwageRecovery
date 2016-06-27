package su.swage.recovery.tasks.backup;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.FileInputStream;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.R;
import su.swage.recovery.tasks.FilesUtils;

public class ContactsExport extends AsyncTask<Void, Integer, Void> {
    private BackupActivity backupActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private FilesUtils filesUtils;

    public ContactsExport(BackupActivity backupActivity, FilesUtils filesUtils) {
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

            Cursor cursor = backupActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String strVCard = "";
                for (int i = 0; i < cursor.getCount(); i++) {
                    strVCard += getContact(cursor);
                    publishProgress((i + 1) * 100 / cursor.getCount());
                    cursor.moveToNext();
                }
                backupActivity.backupData[0] = strVCard;
                cursor.close();
                publishProgress(100);
            } else {
                backupActivity.backupData[0] = "";
                publishProgress(100);
            }

            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getContact(Cursor cursor) {
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        AssetFileDescriptor fd;
        try {
            fd = backupActivity.getContentResolver().openAssetFileDescriptor(uri, "r");

            FileInputStream fis;
            if (fd != null) {
                fis = fd.createInputStream();
                byte[] buf = new byte[(int) fd.getDeclaredLength()];
                fis.read(buf);
                return new String(buf);
            } else {
                throw new Exception("openAssetFileDescriptor is null!");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return "";
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
        backupActivity.nextTask(4);
    }
}