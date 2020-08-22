/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resource.configurable;


import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jonat
 */
public class TestResourceMap {
    
    @Test
    public void testFetchClasspathResource() {
        ResourceMap map = new ResourceMap("classpath:/config");
        assertThat(map.get("resource1.txt"), notNullValue());
        assertThat(map.get("resource2.txt"), notNullValue());
        assertThat(map.get("nothing"), nullValue());
    }
    
    @Test
    public void testClasspathResourceCount() {
        ResourceMap map = new ResourceMap("classpath:/config");
        assertThat(map.size(), equalTo(3));
    }

    @Test
    public void testClasspathResourceSubdir() {
        ResourceMap map = new ResourceMap("classpath:/config");
        ResourceMap sub = (ResourceMap)map.get("sub");
        assertThat(sub, notNullValue());
        assertThat(sub.get("resource3.txt"), notNullValue());
        assertThat(sub.get("nothing"), nullValue());
        assertThat(sub.size(), equalTo(1));
    }    
}
