package su.swage.recovery.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import su.swage.recovery.R;

public class AlertDialogs {
    Activity activity;

    public AlertDialogs(Activity activity) {
        this.activity = activity;
    }

    public void NetworkError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton(R.string.dialog_networkerror_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setMessage(R.string.dialog_networkerror_text).setTitle(R.string.dialog_networkerror_head);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void LoginError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton(R.string.dialog_loginincorrect_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setMessage(R.string.dialog_loginincorrect_text).setTitle(R.string.dialog_loginincorrect_head);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
