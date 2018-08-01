package com.alphagfx.common.database;

import com.alphagfx.common.Updatable;

public interface UserDatabase<T extends Updatable<T>> {

    T get(int id);

    void put(int id, T user);

    void remove(int id);

    void removeAll();

    void updateDatabaseData(int id, T userUpdated);

    void updateUserData(int id, T userToUpdate);
}
