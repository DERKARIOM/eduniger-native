package com.fabi.Model;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ProgresseBar20Animation {
    public ProgresseBar20Animation(ProgressBar progressBar, TextView textView, float valeur) {
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
        for(mI=0;mI<=valeur*5;mI++)
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
                    if(mI/5 +1  < valeur)
                        mTextView.setText(String.valueOf(mI) + "/20");
                    else
                        mTextView.setText(String.valueOf(mDf.format(valeur)) + "/20");
                }
            });
        }


    }

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private float valeur;
    private Handler mHandler;
    private int mI;
    private DecimalFormat mDf = new DecimalFormat("#.#");
    private float mValeur;

}

