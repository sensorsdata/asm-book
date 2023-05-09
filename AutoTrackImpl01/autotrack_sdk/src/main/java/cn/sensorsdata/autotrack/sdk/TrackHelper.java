package cn.sensorsdata.autotrack.sdk;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public final class TrackHelper {
    private static final String TAG = "TrackHelper";

    public static void trackClick(View view) {
        try {
            Log.i(TAG, "track click trigger: " + view);
            if (view == null) {
                return;
            }
            //创建一个 JSONObject 用于构建 json 数据
            JSONObject resultJson = new JSONObject();
            //1. 获取元素内容
            String elementContent = getElementContent(view);
            if (!TextUtils.isEmpty(elementContent)) {
                resultJson.put("element_content", elementContent);
            }
            //2. 获取页面名称和标题
            getScreenNameAndTitle(view, resultJson);
            //3. 设置 time
            resultJson.put("time", System.currentTimeMillis());
            //todo 可以扩展其他功能，添加需要的信息

            //输出结果
            Log.i(TAG, "Final result: \n" + resultJson.toString(4));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static String getElementContent(View view) {
        if (view instanceof Button) {
            Button btn = (Button) view;
            String content = btn.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = btn.getContentDescription().toString();
            }
            return content;
        }
        //此处可以扩展到其他 View
        return null;
    }

    private static String getScreenNameAndTitle(View view, JSONObject jsonObject) {
        try {
            Context context = view.getContext();
            Activity activity = getActivityFromContext(context, view);
            if (activity != null) {
                String screenName = activity.getClass().getCanonicalName();
                jsonObject.put("screen_name", screenName);
                String title = getActivityTitle(activity);
                if (!TextUtils.isEmpty(title)) {
                    jsonObject.put("title", title);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Activity getActivityFromContext(Context context, View view) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    private static String getActivityTitle(Activity activity) {
        if (activity != null) {
            try {
                String activityTitle = null;
                if (!TextUtils.isEmpty(activity.getTitle())) {
                    activityTitle = activity.getTitle().toString();
                }

                if (Build.VERSION.SDK_INT >= 11) {
                    String toolbarTitle = getToolbarTitle(activity);
                    if (!TextUtils.isEmpty(toolbarTitle)) {
                        activityTitle = toolbarTitle;
                    }
                }

                if (TextUtils.isEmpty(activityTitle)) {
                    PackageManager packageManager = activity.getPackageManager();
                    if (packageManager != null) {
                        ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                        if (!TextUtils.isEmpty(activityInfo.loadLabel(packageManager))) {
                            activityTitle = activityInfo.loadLabel(packageManager).toString();
                        }
                    }
                }
                return activityTitle;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    static String getToolbarTitle(Activity activity) {
        try {
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                try {
                    Class<?> appCompatActivityClass = compatActivity();
                    if (appCompatActivityClass != null && appCompatActivityClass.isInstance(activity)) {
                        Method method = activity.getClass().getMethod("getSupportActionBar");
                        Object supportActionBar = method.invoke(activity);
                        if (supportActionBar != null) {
                            method = supportActionBar.getClass().getMethod("getTitle");
                            CharSequence charSequence = (CharSequence) method.invoke(supportActionBar);
                            if (charSequence != null) {
                                return charSequence.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> compatActivity() {
        Class<?> appCompatActivityClass = null;
        try {
            appCompatActivityClass = Class.forName("android.support.v7.app.AppCompatActivity");
        } catch (Exception e) {
            //ignored
        }
        if (appCompatActivityClass == null) {
            try {
                appCompatActivityClass = Class.forName("androidx.appcompat.app.AppCompatActivity");
            } catch (Exception e) {
                //ignored
            }
        }
        return appCompatActivityClass;
    }
}
