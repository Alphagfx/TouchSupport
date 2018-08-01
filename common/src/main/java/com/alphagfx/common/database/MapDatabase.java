package com.alphagfx.common.database;

import com.alphagfx.common.Updatable;

import java.util.HashMap;
import java.util.Map;

public class MapDatabase<T extends Updatable<T>> implements UserDatabase<T> {

    private Map<Integer, T> users;
    private T nullObject;

    private MapDatabase(T nullObject) {
        users = new HashMap<>();
        this.nullObject = nullObject;
    }

    public static UserDatabase create() {
        return new MapDatabase(null);
    }

    public static <T extends Updatable<T>> UserDatabase create(T nullObject) {
        return new MapDatabase(nullObject);
    }

    @Override
    public T get(int id) {
        return users.getOrDefault(id, nullObject);
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
        T temp = get(id);
        temp.update(userUpdated);
        put(id, temp);
    }

    @Override
    public void updateUserData(int id, T userToUpdate) {
        userToUpdate.update(get(id));
    }

    @Override
    public String toString() {
        return users.toString();
    }
}
