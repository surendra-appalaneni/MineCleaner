package com.starfleet.mineCleaner

import  com.starfleet.mineCleaner.Cuboid.MOVE
import  com.starfleet.mineCleaner.Cuboid.FIRE
import com.starfleet.mineCleaner.exceptions.MinePassedZAxisException

class MineCleaner {
    enum MISSIONSTATE {
        CLEARED_MINES, PASSED_MINE,CONTINUE
    }

    public String processAllSteps(Cuboid cuboid, List<String> allSteps) {
        int stepIndex = 0
        MISSIONSTATE missionState;

        for (String stepsLine: allSteps) {
            println "Step ${++stepIndex}"
            println "$stepsLine"
            try {
                missionState = processStepsLine(cuboid, stepsLine)
                if (missionState == MISSIONSTATE.CLEARED_MINES) {
                   break;
                }
                println cuboid.draw()
                println Cuboid.NEWLINE
            }
            catch(MinePassedZAxisException mineEx) {
                missionState == MISSIONSTATE.PASSED_MINE
                break;
            }
        }

        if ((missionState == MISSIONSTATE.PASSED_MINE) || (cuboid.mines.size() > 0) ) {
            println "Result: Fail (0)";
        }
        if ((missionState == MISSIONSTATE.CLEARED_MINES) &&  stepIndex == allSteps.size()) {
            println "Result: Pass (1+)"
        }
        if ((missionState == MISSIONSTATE.CLEARED_MINES)  && stepIndex < allSteps.size()) {
            println "Result: Pass (1)"
        }
    }
   public MISSIONSTATE processStepsLine(Cuboid cuboid,  String steps) {
       for(String step : steps.split(" ")){
           processStepToken(cuboid,step.toUpperCase())
           if(cuboid.mines.size() == 0) {
               return MISSIONSTATE.CLEARED_MINES
           }
       }
       cuboid.dropDown()
       return MISSIONSTATE.CONTINUE
   }

    public void processStepToken(Cuboid cuboid, String stepToken) {
        if (MOVE.valueOfName(stepToken)){
            cuboid.moveStep(stepToken as MOVE);
        }
        else if (FIRE.valueOfName(stepToken)){
            cuboid.fireStep(stepToken as FIRE);
        }
    }
}
