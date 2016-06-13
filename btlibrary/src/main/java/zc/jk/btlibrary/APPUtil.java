package zc.jk.btlibrary;

import android.app.ActivityManager;
import android.content.Context;

import java.util.Iterator;

/**
 * Created by ZhangCheng on 2016/3/18.
 */
public class APPUtil {
    public APPUtil() {
    }

    public static boolean isServiceRunning(String serviceClassName, Context context) {
        ActivityManager manager = (ActivityManager)context.getSystemService("activity");
        Iterator var4 = manager.getRunningServices(2147483647).iterator();

        while(var4.hasNext()) {
            ActivityManager.RunningServiceInfo service = (ActivityManager.RunningServiceInfo)var4.next();
            if(serviceClassName.equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
