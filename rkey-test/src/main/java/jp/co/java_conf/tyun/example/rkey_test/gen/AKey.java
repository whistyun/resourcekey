package jp.co.java_conf.tyun.example.rkey_test.gen;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class AKey {
    /** foo */
    public static final AKey elem1 = new AKey("elem1");
    /** bar */
    public static final AKey elem2 = new AKey("elem2");

    private String key;
    
    private AKey(String key){
        this.key = key;
    }

    public static class test2 {
        /** hoge */
        public static final AKey elem1 = new AKey("test2.elem1");
        /** fuga */
        public static final AKey elem2 = new AKey("test2.elem2");

    }
    public static class test3 {
        /** abcd */
        public static final AKey elem1 = new AKey("test3.elem1");
        /** efgh */
        public static final AKey elem2 = new AKey("test3.elem2");
        /** ijkl */
        public static final AKey elem3 = new AKey("test3.elem3");

        public static class telem3 {
            /** x */
            public static final AKey p1 = new AKey("test3.telem3.p1");
            /** y */
            public static final AKey p2 = new AKey("test3.telem3.p2");
            /** z */
            public static final AKey p3 = new AKey("test3.telem3.p3");

        }
    }
    private static final String bundleName = "res1.AKey";
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
