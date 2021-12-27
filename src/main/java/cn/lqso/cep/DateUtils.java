package cn.lqso.cep;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

public class DateUtils {
    public static String printTime(Date date){
        return new DateTime(date).toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
