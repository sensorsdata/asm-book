package cn.sensorsdata.autotrack.plugin;

import java.util.ArrayList;

public class AutoTrackExtension {
    public ArrayList<String> exclude = new ArrayList<>();

    @Override
    public String toString() {
        return "AutoTrackExtension{" +
                "exclude=" + exclude +
                '}';
    }
}
