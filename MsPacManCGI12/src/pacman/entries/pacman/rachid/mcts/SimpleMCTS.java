package pacman.entries.pacman.rachid.mcts;

import pacman.controllers.examples.StarterGhosts;
import pacman.entries.pacman.rachid.mcts.SimulationResult;
import pacman.entries.pacman.rachid.mcts.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SimpleMCTS {

    private final Params params;

    private final Node rootNode;
    private final Random random = new Random();
    private final StarterGhosts ghostsController = new StarterGhosts();

    private final int numberOfActivePillsStart;
    private final long timeDue;
    boolean printLog;

    public SimpleMCTS(Game gameState, long timeDue, boolean printLog) {
        this(new Params(), gameState, timeDue, printLog);
    }

    public SimpleMCTS(Params params, Game gameState, long timeDue, boolean printLog) {
        this.params = params;
        this.rootNode = new Node(params, gameState.copy(), 0);
        this.numberOfActivePillsStart = gameState.getNumberOfActivePills();
        this.timeDue = timeDue;
        this.printLog = printLog;

        if(printLog) {
            System.out.println("Time started: " + Utils.getFormattedTime(System.currentTimeMillis()));
            System.out.println("Time due: " + Utils.getFormattedTime(timeDue));
        }
    }

    public Constants.MOVE runMCTS() {
        long deltaTimeNS = 0;
        long lastNS = System.nanoTime();

        while (!Terminate(deltaTimeNS)) {

            Node selectedNode = TreePolicy(rootNode);

            float reward = SimulateGame(selectedNode);
            Backpropagate(selectedNode, reward);

            // Timing stuff
            long currentNS = System.nanoTime();
            deltaTimeNS = currentNS - lastNS;
            lastNS = currentNS;
        }

        if (printLog) System.out.println(rootNode);

        Optional<Node> bestNode = rootNode.children.stream().max(Comparator.comparingInt(Node::getTimesVisited));
        if(bestNode.isPresent()) {
            return bestNode.get().parentAction;
        }

        return null;
    }

    /**
     * Check if the algorithm is to be terminated
     * @param lastDeltaNS the amount of time the last iteration took
     * @return
     */
    private boolean Terminate(long lastDeltaNS) {
        long lastDeltaMillis = TimeUnit.MILLISECONDS.convert(lastDeltaNS, TimeUnit.NANOSECONDS);
        long returnTimeMS = 2; // approx. time required to return to the getMove method
        if ( System.currentTimeMillis() + lastDeltaMillis + returnTimeMS > timeDue) return true;
        return false;
    }

    Node TreePolicy(Node currentNode) {
        if(currentNode.isGameOver()) {
            return currentNode.parent != null ? currentNode.parent : currentNode;
        }

        if(!currentNode.isFullyExpanded()) {
            return expandNode(currentNode);
        }

        if(currentNode.children.isEmpty()) {
            return currentNode; // simulation depth reached (fully expanded + no children)
        }

        // randomize selection if visit count of one child < min visit count
        boolean allChildsVisitsAboveMinVisitCount =
                currentNode.children.parallelStream()
                    .map(Node::getTimesVisited)
                    .min(Integer::compareTo).get() > params.MIN_VISIT_COUNT;

        if(allChildsVisitsAboveMinVisitCount) {
            return TreePolicy(currentNode.getBestChild());
        } else {
            return TreePolicy(currentNode.children.get(random.nextInt(currentNode.children.size())));
        }
    }

    Node expandNode(Node parentNode) {
        ArrayList<Constants.MOVE> pacmanMoves = parentNode.getPacmanMovesNotExpanded(params.MAX_PATH_LENGTH);
        assert !pacmanMoves.isEmpty();
        Constants.MOVE pacmanMove = pacmanMoves.get(random.nextInt(pacmanMoves.size()));

        SimulationResult result = Utils.simulateUntilNextJunction(params, parentNode.gameState.copy(), ghostsController, pacmanMove);

        Node child = new Node(params, result.gameState, parentNode.pathLengthInSteps + result.steps);

        child.parentAction = pacmanMove;
        child.parent = parentNode;

        parentNode.children.add(child);

        if(result.diedDuringSimulation || result.powerPillUnnecessarilyEaten) {
            // make sure this node doesn't get simulated but simulate the parent
            child.updateReward(-1);
            child.setCanUpdate(false);

            return parentNode;
        }

        return child;
    }

    private float SimulateGame(Node selectedNode) {
        Game simulationGameState = selectedNode.gameState.copy();
        int totalSteps = params.MAX_PATH_LENGTH - selectedNode.pathLengthInSteps;
        int remainingSteps = totalSteps;
        SimulationResult lastSimulationResult = new SimulationResult();

        while(!Utils.analyzeGameState(simulationGameState, lastSimulationResult, remainingSteps, lastSimulationResult.powerPillUnnecessarilyEaten)) {
            ArrayList<Constants.MOVE> availableMoves = Utils.getPacmanMovesAtJunctionWithoutReverse(simulationGameState);
            Constants.MOVE pacmanMove = availableMoves.get(random.nextInt(availableMoves.size()));

            lastSimulationResult = Utils.simulateToNextJunctionOrLimit(params, simulationGameState, ghostsController, pacmanMove, remainingSteps);

            remainingSteps -= lastSimulationResult.steps;
        }

        if(lastSimulationResult.levelComplete) {
            return 1;
        }

        if(lastSimulationResult.diedDuringSimulation || lastSimulationResult.powerPillUnnecessarilyEaten) {
            return 0;
        }

        // no pills eaten - but survived
        if(simulationGameState.getNumberOfActivePills() == numberOfActivePillsStart) {
            return 0.1f * (1.f/numberOfActivePillsStart);
        }

        return 1.0f - ( simulationGameState.getNumberOfActivePills() / ((float)numberOfActivePillsStart));
    }

    private void Backpropagate(Node selectedNode, float reward) {
        while (selectedNode != null) {
            selectedNode.updateReward(reward);
            selectedNode = selectedNode.parent;
        }
    }
}