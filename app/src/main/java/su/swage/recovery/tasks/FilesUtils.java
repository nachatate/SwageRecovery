package su.swage.recovery.tasks;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FilesUtils {
    private static final String TEMP_DIR = "_Swage";
    public String userFilesZip = "false";
    private String currentDir = "false";
    private Context mContext;

    public FilesUtils(Context mContext) {
        this.mContext = mContext;
        this.currentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + TEMP_DIR;
        File f = new File(this.currentDir);
        if (!f.isDirectory()) {
            f.mkdir();
        }
    }

    public boolean isReadyBackupFiles() {
        long inFreeSpace = new File(this.currentDir).getUsableSpace();
        long inTotalSpace = new File(this.currentDir).getTotalSpace();

        if (System.getenv("SECONDARY_STORAGE") != null) {
            File SDCard = new File(System.getenv("SECONDARY_STORAGE"));
            long exFreeSpace = SDCard.getUsableSpace();
            if ((inTotalSpace - inFreeSpace) < exFreeSpace) {
                this.userFilesZip = SDCard.getPath() + "/Files.zip";
                File f = new File(this.userFilesZip);
                if (f.isFile()) {
                    f.delete();
                }
                return true;
            }
        }
        if ((inTotalSpace - inFreeSpace) < inFreeSpace) {
            this.userFilesZip = this.currentDir + "/Files.zip";
            File f = new File(this.userFilesZip);
            if (f.isFile()) {
                f.delete();
            }
            return true;
        }
        return false;
    }

    public boolean isCorrect() {
        return this.currentDir != "false";
    }

    public String getPath(String filename) {
        return this.currentDir + "/" + filename;
    }

    public boolean writeText(String filename, String text) {
        FileWriter f;
        try {
            f = new FileWriter(this.currentDir + "/" + filename);
            f.write(text);
            f.flush();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean deleteFile(String filename) {
        return (new File(this.currentDir + "/" + filename)).delete();
    }

    public String readText(String filename) throws FileNotFoundException {
        return new Scanner(new File(this.currentDir + "/" + filename)).useDelimiter("\\Z").next();
    }

}
