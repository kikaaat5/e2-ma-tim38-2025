package com.example.mobileapplication.data.models;

import java.io.Serializable;

public class User implements Serializable {

        private String id;
        private String email;
        private String username;
        private String avatarUrl;
        private int xp;
        private int pp;
        private int coins;
        private String title;

        public User() {}

        public User(String id, String email, String username, String avatarUrl) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.xp = 0;
            this.pp = 0;
            this.coins = 0;
            this.title = "Novajlija";
        }


        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

        public int getXp() { return xp; }
        public void setXp(int xp) { this.xp = xp; }

        public int getPp() { return pp; }
        public void setPp(int pp) { this.pp = pp; }

        public int getCoins() { return coins; }
        public void setCoins(int coins) { this.coins = coins; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
}
