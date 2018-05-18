package com.linka.lockapp.aos.module.eventbus;

public class WrongCredentialsBusEventMessage {
    public static final int SHOW = 1;
    public static final int CLOSE = 2;

    private int action;

    public WrongCredentialsBusEventMessage(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
