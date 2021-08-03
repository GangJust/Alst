package cn.edu.swsm.alst.uitls;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    private SharedPreferencesUtil() {

    }

    private static void init(Context context) {
        sp = context.getSharedPreferences("c", Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static SharedPreferences getSp(Context context) {
        if (sp == null) init(context);
        return sp;
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        if (editor == null) init(context);
        return editor;
    }
}
