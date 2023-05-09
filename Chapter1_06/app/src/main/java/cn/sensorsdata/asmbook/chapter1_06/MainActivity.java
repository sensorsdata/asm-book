package cn.sensorsdata.asmbook.chapter1_06;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

//import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.helloBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SensorsDataAPI.sharedInstance().track("he");
            }
        });
    }
}