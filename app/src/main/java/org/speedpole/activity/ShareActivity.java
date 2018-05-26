package org.speedpole.activity;


import android.view.MenuItem;

import org.speedpole.BaseActivity;
import org.speedpole.R;

public class ShareActivity extends BaseActivity {

    @Override
    public int getContentView() {
        return R.layout.activity_share;
    }

    @Override
    public void initData() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0f);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
