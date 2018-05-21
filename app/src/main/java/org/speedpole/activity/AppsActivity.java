package org.speedpole.activity;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.speedpole.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AppsActivity extends AppCompatActivity {

    public static void startAppsActivity(Activity activity)
    {
        activity.startActivity(new Intent(activity,AppsActivity.class));
    }

    @BindView(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mRecyclerViewAdapter = new RecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    public class AppsItemViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mAppIcon;
        public TextView mAppName;
        public Switch mSelect;

        public AppsItemViewHolder(View itemView) {
            super(itemView);
            mAppIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
            mSelect = itemView.findViewById(R.id.app_select);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<AppsItemViewHolder>
    {
        private Context mContext;

        public RecyclerViewAdapter(Context mContext)
        {
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public AppsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.app_item,parent,false);
            AppsItemViewHolder appsItemViewHolder = new AppsItemViewHolder(view);
            return appsItemViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AppsItemViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }

}
