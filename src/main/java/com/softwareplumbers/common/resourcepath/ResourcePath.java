/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resourcepath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;

/** Simple bean allowing a resource to be fetched from one of a number of paths.
 * 
 * Essentially, functions like a classpath except can include locations not in the classpath.
 * This is better and more secure than futzing with the classpath just to load resources.
 * 
 * This is presented using a map interface so that resources can be simply accessed in Spring
 * using SPEL. For example, the SPEL expression #{@ResourcePath["file.txt"]} could be used in
 * an XML config file or @Value tag. 
 *
 * @author Jonathan Essex
 */
public class ResourcePath implements ResourceMap {
        
    private List<ResourceMap> path;
    
    public ResourcePath() {
        path = new ArrayList<>();
    }
    
    private static final List<ResourceMap> flatten(ResourceMap... paths) {
        List<ResourceMap> path = new ArrayList<>();
        for (ResourceMap element : paths) {
            if (element instanceof ResourcePath) {
                path.addAll(((ResourcePath)element).path);
            } else {
                path.add(element);
            }
        }
        return path;
    }
    
    private ResourcePath(ResourceMap... path) {
        this.path = flatten(path);
    }
    
    /** Create a new ResourcePath.
     * 
     * @param locationURIs URIs from which we will load resources.
     */
    public ResourcePath(String... locationURIs) {
        this.path = new ArrayList<>();
        for (String element : locationURIs) {
            this.path.add(new ResourcePathElement(element));
        }    
    }
    /** Set locations from which resource path will load resources.
     * 
     * This is the way we expect to initialize this in spring XML. Each 
     * 
     * @param locationURIs Array of URIs from which we will load resources.
     */
    public void setLocations(String[] locationURIs) {
        this.path = new ArrayList<>();
        for (String element : locationURIs) {
            this.path.add(new ResourcePathElement(element));
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

    /** Unsupported operation.
     * 
     * Any call to this method will generate an UnsupportedOperationException.
     * 
     * @param key
     * @param value
     */
    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    /** Unsupported operation.
     * 
     * Any call to this method will generate an UnsupportedOperationException.
     * 
     * @param key
     */
    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    /** Unsupported operation.
     * 
     * Any call to this method will generate an UnsupportedOperationException.
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("ResourcePath is read only"); 
    }

    /** Unsupported operation.
     * 
     * Any call to this method will generate an UnsupportedOperationException.
     */
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
    
    @Override
    public String toString() {
        return "[ResourcePath: " + path.toString() + "]";
                
    }
}
