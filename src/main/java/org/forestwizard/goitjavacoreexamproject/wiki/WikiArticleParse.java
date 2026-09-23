package org.forestwizard.goitjavacoreexamproject.wiki;

import lombok.Data;

import java.util.Map;

@Data
public class WikiArticleParse {
    private String title;
    private Map<String, String> text;
}
