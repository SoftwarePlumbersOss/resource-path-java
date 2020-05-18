/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedService;

/**
 *
 * @author jonathan
 */
public interface FeedServiceFactory {
    FeedService getService(String name);
}
