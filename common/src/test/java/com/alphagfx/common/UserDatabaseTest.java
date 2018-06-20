package com.alphagfx.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UserDatabaseTest {

    private UserDatabase userDB;

    public UserDatabaseTest(UserDatabase userDB) {
        this.userDB = userDB;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        return Arrays.asList(
                new Object[][]{new Object[]{MapDatabase.create()}
                });
    }

    @Before
    public void clear() {
        userDB.removeAll();
    }

    @Test
    public void getWhileDatabaseIsEmpty() {
        assertEquals(Participant.NULL, userDB.get(0));
    }

    @Test
    public void get() {
        putUser(1, "Test -get-");

        assertEquals("Test -get-", userDB.get(1).getName());
    }

    @Test
    public void put() {
        Participant user = Participant.create(42, "testMe #42");
        userDB.put(15, user);

        assertEquals(user, userDB.get(15));
        assertEquals(Participant.NULL, userDB.get(42));
    }

    @Test
    public void remove() {
        putUser(3, "Test Mouse #3");
        assertEquals("Test Mouse #3", userDB.get(3).getName());

        userDB.remove(3);

        assertEquals(Participant.NULL, userDB.get(3));
    }

    @Test
    public void removeAll() {
        putUser(3, "Henry");

        clear();

        assertEquals(Participant.NULL, userDB.get(3));
    }


    @Test
    public void updateDatabaseData() {
        putUser(1, "Alex");
        Participant userUpdated = Participant.create(2, "Freddy");

        userDB.updateDatabaseData(1, userUpdated);

        assertEquals("Freddy", userDB.get(1).getName());
    }

    @Test
    public void updateUserData() {
        putUser(1, "Jimmy Northlight");
        Participant user = Participant.create(0, "temp");

        userDB.updateUserData(1, user);

        assertEquals(userDB.get(1).getName(), user.getName());
    }

    @Test
    public void testPrivateTestSuiteMethodPutUser() {
        putUser(12, "forget-me-now");

        assertEquals("forget-me-now", userDB.get(12).getName());
    }

    private void putUser(int id, String name) {
        Participant user = Participant.create(id, name);
        userDB.put(id, user);
    }

}