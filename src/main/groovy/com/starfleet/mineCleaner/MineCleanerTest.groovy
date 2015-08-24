package com.starfleet.mineCleaner


class MineCleanerTest {

    public static void main(String[] args) {
        Cuboid cube1 = new Cuboid("c:\\tmp\\field1.txt")
        File stepsFile = new File("c:\\tmp\\steps1.txt")
        List<String> allSteps = stepsFile.readLines() ;

        println "STARTING: "
        println cube1.draw()

        MineCleaner mineCleaner = new MineCleaner();
        mineCleaner.processAllSteps(cube1, allSteps)

    }
}
