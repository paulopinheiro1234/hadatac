package org.hadatac.data.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Cache<K, V> implements Map<K, V> {
    private String name;
    private boolean needCommit = false;
    
    private Map<K, V> mapCache = new HashMap<K, V>();
    
    public Cache(String name, boolean needCommit) {
        this.name = name;
        this.needCommit = needCommit;
    }
    
    public Cache(String name, boolean needCommit, Map<K, V> mapCache) {
        this(name, needCommit);
        this.mapCache = mapCache;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean getNeedCommit() {
        return needCommit;
    }
    
    public Map<K, V> getMapCache() {
        return mapCache;
    }
    
    public void clear() {
        mapCache.clear();
    }

    @Override
    public int size() {
        return mapCache.size();
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return mapCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return mapCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return mapCache.containsValue(value);
    }

    @Override
    public V get(Object key) {
        // TODO Auto-generated method stub
        return mapCache.get(key);
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return mapCache.put(key, value);
    }

    @Override
    public V remove(Object key) {
        // TODO Auto-generated method stub
        return mapCache.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        // TODO Auto-generated method stub
        mapCache.putAll(m);
    }

    @Override
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        return mapCache.keySet();
    }

    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return mapCache.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return mapCache.entrySet();
    }
}
