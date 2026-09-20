package com.jardvcode.bot.user.repository;

import com.jardvcode.bot.user.entity.BotUserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BotUserStateRepository extends JpaRepository<BotUserEntity, Long> {

    Optional<BotUserEntity> findByProviderUserId(String providerUserId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<BotUserEntity> findWithRolesAndPermissionsByProviderUserId(String providerUserId);

    @Modifying
    @Transactional
    @Query("UPDATE BotUserEntity u SET u.previousState = :previousState, u.currentState = :currentState WHERE u.providerUserId = :providerUserId")
    void updateStateByProviderUserId(String previousState, String currentState, String providerUserId);

}
