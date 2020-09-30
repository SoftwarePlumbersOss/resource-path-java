/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resourcepath;

import com.softwareplumbers.common.pipedstream.InputStreamSupplier;
import com.softwareplumbers.common.resourcepath.ResourceMap;
import com.softwareplumbers.common.resourcepath.ResourcePath;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.core.io.Resource;

/**
 *
 * @author jonat
 */
public class TestResourcePath {    
    
    String baseResourcePath = System.getProperty("test.resources");
    
    String readAll(InputStreamSupplier supplier) throws IOException {
        try (InputStream is = supplier.get()) {
            StringBuilder result = new StringBuilder();
            int i;
            while ((i = is.read()) >= 0) result.append((char)i);
            return result.toString();
        }
    }

    @Test
    public void testFetchResource() throws IOException {
        ResourcePath map = new ResourcePath("file:"+baseResourcePath+"/altconfig", "classpath:/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("resource4.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
        assertThat(readAll((InputStreamSupplier)map.getStreams().get("resource1.txt")), equalToIgnoringWhiteSpace("Sea Shells"));
    }
    
    @Test
    public void testFetchResourceAsStream() throws IOException {
        ResourcePath map = new ResourcePath("file:"+baseResourcePath+"/altconfig", "classpath:/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("resource4.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
        assertThat(readAll(()->((Resource)map.get("resource1.txt")).getInputStream()), equalToIgnoringWhiteSpace("Sea Shells"));
    }

    @Test
    public void testFetchResourceAsPath() throws IOException {
        ResourcePath map = new ResourcePath("file:"+baseResourcePath+"/altconfig", "classpath:/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("resource4.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
        assertThat(map.getPaths().get("resource1.txt"), equalTo(Paths.get(baseResourcePath, "altconfig", "resource1.txt")));
    }
    
    @Test
    public void testResourceCount() {
        ResourcePath map = new ResourcePath("file:"+baseResourcePath+"/altconfig", "classpath:/config");
        assertThat(map.size(), equalTo(4));
    }    
    
    @Test
    public void testResourceSubdir() {
        ResourcePath map = new ResourcePath("file:"+baseResourcePath+"/altconfig", "classpath:/config");
        assertThat(map.get("sub"), any((Class)ResourceMap.class));        
        ResourceMap sub = (ResourceMap)map.get("sub");
        assertThat(sub, notNullValue());
        assertThat(sub.get("resource3.txt"), notNullValue());
        assertThat(sub.get("resource5.txt"), notNullValue());
        assertThat(sub.get("nothing"), nullValue());
        assertThat(sub.size(), equalTo(2));
    }    
        
}
