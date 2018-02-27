package jp.co.java_conf.tyun.example.rkey_test.gen;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class BKey {
    /** foo */
    public static final BKey belem1 = new BKey("belem1");
    /** bar */
    public static final BKey belem2 = new BKey("belem2");

    private String key;
    
    private BKey(String key){
        this.key = key;
    }

    public static class btest2 {
        /** hoge */
        public static final BKey elem1 = new BKey("btest2.elem1");
        /** fuga */
        public static final BKey elem2 = new BKey("btest2.elem2");

    }
    public static class btest3 {
        /** abcd */
        public static final BKey elem1 = new BKey("btest3.elem1");
        /** efgh */
        public static final BKey elem2 = new BKey("btest3.elem2");
        /** ijkl */
        public static final BKey elem3 = new BKey("btest3.elem3");

        public static class telem3 {
            /** x */
            public static final BKey p1 = new BKey("btest3.telem3.p1");
            /** y */
            public static final BKey p2 = new BKey("btest3.telem3.p2");
            /** z */
            public static final BKey p3 = new BKey("btest3.telem3.p3");

        }
    }
    private static final String bundleName = "res2.BKey";
    private static final ConcurrentHashMap<String, Object> bundle = getBundleMap(Locale.getDefault());
    private static final ConcurrentHashMap<Locale, ConcurrentHashMap<String, Object>> bundleCache = new ConcurrentHashMap<Locale, ConcurrentHashMap<String, Object>>();
    
    private static ConcurrentHashMap<String, Object> getBundleMap(Locale locale) {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
    
        ResourceBundle rbundle = ResourceBundle.getBundle(bundleName, locale);
        for (String key : rbundle.keySet()) {
            map.put(key, rbundle.getObject(key));
        }
    
        return map;
    }
    
    private static ConcurrentHashMap<String, Object> getBundle(Locale locale) {
        if (locale.equals(Locale.getDefault())) {
            return bundle;
        } else if (bundleCache.containsKey(locale)) {
            return bundleCache.get(locale);
        } else {
            return bundleCache.putIfAbsent(locale, getBundleMap(locale));
        }
    }
    
    protected static Object getObject(String key) {
        return bundle.get(key);
    }
    
    protected static Object getObject(String key, Locale locale) {
        return getBundle(locale).get(key);
    }
    
    protected static Object getString(String key) {
        return (String) getObject(key);
    }
    
    protected static String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }
    
    protected static Object getString(String key, Locale locale) {
        return (String) getObject(key, locale);
    }
    
    protected static String[] getStringArray(String key, Locale locale) {
        return (String[]) getObject(key, locale);
    }
    
    public Object getObject() {
        return bundle.get(this.key);
    }
    
    public Object getObject(Locale locale) {
        return getBundle(locale).get(this.key);
    }
    
    public Object getString() {
        return (String) getObject(this.key);
    }
    
    public String[] getStringArray() {
        return (String[]) getObject(this.key);
    }
    
    public Object getString(Locale locale) {
        return (String) getObject(this.key, locale);
    }
    
    public String[] getStringArray(Locale locale) {
        return (String[]) getObject(this.key, locale);
    }

}
