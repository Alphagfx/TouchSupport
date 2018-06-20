package com.alphagfx.common;

import java.util.HashMap;
import java.util.Map;

public class MapDatabase implements UserDatabase {

    private Map<Integer, Participant> users;

    private MapDatabase() {
        users = new HashMap<>();
    }

    public static UserDatabase create() {
        return new MapDatabase();
    }

    @Override
    public Participant get(int id) {
        return users.getOrDefault(id, Participant.NULL);
    }

    @Override
    public void put(int id, Participant user) {
        users.putIfAbsent(id, user);
    }

    @Override
    public void remove(int id) {
        users.remove(id);
    }

    @Override
    public void removeAll() {
        users.clear();
    }

    @Override
    public void updateDatabaseData(int id, Participant user) {

    }

    @Override
    public void updateUserData(int id, Participant user) {
        Participant data = get(id);

//        user.
    }

    @Override
    public String toString() {
        return users.toString();
    }
}
