package com.alphagfx.common.database;

import com.alphagfx.common.Updatable;

import java.sql.*;
import java.util.List;

public abstract class MySQLDatabase<T extends Updatable<T>> implements UserDatabase<T> {

    private String address = "jdbc:mysql://localhost/mydb";
    private String login = "root";
    private String password = "lambda123";

    MySQLDatabase() throws ClassNotFoundException {
        // Load the JDBC driver
        Class.forName("org.mariadb.jdbc.Driver");
    }

    protected ResultSet executeStatement(String statement, List<String> values) {
        try (Connection connection = DriverManager.getConnection(address, login, password);
             PreparedStatement st = connection.prepareStatement(statement)) {

            for (int i = 0; i < values.size(); i++) {
                st.setString(i + 1, values.get(i));
                System.out.println("Setting string " + i + " : " + values.get(i));
            }

            ResultSet resultSet = st.executeQuery();

            System.out.println("It works!");
            return resultSet;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
