package su.swage.recovery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Hashtable;

import su.swage.recovery.tasks.FilesUtils;
import su.swage.recovery.tasks.restore.CallLogsImport;
import su.swage.recovery.tasks.restore.SMSImport;
import su.swage.recovery.utils.AlertDialogs;
import su.swage.recovery.utils.CompressString;
import su.swage.recovery.utils.DesignUtils;
import su.swage.recovery.utils.InterfaceAPI;
import su.swage.recovery.utils.RequestAPI;
import su.swage.recovery.utils.SessionManager;

public class RestoreActivity extends Activity implements InterfaceAPI {
    private final static int CONTACTS = 0;
    private final static int SMSLIST = 1;
    private final static int CALLLOGS = 2;
    private static String defaultSmsApp;
    private String[] restoreData = new String[3];
    private String bundleID;
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
        bundleID = getIntent().getStringExtra("id");

        progressBar.setLinearProgress(false);
        progressBar.setSpinSpeed(0.25f);
        progressBar.spin();

        nextTask(1);
    }

    public void nextTask(Integer taskid) {
        progressBar.spin();
        progressText.setText(getString(R.string.progress_spin_text));
        if (taskid == 1) {
            DesignUtils.ChangeBackgroundColor("#9B59B6", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.downloadserver_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.downloadserver_title));
            progressText.setText(getString(R.string.progress_uploading_text));
            Hashtable<String, String> hashtable = new Hashtable<>();
            hashtable.put("token", SessionManager.ApiKey);
            hashtable.put("id", bundleID);

            RequestAPI rAPI = new RequestAPI(this, this);
            rAPI.setMethod("/archive/restore");
            rAPI.execute(hashtable);
        }
        if (taskid == 2) {
            this.fileUtils = new FilesUtils(this);
            if (fileUtils.isCorrect()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !getPackageName().contains(Telephony.Sms.getDefaultSmsPackage(this))) {
                    defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                    startActivityForResult(intent, 15);
                } else {
                    DesignUtils.ChangeBackgroundColor("#16A085", "#FFFFFF", layout, progressBar);
                    DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.smsimport_text));
                    DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.smsimport_title));
                    new Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    try {
                                        new SMSImport(RestoreActivity.this, new JSONArray(RestoreActivity.this.restoreData[SMSLIST])).execute();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1500
                    );
                }
            }
        }
        if (taskid == 3) {
            DesignUtils.ChangeBackgroundColor("#3498DB", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.contactsimport_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.contactsimport_title));
            fileUtils.writeText("contacts.vcf", restoreData[CONTACTS]);
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            final File file = new File(fileUtils.getPath("contacts.vcf"));
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), MimeTypeMap.getSingleton().getMimeTypeFromExtension("vcf"));
                            RestoreActivity.this.startActivityForResult(i, 17);
                        }
                    }, 1500
            );
        }
        if (taskid == 4) {
            DesignUtils.ChangeBackgroundColor("#9B59B6", "#FFFFFF", layout, progressBar);
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_text), getString(R.string.calllogsimport_text));
            DesignUtils.ChangeText((TextView) findViewById(R.id.backup_head), getString(R.string.calllogsimport_title));
            new Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            try {
                                new CallLogsImport(RestoreActivity.this, new JSONArray(RestoreActivity.this.restoreData[CALLLOGS])).execute();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1500
            );
        }
    }

    public void startSMSActivity() {
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
            startActivityForResult(intent, 16);
        } else nextTask(3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 15) nextTask(2);
        if (requestCode == 16) nextTask(3);
        if (requestCode == 17) nextTask(4);
    }

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        progressBar.setProgress(1f);
        progressText.setText(R.string.progress_uploaded_text);

        try {
            restoreData[CONTACTS] = CompressString.decodeString(result.getJSONArray("data").getJSONObject(0).getString("contacts"), "somekey");
            restoreData[SMSLIST] = CompressString.decodeString(result.getJSONArray("data").getJSONObject(0).getString("smslist"), "somekey");
            restoreData[CALLLOGS] = CompressString.decodeString(result.getJSONArray("data").getJSONObject(0).getString("calls"), "somekey");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nextTask(2);
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        new AlertDialogs(this).NetworkError();
        Intent intent = new Intent(RestoreActivity.this, MainActivity.class);
        startActivity(intent);
        return null;
    }
}