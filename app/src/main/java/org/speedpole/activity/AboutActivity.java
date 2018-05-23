package org.speedpole.activity;

import android.view.MenuItem;

import org.speedpole.BaseActivity;
import org.speedpole.R;

/**
 * Created by Admin on 2018/5/23.
 */

public class AboutActivity extends BaseActivity {
    @Override
    public int getContentView() {
        return R.layout.activity_about;
    }

    @Override
    public void initData() {
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
