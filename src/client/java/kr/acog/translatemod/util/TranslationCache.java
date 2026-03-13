package kr.acog.translatemod.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationCache {

    public static Map<String, String> create(int maxSize) {
        return Collections.synchronizedMap(
                new LinkedHashMap<>(maxSize + 1, .75F, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                        return size() > maxSize;
                    }
                });
    }

}
