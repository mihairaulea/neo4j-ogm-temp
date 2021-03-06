/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.drivers.http.driver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.http.request.HttpRequest;
import org.neo4j.ogm.drivers.http.transaction.HttpTransaction;
import org.neo4j.ogm.exception.ResultErrorsException;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author vince
 */

public final class HttpDriver extends AbstractConfigurableDriver
{
    private static final int CPUS = Runtime.getRuntime().availableProcessors();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDriver.class);

    private CloseableHttpClient httpClient;

    public HttpDriver() {

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal( CPUS );
        connectionManager.setDefaultMaxPerRoute( CPUS );

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        initialise( httpClient );    }

    public HttpDriver initialise( CloseableHttpClient httpClient ) {

        if (this.httpClient != null) {
            close();
        }
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            LOGGER.warn( "Unexpected Exception when closing http client httpClient: ", e );
        }
    }

    @Override
    public Request request() {
        String url = requestUrl();
        return new HttpRequest(httpClient, url, driverConfig.getCredentials());
    }

    @Override
    public Transaction newTransaction() {

        String url = newTransactionUrl();
        return new HttpTransaction(transactionManager, this, url);
    }

    public CloseableHttpResponse executeHttpRequest(HttpRequestBase request) {

        try {
            try(CloseableHttpResponse response = HttpRequest.execute(httpClient, request, driverConfig.getCredentials())) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseText = EntityUtils.toString(responseEntity);
                    LOGGER.debug( responseText );
                    EntityUtils.consume(responseEntity);
                    if (responseText.contains("\"errors\":[{") || responseText.contains("\"errors\": [{")) {
                        throw new ResultErrorsException(responseText);
                    }
                }
                return response;
            }
        }

        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }

        finally {
            request.releaseConnection();
            LOGGER.debug( "Connection released" );
        }
    }

    private String newTransactionUrl() {

        String url = transactionEndpoint(driverConfig.getURI());
        LOGGER.debug( "POST {}", url );

        CloseableHttpResponse response = executeHttpRequest(new HttpPost(url));
        Header location = response.getHeaders("Location")[0];
        try {
            response.close();
            return location.getValue();
        } catch (IOException e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }
    }

    private String autoCommitUrl() {
        return transactionEndpoint(driverConfig.getURI()).concat("/commit");
    }

    private String transactionEndpoint(String server) {
        if (server == null) {
            return null;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }

    private String requestUrl() {
        if (transactionManager != null) {
            Transaction tx = transactionManager.getCurrentTransaction();
            if (tx != null) {
                LOGGER.debug( "request url {}", ( (HttpTransaction) tx ).url() );
                return ((HttpTransaction) tx).url();
            } else {
                LOGGER.debug( "No current transaction, using auto-commit" );
            }
        } else {
            LOGGER.debug( "No transaction manager available, using auto-commit" );
        }
        LOGGER.debug( "request url {}", autoCommitUrl() );
        return autoCommitUrl();
    }
}
