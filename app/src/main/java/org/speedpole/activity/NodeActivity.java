package org.speedpole.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.speedpole.BaseActivity;
import org.speedpole.BuildConfig;
import org.speedpole.R;
import org.speedpole.util.Constants;
import org.speedpole.util.Encryptor;
import org.speedpole.util.OkHttpUtil;
import org.speedpole.util.Util;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class NodeActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    public RecyclerView mRecyclerView;

    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    public int getContentView() {
        return R.layout.activity_node;
    }

    @Override
    public void initData() {
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerViewAdapter  = new RecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                new LinearLayoutManager(this).getOrientation()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        getNodeInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void getNodeInfo()
    {
        String url = Constants.BaseUrl+"getvpn";
        HashMap<String, String> params = new HashMap<>();
        params.put("username", "chen");
        params.put("password", "123456");
        params.put("lang", "zh");
        if(BuildConfig.DEBUG)
            Log.e("url",url);
        OkHttpUtil.request(url, params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                byte[] buffer = Encryptor.decrypt(Encryptor.key,
                        Encryptor.iv,
                        response.body().bytes(),
                        Encryptor.NoPadding);
                /*buffer = Util.uncompress(buffer);*/
                String json = new String(buffer);
                if(BuildConfig.DEBUG)
                    Log.e("json",json);
            }
        });
    }

    public class NodeViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView nodeIcon;
        public TextView stateName;
        public View itemView;

        public NodeViewHolder(View itemView) {
            super(itemView);
            nodeIcon = itemView.findViewById(R.id.node_icon);
            stateName = itemView.findViewById(R.id.node_name);
            this.itemView = itemView;
            itemView.setOnClickListener(new ItemClickListener());
            itemView.setTag(this);
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<NodeViewHolder>
    {

        private Context mContext;

        public RecyclerViewAdapter(Context mContext)
        {
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.node_item,parent,false);
            return new NodeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
            NodeViewHolder viewHolder = holder;
            if(position == 0)
            {
                viewHolder.nodeIcon.setImageResource(R.drawable.ic_fly_black_24dp);
                viewHolder.stateName.setText("Auto Select");
                viewHolder.itemView.setBackgroundColor(getResources().getColor(R.color.purple_40));
            }
            else
            {
                viewHolder.nodeIcon.setImageResource(R.mipmap.server_icon_ca);
                viewHolder.stateName.setText("加拿大");
                viewHolder.itemView.setBackgroundColor(getResources().getColor(android.R.color.white));
            }
        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }

    public class ItemClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            NodeViewHolder viewHolder = (NodeViewHolder) v.getTag();
            finish();
        }
    }

}
