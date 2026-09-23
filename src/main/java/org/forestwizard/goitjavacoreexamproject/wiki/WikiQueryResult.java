package org.forestwizard.goitjavacoreexamproject.wiki;

import lombok.Data;

import java.util.List;

@Data
public class WikiQueryResult {
    private List<WikiArticleInfo> search;
}
