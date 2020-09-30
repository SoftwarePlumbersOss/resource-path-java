/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resourcepath;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.Resource;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jonat
 */
public class DerivedMap<T> implements Map<String,Object> {
    
    private final ResourceMap base;
    private final Function<Resource,T> transformer;
    
    public DerivedMap(ResourceMap base, Function<Resource,T> transformer) {
        this.transformer = transformer;
        this.base = base;
    }
    
    Object transform(Object o) {
        if (o instanceof Resource) 
            return transformer.apply(((Resource)o));
        else 
            return new DerivedMap((ResourceMap)o, transformer);
    }
    
    SimpleImmutableEntry<String,Object> transform(Map.Entry<String,Object> entry) {
        return new SimpleImmutableEntry<>(entry.getKey(), transform(entry.getValue()));
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return base.values().stream()
            .map(this::transform)
            .anyMatch(value::equals);
    }

    @Override
    public Object get(Object key) {
        return transform(base.get(key));
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> keySet() {
        return base.keySet();
    }

    @Override
    public Collection<Object> values() {
        return base.values().stream().map(this::transform).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return base.entrySet().stream()
            .map(this::transform)
            .collect(Collectors.toSet());
    }    
}
