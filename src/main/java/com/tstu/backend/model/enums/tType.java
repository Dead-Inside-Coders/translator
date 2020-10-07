package com.tstu.backend.model.enums;

import java.util.EnumSet;

public enum tType {
    BOOL("Boolean");

    String name;

    tType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static tType getTypeByName(String name) {
        return EnumSet.allOf(tType.class).stream().filter(el -> el.getName().equals(name)).findAny().orElseThrow();
    }
}
