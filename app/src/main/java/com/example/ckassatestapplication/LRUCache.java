package com.example.ckassatestapplication;

import android.support.v4.util.LruCache;

import java.util.List;

public class LRUCache {

    private final static int SIZE = 100;
    private static LruCache<String, List<String>> lruCache;

    public static LruCache<String, List<String>> getInstance(){
        if (lruCache == null) lruCache = new LruCache<>(SIZE);
        return lruCache;
    }
}
