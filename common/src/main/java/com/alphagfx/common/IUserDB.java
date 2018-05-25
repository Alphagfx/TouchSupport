package com.alphagfx.common;

public interface IUserDB {

    Participant getUserById(int id);

    void putUser(Participant user);
}
