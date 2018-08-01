package com.alphagfx.common.database;

import com.alphagfx.common.Participant;
import com.alphagfx.common.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SQLParticipantDatabase extends MySQLDatabase<Participant> {
    private SQLParticipantDatabase() throws ClassNotFoundException {
        super();
    }

    public static SQLParticipantDatabase create() throws ClassNotFoundException {
        return new SQLParticipantDatabase();
    }

    @Override
    public Participant get(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        List<String> values = Arrays.asList(String.valueOf(id));
        ResultSet result = executeStatement(sql, values);
        try {
            if (result.next()) {
                Participant user = Participant.create(id, result.getString(2));
                user.getData().login = result.getString(3);
                user.getData().password = result.getString(4);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(int id, Participant user) {
        String sql = "INSERT INTO users (id, name, login, password) VALUES (?, ?, ?, ?)";
        List<String> values = Arrays.asList(Integer.toString(id), user.getName(), user.getData().login, user.getData().password);
        executeStatement(sql, values);
    }

    @Override
    public void remove(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        executeStatement(sql, Arrays.asList(Integer.toString(id)));
    }

    @Override
    public void removeAll() {
        String sql = "DELETE FROM users";
        executeStatement(sql, Collections.emptyList());
    }

    @Override
    public void updateDatabaseData(int id, Participant userUpdated) {
        if (userUpdated == null) {
            return;
        }
        String sql = "UPDATE users SET name=?, login=?, password=? WHERE id=?";
        UserData data = userUpdated.getData();
        List<String> values = Arrays.asList(data.name, data.login, data.password, Integer.toString(id));
        executeStatement(sql, values);
    }

    @Override
    public void updateUserData(int id, Participant userToUpdate) {
        Participant user = get(id);
        userToUpdate.update(user);
    }
}
