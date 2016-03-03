package com.company.database;

import com.company.entities.ModeratorEntity;

/**
 * Created by LogiX on 2016-03-03.
 */
public interface ModeratorDAO {

    boolean createModerator(ModeratorEntity moderator);
    void deleteModerator(ModeratorEntity moderator);
    void updateModerator(ModeratorEntity moderator);
    ModeratorEntity fetchModerator(String nickname, String channel);
}
