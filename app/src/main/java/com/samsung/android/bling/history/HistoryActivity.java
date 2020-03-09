package com.samsung.android.bling.history;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.android.bling.R;

public class HistoryActivity extends Activity {

    private RecyclerView mRecyclerView;
    private HistoryAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initView();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new HistoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }
}
