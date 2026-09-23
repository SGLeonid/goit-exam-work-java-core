package org.forestwizard.goitjavacoreexamproject.util;

import com.google.gson.Gson;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private static final String INIT_DB_SQL = "sql/init_db.sql";
    private static final String SELECT_ALL_SQL = "sql/select_all.sql";
    private static final String INSERT_SEARCH_SQL = "sql/insert_search.sql";
    private static HistoryService INSTANCE;

    private final Connection connection;
    private final String selectAllSearchesSql;
    private final String insertSearchSql;

    private HistoryService() throws InternalServiceException {
        try {
            String propertiesJson = ResourceLoader.loadResource("properties.json");
            Gson gson = new Gson();
            Properties properties = gson.fromJson(propertiesJson, Properties.class);
            String sql = ResourceLoader.loadResource(INIT_DB_SQL);
            this.selectAllSearchesSql = ResourceLoader.loadResource(SELECT_ALL_SQL);
            this.insertSearchSql = ResourceLoader.loadResource(INSERT_SEARCH_SQL);
            this.connection = DriverManager.getConnection(properties.getUrl());
            connection.createStatement().execute(sql);
        } catch (SQLException | ResourceLoaderException e) {
            throw new InternalServiceException(e.getMessage(), e);
        }
    }

    public static HistoryService getInstance() throws InternalServiceException {
        if (INSTANCE == null) {
            INSTANCE = new HistoryService();
        }
        return INSTANCE;
    }

    public void add(SearchInfo info) throws InternalServiceException {
        try (PreparedStatement statement = connection.prepareStatement(insertSearchSql)) {
            statement.setString(1, info.getTitle());
            statement.setString(2, info.getDateTime().toString());
            statement.execute();
        } catch (SQLException e) {
            throw new InternalServiceException(e.getMessage(), e);
        }
    }

    public List<SearchInfo> listAll() throws InternalServiceException {
        List<SearchInfo> searchList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(selectAllSearchesSql);
            while(resultSet.next()) {
                searchList.add(new SearchInfo(
                        resultSet.getString("title"),
                        LocalDateTime.parse(resultSet.getString("date_time"))
                ));
            }
        } catch (SQLException e) {
            throw new InternalServiceException(e.getMessage(), e);
        }

        return searchList;
    }
}
