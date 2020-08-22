/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resource.configurable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;

/**
 *
 * @author jonathan essex
 */
public class ResourcePath implements ResourceMap {
        
    private List<ResourceMap> path;
    
    public ResourcePath() {
        path = new ArrayList<>();
    }
    
    public ResourcePath(ResourceMap... paths) {
        path = new ArrayList<>();
        for (ResourceMap element : paths) {
            if (element instanceof ResourcePath) {
                path.addAll(((ResourcePath)element).path);
            } else {
                path.add(element);
            }
        }
    }
    
    public ResourcePath(String... paths) {
        path = new ArrayList<>();
        for (String element : paths) {
            path.add(new BaseResourceMap(element));
        }    
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public boolean isEmpty() {
        return path.stream().allMatch(Map::isEmpty);
    }

    @Override
    public boolean containsKey(Object key) {
        return path.stream().anyMatch(map->map.containsKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public Object get(Object key) {
        return path.stream().map(map->map.get(key)).filter(Objects::nonNull).reduce(ResourcePath::merge).orElse(null);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    @Override
    public Set<String> keySet() {
        return path.stream().flatMap(map->map.keySet().stream()).collect(Collectors.toSet());
    }
    

    @Override
    public Collection<Object> values() {
        return merge(path).values();
    }
    
    private static Object merge(Object a, Object b) {
        if (a instanceof Resource && b instanceof Resource) return a;
        if (a instanceof Resource) return b;
        if (b instanceof Resource) return a;
        return new ResourcePath((ResourceMap)a,(ResourceMap)b);               
    }
    
    private static Map<String, Object> merge(List<ResourceMap> path) {
        return path.stream()
            .flatMap(map->map.entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, ResourcePath::merge));        
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return merge(path).entrySet();
    }
    
    public String toString() {
        return "[ResourcePath: " + path.toString() + "]";
                
    }
}
