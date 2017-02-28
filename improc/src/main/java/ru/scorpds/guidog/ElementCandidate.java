/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.scorpds.guidog;

/**
 *
 * @author sbt-ulyanov-ka
 */
public class ElementCandidate {

    private String path;
    private ElementType type;
    private double fitness;
    private int x;
    private int y;

    public enum ElementType {
        BUTTON,
        CHECKBOX,
        INPUT,
        OTHER
    }

    public ElementCandidate(String path, ElementType type, double fitness) {
        this.path = path;
        this.type = type;
        this.fitness = fitness;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
