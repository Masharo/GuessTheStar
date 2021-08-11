package com.example.topstar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

//Класс управляющий игрой

public class Game {

    private ArrayList<Star> stars;
    private HashSet<Integer> ides;
    private Star winner;

    public Game(ArrayList<Star> stars) {

        this.stars = stars;
        ides = new HashSet<>();
    }

    //получаем случайную звезду и положение
    public void rollStar() {
        ides.clear();

        Random random = new Random();
        int id, thisStar = 0, winnerId = random.nextInt(4);

        while (ides.size() < 4) {
            id = random.nextInt(stars.size());
            ides.add(id);

            if (winnerId == thisStar) {
                winner = stars.get(id);
            }

            thisStar++;
        }
    }

    public HashSet<Integer> getIdes() {
        return ides;
    }

    public ArrayList<Star> getStars() {
        return stars;
    }

    public boolean isWinner(String name) {
        return name.equals(getWinner().getName());
    }

    public Star getWinner() {
        return winner;
    }
}