package su.swage.recovery.utils;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import su.swage.recovery.R;
import su.swage.recovery.listener.MainListener;

public class MenuAdapter extends BaseSwipeAdapter {
    public MainListener mainActivity;
    public ArrayList<Map<String, String>> adapter;

    public MenuAdapter(List<Map<String, String>> adapter, MainListener mainActivity) {
        this.mainActivity = mainActivity;
        this.adapter = (ArrayList<Map<String, String>>) adapter;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = LayoutInflater.from(mainActivity).inflate(R.layout.menu_swipe, null);
        v.findViewById(R.id.trash).setOnClickListener(new HandleClick(this, position));
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        ((TextView) convertView.findViewById(R.id.backup_id)).setText(adapter.get(position).get("id"));
        ((TextView) convertView.findViewById(R.id.backup_date)).setText(adapter.get(position).get("date"));
    }

    @Override
    public int getCount() {
        return adapter.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

class HandleClick implements View.OnClickListener, InterfaceAPI {
    private MenuAdapter menuAdapter;
    private Integer position;

    public HandleClick(MenuAdapter menuAdapter, Integer position) {
        this.menuAdapter = menuAdapter;
        this.position = position;
    }

    public void onClick(final View view) {
        if (view.getId() == R.id.trash) {
            final String item = menuAdapter.adapter.get(position).get("id").replaceAll("[\\D]", "");

            AlertDialog.Builder builder = new AlertDialog.Builder(menuAdapter.mainActivity);
            builder.setPositiveButton(R.string.dialog_deletebackup_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Hashtable<String, String> hashtable = new Hashtable<>();
                    hashtable.put("token", SessionManager.ApiKey);
                    hashtable.put("id", item);

                    RequestAPI rAPI = new RequestAPI(menuAdapter.mainActivity, HandleClick.this);
                    rAPI.setMethod("/archive/delete");
                    rAPI.execute(hashtable);
                }
            });
            builder.setNegativeButton(R.string.dialog_deletebackup_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    menuAdapter.closeAllItems();
                    dialog.cancel();
                }
            });
            builder.setMessage(R.string.dialog_deletebackup_text).setTitle(R.string.dialog_deletebackup_head);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        menuAdapter.closeAllItems();
        menuAdapter.mainActivity.ReloadList();
        Toast.makeText(menuAdapter.mainActivity, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        menuAdapter.closeAllItems();
        menuAdapter.mainActivity.ReloadList();
        new AlertDialogs(menuAdapter.mainActivity).NetworkError();
        return null;
    }
}