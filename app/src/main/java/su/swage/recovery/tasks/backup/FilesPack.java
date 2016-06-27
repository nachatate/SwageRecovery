package su.swage.recovery.tasks.backup;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import su.swage.recovery.BackupActivity;
import su.swage.recovery.R;
import su.swage.recovery.tasks.FilesUtils;

public class FilesPack extends AsyncTask<Void, Integer, Void> {
    private BackupActivity backupActivity;
    private TextView pText;
    private ProgressWheel pBar;
    private FilesUtils filesUtils;
    private ArrayList<File> filesArray = new ArrayList<>();
    private String[] excludeNames = {"_Swage", "Screenshot", ".apk", "opensignalmaps", "Halfbrick", "baidu", "TempImages", "ScreenRecordings", "CM_Backup", "games", "Android", "temp", "SportsCamera", "Files.zip", ".bak", ".0", ".clean", ".1"};

    public FilesPack(BackupActivity backupActivity, FilesUtils filesUtils) {
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
            Thread.sleep(3000);

            ZipFile zipFile = new ZipFile(filesUtils.userFilesZip);
            zipFile.setRunInThread(true);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            parameters.setPassword("1234");

            addFolderToZip(Environment.getExternalStorageDirectory());

            zipFile.addFiles(filesArray, parameters);

            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                publishProgress(progressMonitor.getPercentDone());
            }
            if (progressMonitor.getResult() == ProgressMonitor.RESULT_ERROR) {
                if (progressMonitor.getException() != null) {
                    progressMonitor.getException().printStackTrace();
                } else {
                    System.err.println("An error occurred without any exception");
                }
            }

            Thread.sleep(5000);
        } catch (InterruptedException | IOException | ZipException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addFolderToZip(File folder) throws IOException {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isHidden()) continue;
            if (contains(file.getName(), excludeNames)) continue;
            if (file.isDirectory()) {
                addFolderToZip(file);
            } else {
                if (!file.getName().contains(".")) continue;
                System.out.println(file.getName());
                filesArray.add(file);
            }
        }
    }

    private boolean contains(String find, String[] in) {
        for (String f : in) {
            if (f.contains(find)) return true;
        }
        return false;
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
        //backupActivity.nextTask(4);
    }
}