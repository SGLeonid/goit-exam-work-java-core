package org.forestwizard.goitjavacoreexamproject.http;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;

public class JsoupHttpService implements HttpService {
    @Override
    public String doGet(String url) throws HttpServiceException {
        try {
            Connection.Response response = Jsoup.connect(url).ignoreContentType(true).execute();
            if (response.statusCode() != 200) {
                throw new HttpServiceException("HTTP Status " + response.statusCode());
            }

            return response.body();
        } catch (IOException e) {
            throw new HttpServiceException("I/O error: " + e.getMessage(), e);
        }
    }
}
