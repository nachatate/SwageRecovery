package su.swage.recovery.listener;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import su.swage.recovery.R;
import su.swage.recovery.RestoreActivity;
import su.swage.recovery.utils.AlertDialogs;
import su.swage.recovery.utils.InterfaceAPI;
import su.swage.recovery.utils.MenuAdapter;
import su.swage.recovery.utils.RequestAPI;
import su.swage.recovery.utils.SessionManager;

public class MainListener extends AppCompatActivity implements InterfaceAPI, SwipeRefreshLayout.OnRefreshListener {
    public SwipeRefreshLayout mSwipeRefreshLayout;
    protected SwipeActionAdapter mAdapter;
    private List<Map<String, String>> listViewItems;

    @Override
    public Void onSuccessResponseAPI(JSONObject result) {
        try {
            listViewItems = new ArrayList<>();
            Map<String, String> map;
            int count = result.getJSONArray("data").length();
            for (int i = 0; i < count; i++) {
                Date mydate = new Date(result.getJSONArray("data").getJSONObject(i).getLong("createdate"));
                map = new HashMap<>();
                if (result.getJSONArray("data").getJSONObject(i).getInt("isAuto") == 1) {
                    map.put("id", getString(R.string.backup_auto) + " №" + result.getJSONArray("data").getJSONObject(i).getString("id"));
                } else {
                    map.put("id", getString(R.string.backup_manual) + " №" + result.getJSONArray("data").getJSONObject(i).getString("id"));
                }
                map.put("date", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(mydate));
                listViewItems.add(map);
            }

            final MenuAdapter mAdapter = new MenuAdapter(listViewItems, this);
            mAdapter.setMode(Attributes.Mode.Multiple);
            final ListView menuList = (ListView) findViewById(R.id.menuList);
            if (menuList != null) {
                menuList.setAdapter(mAdapter);
                menuList.setEmptyView(findViewById(R.id.ScrollView_menu));
                menuList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SwipeLayout swipeLayout = ((SwipeLayout) (menuList.getChildAt(position - menuList.getFirstVisiblePosition())));
                        if (swipeLayout.isActivated()) return;

                        final String item = listViewItems.get(position).get("id").replaceAll("[\\D]", "");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainListener.this);
                        builder.setPositiveButton(R.string.dialog_gorecovery_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(MainListener.this, RestoreActivity.class);
                                intent.putExtra("id", item);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton(R.string.dialog_gorecovery_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        builder.setMessage(R.string.dialog_gorecovery_text).setTitle(R.string.dialog_gorecovery_head);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        return null;
    }

    @Override
    public Void onFailureResponseAPI(Boolean isSystem, JSONObject result) {
        mSwipeRefreshLayout.setRefreshing(false);
        new AlertDialogs(this).NetworkError();
        return null;
    }

    @Override
    public void onRefresh() {
        ReloadList();
    }

    public void ReloadList() {
        mSwipeRefreshLayout.setRefreshing(true);
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("token", SessionManager.ApiKey);

        RequestAPI rAPI = new RequestAPI(this, this);
        rAPI.setMethod("/archive/list");
        rAPI.execute(hashtable);
    }
}
