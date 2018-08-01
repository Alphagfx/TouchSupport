package com.alphagfx.common.database;

import com.alphagfx.common.Participant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UserDatabaseTest {

    private UserDatabase<Participant> userDB;
    private Object nullObject;

    public UserDatabaseTest(UserDatabase userDB, Object nullObject) {
        this.userDB = userDB;
        this.nullObject = nullObject;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() throws ClassNotFoundException {
        return Arrays.asList(
                new Object[]{MapDatabase.create(), null},
                new Object[]{MapDatabase.create(Participant.NULL), Participant.NULL},
                new Object[]{SQLParticipantDatabase.create(), null});
    }

    @Before
    public void clear() {
        userDB.removeAll();
    }

    @Test
    public void getWhileDatabaseIsEmpty() {
        Assert.assertEquals(nullObject, userDB.get(0));
    }

    @Test
    public void get() {
        putUser(1, "Test -get-");

        assertEquals("Test -get-", userDB.get(1).getName());
    }

    @Test
    public void put() {
        Participant user = Participant.create(42, "testMe #42");
        user.getData().login = "unique_login";
        user.getData().password = "random password";

        userDB.put(15, user);

        user.getData().id = 15;
        assertEquals(user.getName(), userDB.get(15).getName());
        assertEquals(nullObject, userDB.get(42));
    }

    @Test
    public void remove() {
        putUser(3, "Test Mouse #3");
        assertEquals("Test Mouse #3", userDB.get(3).getName());

        userDB.remove(3);

        assertEquals(nullObject, userDB.get(3));
    }

    @Test
    public void removeAll() {
        putUser(3, "Henry");

        clear();

        assertEquals(nullObject, userDB.get(3));
    }


    @Test
    public void updateDatabaseData() {
        Participant user = Participant.create(1, "Alex");
        user.getData().login = "default_my_login";
        userDB.put(1, user);

        Participant userUpdated = Participant.create(2, "Freddy");
        userUpdated.getData().login = "very_unique_login";
        userUpdated.getData().password = "happy password";

        userDB.updateDatabaseData(1, userUpdated);

        assertEquals("Freddy", userDB.get(1).getName());
    }

    @Test
    public void updateDatabaseFromNull() {
        putUser(1, "Tom");
        userDB.updateDatabaseData(1, (Participant) nullObject);
    }

    @Test
    public void updateUserData() {
        putUser(1, "Jimmy Northlight");
        Participant user = Participant.create(0, "temp");

        userDB.updateUserData(1, user);

        assertEquals(userDB.get(1).getName(), user.getName());
    }

    @Test
    public void updateUserFromNull() {
        String name = "temp#1";
        Participant user = Participant.create(0, name);
        userDB.updateUserData(1, user);
        assertEquals(name, user.getName());
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