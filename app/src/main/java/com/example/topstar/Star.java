package com.example.topstar;

public class Star {

    private String name;
    private String image;

    public Star(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    @Override
    public boolean equals(Object obj) {
        Star star = (Star) obj;
        return name.equals(star.getName());
    }
}