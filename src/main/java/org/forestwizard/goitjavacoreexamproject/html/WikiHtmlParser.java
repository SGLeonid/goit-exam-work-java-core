package org.forestwizard.goitjavacoreexamproject.html;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.*;
import org.forestwizard.goitjavacoreexamproject.wiki.WikiArticleParse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WikiHtmlParser {
    private static final String ARTICLE_FONT = "Times New Roman";
    private static final String CODE_FONT = "Consolas";
    private static final int ARTICLE_CONTENT_FONT_SIZE = 18;
    private static final int ARTICLE_HEADING_1_FONT_SIZE = 32;
    private static final Map<String, HtmlTagHandler> TAG_HANDLER_MAP = Map.of(
            "ol", WikiHtmlParser::parseOl,
            "ul", WikiHtmlParser::parseUl,
            "table", WikiHtmlParser::parseTable,
            "p", WikiHtmlParser::parseP,
            "dl", WikiHtmlParser::parseP,
            "div", WikiHtmlParser::parseDiv
    );
    private static final Map<String, Integer> H_FONT_SIZE_LOOKUP_MAP = Map.of(
            "h1", 32, "h2", 28, "h3", 24, "h4", 22, "h5", 20, "h6", 18
    );

    private WikiHtmlParser() {}

    public static TextFlow parse(WikiArticleParse parse) {
        List<Node> textList = new ArrayList<>();

        Text titleText = new Text(parse.getTitle() + "\n");
        titleText.setFont(Font.font(ARTICLE_FONT, FontWeight.BOLD, ARTICLE_HEADING_1_FONT_SIZE));
        textList.add(titleText);

        Elements elements = Jsoup.parse(parse.getText().get("*")).getAllElements();
        Iterator<Element> iterator = elements.iterator();
        Element element = iterator.next();
        while (iterator.hasNext()) {
            if (element.tagName().equals("p") && !element.hasClass("mw-empty-elt")) {
                break;
            }
            element = iterator.next();
        }

        do {
            if (element.tagName().startsWith("h")) {
                Integer fontSize = Optional.of(
                        H_FONT_SIZE_LOOKUP_MAP.get(element.tagName())
                ).orElse(ARTICLE_CONTENT_FONT_SIZE);

                Text text = new Text(element.text());
                text.setFont(Font.font(ARTICLE_FONT, FontWeight.BOLD, fontSize));
                textList.add(text);
                textList.add(new Text("\n\n"));
            }

            if (TAG_HANDLER_MAP.containsKey(element.tagName())) {
                TAG_HANDLER_MAP.get(element.tagName()).handle(textList, element);
            }

            element = iterator.next();
        } while (!element.id().equals("External_links") && iterator.hasNext());

        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(20.0f));
        textFlow.getChildren().addAll(textList);
        return textFlow;
    }

    private static void parseNode(List<Node> uiNodes, org.jsoup.nodes.Node htmlNode, String font) {
        Set<String> ignoredTags = Set.of("ul", "ol", "sup");
        if (ignoredTags.contains(htmlNode.nodeName())) {
            return;
        }

        if (htmlNode.nodeName().equals("#text") || htmlNode.nodeName().equals("a")) {
            Text text = new Text(htmlNode.nodeValue());
            text.setFont(Font.font(font, FontPosture.REGULAR, ARTICLE_CONTENT_FONT_SIZE));
            uiNodes.add(text);
            return;
        }

        if (htmlNode.nodeName().equals("b") || htmlNode.nodeName().equals("dt")) {
            Text text = new Text(htmlNode.nodeValue());
            text.setFont(Font.font(font, FontWeight.BOLD, ARTICLE_CONTENT_FONT_SIZE));
            uiNodes.add(text);
            return;
        }

        if (htmlNode.nodeName().equals("i")) {
            Text text = new Text(htmlNode.nodeValue());
            text.setFont(Font.font(font, FontPosture.ITALIC, ARTICLE_CONTENT_FONT_SIZE));
            uiNodes.add(text);
            return;
        }

        for (var node : htmlNode.childNodes()) {
            parseNode(uiNodes, node, font);
        }
    }

    private static void parseP(List<Node> uiNodes, Element element) {
        for (var htmlNode : element.childNodes()) {
            parseNode(uiNodes, htmlNode, ARTICLE_FONT);
        }
        uiNodes.add(new Text("\n\n"));
    }

    private static void parseOl(List<Node> uiNodes, Element element) {
        int index = 1;
        for (var liNode : element.childNodes()) {
            if (liNode.nodeName().equals("li")) {
                Text indexText = new Text(index++ + ". ");
                indexText.setFont(new Font(ARTICLE_FONT, ARTICLE_CONTENT_FONT_SIZE));
                uiNodes.add(indexText);

                parseNode(uiNodes, liNode, ARTICLE_FONT);
                uiNodes.add(new Text("\n\n"));
            }
        }
    }

    private static void parseUl(List<Node> uiNodes, Element element) {
        for (var liNode : element.childNodes()) {
            if (liNode.nodeName().equals("li")) {
                Text indexText = new Text("* ");
                indexText.setFont(new Font(ARTICLE_FONT, ARTICLE_CONTENT_FONT_SIZE));
                uiNodes.add(indexText);

                parseNode(uiNodes, liNode, ARTICLE_FONT);
                uiNodes.add(new Text("\n\n"));
            }
        }
    }

    private static void parseTable(List<Node> uiNodes, Element element) {
        if (element.hasClass("wikitable")) {
            Elements captionElements = element.getElementsByTag("caption");
            if (!captionElements.isEmpty()) {
                Text captionText = new Text(captionElements.getFirst().text() + "\n\n");
                captionText.setFont(Font.font(ARTICLE_FONT, FontWeight.BOLD, ARTICLE_CONTENT_FONT_SIZE));
                uiNodes.add(captionText);
            }

            Element tableBody = element.getElementsByTag("tbody").getFirst();
            TableView<List<String>> table = new TableView<>();
            List<TableColumn<List<String>, String>> columns = new ArrayList<>();

            var tableRowIterator = tableBody.childNodes().iterator();
            if (tableRowIterator.hasNext()) {
                var tableHeaderRow = tableRowIterator.next();
                if (tableHeaderRow.nodeName().equals("tr")) {
                    columns = parseTableHeaders(tableHeaderRow);
                }
            }
            table.getColumns().addAll(columns);

            while (tableRowIterator.hasNext()) {
                var tableRow = tableRowIterator.next();
                if (tableRow.nodeName().equals("tr")) {
                    parseTableRows(table, tableRow);
                }
            }

            table.setEditable(false);
            uiNodes.add(table);
            uiNodes.add(new Text("\n\n"));
        }
    }

    private static void parseTableRows(TableView<List<String>> table, org.jsoup.nodes.Node tableRow) {
        List<String> rowList = new ArrayList<>();
        tableRow.nodeStream().filter(node -> node.nodeName().equals("td")).forEach(node -> {
            StringBuilder builder = new StringBuilder();
            node.nodeStream().filter(textNode -> textNode.nodeName().equals("#text"))
                    .forEach(textNode -> builder.append(textNode.nodeValue()));
            rowList.add(builder.toString());
        });
        table.getItems().add(rowList);
    }

    private static List<TableColumn<List<String>, String>> parseTableHeaders(org.jsoup.nodes.Node tableRow) {
        List<TableColumn<List<String>, String>> columns = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();
        tableRow.nodeStream().filter(node -> node.nodeName().equals("th")).forEach(node -> {
            TableColumn<List<String>, String> column = new TableColumn<>(node.nodeValue());
            final int finalIndex = index.getAndIncrement();
            column.setCellValueFactory(
                    list -> new SimpleStringProperty(list.getValue().get(finalIndex))
            );
            columns.add(column);
        });
        return columns;
    }

    private static void parseDiv(List<Node> uiNodes, Element element) {
        if (element.classList().stream().anyMatch(str -> str.startsWith("mw-highlight-lang-"))) {
            Text text = new Text(element.text());
            text.setFont(new Font(CODE_FONT, ARTICLE_CONTENT_FONT_SIZE));
            uiNodes.add(text);
            uiNodes.add(new Text("\n\n"));
        }
    }
}
