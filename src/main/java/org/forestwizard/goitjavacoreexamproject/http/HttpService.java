package org.forestwizard.goitjavacoreexamproject.http;

public interface HttpService {
    String doGet(String url) throws HttpServiceException;
}
