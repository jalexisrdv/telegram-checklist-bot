package com.jardvcode.bot.checklist.domain;

public enum BotCommand {

    GUIDE("/g"),
    OVERVIEW("/r"),
    ASSIGNMENTS("/a"),
    SECTIONS("/s");

    private String value;

    private BotCommand(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
