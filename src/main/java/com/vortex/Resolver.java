package com.vortex;

import com.vortex.domain.NumValue;
import com.vortex.domain.Point;
import com.vortex.domain.cell.Cell;
import com.vortex.domain.cell.ResolvedCell;
import com.vortex.domain.cell.ResolvedCell.Resolution;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.vortex.domain.cell.Cell.Type.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;

public class Resolver {

    private static List<Point> NEAREST_CELLS = asList(new Point(0, -2), new Point(1, -1), new Point(1, 1),
            new Point(0, 2), new Point(-1, 1), new Point(-1, -1));

    public List<ResolvedCell> resolve(List<Cell> cellList, int maxInfected) {
//        Map<Point, Cell> cellMap = convertToMap(cellList);
        Cell[][] cellsField = convertToCellsField(cellList);
        List<Cell> possibleCellsToResolve = getPossibleCellsToResolve(cellsField);
        List<List<Cell>> refCells = getRefCells(cellsField);
        // sortPossibleCellsToResolve

        resolve(possibleCellsToResolve, refCells, maxInfected, cellsField);

        return null;
    }

    private List<Cell> getPossibleCellsToResolve(Cell[][] cellsField) {
        Set<Cell> possibleCellsToResolve = new HashSet<>();
        findRefCellsAndPossibleCellsToResolve(cellsField, (refCell, cellToResolve) -> possibleCellsToResolve.add(cellToResolve));
        return new ArrayList<>(possibleCellsToResolve);
    }

    private List<List<Cell>> getRefCells(Cell[][] cellsField) {
        Set<Cell> res = new HashSet<>();
        findRefCellsAndPossibleCellsToResolve(cellsField, (refCell, cellToResolve) -> res.add(refCell));
        return res.stream()
                .map(Arrays::asList)
                .collect(toList());
    }

    private void findRefCellsAndPossibleCellsToResolve(Cell[][] cellsField, BiConsumer<Cell, Cell> biConsumer) {
        for (int x = 0; x < cellsField.length; x++) {
            for (int y = 0; y < cellsField[x].length; y++) {
                Cell refCell = cellsField[x][y];
                if (refCell != null && refCell.getType().equals(OPENED)) {
                    for (Point nearestCell : NEAREST_CELLS) {
                        Point refPoint = refCell.getPoint();
                        int posX = refPoint.getX() + nearestCell.getX();
                        int posY = refPoint.getY() + nearestCell.getY();
                        if (posX >= 0 && posX < cellsField.length && posY >= 0 && posY < cellsField[0].length) {
                            Cell cellToResolve = cellsField[posX][posY];
                            if (cellToResolve != null && cellToResolve.getType().equals(UNKNOWN)) {
                                //possibleCellsToResolve.add(cellToResolve);
                                biConsumer.accept(refCell, cellToResolve);
                            }
                        }
                    }
                }
            }
        }
    }

    private Cell[][] convertToCellsField(List<Cell> cellList) {
        int maxX = -1, maxY = -1;
        for (Cell cell : cellList) {
            int x = cell.getPoint().getX();
            int y = cell.getPoint().getY();
            if (maxX == -1 || x > maxX) {
                maxX = x;
            }
            if (maxY == -1 || y > maxY) {
                maxY = y;
            }
        }

        Cell[][] result = new Cell[maxX + 1][maxY + 1];
        for (Cell cell : cellList) {
            Point point = cell.getPoint();
            result[point.getX()][point.getY()] = cell;
        }
        return result;
    }

    private List<ResolvedCell> resolve(List<Cell> possibleCellsToResolve, List<List<Cell>> refCells, int maxInfected, Cell[][] cellsField) {
        Resolution[][] possibleResolutionField = new Resolution[cellsField.length][cellsField[0].length];
        int infectedToProcess = maxInfected < possibleCellsToResolve.size() ? maxInfected : possibleCellsToResolve.size();
        List<List<ResolvedCell>> result = new ArrayList<>();
        for (int totalInfected = 1; totalInfected <= infectedToProcess; totalInfected++) {
            tryResolve(result, possibleCellsToResolve, refCells, possibleResolutionField, cellsField, 0, totalInfected, 0);
        }


        result.stream()
                .map(a -> a.stream().sorted((o1, o2) -> o1.getPoint().getX() == o2.getPoint().getX()
                        ? o1.getPoint().getY() - o2.getPoint().getY()
                        : o1.getPoint().getX() - o2.getPoint().getX())
                        .map(ResolvedCell::toString)
                        .collect(joining(",")))
                .forEach(System.out::println);
        return null;
    }

