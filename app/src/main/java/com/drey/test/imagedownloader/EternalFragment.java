package com.drey.test.imagedownloader;

import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * Created by drey on 01.11.2015.
 */
public class EternalFragment extends Fragment {
    ArrayList<Task> _data = new ArrayList<Task>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public ArrayList<Task> getData() {
        return _data;
    }
}
