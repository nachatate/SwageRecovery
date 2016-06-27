package su.swage.recovery;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONObject;

import su.swage.recovery.tasks.CloseApps;
import su.swage.recovery.tasks.FilesUtils;
import su.swage.recovery.tasks.backup.CallLogsExport;
import su.swage.recovery.tasks.backup.ContactsExport;
import su.swage.recovery.tasks.backup.SMSExport;
import su.swage.recovery.tasks.backup.UploadToServer;
import su.swage.recovery.utils.AlertDialogs;
import su.swage.recovery.utils.DesignUtils;
import su.swage.recovery.utils.InterfaceAPI;

public class BackupActivity extends Activity implements InterfaceAPI {
    public String[] backupData = new String[3];
    private FilesUtils fileUtils;
    private ProgressWheel progressBar = null;
    private TextView progressText = null;
    private LinearLayout layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        progressBar = (ProgressWheel) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.progressText);
        layout = (LinearLayout) findViewById(R.id.backup_act);

        progressBar.setLinearProgress(false);
        progressBar.setSpinSpeed(0.25f);
        progressBar.spin();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        KillAppsDialog();
                    }
                }, 3000
        );
    }

    public void nextTask(Integer taskid) {
        progressBar.spin();
        progressText.setText(getString(R.string.progress_spin_text));
        if (taskid == 2) {
            this.fileUtils = new FilesUtils(this);
            if (fileUtils.isCorrect()) {
                DesignUtils.ChangeBackgroundColor("#16A085", "#FFFFFF", layout, progressBar);
                DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.smsexport_text));
                DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.smsexport_title));

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                new SMSExport(BackupActivity.this, fileUtils).execute();
                            }
                        },
                        1500
                );
            }
        }
        if (taskid == 3) {
            DesignUtils.ChangeBackgroundColor("#3498DB", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.contactsexport_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.contactsexport_title));
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            new ContactsExport(BackupActivity.this, fileUtils).execute();
                        }
                    }, 1500
            );
        }
        if (taskid == 4) {
            DesignUtils.ChangeBackgroundColor("#9B59B6", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.calllogsexport_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.calllogsexport_title));
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            new CallLogsExport(BackupActivity.this, fileUtils).execute();
                        }
                    }, 1500
            );
        }
        if (taskid == 5) {
            DesignUtils.ChangeBackgroundColor("#9B59B6", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.uploadserver_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.uploadserver_title));
            progressText.setText(getString(R.string.progress_uploading_text));
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            new UploadToServer(BackupActivity.this).execute();
                        }
                    }, 1500
            );
        }
        /*if(taskid == 5){
            if(fileUtils.isReadyBackupFiles()) {
                ChangeBackgroundColor("#E67E22", "#FFFFFF");
                ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.calllogsexport_text));
                ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.calllogsexport_title));
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                new FilesPack(BackupActivity.this, fileUtils).execute();
                            }
                        }, 3000
                );
            }
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == 15) {
            nextTask(2);
        }
    }

    protected void KillAppsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.dialog_killapps_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new CloseApps(BackupActivity.this).execute();
            }
        });
        builder.setNegativeButton(R.string.dialog_killapps_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setMessage(R.string.dialog_killapps_text).setTitle(R.string.dialog_killapps_head);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        progressBar.setProgress(1f);
        progressText.setText(R.string.progress_uploaded_text);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Intent intent = new Intent(BackupActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }, 2500
        );
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        new AlertDialogs(this).NetworkError();
        Intent intent = new Intent(BackupActivity.this, MainActivity.class);
        startActivity(intent);
        return null;
    }
}