    private void tryResolve(List<List<ResolvedCell>> result, List<Cell> possibleCellsToResolve, List<List<Cell>> refCells,
                            Resolution[][] possibleResolutionField, Cell[][] cellsField,
                            int currElementNumber, int totalInfected, int nestedLevel) {
//        System.out.println("currElementNumber: " + currElementNumber + ", nestedLevel: " + nestedLevel + ", totalInfected: " + totalInfected);
        for (int i = currElementNumber; i <= possibleCellsToResolve.size() - totalInfected + nestedLevel; i++) {
            Point point = possibleCellsToResolve.get(i).getPoint();
            possibleResolutionField[point.getX()][point.getY()] = Resolution.INFECTED;
            if (nestedLevel + 1 < totalInfected) {
                tryResolve(result, possibleCellsToResolve, refCells, possibleResolutionField, cellsField, i + 1, totalInfected, nestedLevel + 1);
            } else {
                if (checkConflicts(possibleResolutionField, cellsField, refCells)) {
                    result.add(collapseField(possibleResolutionField));
                }
            }
            possibleResolutionField[point.getX()][point.getY()] = null;
//            possibleResolutionField[point.getX()][point.getY()] = Resolution.OPENED;
        }
    }

    private List<ResolvedCell> collapseField(Resolution[][] field) {
        List<ResolvedCell> result = new ArrayList<>();
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field[0].length; y++) {
                if (field[x][y] != null) {
                    ResolvedCell cell = new ResolvedCell();
                    cell.setPoint(new Point(x, y));
                    cell.setResolution(field[x][y]);
                    result.add(cell);
                }
            }
        }
        return result;
    }


    private boolean checkConflicts(Resolution[][] possibleResolutionField, Cell[][] cellsField, List<List<Cell>> refCells) {
        for (List<Cell> refs : refCells) {
            for (Cell ref : refs) {
                Point refPoint = ref.getPoint();
                if (ref.getNumValue().getType().equals(NumValue.Type.SIMPLE_VALUE)) {
                    if (!checkSimpleConflicts(refPoint, ref.getNumValue().getValue(), possibleResolutionField, cellsField)) {
                        return false;
                    }
                } else {
                    throw new IllegalArgumentException("not implemented");
                }


            }
        }
        return true;
    }

    private boolean checkSimpleConflicts(Point refPoint, int expectedInfectedNumber, Resolution[][] possibleResolutionField, Cell[][] cellsField) {
        int infectedCnt = 0;
        for (int i = 0; i < NEAREST_CELLS.size(); i++) {
            Point nearest = NEAREST_CELLS.get(i);
            int x = refPoint.getX() + nearest.getX();
            int y = refPoint.getY() + nearest.getY();
            if (x >= 0 && x < cellsField.length && y >= 0 && y < cellsField[0].length) {
                if ((cellsField[x][y] != null && INFECTED.equals(cellsField[x][y].getType())) || Resolution.INFECTED.equals(possibleResolutionField[x][y])) {
                    infectedCnt++;
                }
            }
        }
        return expectedInfectedNumber == infectedCnt;
    }

//    Map<Point, Cell> convertToMap(List<Cell> cellList) {
//        return cellList.stream()
//                .collect(toMap(Cell::getPoint, c -> c));
//    }

//    private List<Cell> getPossibleCellsToResolve(Map<Point, Cell> cellMap) {
//        return new ArrayList<>(cellMap.values().stream()
//                .filter(c -> OPENED.equals(c.getType()))
//                .map(c -> getNearestCells(c.getPoint(), cellMap))
//                .filter(Objects::nonNull)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toSet()));
//    }

//    List<Cell> getNearestCells(Point refPoint, Map<Point, Cell> cellMap) {
//        List<Cell> result = null;
//        for (Point nearestCell : NEAREST_CELLS) {
//            Point point = new Point(refPoint.getX() - nearestCell.getX(), refPoint.getY() - nearestCell.getY());
//            Cell cell = cellMap.get(point);
//            if (cell != null) {
//                if (result == null) {
//                    result = new ArrayList<>(6);
//                }
//                result.add(cell);
//            }
//        }
//        return result;
//    }


//    public static void main(String[] args) {
//        new Resolver().generateMatrix(15, 6);
//    }

//    void generateMatrix(int width, int maxElements) {
//        List<int[]> result = new ArrayList<>();
//        int[] arr = new int[width];
//        for (int i = 0; i < width; i++) {
//            arr[i] = 0;
//        }
//
//        for (int elements = 1; elements <= maxElements; elements++) {
//            iterate(result, arr, 0, elements, 0);
//        }
//
//        System.out.println("+++++");
//        result.forEach(this::printArr);
//    }
//
//    private void printArr(int[] arr) { // TODO: remove
//        String collect = Arrays.stream(arr)
//                .boxed()
//                .map(Object::toString)
//                .collect(joining(","));
//        System.out.println(collect);
//    }
//
//    private void iterate(List<int[]> result, int[] arr, int currElementNumber, int elements, int nestedLevel) {
//        for (int i = currElementNumber; i <= arr.length - elements + nestedLevel; i++) {
//            arr[i] = 1;
//            if (nestedLevel + 1 < elements) {
//                iterate(result, arr, i + 1, elements, nestedLevel + 1);
//            } else {
//                result.add(Arrays.copyOf(arr, arr.length));
//            }
//            arr[i] = 0;
//        }
//    }
}
