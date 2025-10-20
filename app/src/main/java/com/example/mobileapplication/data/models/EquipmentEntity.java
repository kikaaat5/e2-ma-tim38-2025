package com.example.mobileapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "equipment")
public class EquipmentEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String userId = "";

    public String name;
    public String type;     // "POTION", "ARMOR", "WEAPON"
    public String effect;   // opis efekta
    public double value;    // procenat bonusa (npr. 0.05 za 5%)
    public int price;       // cena u novčićima
    public int duration;    // broj borbi (0 = trajno)
    public boolean isActive;
    public int battlesLeft; // preostale borbe ako je aktivna

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EquipmentEntity)) return false;
        EquipmentEntity other = (EquipmentEntity) o;
        return id == other.id &&
                isActive == other.isActive &&
                battlesLeft == other.battlesLeft &&
                Double.compare(other.value, value) == 0 &&
                price == other.price &&
                duration == other.duration &&
                userId.equals(other.userId) &&
                safeEquals(name, other.name) &&
                safeEquals(type, other.type) &&
                safeEquals(effect, other.effect);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + userId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (effect != null ? effect.hashCode() : 0);
        result = 31 * result + Double.hashCode(value);
        result = 31 * result + price;
        result = 31 * result + duration;
        result = 31 * result + Boolean.hashCode(isActive);
        result = 31 * result + battlesLeft;
        return result;
    }



@NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getBattlesLeft() {
        return battlesLeft;
    }

    public void setBattlesLeft(int battlesLeft) {
        this.battlesLeft = battlesLeft;
    }

}