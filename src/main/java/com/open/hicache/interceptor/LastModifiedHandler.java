package com.open.hicache.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static org.springframework.http.HttpHeaders.*;

/**
 * 实现304协商缓存
 *
 * @author cuiwy
 * @date 2022/6/3
 */
@Slf4j
public class LastModifiedHandler {

    /**
     * 5 min
     */
    private final static long FIVE_MIN = 1000 * 60 * 5;
    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /**
     * get default time
     *
     * @return
     */
    public long getLastModifiedInternal() {
        return this.getLastModifiedInternal(null);
    }

    /**
     * get internal time
     *
     * @param cacheTime cache time (unit:ms)
     * @return
     */
    public long getLastModifiedInternal(Long cacheTime) {

        try {
            HttpServletResponse httpServletResponse = this.getHttpServletResponse();
            httpServletResponse.setHeader(CACHE_CONTROL, "no-cache,max-age=0,must-revalidate");
            String header = this.getHttpServletRequest().getHeader(IF_MODIFIED_SINCE);
            if (StrUtil.isBlank(header)) {
                httpServletResponse.addDateHeader(LAST_MODIFIED, System.currentTimeMillis());
            } else {
                // cache five min
                long modifiedVal = this.parseDateHeader(IF_MODIFIED_SINCE);
                long now = System.currentTimeMillis();
                boolean b = (now - modifiedVal) < (ObjectUtil.isNotNull(cacheTime) ? cacheTime : FIVE_MIN);
                return b ? modifiedVal : now;
            }

        } catch (Exception e) {
            log.error("LastModifiedHandler error:{}", e);
        }
        return -1;
    }

    private HttpServletResponse getHttpServletResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    private long parseDateHeader(String headerName) {
        long dateValue = -1;
        try {
            dateValue = this.getHttpServletRequest().getDateHeader(headerName);
        } catch (IllegalArgumentException ex) {
            String headerValue = this.getHttpServletRequest().getHeader(headerName);
            if (headerValue != null) {
                int separatorIndex = headerValue.indexOf(';');
                if (separatorIndex != -1) {
                    String datePart = headerValue.substring(0, separatorIndex);
                    dateValue = this.parseDateValue(datePart);
                }
            }
        }
        return dateValue;
    }

    private long parseDateValue(@Nullable String headerValue) {
        if (headerValue == null) {
            return -1;
        }
        if (headerValue.length() >= 3) {
            for (String dateFormat : DATE_FORMATS) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
                simpleDateFormat.setTimeZone(GMT);
                try {
                    return simpleDateFormat.parse(headerValue).getTime();
                } catch (ParseException ex) {
                    // do nothing
                }
            }
        }
        return -1;
    }


}
