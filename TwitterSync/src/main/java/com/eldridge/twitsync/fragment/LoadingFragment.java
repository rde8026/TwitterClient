package com.eldridge.twitsync.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eldridge.twitsync.R;

/**
 * Created by ryaneldridge on 8/2/13.
 */
public class LoadingFragment extends Fragment {

    public static final String FRAGMENT_TAG = "LOADING_FRAG";
    private static final String TAG = LoadingFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loading_layout, container, false);
    }
}
