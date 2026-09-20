package com.jardvcode.bot.checklist.domain;


public enum StatusEnum {
    PENDING("\u23F3", "Pendiente"),
    COMPLETED("\u2705", "Completado"),
    APPROVED("\u2705", "Aprobado");

    private final String emoji;
    private final String label;

    StatusEnum(String emoji, String label) {
        this.emoji = emoji;
        this.label = label;
    }

    public String emoji() {
        return emoji;
    }

    public String label() {
        return label;
    }

}