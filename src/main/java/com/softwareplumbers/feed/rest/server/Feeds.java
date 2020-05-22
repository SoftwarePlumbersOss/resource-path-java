/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softwareplumbers.feed.rest.server;

import com.softwareplumbers.feed.FeedExceptions;
import com.softwareplumbers.feed.FeedExceptions.InvalidPath;
import com.softwareplumbers.feed.FeedExceptions.StreamingException;
import com.softwareplumbers.feed.FeedPath;
import com.softwareplumbers.feed.FeedService;
import com.softwareplumbers.feed.Message;
import com.softwareplumbers.feed.MessageIterator;
import com.softwareplumbers.feed.impl.MessageFactory;
import com.softwareplumbers.rest.server.core.Authenticated;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jonathan
 */
@Component
@Authenticated
@Path("/feed")
public class Feeds {
    
    private static final XLogger LOG = XLoggerFactory.getXLogger(Feeds.class);
    
    private FeedServiceFactory feedServiceFactory;
    private MessageFactory messageFactory = new MessageFactory();
    
    private static class OutputConsumer implements StreamingOutput {
        private final Consumer<OutputStream> consumer;
        public OutputConsumer(Consumer<OutputStream> consumer) { this.consumer = consumer; }
        public void write(OutputStream os) { consumer.accept(os); }
        public static OutputConsumer of(Consumer<OutputStream> consumer) {
            return new OutputConsumer(consumer);
        }
    }
    
    /**
     * Use by Spring to inject a service factory for retrieval of a named repository service.
     * 
     * @param serviceFactory A factory for retrieving named services
     */
    @Autowired
    public void setFeedServiceFactory(FeedServiceFactory serviceFactory) {
        this.feedServiceFactory = serviceFactory;
    }
        
    private static URI getURI(UriInfo info, String repository, FeedPath path) {
        UriBuilder builder = info.getBaseUriBuilder().path("feed").path(repository);
        path.apply(builder, (bldr,element)->bldr.path(element.toString()));
        return builder.build();
    }
    
    @POST
    @Path("/{repository}/{feed:[^?]+}")
    @Consumes({ MediaType.APPLICATION_OCTET_STREAM })
    public Response post(
            @PathParam("repository") String repository,
            @PathParam("feed") FeedPath feedPath,
            @Context UriInfo uriInfo,
            InputStream data
    ) throws FeedExceptions.BaseException {
        LOG.entry(repository, feedPath);
        FeedService feedService = feedServiceFactory.getService(repository);
        if (feedService == null) throw LOG.throwing(new RuntimeException("feed service not found"));
        Message parsed = messageFactory.build(data, false)
            .orElseThrow(()->LOG.throwing(new RuntimeException("feed service not found")));
        Message result = feedService.post(feedPath, parsed);
        return LOG.exit(Response
            .created(getURI(uriInfo, repository, result.getName()))
            .entity(OutputConsumer.of(FeedExceptions.runtime(result::writeHeaders)))
            .build());
    }
    


    public void resume(AsyncResponse response, MessageIterator messages) {
        LOG.entry(response, messages);
        response.resume(OutputConsumer.of(os -> {
            try {
                int count = 0;
                while (messages.hasNext()) {
                    Message message = messages.next();
                    message.writeHeaders(os);
                    message.writeData(os);
                    count++;
                }
                messages.close();
                LOG.debug("wrote {} messages", count);
            } catch (StreamingException e) {
                throw FeedExceptions.runtime(e);
            } 
        })); 
        LOG.exit();
    }
    
    @GET
    @Path("/{repository}/{feed:[^?]+}")
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    public void listen(
            @PathParam("repository") String repository,
            @PathParam("feed") FeedPath feedPath,
            @QueryParam("from") Instant fromTime,
            @QueryParam("wait") @DefaultValue("0") int waitTime,
            @Suspended AsyncResponse response
    ) {
        LOG.entry(repository, feedPath, fromTime, waitTime);
        FeedService feedService = feedServiceFactory.getService(repository);
        
        if (fromTime == null) fromTime = Instant.now();
        try {
            if (waitTime > 0) {
                Consumer<MessageIterator> consumer = m->resume(response,m);
                response.setTimeout(waitTime, TimeUnit.SECONDS);
                response.setTimeoutHandler(ar -> {
                    LOG.trace("timeout {}, {}", feedPath, consumer);
                    try {
                        feedService.cancelCallback(feedPath, consumer);
                    } catch (InvalidPath path) {
                        LOG.warn("failed to cancel callback");
                    }
                    ar.resume(Response.status(Response.Status.NOT_FOUND).build());
                });
                feedService.listen(feedPath, fromTime, consumer);
            } else {
                resume(response, feedService.sync(feedPath, fromTime));
            }
        } catch (FeedExceptions.BaseException err) {
            response.resume(err);
        } catch (FeedExceptions.BaseRuntimeException err) {
            response.resume(err);
        }
        LOG.exit();
    }
    

}
