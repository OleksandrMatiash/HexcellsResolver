package com.vortex.domain.cell;

import com.vortex.domain.NumValue;
import com.vortex.domain.Point;
import lombok.Data;

@Data
public class Cell {

    public enum Type {
        UNKNOWN, EMPTY, INFECTED, OPENED
    }

    private Point point;
    private Type type;
    private NumValue numValue;

    public Cell(int x, int y, Type type) {
        this(x, y, type, null);
    }

    public Cell(int x, int y, Type type, NumValue numValue) {
        this.point = new Point(x,y);
        this.type = type;
        this.numValue = numValue;
    }
}
