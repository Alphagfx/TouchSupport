package com.alphagfx.common.database;

import com.alphagfx.common.Participant;

import java.util.HashMap;
import java.util.Map;

public class MapDatabase<T> implements UserDatabase<T> {

    private Map<Integer, T> users;

    private MapDatabase() {
        users = new HashMap<>();
    }

    public static UserDatabase create() {
        return new MapDatabase();
    }

    @Override
    public T get(int id) {
        return users.getOrDefault(id, (T) Participant.NULL);
    }

    @Override
    public void put(int id, T user) {
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
    public void updateDatabaseData(int id, T userUpdated) {

    }

    @Override
    public void updateUserData(int id, T userToUpdate) {
        T data = get(id);

//        user.
    }

    @Override
    public String toString() {
        return users.toString();
    }
}
