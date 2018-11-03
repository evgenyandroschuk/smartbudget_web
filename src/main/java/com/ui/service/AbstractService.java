package com.ui.service;

import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.Map;

public abstract class AbstractService {

    protected CloseableHttpClient client = HttpClients.createDefault();
    protected String user;
    protected String password;

    protected void authRequest(HttpRequest request) throws AuthenticationException {
        UsernamePasswordCredentials basicAuth = new UsernamePasswordCredentials(user, password);
        request.addHeader(new BasicScheme().authenticate(basicAuth, request, null));
    }

}
