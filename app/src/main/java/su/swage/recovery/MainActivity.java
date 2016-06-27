package su.swage.recovery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.View;

import com.mikepenz.iconics.context.IconicsLayoutInflater;

import su.swage.recovery.listener.MainListener;
import su.swage.recovery.utils.DesignUtils;
import su.swage.recovery.utils.SessionManager;

public class MainActivity extends MainListener {
    public static final Integer PINCODE_INSTALLED = 15;
    public static final Integer PINCODE_SUCCESS = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        if (sharedPref.getBoolean("HasWelcome", false)) {
            if (!SessionManager.isLogged) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                setContentView(R.layout.activity_main);

                DesignUtils.InitLockScreen(this, sharedPref, editor);
                DesignUtils.InitSwipeRefresh(this);
                DesignUtils.InitNavDrawMenu(this);

                ReloadList();
            }
        } else {
            editor.putBoolean("HasWelcome", true);
            editor.apply();

            Intent intent = new Intent(this, WelcomeIntro.class);
            startActivity(intent);
        }
    }

    public void btnBackup(View v) {
        Intent intent = new Intent(this, BackupActivity.class);
        startActivity(intent);
    }
}