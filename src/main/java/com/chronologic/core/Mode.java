package com.chronologic.core;

import java.util.Arrays;

public enum Mode {

    NATIVE("nativeModeBtn"),
    CUSTOM("customModeBtn");

    private final String modeButtonId;

    Mode(String modeButtonId) {
        this.modeButtonId = modeButtonId;
    }

    public static Mode getEnum(String modeBtnId) {
        return Arrays.stream(Mode.values())
                .filter(el -> modeBtnId.equals(el.getModeButtonId()))
                .findFirst()
                .orElse(null);
    }

    public String getModeButtonId() {
        return modeButtonId;
    }

}

