package com.linka.lockapp.aos.module.eventbus;

public class InviteUserBusEvent {
    private String email;

    public InviteUserBusEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
