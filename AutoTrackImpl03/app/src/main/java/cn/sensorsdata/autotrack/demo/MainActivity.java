package cn.sensorsdata.autotrack.demo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    long b = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLambdaStyle();
    }

//    private void init(){
//        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("MainActivity", "Button is Clicked");
//                //TrackHelper.trackClick(v);
//            }
//        });
//        //AppCompatButton
//    }
//
//    private void init2(){
//        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int a = 10;
//            }
//        });
//        //AppCompatButton
//    }

    //Lambda 写法，测试代码
    private void initLambdaStyle() {
        final Date date = new Date();
        findViewById(R.id.btn).setOnClickListener(v -> {
            System.out.println(v);
        });

        findViewById(R.id.btn).setOnClickListener(v -> {
            System.out.println(v + "  " + date);
        });
        findViewById(R.id.btn).setOnClickListener(v -> {
            System.out.println(v + "  " + date + "===" + b);
        });
    }

    //方法引用写法
    private void methodRefStyle() {
        findViewById(R.id.btn).setOnClickListener(System.out::println);
    }
}