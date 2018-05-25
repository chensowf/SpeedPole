package org.speedpole.activity;

import android.content.Intent;

import com.airbnb.lottie.LottieAnimationView;

import org.speedpole.BaseActivity;
import org.speedpole.R;
import org.speedpole.rxjava.RxTimer;

import butterknife.BindView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by Admin on 2018/5/24.
 */

public class StartActivity extends BaseActivity {

    @BindView(R.id.lav_view)
    public LottieAnimationView mLottieAnimationView;

    @Override
    public int getContentView() {
        return R.layout.activity_start;
    }

    @Override
    public void initData() {
        delayJump();
    }

    private void delayJump()
    {
        RxTimer.timer(0,1,5).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Long value) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                mLottieAnimationView.pauseAnimation();
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
