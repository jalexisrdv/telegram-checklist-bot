package com.jardvcode.bot.user.entity;

import com.jardvcode.bot.shared.domain.state.State;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "bot_users")
public class BotUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "platform")
    private String platform;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "previous_state")
    private String previousState;

    @Column(name = "current_state")
    private String currentState;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    public static BotUserEntity createWithoutErpUser(String platform, String providerUserId, Class<? extends State> currentState) {
        BotUserEntity entity = new BotUserEntity();

        entity.platform = platform;
        entity.providerUserId = providerUserId;
        entity.currentState = currentState.getCanonicalName();

        return entity;
    }

    public void updateCurrentState(Class<? extends State> newState) {
        if (newState.getCanonicalName().equals(currentState)) {
            return;
        }

        this.previousState = this.currentState;
        this.currentState = newState.getCanonicalName();
    }

    public Class<? extends State> previousStateClass() {
        if(previousState == null) {
            return null;
        }

        try {
            return Class.forName(previousState)
                    .asSubclass(State.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("State class not found: " + previousState, e);
        }
    }

    public Class<? extends State> currentStateClass() {
        try {
            return Class.forName(currentState)
                    .asSubclass(State.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("State class not found: " + currentState, e);
        }
    }

    public Set<String> permissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(PermissionEntity::getCode)
                .collect(Collectors.toSet());
    }

    public void linkToErpUser(Long userId) {
        this.userId = userId;
    }

    public boolean isErpUserLinked() {
        return userId != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void updateCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEntity> roles) {
        this.roles = roles;
    }

}
