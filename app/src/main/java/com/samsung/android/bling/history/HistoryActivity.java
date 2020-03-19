package com.samsung.android.bling.history;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

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
        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(this::onBackPressed, 250));

        mRecyclerView = findViewById(R.id.history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(true);

        mAdapter = new HistoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
