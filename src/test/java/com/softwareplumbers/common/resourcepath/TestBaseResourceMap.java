/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resourcepath;


import com.softwareplumbers.common.resourcepath.ResourcePathElement;
import com.softwareplumbers.common.resourcepath.ResourceMap;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

/**
 *
 * @author jonat
 */
public class TestBaseResourceMap {
    
    String baseResourcePath = System.getProperty("test.resources");
    
    @Test
    public void testFetchClasspathResource() {
        ResourcePathElement map = new ResourcePathElement("classpath:/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
    }
    
    @Test
    public void testClasspathResourceCount() {
        ResourcePathElement map = new ResourcePathElement("classpath:/config");
        assertThat(map.size(), equalTo(3));
    }

    @Test
    public void testClasspathResourceSubdir() {
        ResourcePathElement map = new ResourcePathElement("classpath:/config");
        ResourcePathElement sub = (ResourcePathElement)map.get("sub");
        assertThat(sub, notNullValue());
        assertThat(sub.get("resource3.txt"), notNullValue());
        assertThat(sub.get("nothing"), nullValue());
        assertThat(sub.size(), equalTo(1));
    }    
    
    @Test
    public void testFetchFilesystemResource() {
        ResourcePathElement map = new ResourcePathElement("file:" + baseResourcePath + "/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
    }
    
    @Test
    public void testFilesystemResourceCount() {
        ResourcePathElement map = new ResourcePathElement("file:" + baseResourcePath + "/config");
        assertThat(map.size(), equalTo(3));
    }

    @Test
    public void testFilesystemResourceSubdir() {
        ResourcePathElement map = new ResourcePathElement("file:" + baseResourcePath + "/config");
        ResourceMap sub = (ResourceMap)map.get("sub");
        assertThat(sub, notNullValue());
        assertThat(sub.get("resource3.txt"), notNullValue());
        assertThat(sub.get("nothing"), nullValue());
        assertThat(sub.size(), equalTo(1));
    }    
}
