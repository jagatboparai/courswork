package com.mlops.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API observability filter that logs the HTTP method, request URI,
 * and final response status code for every incoming and outgoing request.
 *
 * Implements both ContainerRequestFilter and ContainerResponseFilter
 * to provide full request/response lifecycle logging.
 *
 * Metadata extractable from contexts (Part 5.5 question):
 *  - ContainerRequestContext: getUriInfo().getRequestUri() (full URI),
 *    getMethod() (HTTP verb), getHeaders() (Authorization, Content-Type, etc.)
 *  - ContainerResponseContext: getStatus() (response code),
 *    getHeaders() (Cache-Control, Content-Type, Location),
 *    getMediaType() (response content type)
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    // Property key used to pass the start time through request context
    private static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * Fired before the resource method is called.
     * Logs the HTTP method and full request URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(REQUEST_START_TIME, startTime);

        String method  = requestContext.getMethod();
        String uri     = requestContext.getUriInfo().getRequestUri().toString();
        String headers = requestContext.getHeaderString("Content-Type") != null
                ? requestContext.getHeaderString("Content-Type") : "none";

        LOGGER.info(String.format(
                "[REQUEST]  method=%s  uri=%s  content-type=%s",
                method, uri, headers));
    }

    /**
     * Fired after the resource method has completed and a response is ready.
     * Logs the HTTP status code and the time taken to process the request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        int    status       = responseContext.getStatus();
        String method       = requestContext.getMethod();
        String uri          = requestContext.getUriInfo().getRequestUri().toString();
        String contentType  = responseContext.getMediaType() != null
                ? responseContext.getMediaType().toString() : "none";

        Long startTime  = (Long) requestContext.getProperty(REQUEST_START_TIME);
        long durationMs = startTime != null ? System.currentTimeMillis() - startTime : -1L;

        LOGGER.info(String.format(
                "[RESPONSE] method=%s  uri=%s  status=%d  content-type=%s  duration=%dms",
                method, uri, status, contentType, durationMs));
    }
}
