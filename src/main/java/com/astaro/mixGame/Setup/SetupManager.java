package com.astaro.mixGame.Setup;

import com.astaro.mixGame.data.SetupSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SetupManager {
    private final Map<UUID, SetupSession> activeSessions = new ConcurrentHashMap<>();

    public SetupSession startSession(UUID uuid, String name) {
        SetupSession session = new SetupSession(uuid, name);
        activeSessions.put(uuid, session);
        return session;
    }

    public SetupSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public void removeSession(UUID uuid) {
        activeSessions.remove(uuid);
    }
}

