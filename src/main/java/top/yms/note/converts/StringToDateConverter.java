package top.yms.note.converts;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * request 时间参数转换（输入转换）
 * @author fengcl
 *
 */
public class StringToDateConverter implements Converter<String, Date> {
    private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String shortDateFormat = "yyyy-MM-dd";
    private static final String longDateFormat = "yyyyMMddHHmmss";

    @Override
    public Date convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        source = source.trim();
        try {
            SimpleDateFormat formatter = null;
            if (source.contains("-")) {
                if (source.contains(":")) {
                    formatter = new SimpleDateFormat(dateFormat);
                } else {
                    formatter = new SimpleDateFormat(shortDateFormat);
                }
                Date dtDate = formatter.parse(source);
                return dtDate;
            } else if (source.length() == 14 && source.matches("^\\d+$")) {
                formatter = new SimpleDateFormat(longDateFormat);
                return formatter.parse(source);
            } else if(source.length()>5) {
                Long lDate = new Long(source);
                return new Date(lDate);
            }else{
                //~ 传过来的数字长度必须大于等于6位；
                // 其他情况直接就是当null处理；
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("parser %s to Date fail", source));
        }
    }

}

