package org.speedpole.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.dd.CircularProgressButton;
import com.txket.ss.Launchss;
import com.txket.ss.core.ProxyConfig;

import org.speedpole.BaseActivity;
import org.speedpole.R;
import org.speedpole.activity.AboutActivity;
import org.speedpole.activity.AppsActivity;
import org.speedpole.activity.NodeActivity;
import org.speedpole.mode.SelectApp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.circularButton)
    public CircularProgressButton mConnectButton;
    @BindView(R.id.lav_show)
    public LottieAnimationView mLottieAnimationView;

    private SelectApp mSelectApp;
    private Menu mMenu;
    private Launchss mLaunchss;

    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    public void initData() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LottieComposition.Factory.fromAssetFileName(this, "lottiefiles.com - Mail Sent.json",
                new OnCompositionLoadedListener() {
                    @Override public void onCompositionLoaded(@Nullable LottieComposition composition) {
                        mLottieAnimationView.setComposition(composition);
                    }
                });
        mLottieAnimationView.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(mLottieAnimationView.getFrame() == 69)
                {
                    mLottieAnimationView.pauseAnimation();
                }
            }
        });

        mSelectApp = (SelectApp) loadParcelable(SelectApp.class.getSimpleName(),SelectApp.class);
        if(mSelectApp == null)
        {
            mSelectApp = new SelectApp(true,new ArrayList<String>());
            Log.e("debug","debug:"+mSelectApp.isSelectAll);
        }
        mLaunchss = new Launchss();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, NodeActivity.class),12);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

    /*    if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/
        if(id == R.id.nav_apps)
        {
            AppsActivity.startAppsActivity(this, mSelectApp, AppsActivity.AppsRequestCode);
        }
        if(id == R.id.nav_about)
        {
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick(R.id.circularButton) public void gotoConnect()
    {
        if(mConnectButton.getProgress() == 0)
            simulateSuccessProgress(mConnectButton);
        else {
            mLaunchss.stopSSVpn();
            mConnectButton.setProgress(0);
        }
    }

    private void simulateSuccessProgress(final CircularProgressButton button) {
        ValueAnimator widthAnimation = ValueAnimator.ofInt(1, 100);
        widthAnimation.setDuration(1500);
        widthAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                button.setProgress(value);
                if(value == 100)
                {
                    mLottieAnimationView.playAnimation();
                    mLaunchss.startSSVpn(MainActivity.this,
                            "aes-256-cfb","saGFhQgkkm","104.194.78.71",4433,null);
                    ProxyConfig.Instance.globalMode = true;
                }
            }
        });
        widthAnimation.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppsActivity.AppsRequestCode)
        {
            if(resultCode == RESULT_OK)
            {
                mSelectApp = data.getParcelableExtra(AppsActivity.Extra_SelectApp_Key);
                Log.e("onActivityResult","onActivityResult:"+mSelectApp.isSelectAll);
            }
        }
        if(requestCode == Launchss.LaunchSSCode)
        {
            if(resultCode == RESULT_OK)
            {
                mLaunchss.startSSVpn();
            }
        }
        if(requestCode == 12)
        {
            mMenu.findItem(R.id.action_settings).setIcon(R.mipmap.server_icon_ae);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLaunchss.stopSSVpn();
    }
}
