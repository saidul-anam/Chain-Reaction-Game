package com.chainreaction.model;

public class Cell {
    private int owner;
    private int value;
    public Cell(){
        owner=0;
        value=0;
    }
    public int getOwner() {
        return owner;
    }
    public void setOwner(int owner) {
        this.owner = owner;
    }
    public int getValue() {
    return value;
    }
    public void setValue(int value) {
        this.value = value;
}
public void reset(){
    owner=0;
    value=0;
}

@Override
public String toString() {
    if (this.value == 0 || this.owner == 0) {
        return "0";
    }
    String own = (this.owner == 1) ? "R" : "B";
    return this.value + own;
}

}
