package org.forestwizard.goitjavacoreexamproject.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.forestwizard.goitjavacoreexamproject.html.WikiHtmlParser;
import org.forestwizard.goitjavacoreexamproject.http.HttpServiceException;
import org.forestwizard.goitjavacoreexamproject.util.HistoryService;
import org.forestwizard.goitjavacoreexamproject.util.InternalServiceException;
import org.forestwizard.goitjavacoreexamproject.util.SearchInfo;
import org.forestwizard.goitjavacoreexamproject.wiki.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class WikiSearchController {
    private static final int ARTICLE_WINDOW_WIDTH = 800;
    private static final int ARTICLE_WINDOW_HEIGHT = 600;
    private static final String HTTP_ERROR_MESSAGE = "HTTP Error: ";
    private static final String INTERNAL_ERROR = "Internal error: ";
    private static final String INVALID_PARAMETER_ERROR = "Invalid parameter error: ";
    private static final String ALERT_ERROR_TITLE_TEXT = "Error occurred";
    private static final Logger LOGGER = Logger.getLogger(WikiSearchController.class.getSimpleName());

    WikiService wikiService;

    public WikiSearchController() {
        this.wikiService = new WikiService();
    }

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> articleList;

    @FXML
    protected void onSearchButtonClick() {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            return;
        }

        LOGGER.info(() -> "Searching " + searchField.getText());

        try {
            List<WikiArticleInfo> articles = wikiService.searchArticles(searchField.getText());
            List<String> articleTitles = articles.stream().map(WikiArticleInfo::getTitle).toList();
            articleList.setItems(FXCollections.observableArrayList(articleTitles));
        } catch (HttpServiceException e) {
            LOGGER.severe(() -> HTTP_ERROR_MESSAGE + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ALERT_ERROR_TITLE_TEXT);
            alert.setHeaderText(HTTP_ERROR_MESSAGE + e.getMessage());
            alert.showAndWait();
        } catch (IllegalArgumentException e) {
            LOGGER.severe(() -> INVALID_PARAMETER_ERROR + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ALERT_ERROR_TITLE_TEXT);
            alert.setHeaderText(INVALID_PARAMETER_ERROR + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onArticleListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
            String item = articleList.getSelectionModel().getSelectedItem();
            if (item == null || item.isEmpty()){
                return;
            }
            createArticleWindow(item);
            try {
                HistoryService.getInstance().add(
                        new SearchInfo(item, LocalDateTime.now(TimeZone.getDefault().toZoneId()))
                );
            } catch (InternalServiceException e) {
                LOGGER.severe(() -> INTERNAL_ERROR + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(ALERT_ERROR_TITLE_TEXT);
                alert.setHeaderText(INTERNAL_ERROR + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    protected void onHistoryButtonClick() {
        ListView<String> listView = new ListView<>();
        try {
            listView.setItems(FXCollections.observableArrayList(HistoryService.getInstance().listAll().stream()
                    .map(SearchInfo::getTitle).toList()
            ));
        } catch (InternalServiceException e) {
            LOGGER.severe(() -> INTERNAL_ERROR + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ALERT_ERROR_TITLE_TEXT);
            alert.setHeaderText(INTERNAL_ERROR + e.getMessage());
            alert.showAndWait();
            return;
        }

        listView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                if (item == null || item.isEmpty()) {
                    return;
                }
                createArticleWindow(item);
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        Stage articleWindow = new Stage();
        articleWindow.setMinWidth(ARTICLE_WINDOW_WIDTH);
        articleWindow.setMinHeight(ARTICLE_WINDOW_HEIGHT);
        articleWindow.setTitle("History");
        articleWindow.setScene(new Scene(new VBox(listView), ARTICLE_WINDOW_WIDTH, ARTICLE_WINDOW_HEIGHT));
        articleWindow.show();
    }

    @FXML
    protected void onExitButtonClick() {
        System.exit(0);
    }

    private void createArticleWindow(String title) {
        LOGGER.info(() -> "Requesting for page " + title);

        try {
            WikiArticleParseResponse response = wikiService.getArticlePage(title);
            WikiArticleParse parse = response.getParse();

            TextFlow textFlow = WikiHtmlParser.parse(parse);
            ScrollPane scrollPane = new ScrollPane(textFlow);
            scrollPane.setFitToWidth(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            Stage articleWindow = new Stage();
            articleWindow.setMinWidth(ARTICLE_WINDOW_WIDTH);
            articleWindow.setMinHeight(ARTICLE_WINDOW_HEIGHT);
            articleWindow.setTitle(parse.getTitle());
            articleWindow.setScene(new Scene(new VBox(scrollPane), ARTICLE_WINDOW_WIDTH, ARTICLE_WINDOW_HEIGHT));
            articleWindow.show();
        } catch (HttpServiceException e) {
            LOGGER.severe(() -> HTTP_ERROR_MESSAGE + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ALERT_ERROR_TITLE_TEXT);
            alert.setHeaderText(HTTP_ERROR_MESSAGE + e.getMessage());
            alert.showAndWait();
        }
    }
}
