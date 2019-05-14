package com.vortex;

import com.vortex.domain.NumValue;
import com.vortex.domain.Point;
import com.vortex.domain.cell.Cell;
import org.junit.Test;

import java.util.List;

import static com.vortex.domain.NumValue.Type.IN_A_ROW;
import static com.vortex.domain.NumValue.Type.SIMPLE_VALUE;
import static com.vortex.domain.cell.Cell.Type.OPENED;
import static com.vortex.domain.cell.Cell.Type.UNKNOWN;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class ResolverTest {

    @Test
    public void getNearestCells() {
        Point p = new Point(2, 3);
        List<Cell> cells = asList(
                new Cell(0, 1, UNKNOWN),
                new Cell(0, 3, UNKNOWN),
                new Cell(1, 0, UNKNOWN),
                new Cell(1, 4, UNKNOWN),
                new Cell(1, 6, UNKNOWN),
                new Cell(2, 1, UNKNOWN),
                new Cell(2, 5, UNKNOWN),
                new Cell(3, 2, UNKNOWN),
                new Cell(3, 4, UNKNOWN),
                new Cell(0, 5, OPENED, new NumValue(IN_A_ROW, 2)),
                new Cell(2, 3, OPENED, new NumValue(IN_A_ROW, 3))
        );
        Resolver r = new Resolver();
        r.getNearestCells(p, r.convertToMap(cells)).forEach(System.out::println);

    }
}