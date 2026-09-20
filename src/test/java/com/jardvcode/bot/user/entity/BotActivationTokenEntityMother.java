package com.jardvcode.bot.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public final class BotActivationTokenEntityMother {

    public static BotActivationTokenEntity withUsedToken() {
        BotActivationTokenEntity entity = new BotActivationTokenEntity();
        entity.setToken(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        entity.setExpiresAt(LocalDateTime.now().plusDays(1));
        entity.setUsedAt(LocalDateTime.now().plusDays(1));
        return entity;
    }

    public static BotActivationTokenEntity withExpiredToken() {
        BotActivationTokenEntity entity = new BotActivationTokenEntity();
        entity.setUsedAt(null);
        entity.setToken(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        entity.setExpiresAt(LocalDateTime.now().minusDays(2));
        return entity;
    }

    public static BotActivationTokenEntity withValidToken() {
        BotActivationTokenEntity entity = new BotActivationTokenEntity();
        entity.setUserId(1L);
        entity.setToken(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        entity.setExpiresAt(LocalDateTime.now().plusDays(1));
        entity.setUsedAt(null);
        return entity;
    }

}