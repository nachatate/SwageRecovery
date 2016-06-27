package su.swage.recovery.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.pnikosis.materialishprogress.ProgressWheel;

import su.swage.recovery.LoginActivity;
import su.swage.recovery.MainActivity;
import su.swage.recovery.R;
import su.swage.recovery.RequestPIN;

public class DesignUtils {
    public static void ChangeText(TextView obj, String text) {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1500);
        fadeOut.setFillAfter(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);
        fadeIn.setFillAfter(true);
        fadeIn.setStartOffset(fadeOut.getStartOffset());

        obj.startAnimation(fadeOut);
        obj.setText(text);
        obj.startAnimation(fadeIn);
    }

    public static void ChangeBackgroundColor(String layTo, String barTo, final LinearLayout layout, final ProgressWheel progressBar) {
        ColorDrawable viewColor = (ColorDrawable) layout.getBackground();
        int LayColorFrom = viewColor.getColor();
        int LayColorTo = Color.parseColor(layTo);

        int BarColorFrom = progressBar.getBarColor();
        int BarColorTo = Color.parseColor(barTo);

        ValueAnimator LayColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), LayColorFrom, LayColorTo);
        LayColorAnimation.setDuration(3000);
        LayColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                layout.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        LayColorAnimation.start();

        ValueAnimator BarColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), BarColorFrom, BarColorTo);
        BarColorAnimation.setDuration(5000);
        BarColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                progressBar.setBarColor((int) animator.getAnimatedValue());
            }
        });
        BarColorAnimation.start();
    }

    public static void InitSwipeRefresh(MainActivity mainActivity) {
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) mainActivity.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(mainActivity);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public static void InitLockScreen(MainActivity mainActivity, SharedPreferences sharedPref, SharedPreferences.Editor editor) {
        LockManager<RequestPIN> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(mainActivity, RequestPIN.class);
        lockManager.getAppLock().setLogoId(R.mipmap.ic_launcher);
        lockManager.getAppLock().setShouldShowForgot(false);

        if (!sharedPref.getBoolean("HasPINCode", false)) {
            Intent intent = new Intent(mainActivity, RequestPIN.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            editor.putBoolean("HasPINCode", true);
            editor.commit();
            mainActivity.startActivityForResult(intent, MainActivity.PINCODE_INSTALLED);
        } else {
            Intent intent = new Intent(mainActivity, RequestPIN.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN);
            mainActivity.startActivityForResult(intent, MainActivity.PINCODE_SUCCESS);
        }
    }

    public static void InitNavDrawMenu(MainActivity mainActivity) {
        final MainActivity finalMain = mainActivity;

        Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
        mainActivity.setSupportActionBar(toolbar);
        mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(mainActivity)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(SessionManager.UserName).withEmail(SessionManager.eMail).withIcon(
                                new IconicsDrawable(mainActivity)
                                        .icon(FontAwesome.Icon.faw_user)
                                        .color(Color.GRAY)
                                        .sizeDp(24)
                                        .backgroundColor(Color.DKGRAY)
                        )
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        new DrawerBuilder()
                .withActivity(mainActivity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_ping).withIcon(FontAwesome.Icon.faw_server).withBadge(RequestAPI.lastPing + "ms").withEnabled(false),
                        new SectionDrawerItem().withName(R.string.drawer_item_home),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_buy).withIcon(FontAwesome.Icon.faw_usd).withBadge("5$"),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_sign_out).withIcon(FontAwesome.Icon.faw_sign_out)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (position == 4) {
                            Intent intent = new Intent(finalMain, LoginActivity.class);
                            intent.putExtra("unLogin", true);
                            finalMain.startActivity(intent);
                            finalMain.finish();
                            return true;
                        }
                        return false;
                    }
                })
                .withSelectedItem(-1)
                .build();
    }
}
