package com.whis.app.common;

import java.util.regex.Pattern;

public class Config {
    public static final Pattern[] PATH_GUEST_CAN_ACCESS_PATTERN = new Pattern[]{
            Pattern.compile("^/client/1/user/(login|register|password-reset)"),
            Pattern.compile("^/test/.*"),
            Pattern.compile("^/user/.*")
    };

    public static final String[] DEFAULT_INTERCEPTOR_EXCLUDE_PATH_PATTERN = new String[]{
            "/file/**",
    };
}