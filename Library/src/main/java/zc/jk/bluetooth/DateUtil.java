package zc.jk.bluetooth;

import java.util.Calendar;

/**
 * Created by ZhangCheng on 2016/2/14.
 */

public class DateUtil {
    public static String[] weekName = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    public DateUtil() {
    }

    public static int getYear() {
        return Calendar.getInstance().get(1);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(2) + 1;
    }

    public static int getCurrentMonthDay() {
        return Calendar.getInstance().get(5);
    }

    public static int getWeekDay() {
        return Calendar.getInstance().get(7);
    }

    public static int getHour() {
        return Calendar.getInstance().get(11);
    }

    public static int getMinute() {
        return Calendar.getInstance().get(12);
    }

    public static int getSecond() {
        return Calendar.getInstance().get(13);
    }


}
