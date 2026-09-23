package org.forestwizard.goitjavacoreexamproject.wiki;

import lombok.Data;

@Data
public class WikiArticleInfo {
    private Integer ns;
    private String title;
    private Integer size;
    private String snippet;
    private String timestamp;
}
