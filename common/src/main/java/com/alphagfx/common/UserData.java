package com.alphagfx.common;

import java.util.Objects;

public class UserData implements Updatable<UserData> {
    private static final int ID_SKIP_MASK = -1;
    private static final String STRING_SKIP_MASK = "";

    public int id = ID_SKIP_MASK;
    public String name = STRING_SKIP_MASK;
    public String login = STRING_SKIP_MASK;
    public String password = STRING_SKIP_MASK;

    static <T> T parse(T own, T alien, T mask) {
        return Objects.equals(alien, mask) ? own : alien;
    }

    @Override
    public void update(UserData toUpdateFrom) {
        this.id = parse(id, toUpdateFrom.id, ID_SKIP_MASK);
        this.name = parse(name, toUpdateFrom.name, STRING_SKIP_MASK);
        this.login = parse(login, toUpdateFrom.login, STRING_SKIP_MASK);
        this.password = parse(password, toUpdateFrom.password, STRING_SKIP_MASK);
    }
}
