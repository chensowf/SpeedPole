package org.speedpole.rxjava;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Admin on 2018/3/31.
 */

public class RxTimer {
    public static Observable<Long> timer(int delay, int period, final int count)
    {
        return Observable.interval(0,1, TimeUnit.SECONDS)
                .take(count+1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return count - aLong;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void cancel(Disposable mDisposable)
    {
        mDisposable.dispose();
    }


}
