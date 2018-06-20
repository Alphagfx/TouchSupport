package com.alphagfx.common;

public interface UserDatabase {

    Participant get(int id);

    void put(int id, Participant user);

    void remove(int id);

    void removeAll();

    void updateDatabaseData(int id, Participant user);

    void updateUserData(int id, Participant user);
}
