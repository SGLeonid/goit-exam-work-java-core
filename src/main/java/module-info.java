module org.forestwizard.goitjavacoreexamproject {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.jsoup;
    requires com.google.gson;
    requires static lombok;
    requires java.desktop;
    requires java.logging;
    requires java.sql;

    opens org.forestwizard.goitjavacoreexamproject.wiki to com.google.gson;
    opens org.forestwizard.goitjavacoreexamproject to javafx.fxml;
    exports org.forestwizard.goitjavacoreexamproject;
    exports org.forestwizard.goitjavacoreexamproject.ui;
    opens org.forestwizard.goitjavacoreexamproject.ui to javafx.fxml;
    exports org.forestwizard.goitjavacoreexamproject.util;
    opens org.forestwizard.goitjavacoreexamproject.util to com.google.gson;
}