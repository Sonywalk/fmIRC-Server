package com.company.database;

import com.company.entities.ModeratorEntity;

public interface ModeratorDAO {

    boolean createModerator(ModeratorEntity moderator);
    void deleteModerator(ModeratorEntity moderator);
    void updateModerator(ModeratorEntity moderator);
    ModeratorEntity fetchModerator(String nickname, String channel);
}
