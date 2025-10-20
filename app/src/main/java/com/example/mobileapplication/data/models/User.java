package com.example.mobileapplication.data.models;

import java.io.Serializable;

public class User implements Serializable {


    private String id;
    private String email;
    private String username;
    private int xp;
    private int pp;
    private int coins = 200;
    private String title;
    private String avatar;
    private int level = 1;
    private int badges = 0;
    private int nextLevelXp = 200;

    public int getNextLevelXp() {
        return nextLevelXp;
    }

    public void setNextLevelXp(int nextLevelXp) {
        this.nextLevelXp = nextLevelXp;
    }

    private String equipment = "Osnovna oprema";

    public User() {
    }

    public User(String userId) {
        this.id = userId;
        this.level = 1;
        this.xp = 0;
        this.nextLevelXp = 200; // početni prag
        this.pp = 0;
        this.title = "Novajlija";
    }
    public User(String id, String email, String username, String avatar) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.xp = 0;
        this.pp = 0;
        this.title = "Novajlija";

    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getPp() {
        return pp;
    }

    public void setPp(int pp) {
        this.pp = pp;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBadges() {
        return badges;
    }

    public void setBadges(int badges) {
        this.badges = badges;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

}
