package com.alphagfx.common.database;

public class MySQLDatabase<T> implements UserDatabase<T> {

    public static MySQLDatabase create() {
        return new MySQLDatabase();
    }

    @Override
    public T get(int id) {
        return null;
    }

    @Override
    public void put(int id, T user) {

    }

    @Override
    public void remove(int id) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void updateDatabaseData(int id, T userUpdated) {

    }

    @Override
    public void updateUserData(int id, T userToUpdate) {

    }
}
