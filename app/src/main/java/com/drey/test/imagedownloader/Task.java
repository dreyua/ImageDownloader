package com.drey.test.imagedownloader;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by drey on 01.11.2015.
 */
public class Task{
/*

    protected Task(Parcel in) {
        _url = in.readString();
        _msg = in.readString();
        _progress = in.readInt();
        _img = in.readParcelable(Bitmap.class.getClassLoader());
        _created = (Long) in.readLong();
        _state =  State.valueOf(in.readString());
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_url);
        dest.writeString(_msg);
        dest.writeInt(_progress);
        dest.writeParcelable(_img, flags);
        dest.writeLong(_created);
        dest.writeString(_state.toString());
    }
*/

    public enum State {New, Downloading, Processing, Complete, Error};
    private String _url;
    private String _msg;
    private int _progress = 0;
    private State _state = State.New;
    private Long _created = Calendar.getInstance().getTimeInMillis();
    private Bitmap _img;

    public String getUrl() {
        return _url;
    }

    public void setProgress(int p) {
        _progress = p;
    }

    public int getProgress() {
        return _progress;
    }

    public Task(String url) {
        _url = url;
    }

    public Long getCreated() {return _created; }

    public State getState() {return _state;}

    public void setMsg(String m){ _msg = m;}

    public void setState(State state) {_state = state;}

    public Bitmap getImg() {
        return _img;
    }

    public void setImg(Bitmap img) {
        this._img = img;
    }

    public String getStatusString(){
        StringBuilder sb = new StringBuilder();
        switch (_state){
            case Error:
                sb.append(_state.toString() + ": " + _msg);
                break;
            case Downloading:
                sb.append(_progress + "%");
            case Processing:
                sb.insert(0, "...  ");
            case New:
            case Complete:
                sb.insert(0, _state.toString());
        }
        return  sb.toString();
    }



}
