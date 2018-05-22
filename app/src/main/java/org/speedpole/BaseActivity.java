package org.speedpole;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import butterknife.ButterKnife;

/**
 * Created by Admin on 2018/5/22.
 */

public abstract class BaseActivity extends AppCompatActivity{

    private String Path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        ButterKnife.bind(this);
        initData();
        Path = getCacheDir().getPath();
    }

    public abstract int getContentView();

    public abstract void initData();

    /**
     *
     * @param fileName
     * @param parcelable
     * @return
     */
    protected boolean saveParcelable(String fileName, Parcelable parcelable)
    {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = getApplicationContext().openFileOutput(fileName,
                    Context.MODE_PRIVATE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            Parcel parcel = Parcel.obtain();
            parcel.writeParcelable(parcelable,0);
            bufferedOutputStream.write(parcel.marshall());
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     * @param fileName
     * @param c
     * @return
     */
    protected Parcelable loadParcelable(String fileName, Class c)
    {
        FileInputStream fileInputStream;
        try {
            fileInputStream = getApplicationContext().openFileInput(fileName);
            byte[] buffer = new byte[fileInputStream.available()];
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(buffer,0,buffer.length);
            parcel.setDataPosition(0);
            fileInputStream.close();

            return parcel.readParcelable(c.getClassLoader());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
