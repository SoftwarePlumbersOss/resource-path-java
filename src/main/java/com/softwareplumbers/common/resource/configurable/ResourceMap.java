/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.common.resource.configurable;

import java.util.Map;

/** Basic interface for ResourcePath and internal components.
 * 
 * A ResourceMap maps a String key to either a spring Resource object
 * or another ResourceMap object.
 *
 * @author Jonathan Essex.
 */
public interface ResourceMap extends Map<String,Object> {
    
}
