package com.example.administrator.aduiorecordui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.aduiorecordui.record.AudioRecord;
import com.example.administrator.aduiorecordui.record.RecordCallBack;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_TIME = 10;

    boolean isMakeWave;
    Handler handlerRadio = new Handler();
    Runnable runnableRadio = new Runnable() {
        @Override
        public void run() {
            ruler.makeRect(new Random().nextInt(100));
            handlerRadio.postDelayed(runnableRadio, 10);
        }
    };
    private AudioRecord ruler;

    public void startMakeRadioWave() {
        if (!isMakeWave) {
            handlerRadio.post(runnableRadio);
            isMakeWave = true;
        }
    }

    public void stopMakeRadioWave() {
        if (isMakeWave) {
            handlerRadio.removeCallbacks(runnableRadio);
            isMakeWave = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ruler = findViewById(R.id.ruler);
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        Button btnRadioStart = findViewById(R.id.btn_radio_start);
        Button btnRadioStop = findViewById(R.id.btn_radio_stop);
        final TextView tvTime = findViewById(R.id.tv_time);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.startAutoScroll();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.stopAutoScroll();
            }
        });

        btnRadioStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMakeRadioWave();
            }
        });

        btnRadioStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMakeRadioWave();
            }
        });

        ruler.setRecordCallBack(new RecordCallBack() {
            @Override
            public void onScaleChange(int scrollX, long timeInMillis) {
                Log.d("MainActivity", "lll timeInMillis = " + timeInMillis);
                SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
                Date date = new Date();
                date.setTime(timeInMillis);
                tvTime.setText(MessageFormat.format("{0}/{1}", dateFormat.format(date), RECORD_TIME));
            }
        });

    }
}
