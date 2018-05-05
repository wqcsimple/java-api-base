package com.whis.base.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by dd on 9/8/15.
 */
public class CoreConfig {

    private static volatile CoreConfig instance;

    private ConfigItemList<Pattern> guestCanAccessPathPatternList;

    private ConfigItemList<String> defaultInterceptorExcludePathList;

    private ConfigItem<Boolean> debug;

    private ConfigItem<Boolean> enableTokenSecurityInterceptor;


    private CoreConfig() {

        guestCanAccessPathPatternList = new ConfigItemList<Pattern>((item1, item2) -> (item1.pattern().equals(item2.pattern()) ? 1 : 0));
        guestCanAccessPathPatternList.add(Pattern.compile("^/error*"));

        defaultInterceptorExcludePathList = new ConfigItemList<String>((item1, item2) -> (item1.equals(item2) ? 1 : 0));
        defaultInterceptorExcludePathList.add("/error**");
        defaultInterceptorExcludePathList.add("/static/**");

        debug = new ConfigItem<>(false);

        enableTokenSecurityInterceptor = new ConfigItem<>(true);
    }

    public static CoreConfig getInstance() {
        if (instance == null) {
            synchronized (CoreConfig.class) {
                if (instance == null) {
                    instance = new CoreConfig();
                }
            }
        }
        return instance;
    }

    public ConfigItemList<Pattern> getGuestCanAccessPathPatternList()
    {
        return guestCanAccessPathPatternList;
    }

    public ConfigItemList<String> getDefaultInterceptorExcludePathList()
    {
        return defaultInterceptorExcludePathList;
    }

    public ConfigItem<Boolean> getDebug()
    {
        return debug;
    }

    public ConfigItem<Boolean> getEnableTokenSecurityInterceptor() {
        return enableTokenSecurityInterceptor;
    }

    public class ConfigItemList<T>
    {
        private List<T> itemList;
        private Comparator<T> comparator;

        public ConfigItemList(Comparator<T> comparator)
        {
            itemList = new ArrayList<T>();
            this.comparator = comparator;
        }

        public void add(T item)
        {
            if (itemList.stream().filter(i -> (comparator.compare(i, item) > 0)).count() == 0)
            {
                itemList.add(item);
            }
        }

        public void add(List<T> itemList)
        {
            itemList.forEach(this::add);
        }

        public void remove(T item)
        {
            itemList.removeIf(i -> (comparator.compare(i, item) > 0));
        }

        public List<T> get()
        {
            return itemList;
        }
    }

    public class ConfigItem<T>
    {
        private T item;

        public ConfigItem(T item)
        {
            this.item = item;
        }

        public void set(T item)
        {
            this.item = item;
        }

        public T get()
        {
            return item;
        }
    }

}
