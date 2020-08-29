/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resourcepath;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/** Base resource class which defines a location from which we would like to load resources.
 *
 * @author Jonathan Essex
 */
class ResourcePathElement implements ResourceMap {
    
    private static XLogger LOG = XLoggerFactory.getXLogger(ResourcePathElement.class);
    
    private PathMatchingResourcePatternResolver resolver;
    private Map<String, Object> underlyingMap;
    private String root;
    
    private ResourcePathElement(PathMatchingResourcePatternResolver resolver, String root, Map<String, Object> underlyingMap) {
        LOG.entry(resolver, root, underlyingMap);
        this.root = root;
        this.underlyingMap = underlyingMap;
        this.resolver = resolver;
        LOG.exit();
    }
    
    public ResourcePathElement(String locationURI) {
        this(new PathMatchingResourcePatternResolver(), locationURI, null);
    }
    
    public void setLocationURI(String locationURI) {
        LOG.entry(locationURI);
        this.root = locationURI;
        this.underlyingMap = null;
        LOG.exit();
    }
    
    public String getLocationURI() {
        return root;
    }

    @Override
    public int size() {
        LOG.entry();
        return LOG.exit(getUnderlyingMap().size());
    }

    @Override
    public boolean isEmpty() {
        LOG.entry();
        return LOG.exit(getUnderlyingMap().isEmpty());
    }

    @Override
    public boolean containsKey(Object key) {
        LOG.entry();
        return LOG.exit(get(key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Resources not comparable"); 
    }

    @Override
    public Object get(Object key) {
        LOG.entry(key);
        if (underlyingMap == null) {
            String resourceURI = root + "/" + key;
            LOG.trace("Resource URI: {}", resourceURI);
            Resource resource = resolver.getResource(resourceURI);
            LOG.trace("resolver.getResource returned: {}", resource);
            if (resource != null && resource.exists()) {
                //long contentLength = 0;
                //try { contentLength = resource.contentLength(); } catch (IOException e) { /* do nothing */ }
                if (resource.isReadable()) {
                    LOG.trace("Resource is readable");
                    return LOG.exit(resource);
                } else {
                    LOG.trace("Resource is not readable");
                    return LOG.exit(new ResourcePathElement(resolver, resourceURI, null)); 
                }
            } else {
                return LOG.exit(null);
            }
        } else {
            return LOG.exit(underlyingMap.get(key));
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
        LOG.entry();
        return LOG.exit(getUnderlyingMap().keySet());
    }

    @Override
    public Collection<Object> values() {
        LOG.entry();
        return LOG.exit(getUnderlyingMap().values());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        LOG.entry();
        return LOG.exit(getUnderlyingMap().entrySet());
    }
    
    private  void underlyingPut(String[] path, Object resource) {
        LOG.entry(path, resource);
        String head = path[0];
        if (path.length > 1) {
            ResourcePathElement headMap = (ResourcePathElement)underlyingMap.compute(head, (key, value)->{
                if (value == null || value instanceof Resource) return new ResourcePathElement(resolver, root + "/" + head, new TreeMap<>());
                return value;
            });
            headMap.underlyingPut(Arrays.copyOfRange(path, 1, path.length), resource);
        } else {
            underlyingMap.put(head, resource);
        }
        LOG.exit();
    }
    
    private String[] segments(String path) {
        return path.split("/");
    }
    
    private static String munchSeparators(String path) {
        int toMunch = 0;
        while (path.charAt(toMunch) == '/') toMunch++;
        return path.substring(toMunch);
    }
    
    private static String getPath(URI uri) {
        // Bugnuts crazy URI.getPath doesn't seem to work properly (when there isnt a '/' after file:?)
        String stringURI = uri.toString();
        int separator = stringURI.indexOf(':');
        int from = separator > 0 ? separator + 1 : 0;
        return stringURI.substring(from);
    }
    
    private static String relativizePath(URI parent, URI child) {
        String pathParent = munchSeparators(getPath(parent));
        String pathChild = munchSeparators(getPath(child));
        if (pathChild.startsWith(pathParent)) 
            pathChild = pathChild.substring(pathParent.length());
        else
            throw new RuntimeException(child + " is not a subpath of " + parent);
        return munchSeparators(pathChild);
    }
    
    private synchronized Map<String, Object> getUnderlyingMap() {
        LOG.entry();
        if (underlyingMap == null) {
            underlyingMap = new TreeMap<>();
            try {
                URI baseURI = resolver.getResource(root).getURI().normalize();
                for (Resource resource: resolver.getResources(root + "/**/*")) {
                    underlyingPut(segments(relativizePath(baseURI, resource.getURI().normalize())), resource);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return LOG.exit(underlyingMap);
    }    
    
    public String toString() {
        return "[ResourceMap of: " + root + "]";
    }
}
