package org.forestwizard.goitjavacoreexamproject.html;

import javafx.scene.Node;
import org.jsoup.nodes.Element;

import java.util.List;

@FunctionalInterface
public interface HtmlTagHandler {
    void handle(List<Node> textList, Element node);
}
