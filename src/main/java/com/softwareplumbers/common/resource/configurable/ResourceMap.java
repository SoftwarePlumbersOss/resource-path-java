/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resource.configurable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 *
 * @author jonat
 */
public class ResourceMap implements Map<String,Object> {
    
    private PathMatchingResourcePatternResolver resolver;
    private Map<String, Object> underlyingMap;
    private String root;
    
    private ResourceMap(PathMatchingResourcePatternResolver resolver, String root, Map<String, Object> underlyingMap) {
        this.root = root;
        this.underlyingMap = underlyingMap;
        this.resolver = resolver;
    }
    
    public ResourceMap(String root) {
        this(new PathMatchingResourcePatternResolver(), root, null);
    }

    @Override
    public int size() {
        return getUnderlyingMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getUnderlyingMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Resources not comparable"); 
    }

    @Override
    public Object get(Object key) {
        if (underlyingMap == null) {
            String resourceURI = root + "/" + key;
            Resource resource = resolver.getResource(resourceURI);
            if (resource.exists()) {
                long contentLength = 0;
                try { contentLength = resource.contentLength(); } catch (IOException e) { /* do nothing */ }
                if (contentLength > 0)
                    return resource;
                else
                    return new ResourceMap(resolver, resourceURI, null); 
            } else {
                return null;
            }
        } else {
            return underlyingMap.get(key);
        }
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("Map is unmodifiable"); 
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Map is unmodifiable"); 
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("Map is unmodifiable"); 
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Map is unmodifiable"); 
    }

    @Override
    public Set<String> keySet() {
        return getUnderlyingMap().keySet();
    }

    @Override
    public Collection<Object> values() {
        return getUnderlyingMap().values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return getUnderlyingMap().entrySet();
    }
    
    private  void underlyingPut(String[] path, Object resource) {
        String head = path[0];
        if (path.length > 1) {
            ResourceMap headMap = (ResourceMap)underlyingMap.compute(head, (key, value)->{
                if (value == null || value instanceof Resource) return new ResourceMap(resolver, root + "/" + head, new TreeMap<>());
                return value;
            });
            headMap.underlyingPut(Arrays.copyOfRange(path, 1, path.length), resource);
        } else {
            underlyingMap.put(head, resource);
        }
    }
    
    private String[] segments(String path) {
        return path.split("/");
    }
    
    private synchronized Map<String, Object> getUnderlyingMap() {
        if (underlyingMap == null) {
            underlyingMap = new TreeMap<>();
            try {
                URI baseURI = resolver.getResource(root).getURI();
                for (Resource resource: resolver.getResources(root + "/**/*")) {
                    URI relativePath = baseURI.relativize(resource.getURI());
                    underlyingPut(segments(relativePath.getPath()), resource);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return underlyingMap;
    }    
}
