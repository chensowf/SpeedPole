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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.speedpole.BaseActivity;
import org.speedpole.R;
import org.w3c.dom.Node;

import butterknife.BindView;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
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
            
        }
    }


}
