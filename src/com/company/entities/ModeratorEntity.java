package com.company.entities;

/**
 * Created by LogiX on 2016-03-03.
 */
public class ModeratorEntity {

    private int id;
    private String nickname;
    private String password;
    private String channel;
    private String server;

    public ModeratorEntity(int id, String nickname, String password, String channel, String server) {
        this.id = id;
        this.nickname = nickname;
        this.password = password;
        this.channel = channel;
        this.server = server;
    }

    public ModeratorEntity() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
