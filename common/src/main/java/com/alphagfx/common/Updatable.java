package com.alphagfx.common;

public interface Updatable<T extends Updatable> {
    void update(T toUpdateFrom);
}
