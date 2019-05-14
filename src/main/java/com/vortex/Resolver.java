package com.vortex;

import com.vortex.domain.Point;
import com.vortex.domain.cell.Cell;
import com.vortex.domain.cell.ResolvedCell;

import java.util.*;
import java.util.stream.Collectors;

import static com.vortex.domain.cell.Cell.Type.OPENED;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class Resolver {

    private static List<Point> NEAREST_CELLS = asList(new Point(0, -2), new Point(1, -1), new Point(1, 1),
            new Point(0, 2), new Point(-1, 1), new Point(-1, -1));

    public List<ResolvedCell> resolve(List<Cell> cellList, int maxInfected) {
        Map<Point, Cell> cellMap = convertToMap(cellList);
        List<Cell> possibleCellsToResolve = getPossibleCellsToResolve(cellMap);
        // sortPossibleCellsToResolve

        for (int infected = 0; infected < maxInfected; infected++) {
//            int[]
        }

        return null;
    }

    Map<Point, Cell> convertToMap(List<Cell> cellList) {
        return cellList.stream()
                .collect(toMap(Cell::getPoint, c -> c));
    }

    private List<Cell> getPossibleCellsToResolve(Map<Point, Cell> cellMap) {
        return new ArrayList<>(cellMap.values().stream()
                .filter(c -> OPENED.equals(c.getType()))
                .map(c -> getNearestCells(c.getPoint(), cellMap))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    List<Cell> getNearestCells(Point refPoint, Map<Point, Cell> cellMap) {
        List<Cell> result = null;
        for (Point nearestCell : NEAREST_CELLS) {
            Point point = new Point(refPoint.getX() - nearestCell.getX(), refPoint.getY() - nearestCell.getY());
            Cell cell = cellMap.get(point);
            if (cell != null) {
                if (result == null) {
                    result = new ArrayList<>(6);
                }
                result.add(cell);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        new Resolver().generateMatrix(15, 6);
    }

    void generateMatrix(int width, int maxElements) {
        List<int[]> result = new ArrayList<>();
        int[] arr = new int[width];
        for (int i = 0; i < width; i++) {
            arr[i] = 0;
        }

        for (int elements = 1; elements <= maxElements; elements++) {
            iterate(result, arr, 0, elements, 0);
        }

        System.out.println("+++++");
        result.forEach(this::printArr);
    }

    private void printArr(int[] arr) { // TODO: remove
        String collect = Arrays.stream(arr)
                .boxed()
                .map(Object::toString)
                .collect(joining(","));
        System.out.println(collect);
    }

    private void iterate(List<int[]> result, int[] arr, int currElementNumber, int elements, int nestedLevel) {
        for (int i = currElementNumber; i <= arr.length - elements + nestedLevel; i++) {
            arr[i] = 1;
            if (nestedLevel + 1 < elements) {
                iterate(result, arr, i + 1, elements, nestedLevel + 1);
            } else {
                result.add(Arrays.copyOf(arr, arr.length));
            }
            arr[i] = 0;
        }
    }
}
