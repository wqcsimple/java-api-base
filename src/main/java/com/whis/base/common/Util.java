package com.whis.base.common;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class Util {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Util.class);

    public static String getStackTrace(final Throwable throwable) {
        return getStackTrace(throwable, 100);
    }

    public static String getStackTrace(final Throwable throwable, int maxLines) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        String traces = sw.getBuffer().toString();
        List traceList = Lists.newArrayList(Splitter.on("\n").trimResults().omitEmptyStrings().split(traces));
        maxLines = maxLines > traceList.size() ? traceList.size() : maxLines;
        return Joiner.on("\n    ").join(traceList.subList(0, maxLines));
    }
}
