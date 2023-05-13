package com.jtconnors.socketclientfx;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.image.Image;

import java.math.BigInteger;

public class Weapon {

    String weaponName;
    String weaponType;
    int range;
    int damage;
    int ammo;
    int maxAmmo;
    double speed;
    double startTime = System.nanoTime();
    int squaresTravelled;

    public Weapon(){
    }

    public Weapon(String weaponName, String weaponType, int range, int damage, int maxAmmo, double speed){
        this.weaponName = weaponName;
        this.weaponType = weaponType;
        this.range = range;
        this.damage = damage;
        this.ammo = maxAmmo;
        this.maxAmmo = maxAmmo;
        this.speed = speed;
        startTime = System.nanoTime();
    }


}

class Bullets extends Weapon{

    int x;
    int y;
    double startTime;

    public Bullets(int x, int y){
        this.x = x;
        this.y = y;
    }
    public void fire(int targetX, int targetY, Map[][] map, AnimationTimer animationTimer, Weapon weapon){
        if (y > 0 && y < 50 && x > 0 && x < 100)
            map[y][x].newNum = map[y][x].Orignum;
        System.out.println("called fire");
        System.out.println(squaresTravelled);
        System.out.println("ty:" + targetY + " tx:" + targetX + " y:" + y + " x:" + x);
        int[] slope = new int[2];
        squaresTravelled++;

        if (targetY == y && targetX > x){
            x++;
        } else if (targetY == y && targetX < x){
            x--;
        } else if (targetX == x && targetY > y){
            y++;
        } else if (targetX == x && targetY < y){
            y--;
        } else if (targetY > y && targetY - y == slope[0]){
            y += 1;
        } else if (targetY < y && targetY - y == slope[0]){
            y -= 1;
        } else if (targetY > y && targetX > x){
            slope = reduceFraction(targetY - y, targetX - x);
            y += slope[0];
            x += slope[1];
        } else if (targetY > y && targetX < x){
            slope = reduceFraction(targetY - y, x - targetX);
            y += slope[0];
            x -= slope[1];
        } else if (targetY < y && targetX > x){
            slope = reduceFraction(y - targetY, targetX - x);
            y -= slope[0];
            x += slope[1];
        } else if (targetY < y && targetX < x){
            slope = reduceFraction(y - targetY, x - targetX);
            y -= slope[0];
            x -= slope[1];
        }

//        if (targetX > x && targetX - x == slope[1]){
//            x += 1;
//        } else if (targetX < x && targetX - x == slope[1]){
//            x -= 1;
//        }

        if (squaresTravelled >= 50){
            animationTimer.stop();
        }
        if (map[y][x].newNum == 8){
            animationTimer.stop();
        }

//        else if (targetX > x) {
//            slope = reduceFraction(targetY - y, targetX - x);
//            x += slope[1];
//        } else {
//            slope = reduceFraction(targetY - y, targetX - x);
//            x -= slope[1];
//        }
//        x += slope[1];
//        y += slope[0];
//        x += findGCD(y - targetY, targetX - x);
//        y -= findGCD(y - targetY, targetX - x);

        if (x < 100 && y < 50 && x > 0 && y > 0){
//            buttons[x][y].setStyle("-fx-background-color: brown");
            map[y][x].newNum = 7;
        }
    }

    public static int[] reduceFraction(int numerator, int denominator) {
        System.out.println("nume: " + numerator + " deno:" + denominator);

//        int n = numerator;
//        int d = denominator;
//        int x = Math.max(n, d);
//        if (n == d){
//            x = 1;
//        }
//
//        for (int i = 1; i < x; i++) {
//            if (n % i == 0 && d % i == 0){
//                n/=i;
//                d/=i;
//                i = 1;
//            }
//        }
//
//        System.out.println("n:" + n + " d:" + d);
//
//        int[] result = {n, d};

        int[] result = new int[2];

        int gcd = findGCD(numerator, denominator);
        System.out.println("GCD: " + gcd);
        if (gcd == 1){
//            gcd = findGCD(numerator - 1, denominator);
            return new int[] {1, 1};
        } else if (gcd == -1){
            gcd = findGCD(numerator - 1, denominator);
        } else if (gcd == 0){
            return result;
        }
        result[0] = numerator/gcd;
        result[1] = denominator/gcd;
        return result;
    }

//    public static int findGCD(int a, int b) {
//        if (b == 0) {
//            return a;
//        } else {
//            return findGCD(b, a % b);
//        }
//    }

    public static int findGCD(int x, int y)
    {
        BigInteger n = new BigInteger(String.valueOf(x));
        BigInteger d = new BigInteger(String.valueOf(y));

        return Integer.parseInt(String.valueOf(n.gcd(d)));
//        int r = 0, a, b;
//        a = Math.max(x, y); // a is greater number
//        b = Math.min(x, y); // b is smaller number
//        r = b;
////        if (a == b)
////            r = 1;
//
//        while(a % b != 0) {
//            r = a % b;
//            a = b;
//            b = r;
//        }
//        if (r == 0){
//            return 1;
//        }
//        System.out.println("r:" + r);
//        return r;
    }
}

