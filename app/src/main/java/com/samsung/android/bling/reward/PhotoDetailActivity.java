package com.samsung.android.bling.reward;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.samsung.android.bling.R;
import com.samsung.android.bling.util.Utils;

public class PhotoDetailActivity extends Activity {

    private static final String TAG = "Bling/PhotoDetail";

    String mMemberUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        Utils.setBarDark(this, R.color.black);

        mMemberUrl = getIntent().getStringExtra("memberUrl");

        initView();
    }

    private void initView() {
        findViewById(R.id.close).setOnClickListener(v -> new Handler().postDelayed(() -> onBackPressed(), 250));

        FrameLayout layout = findViewById(R.id.photo_image_layout);
        ImageView memberImageView = findViewById(R.id.photo_image);

        int displayWidth = Utils.getDisplayWidth(this);
        layout.getLayoutParams().width = displayWidth;
        layout.getLayoutParams().height = (271 * displayWidth) / 180;

        //Glide.with(this).load(mMemberUrl).placeholder(R.drawable.bling_applogo).into(memberImageView);
        ProgressBar progressBar = findViewById(R.id.loading_progress_bar);
        Glide.with(this).load(mMemberUrl)
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
                }).into(memberImageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}