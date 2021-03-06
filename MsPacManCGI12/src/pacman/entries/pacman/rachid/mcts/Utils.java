package pacman.entries.pacman.rachid.mcts;

import pacman.controllers.Controller;
import pacman.entries.pacman.rachid.mcts.Params;
import pacman.game.Constants;
import pacman.game.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;

public abstract class Utils {

    public static String getFormattedTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        return sdf.format(date);
    }

    public static ArrayList<Constants.MOVE> getPacmanMovesWithoutNeutral(Game gameState) {
        Constants.MOVE[] possibleMoves = gameState.getPossibleMoves(gameState.getPacmanCurrentNodeIndex());

        ArrayList<Constants.MOVE> pacmanMoves = new ArrayList<>(Arrays.asList(possibleMoves));
        pacmanMoves.remove(Constants.MOVE.NEUTRAL);

        return pacmanMoves;
    }

    public static SimulationResult simulateUntilNextJunction(Params params, Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction) {
        SimulationResult result = simulateToNextJunctionOrLimit(params, gameState, ghostController, direction, Integer.MAX_VALUE);

        return result;
    }

    public static SimulationResult simulateToNextJunctionOrLimit(Params params, Game gameState, Controller<EnumMap<Constants.GHOST,Constants.MOVE>> ghostController, Constants.MOVE direction, int maxSteps) {
        SimulationResult result = new SimulationResult();

        Constants.MOVE currentPacmanDirection = direction;
        boolean hadEdibleGhost;
        do {
            currentPacmanDirection = getPacmanMoveAlongPath(gameState, currentPacmanDirection);

            hadEdibleGhost = hasEdibleGhost(gameState);
            gameState.advanceGame(currentPacmanDirection, ghostController.getMove(gameState, System.currentTimeMillis() + params.ghostSimulationTimeMS));

            // update stats
            result.steps++;
            maxSteps--;

        } while(!analyzeGameState(gameState, result, maxSteps, hadEdibleGhost) && !gameState.isJunction(gameState.getPacmanCurrentNodeIndex()));

        result.gameState = gameState;

        return result;
    }

    /**
     * Analyzes the game state and sets the variables in the simulation result accordingly
     * @param gameState
     * @param result
     * @return exit the simulation loop?
     */
    public static boolean analyzeGameState(Game gameState, SimulationResult result, int remainingSteps, boolean hadEdibleGhost) {
        boolean shouldStop = false;

        if(gameState.getNumberOfActivePills() == 0) {
            result.levelComplete = true;
            shouldStop = true;
        }

        if(gameState.wasPacManEaten()) {
            result.diedDuringSimulation = true;
            shouldStop = true;
        }

        if(gameState.wasPowerPillEaten() && (hadEdibleGhost || !wasAGhostClose(gameState))) {
            result.powerPillUnnecessarilyEaten = true;
            shouldStop = true;
        }

        if(remainingSteps <= 0) {
            shouldStop = true;
        }

        return shouldStop;
    }

    public static boolean hasEdibleGhost(Game gameState) {
        for(Constants.GHOST ghost : Constants.GHOST.values()) {
            if (gameState.isGhostEdible(ghost)) {
                return true;
            }
        }
        return false;
    }

    public static boolean wasAGhostClose(Game gameState) {
        int pacmanNode = gameState.getPacmanCurrentNodeIndex();
        for(Constants.GHOST ghost : Constants.GHOST.values()) {
            int ghostNode = gameState.getGhostCurrentNodeIndex(ghost);
            if (gameState.getShortestPathDistance(pacmanNode, ghostNode) < 20) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Constants.MOVE> getPacmanMovesAtJunctionWithoutReverse(Game gameState) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);
        moves.remove(gameState.getPacmanLastMoveMade().opposite());

        return moves;
    }

    public static Constants.MOVE getPacmanMoveAlongPath(Game gameState, Constants.MOVE direction) {
        ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutNeutral(gameState);
        if(moves.contains(direction)) return direction;
        moves.remove(gameState.getPacmanLastMoveMade().opposite());
        assert moves.size() == 1; // along a path there is only one possible way remaining

        return moves.get(0);
    }
}