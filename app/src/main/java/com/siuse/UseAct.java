package com.siuse;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.invisible.silentinstall.core.SICtrl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shuxiong on 2017/6/5.
 */

public class UseAct extends Activity{
    ExecutorService es = Executors.newSingleThreadExecutor();
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        Button button = new Button(this);
        button.setText("use temp");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        SICtrl.getIns().execute(UseAct.this);

                    }
                });
            }
        });

        setContentView(button);
    }

}
