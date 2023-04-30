package com.jtconnors.socketclientfx;

public class Map {
    boolean isWall = false;
    int x;
    int y;
    int num;

    public Map(int x, int y, int num, boolean isWall){
        this.x = x;
        this.y = y;
        this.num = num;
        this.isWall = isWall;
    }

    public void changeNum(int num){
        this.num = num;
    }
}
