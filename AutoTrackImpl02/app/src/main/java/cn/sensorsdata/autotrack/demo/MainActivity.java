package cn.sensorsdata.autotrack.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

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

    //Lambda 写法
    private void initLambdaStyle(){
        findViewById(R.id.btn).setOnClickListener(v -> {
            System.out.println(v);
        });
    }

    //方法引用写法
    private void methodRefStyle(){
        findViewById(R.id.btn).setOnClickListener(System.out::println);
    }
}