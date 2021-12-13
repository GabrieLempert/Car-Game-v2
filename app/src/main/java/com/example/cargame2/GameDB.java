package com.example.cargame2;

import java.util.ArrayList;
import java.util.Collections;

public class GameDB {
    private ArrayList<Record> records = new ArrayList<>();

    public GameDB() {

    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public GameDB setRecords(ArrayList<Record> records) {
        this.records = records;
        return this;
    }

    public void sortRecords() {
        Collections.sort(records);
    }
}
