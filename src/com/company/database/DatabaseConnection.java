package com.company.database;

import com.company.entities.ModeratorEntity;
import java.sql.*;

public class DatabaseConnection implements ModeratorDAO {

    private final static String ID = "id";
    private final static String NICKNAME = "nickname";
    private final static String PASSWORD = "password";
    private final static String CHANNEL = "channel";
    private final static String SERVER = "server";

    public DatabaseConnection() {
        Statement statement;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:fmirc.db");

            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Moderators" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nickname VARCHAR(30) NOT NULL," +
                    "channel VARCHAR(30) NOT NULL," +
                    "password VARCHAR(50) NOT NULL," +
                    "server VARCHAR(50))";
            int result = statement.executeUpdate(sql);

            //If the table was created insert first moderator
            if (result > 0) {
                sql = "INSERT INTO Moderators (nickname, channel, password) VALUES ('lanfear', '#molk', 'kanske')";
                statement.executeUpdate(sql);
            }

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean createModerator(ModeratorEntity moderator) {
        String query = "INSERT INTO Moderators VALUES (?, ?, ?, ?)";
        int result = 0;
        Connection connection;
        PreparedStatement preparedStatement;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:fmirc.db");

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, moderator.getNickname());
            preparedStatement.setString(2, moderator.getChannel());
            preparedStatement.setString(3, moderator.getPassword());
            preparedStatement.setString(4, moderator.getServer());

            result = preparedStatement.executeUpdate();

            preparedStatement.close();
            connection.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result > 0;
    }

    @Override
    public ModeratorEntity fetchModerator(String nickname, String channel) {
        String query = "SELECT * FROM Moderators WHERE "
                + NICKNAME + " = ? AND " + CHANNEL + " = ?";
        Connection connection;
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        ModeratorEntity moderatorEntity = null;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:fmirc.db");

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, channel);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                moderatorEntity = new ModeratorEntity();
                moderatorEntity.setId(resultSet.getInt(ID));
                moderatorEntity.setNickname(resultSet.getString(NICKNAME));
                moderatorEntity.setChannel(resultSet.getString(CHANNEL));
                moderatorEntity.setPassword(resultSet.getString(PASSWORD));
                moderatorEntity.setServer(resultSet.getString(SERVER));
            }

            preparedStatement.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moderatorEntity;
    }

    @Override
    public void deleteModerator(ModeratorEntity moderator) {

    }

    @Override
    public void updateModerator(ModeratorEntity moderator) {

    }
}
