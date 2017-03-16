/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.guidog.service;

import java.util.ArrayList;
import java.util.List;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.RectVector;

/**
 *
 * @author ScorpDS
 * 
 * This class was created with purpose of extending ability to manipulate 
 * collection of rectangles from origin RectVector
 * 
 */
public class RectVectorExt extends RectVector {

    private List<Rect> rectList = new ArrayList<>();

    public RectVectorExt() {
        super();
    }

    public void sync() {
        for (int i = 0; i < this.size(); i++) {
            rectList.add(this.get(i));
        }
    }

    public List<Rect> getRectList() {
        return rectList;
    }
}
