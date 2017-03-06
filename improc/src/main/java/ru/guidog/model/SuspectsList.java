/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.guidog.model;

import ru.guidog.model.Suspect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 *
 * @author sbt-ulyanov-ka
 */
public class SuspectsList implements Iterable<Suspect> {

    private ArrayList<Suspect> list;

    public SuspectsList() {
        this.list = new ArrayList<>();
    }

    public SuspectsList setList(ArrayList<Suspect> list) {
        if (null != list && list.size() > 0) {
            this.list = list;
        }
        return this;
    }

    public void add(Suspect sus) {
        if (null != sus) {
            list.add(sus);
        }
    }

    public Suspect getLast() {
        if (list.size() > 0) {
            return list.get(list.size() - 1);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<Suspect> iterator() {
        return list.iterator();
    }

    @Override
    public void forEach(Consumer<? super Suspect> cnsmr) {
        Iterable.super.forEach(cnsmr); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int size() {
        return list.size();
    }

}
