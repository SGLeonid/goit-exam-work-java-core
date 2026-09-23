package org.forestwizard.goitjavacoreexamproject.wiki;

import com.google.gson.Gson;
import org.forestwizard.goitjavacoreexamproject.http.HttpService;
import org.forestwizard.goitjavacoreexamproject.http.HttpServiceException;
import org.forestwizard.goitjavacoreexamproject.http.JsoupHttpService;

import java.util.List;

public class WikiService {
    private static final String WIKI_API_BASE_URL = "https://en.wikipedia.org/w/api.php";
    private static final String WIKI_SEARCH_URL_PARAMS = "?action=query&list=search&srsearch=%s&format=json&origin=*";
    private static final String WIKI_ARTICLE_URL_PARAMS = "?action=parse&page=%s&format=json";

    private final HttpService httpService;
    private final Gson gson;

    public WikiService() {
        this.httpService = new JsoupHttpService();
        this.gson = new Gson();
    }

    public List<WikiArticleInfo> searchArticles(String query) throws HttpServiceException {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("Query string is null or empty");
        }
        String url = WIKI_API_BASE_URL + String.format(WIKI_SEARCH_URL_PARAMS, query);
        String json = httpService.doGet(url);
        WikiSearchResponse response = gson.fromJson(json, WikiSearchResponse.class);
        return response.getQuery().getSearch();
    }

    public WikiArticleParseResponse getArticlePage(String title) throws HttpServiceException {
        String canonicalTitle = title.replace(" ", "_");
        String url = WIKI_API_BASE_URL + String.format(WIKI_ARTICLE_URL_PARAMS, canonicalTitle);
        String json = httpService.doGet(url);
        return gson.fromJson(json, WikiArticleParseResponse.class);
    }
}
