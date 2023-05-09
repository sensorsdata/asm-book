package cn.sensorsdata.autotrack.plugin;

import com.android.build.gradle.AppExtension;

public final class AutoTrackHelper {
    public static final AutoTrackHelper INSTANCE = new AutoTrackHelper();
    public AutoTrackExtension autoTrackExtension;
    public AppExtension appExtension;

    private AutoTrackHelper(){
    }
    public static AutoTrackHelper getHelper(){
        return INSTANCE;
    }
}
