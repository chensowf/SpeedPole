package org.speedpole.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.speedpole.BaseActivity;
import org.speedpole.R;
import org.speedpole.mode.AppItem;
import org.speedpole.mode.SelectApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class AppsActivity extends BaseActivity {

    /**
     * 获取SelectApp的数据key
     */
    public final static String Extra_SelectApp_Key = "Extra_SelectApp_Key";
    public final static int AppsRequestCode = 100<<3;

    public static void startAppsActivity(Activity activity, SelectApp mSelectApp, int requestCode)
    {
        Intent intent = new Intent(activity,AppsActivity.class);
        intent.putExtra(Extra_SelectApp_Key,mSelectApp);
        activity.startActivityForResult(intent,requestCode);
    }

    @BindView(R.id.recyclerView)
    public RecyclerView mRecyclerView;
    @BindView(R.id.all_app_switch)
    public Switch mSwitch;

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Disposable disposable;
    private List<String> mSelectAppItemList;
    private SelectApp mSelectApp;

    @Override
    public int getContentView() {
        return R.layout.activity_apps;
    }

    @Override
    public void initData() {
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSelectApp = getIntent().getParcelableExtra(Extra_SelectApp_Key);
        if(mSelectApp != null)
        {
            Log.e("debug","debug:"+mSelectApp.isSelectAll);
            mSwitch.setChecked(mSelectApp.isSelectAll);
            mSelectAppItemList = mSelectApp.selectAppList;
        }
        else
        {
            mSwitch.setChecked(true);
            mSelectApp = new SelectApp();
        }

        if(mSelectAppItemList == null)
            mSelectAppItemList = new ArrayList<>();

        mRecyclerViewAdapter = new RecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                new LinearLayoutManager(this).getOrientation()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        asyncLoading();

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("mSwitch","isChecked:"+isChecked);
                if(buttonView.getTag() != null)
                {
                    if(buttonView.getTag().equals("item set")) return;
                }
                mSelectApp.isSelectAll = isChecked;
                SparseArray<AppItem> appItemSparseArray = mRecyclerViewAdapter.getAppItemSparseArray();
                for(int i = 0; i < appItemSparseArray.size(); i++)
                {
                    AppItem appItem = appItemSparseArray.get(i);
                    appItem.isSelect = isChecked;
                }
                mRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setForActivityResult();
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setForActivityResult()
    {
        Intent intent = new Intent();
        intent.putExtra(Extra_SelectApp_Key,mSelectApp);
        setResult(RESULT_OK,intent);
    }

    private void asyncLoading()
    {
        disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                SparseArray<AppItem> sparseArray = queryAppInfo();
                e.onNext(sparseArray);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        SparseArray<AppItem> appItemSparseArray = (SparseArray<AppItem>)o;
                        mRecyclerViewAdapter.setAppItemSparseArray(appItemSparseArray)
                                .notifyDataSetChanged();
                    }
                });
    }

    private SparseArray<AppItem> queryAppInfo()
    {
        SparseArray<AppItem> deviceApps = new SparseArray<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,0);
        Collections.sort(resolveInfos,new ResolveInfo.DisplayNameComparator(packageManager));
        int key = 0;
        for(ResolveInfo resolveInfo:resolveInfos)
        {
            AppItem appItem = new AppItem();
            appItem.packetName = resolveInfo.activityInfo.packageName;
            appItem.appName = resolveInfo.loadLabel(packageManager).toString();
            appItem.icon = resolveInfo.loadIcon(packageManager);
            if(!mSwitch.isChecked())
                appItem.isSelect = mSelectAppItemList.contains(appItem.packetName);
            if(!appItem.packetName.equals(getPackageName()))
            {
                deviceApps.put(key,appItem);
                key ++;
            }
        }
        return deviceApps;
    }

    public class AppsItemViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView mAppIcon;
        public TextView mAppName;
        public Switch mSelect;
        public int position;

        public AppsItemViewHolder(View itemView) {
            super(itemView);
            mAppIcon = itemView.findViewById(R.id.app_icon);
            mAppName = itemView.findViewById(R.id.app_name);
            mSelect = itemView.findViewById(R.id.app_select);
            mSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AppItem appItem = mRecyclerViewAdapter.getItem(position);
                    appItem.isSelect = isChecked;

                    if(!isChecked)
                    {
                        mSwitch.setTag("item set");
                        mSwitch.setChecked(isChecked);
                        mSwitch.setTag("item unset");
                    }

                    if(!mSwitch.isChecked()){
                        if(isChecked)
                        {
                            mSelectAppItemList.add(appItem.packetName);
                        }
                        else {
                            mSelectAppItemList.remove(appItem.packetName);
                        }
                    }

                    if(isChecked)
                    {
                        boolean isAllSelect = true;
                        for(int i = 0; i < mRecyclerViewAdapter.getAppItemSparseArray().size(); i++)
                        {
                            if(!mRecyclerViewAdapter.getItem(i).isSelect)
                            {
                                isAllSelect = false;
                            }
                        }
                        if(isAllSelect)
                            mSwitch.setChecked(true);
                    }
                }
            });
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<AppsItemViewHolder>
    {
        private Context mContext;
        private SparseArray<AppItem> appItemSparseArray;

        public RecyclerViewAdapter(Context mContext)
        {
            this.mContext = mContext;
            appItemSparseArray = new SparseArray<>();
        }

        public RecyclerViewAdapter setAppItemSparseArray(SparseArray<AppItem> appItemSparseArray) {
            this.appItemSparseArray = appItemSparseArray;
            return RecyclerViewAdapter.this;
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
            AppsItemViewHolder viewHolder = holder;
            AppItem appItem = appItemSparseArray.get(position);
            viewHolder.position = position;
            viewHolder.mAppIcon.setImageDrawable(appItem.icon);
            viewHolder.mAppName.setText(appItem.appName);
            viewHolder.mSelect.setChecked(appItem.isSelect);
        }

        @Override
        public int getItemCount() {
            return appItemSparseArray.size();
        }

        public AppItem getItem(int position)
        {
            return appItemSparseArray.get(position);
        }

        public SparseArray<AppItem> getAppItemSparseArray()
        {
            return appItemSparseArray;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!disposable.isDisposed())
            disposable.dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            setForActivityResult();
        }
        return super.onKeyDown(keyCode,event);
    }
}
