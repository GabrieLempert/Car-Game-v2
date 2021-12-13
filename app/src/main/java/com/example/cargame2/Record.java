package com.example.cargame2;

public class Record implements Comparable<Record> {
    //    private String name ="";
    private long score = 0;
    private double lat = 0.0;
    private double lon = 0.0;


        public long getScore() {
        return score;
    }

    public Record setScore(long score) {
        this.score = score;
        return this;
    }

    public double getLat() {
        return lat;
    }

    public Record setLat(double lat) {
        this.lat = lat;
        return this;
    }

    public double getLon() {
        return lon;
    }

    public Record setLon(double lon) {
        this.lon = lon;
        return this;
    }

    @Override
    public String toString() {
        return "Score: " + score;
    }

    @Override
    public int compareTo(Record r) {
        return (int) (r.getScore() - this.getScore());
    }

}