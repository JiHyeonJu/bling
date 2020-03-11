package com.samsung.android.bling.reward;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.samsung.android.bling.R;

public class RewardDetailActivity extends Activity {
    private static final String TAG = "Bling/RewardDetail";

    String mTitle, mSubTitle, mAlbumUrl, mMemberUrl, mStarName, mMemberName, mAlbumDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        findViewById(R.id.home_as_up).setOnClickListener(v -> new Handler().postDelayed(() -> onBackPressed(), 250));

        mTitle = getIntent().getStringExtra("title");
        mSubTitle = getIntent().getStringExtra("subtitle");
        mAlbumUrl = getIntent().getStringExtra("albumUrl");
        mMemberUrl = getIntent().getStringExtra("memberUrl");
        mStarName = getIntent().getStringExtra("starName");
        mMemberName = getIntent().getStringExtra("memberName");
        mAlbumDate = getIntent().getStringExtra("albumDate");

        initView();
    }

    private void initView() {
        TextView NameView = findViewById(R.id.member_name);
        TextView StarView = findViewById(R.id.star_name);
        TextView TitleView = findViewById(R.id.album_title);
        TextView SubView = findViewById(R.id.album_sub_text);
        TextView DateView = findViewById(R.id.album_date);

        ImageView memberImageView = findViewById(R.id.photo_image);
        ImageView albumImageView = findViewById(R.id.album_image);

        ProgressBar progressBar = findViewById(R.id.loading_progress_bar);

        /*Glide.with(this).load(mMemberUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(memberImageView);*/

        NameView.setText(mMemberName);
        StarView.setText(mStarName);
        TitleView.setText(mTitle);
        SubView.setText(mSubTitle);
        DateView.setText(mAlbumDate);

        Glide.with(this).load(mAlbumUrl).into(albumImageView);

        // send data
        memberImageView.setOnClickListener(v -> {
            String url = mMemberUrl.replace("_m_", "_l_");

            Intent intent = new Intent(this, PhotoDetailActivity.class);
            intent.putExtra("memberUrl", url);
            startActivity(intent);

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // ->
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
