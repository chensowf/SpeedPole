package org.speedpole.mode;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2018/5/22.
 */

public class SelectApp implements Parcelable{

    public boolean isSelectAll;
    public List<String> selectAppList;

    public SelectApp()
    {
        new SelectApp(true, new ArrayList<String>());
    }

    public SelectApp(boolean isSelectAll, List<String> selectAppList)
    {
        this.isSelectAll = isSelectAll;
        this.selectAppList = selectAppList;
    }

    protected SelectApp(Parcel in) {
        isSelectAll = in.readByte() != 0;
        selectAppList = in.createStringArrayList();
    }

    public static final Creator<SelectApp> CREATOR = new Creator<SelectApp>() {
        @Override
        public SelectApp createFromParcel(Parcel in) {
            return new SelectApp(in);
        }

        @Override
        public SelectApp[] newArray(int size) {
            return new SelectApp[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isSelectAll ? 1 : 0));
        dest.writeStringList(selectAppList);
    }
}
