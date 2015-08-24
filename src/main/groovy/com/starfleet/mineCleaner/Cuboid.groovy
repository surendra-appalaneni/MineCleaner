package com.starfleet.mineCleaner

import com.starfleet.mineCleaner.exceptions.MinePassedZAxisException

class Cuboid {


    public static final String EMPTY_CELL = ".";
    public static final String NEWLINE = System.getProperty("line.separator")
    public static final List<String> depthArray = ("a".."z") + ("A".."Z")

    enum MOVE {
        NORTH,SOUTH,EAST,WEST
        static MOVE valueOfName( String name ) {
            values().find { it.toString() == name }
        }
    }
    enum FIRE {
        ALPHA,BETA,GAMMA,DELTA
        static FIRE valueOfName( String name ) {
            values().find { it.toString() == name }
        }
    }
    int length;
    int width;
    int depth;
    Map<String, Integer> mines = new HashMap<String, Integer>();

    public Cuboid(String filePath) {
        File fieldFile = new File(filePath);
        List<String> lines = fieldFile.readLines();
        length = lines.size;
        width = lines[0].size();
        lines.eachWithIndex { String line, int lineIndex  ->
                            line.eachWithIndex { String letter, int letterIndex ->
                                if  (!EMPTY_CELL.equals(letter)) {
                                    mines.put("$lineIndex,$letterIndex", getDepth(letter))
                                }
                            }
        }
        depth = mines.values().max()
    }

    public String draw() {
        StringBuilder  cubeDrawing =  new StringBuilder();
        cubeDrawing.append("Length : ${length}, WIDTH: ${width}, DEPTH: ${depth}, Mines: ${mines.toString()}")
        cubeDrawing.append(NEWLINE)
        (0..(length-1)).each { rowIndex ->
            (0..(width-1)).each { colIndex ->
                cubeDrawing.append(mines.containsKey("$rowIndex,$colIndex") ? getDepthChar(mines.get("$rowIndex,$colIndex") ) : EMPTY_CELL);
            }
            cubeDrawing.append(NEWLINE)
        }
        return cubeDrawing.toString();
    }


    public void  moveStep (MOVE move) {
        switch (move) {
            case MOVE.NORTH:
                length = length + 2;
                mines = mines.collectEntries {key, value ->
                                        String[] xy = key.split(",");
                                        return [ "${(xy[0] as int) + 2},${xy[1]}" : value]
                }
                normalizeOnRows();
                break;

            case MOVE.SOUTH:
                length = length + 2;
                normalizeOnRows();
                break;

            case MOVE.WEST :
                width = width + 2;
                mines = mines.collectEntries {key, value ->
                    String[] xy = key.split(",");
                    return [ "${xy[0]},${(xy[1] as int) + 2}" : value]
                }
               normalizeOnColumns();
                break;

            case MOVE.EAST:
                width = width + 2;
                normalizeOnColumns();
                break;
        }
    }

    public fireStep(FIRE fire) {
        int centerRow = (length/2) as int
        int centerColumn = (width/2) as int
        List<String> firePoints;
        switch(fire) {
            case FIRE.ALPHA:
                firePoints = ["${centerRow-1},${centerColumn-1}",
                 "${centerRow-1},${centerColumn+1}",
                 "${centerRow+1},${centerColumn-1}",
                 "${centerRow+1},${centerColumn+1}"];
                break;
            case FIRE.BETA:
                firePoints = ["${centerRow-1},${centerColumn}",
                                     "${centerRow},${centerColumn-1}",
                                    "${centerRow},${centerColumn+1}",
                                    "${centerRow+1},${centerColumn}"];
                break;
            case FIRE.GAMMA:
                firePoints = ["${centerRow},${centerColumn-1}",
                              "${centerRow},${centerColumn}",
                              "${centerRow},${centerColumn+1}"];
                break;
            case FIRE.DELTA:
                firePoints = ["${centerRow-1},${centerColumn}",
                              "${centerRow},${centerColumn}",
                              "${centerRow+1},${centerColumn}"];
                break;
        }
        firePoints.each{key ->
            if (mines.containsKey(key)){
                mines.remove(key)
            }
        }
        normalizeOnRows()
        normalizeOnColumns()
    }

