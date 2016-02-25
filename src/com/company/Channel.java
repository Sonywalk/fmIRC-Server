package com.company;

import java.util.ArrayList;

/**
 * Created by LogiX on 2016-02-24.
 */
public class Channel {
    private String id;
    private ArrayList<ConnectedClient> onlineList;

    public Channel(String id) {
        this.id = id;
        onlineList = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public ArrayList<ConnectedClient> getOnlineList() {
        return onlineList;
    }

    public void addToOnlineList(ConnectedClient client) {
        this.onlineList.add(client);
    }
    public void removeFromOnlineList(ConnectedClient client) {
        onlineList.remove(client);
    }
}
