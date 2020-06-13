/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedExceptions.InvalidPath;
import com.softwareplumbers.feed.FeedPath;
import com.softwareplumbers.feed.Message;
import com.softwareplumbers.feed.TestFeedService;
import com.softwareplumbers.feed.test.TestUtils;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author jonathan
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class)
@EnableConfigurationProperties
@ContextConfiguration(classes = { LocalConfig.class }, locations= { "/services.xml" })
public class TestServer extends TestFeedService  {
    
    @Test 
    public void testUsernamePresentInPostedMessage() throws InvalidPath {
        Message message = TestUtils.generateMessage(TestUtils.randomFeedPath());
        Message result = service.post(FeedPath.ROOT.add("test"), message);
        assertThat(result.getSender(), equalTo("DEFAULT_SERVICE_ACCOUNT"));
    }
}