    public normalizeOnRows() {
        int centerRow = (length/2) as int
        int topBlankRowsCount = 0
        int bottomBlankRowCount = 0

        if (length > 1) {
            for (int row in (0..centerRow - 1)) {
                if (getMineCountOnRow(row) == 0) {
                    topBlankRowsCount++
                } else {
                    break;
                }
            }
            for (int row in (length - 1..centerRow + 1)) {
                if (getMineCountOnRow(row) == 0) {
                    bottomBlankRowCount++
                } else {
                    break;
                }
            }
            int rowsToCutDown = [topBlankRowsCount, bottomBlankRowCount].min()
            if (rowsToCutDown >= 1) {
                length = length - (rowsToCutDown * 2)
                mines = mines.collectEntries { key, value ->
                    String[] xy = key.split(",");
                    return ["${(xy[0] as int) - (rowsToCutDown)},${xy[1]}": value]
                }
            }
        }
    }


    public normalizeOnColumns() {
        int centerColumn = (width/2) as int
        int topBlankColsCount = 0
        int bottomBlankColCount = 0

        if (width > 1) {
            for (int column in (0..centerColumn - 1)) {
                if (getMineCountOnColumn(column) == 0) {
                    topBlankColsCount++
                } else {
                    break;
                }
            }
            for (int column in (width - 1..centerColumn + 1)) {
                if (getMineCountOnColumn(column) == 0) {
                    bottomBlankColCount++
                } else {
                    break;
                }
            }
            int colsToCutDown = [topBlankColsCount, bottomBlankColCount].min()
            if (colsToCutDown >= 1) {
                width = width - (colsToCutDown * 2)
                mines = mines.collectEntries { key, value ->
                    String[] xy = key.split(",");
                    return ["${xy[0]},${(xy[1] as int) - (colsToCutDown)}": value]
                }
            }
        }
    }

    private int getMineCountOnRow(int rowNumber) {
        int mineCount = 0
        (0..width-1).each{columnNumber ->
            mineCount = mineCount + (mines.containsKey("$rowNumber,$columnNumber")?1:0)
        }
        return mineCount;
    }


    private int getMineCountOnColumn(int columnNumber) {
        int mineCount = 0
        (0..length-1).each{rowNumber ->
            mineCount = mineCount + (mines.containsKey("$rowNumber,$columnNumber")?1:0)
        }
        return mineCount;
    }
    public void dropDown() {
        mines.each { key, value ->
            if (value <= 1) {
                throw new MinePassedZAxisException("Ship passed mine at (${key},${value})");
            }
            else {
                mines.put(key, value-1)
            }
        }
    }
    static int getDepth(String depthChar) {
        return depthArray.indexOf(depthChar) + 1
    }

    static String getDepthChar(int depth) {
        return (depth < 1 || depth > depthArray.size()) ? "" : depthArray[depth - 1]
    }

    public static void main(String[] args) {
        Cuboid cube1 = new Cuboid("c:\\tmp\\field1.txt")
         println "CUBOID Dimensions: ${cube1.length}, ${cube1.width}, ${cube1.depth}"
        println "CUBOID is  : "
        println cube1.draw()
        println cube1.mines.toString()

//        FIRE fire = Cuboid.FIRE.DELTA
//        println "Fire: ${fire}"
//        cube1.fireStep(fire)
        cube1.dropDown()
        println cube1.draw()
        println cube1.mines.toString()
//        println "${Cuboid.MOVE.SOUTH}"
//        cube1.moveStep(Cuboid.MOVE.SOUTH)
//        println cube1.draw()
//        println cube1.mines.toString()
//
//        println "${Cuboid.MOVE.WEST}"
//        cube1.moveStep(Cuboid.MOVE.WEST)
//        println cube1.draw()
//        println cube1.mines.toString()
        //(0..cube1.length-1).each{ entry -> println "Mines on row $entry : ${cube1.getMineCountOnRow(entry)} " }
        //(0..cube1.width-1).each{entry -> println "Mines on Column $entry : ${cube1.getMineCountOnColumn(entry)} " }
//        cube1.dropDown();
//        println cube1.draw()

    }
}