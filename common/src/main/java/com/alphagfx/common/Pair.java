package com.alphagfx.common;

public class Pair<L, R> {
    private L l;
    private R r;

    private Pair() {

    }

    public static <L, R> Pair of(L l, R r) {
        return new Pair().set(l, r);
    }

    public Pair set(L l, R r) {
        this.l = l;
        this.r = r;

        return this;
    }

    public L getKey() {
        return l;
    }

    public R getValue() {
        return r;
    }

}
