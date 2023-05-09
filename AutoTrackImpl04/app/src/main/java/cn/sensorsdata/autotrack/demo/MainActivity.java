package cn.sensorsdata.autotrack.demo;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Object obj = new String("sss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        findViewById(R.id.normal_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.lambda_stateless_btn1).setOnClickListener(v -> {

        });

        findViewById(R.id.lambda_state_btn1).setOnClickListener(v -> {
            System.out.println(this.obj);
        });

        findViewById(R.id.mr_style_new_btn).setOnClickListener(MyLayout::new);

        findViewById(R.id.mr_style_static_btn).setOnClickListener(MainActivity::show);

        MyLayout layout = new MyLayout();
        findViewById(R.id.mr_style_instance_btn).setOnClickListener(layout::setNewView);
    }

    public static void show(View view) {

    }

    static class MyLayout {
        public MyLayout(View view) {
        }

        public MyLayout(){

        }

        public void setNewView(View view){

        }
    }
}