package top.yms.note.utils;


import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateHelper {

    public static final String PATTERN1 = "yyyy-MM-dd";
    public static final String PATTERN2 = "yyyy-MM-dd HH:mm";
    public static final String PATTERN3 = "yyyy-MM";
    public static final String PATTERN4 = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN5 = "yyyy.MM.dd HH:mm:ss";
    public static final String PATTERN6 = "yyyy年MM月dd日HH时";

    private final static int [] MONTH = {0,31,28,31,30,31,30,31,31,30,31,30,31};


    public static final String PATTERN7 = "yyyy/MM/dd HH:mm";


    /**
     * <p>input=> 2022-05-31 23:18:00 => out => Date</p>
     * <p>input=> 2022/5/28 17:20 => Out => date</p>
     * <p>input=> 2022/5/31 8:18  => out => date</p>
     * input=>
     * <p>
     * 将长时间格式字符串转换为Date类型
     *
     * @param strDate
     * @return
     */
    public static Date strToDateTime(String strDate) {
        if (strDate == null) return null;
        Date date = null;
        if (strDate.indexOf("/") > 0) {
            date = doStrToDateTime(strDate, PATTERN7);
        } else if (strDate.indexOf("-") > 0) {
            date = doStrToDateTime(strDate, PATTERN4);
        } else {
            throw new RuntimeException("没有匹配的格式：" + strDate);
        }

        return date;
    }

    /**
     * <p>给定日期字符串及模式，输出Date类型</p>
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date doStrToDateTime(String dateStr, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(dateStr, pos);
        return strtodate;
    }


    /**
     * <p>给定一个date类型，输出 yyyy-MM-dd HH:mm:ss 格式字符串</p>
     * @param date
     * @return
     */
    public static String getDateTimeStr(Date date) {
        if (date == null) return " ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN4);
        String timeStampStr = simpleDateFormat.format(date);
        return timeStampStr;
    }

    /**
     * 解析 yyyy-MM-dd HH:mm:ss => date格式
     * @param datetimeStr
     * @return
     */
    public static Date getDate(String datetimeStr) {
        return doStrToDateTime(datetimeStr, PATTERN4);
    }


    private static String match(String pattern) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String timeStampStr = simpleDateFormat.format(date);
        return timeStampStr;
    }

    public static String dateStrMatch(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String getYYYY_MM_DD() {
        return match(PATTERN1);
    }

    public static String getYYYY_MM_DD_HH_MM() {
        return match(PATTERN2);
    }

    public static String getYYYY_MM() {
        return match(PATTERN3);
    }

    public static String
    getYYYY_MM_DD_HH_MM_SS() {
        return match(PATTERN4);
    }

    /***
     * get yyyy.MM.dd HH:mm:ss
     * @returna
     */
    public static String getYYYY_MM_DD_HH_MM_SS_() {
        return match(PATTERN5);
    }

    /***
     * get yyyy年MM月dd日HH时
     * @return
     */
    public static String getYYYY_MM_DD_HH_MM_SS__() {
        return match(PATTERN6);
    }

    /****************************Calendar***********************************************/

    private static Calendar CALR = Calendar.getInstance();

    public static String getYear() {
        Integer year = CALR.get(Calendar.YEAR);
        return year.toString();
    }

    /**
     *  获取某年某月的实际总 共天数
     * 如果month数据异常 返回 -1
     * @param year
     * @param month
     * @return
     */
    public static int getDays(int year, int month) {
        if (month <1 || month > 12) return -1;

        if (month == 2 && (year %4 == 0 || year%400 ==0)) {
            return 29;
        }
        return MONTH[month];
    }


    public static Date addSecond(Date date, int second) {
        long time = date.getTime();
        long addTime = second*1000l;

        return new Date(time+addTime);
    }

    public static Date addMinute(Date date, int minute) {
        long time = date.getTime();
        long addTime = minute*60*1000l;

        return new Date(time+addTime);
    }

    public static Date addHour(Date date, int hour) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        cal.add(Calendar.HOUR_OF_DAY, hour);
        return cal.getTime();
    }

    public static void main(String[] args) {
        String str = "2022-10-17 18:00:00";
        Date date = doStrToDateTime(str, PATTERN4);


        long timss = 60*1000+30*1000;

        Date date1 = new Date(date.getTime() + timss*-1);
        String dateTimeStr = getDateTimeStr(date1);
        System.out.println(dateTimeStr);

    }



}


