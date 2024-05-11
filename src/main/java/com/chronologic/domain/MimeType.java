package com.chronologic.domain;

import java.util.Arrays;

public enum MimeType {
    IMAGE("image"),
    VIDEO("video");

    private final String type;

    MimeType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MimeType getEnum(String type) {
        return Arrays.stream(MimeType.values())
                .filter(el -> type.contains(el.getType()))
                .findFirst()
                .orElse(null);
    }

    public static boolean isSupportedType(String type) {
        return Arrays.stream(MimeType.values()).anyMatch(el -> type.contains(el.getType()));
    }

}
