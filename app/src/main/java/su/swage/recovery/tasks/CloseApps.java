package su.swage.recovery.tasks;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.List;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.R;

public class CloseApps extends AsyncTask<Void, Integer, Void> {
    private BackupActivity backupActivity;
    private PackageManager pManager;
    private TextView pText;
    private ProgressWheel pBar;

    public CloseApps(BackupActivity backupActivity) {
        this.pManager = backupActivity.getPackageManager();
        this.backupActivity = backupActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.pText = (TextView) backupActivity.findViewById(R.id.progressText);
        this.pBar = (ProgressWheel) backupActivity.findViewById(R.id.progressBar);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Thread.sleep(1000);

            List<ApplicationInfo> packages = pManager.getInstalledApplications(0);
            ActivityManager mActivityManager = (ActivityManager) backupActivity.getSystemService(Context.ACTIVITY_SERVICE);

            for (int i = 0; i < packages.size(); i++) {
                ApplicationInfo packageInfo = packages.get(i);
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
                if (packageInfo.packageName.equals(backupActivity.getPackageName())) continue;
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);

                publishProgress((i + 1) * 100 / packages.size());
                Thread.sleep(50);
            }
            publishProgress(100);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate();
        pBar.setProgress(progress[0] / 100f);
        String setText = progress[0] + "%";
        pText.setText(setText);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        backupActivity.nextTask(2);
    }
}