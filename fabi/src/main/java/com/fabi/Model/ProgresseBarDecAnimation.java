package com.fabi.Model;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ProgresseBarDecAnimation {
    public ProgresseBarDecAnimation(ProgressBar progressBar, TextView textView, float valeur) {
        mProgressBar = progressBar;
        mTextView = textView;
        this.valeur = valeur;
        mHandler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startProgress();
            }
        });
        thread.start();
    }
    private void startProgress() {
        for(mI=(int)valeur;mI>0;mI--)
        {
            try {
                Thread.sleep(20);
                mProgressBar.setProgress(mI);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mI <= valeur && mI != 0)
                        mTextView.setText(String.valueOf(mI) + "%");
                    else
                        mTextView.setText("00%");
                }
            });
        }


    }

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private float valeur;
    private Handler mHandler;
    private int mI;
    private DecimalFormat mDf = new DecimalFormat("#.##");

}

