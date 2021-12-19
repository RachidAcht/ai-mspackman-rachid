package pacman.entries.pacman.rachid.mcts;

import pacman.entries.pacman.rachid.mcts.Utils;
import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Node {

    final Params params;
    final Game gameState;
    final int pathLengthInSteps;

    Node parent = null;
    Constants.MOVE parentAction = null;
    List<Node> children = new ArrayList<>();

    private double reward = 0;
    private int timesVisited = 0;

    private boolean canUpdate = true;


    Node(Params params, Game gameState, int pathLengthInSteps){
        this.params = params;
        this.gameState = gameState;
        this.pathLengthInSteps = pathLengthInSteps;
    }

    double getReward() {
        double childrenMax = children.stream().map(Node::getReward).max(Double::compareTo).orElse(0d);

        return Math.max(childrenMax * 0.5, reward);
    }

    double getUCTValue() {
        double exploitation = getReward() / timesVisited;
        double exploration = params.explorationCoefficient * Math.sqrt( Math.log(parent.timesVisited) / timesVisited);

        return exploitation + exploration;
    }

    public boolean isFullyExpanded() {
        ArrayList<Constants.MOVE> moves = getPacmanMovesNotExpanded(params.MAX_PATH_LENGTH);

        return moves.isEmpty();
    }

    public ArrayList<Constants.MOVE> getPacmanMovesWithoutReverse() {
        ArrayList<Constants.MOVE> moves = Utils.getPacmanMovesWithoutNeutral(gameState);

        if(parent != null) {
            moves.remove(parentAction.opposite());
        }

        return moves;
    }

    public ArrayList<Constants.MOVE> getPacmanMovesNotExpanded(final int MAX_PATH_LENGTH) {
        // check if enough simulation steps would be made
        if(pathLengthInSteps > 0.8f * MAX_PATH_LENGTH) {
            return new ArrayList<>();
        } else {
            ArrayList<Constants.MOVE> moves = getPacmanMovesWithoutReverse();

            // remove explored child moves
            moves.removeAll(children.parallelStream().map(child -> child.parentAction).collect(Collectors.toList()));

            return moves;
        }
    }

    public Node getBestChild() {
        Node bestChild = null;
        double bestUCTValue = Float.MIN_VALUE;

        for(Node child : children) {
            double childValue = child.getUCTValue();
            if(childValue > bestUCTValue) {
                bestChild = child;
                bestUCTValue = childValue;
            }
        }

        if(bestChild == null) {
            // panic
            return children.get(0);
        }

        return bestChild;
    }

    public int getTimesVisited() {
        return timesVisited;
    }

    public boolean isGameOver() {
        return gameState.wasPacManEaten() || gameState.getNumberOfActivePills() == 0;
    }

    public void updateReward(float deltaReward) {
        if(canUpdate) {
            reward += deltaReward;
        }
        timesVisited++;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    public String path() {
        Stack<Node> pathStack = new Stack<>();
        Node currentNode = this;

        while (currentNode.parent != null) {
            pathStack.push(currentNode);
            currentNode = currentNode.parent;
        }

        StringBuilder sb = new StringBuilder();
        while (!pathStack.empty()) {
            Node node = pathStack.pop();
            sb.append('/');
            sb.append(node.parentAction);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(path() + " distance: " + pathLengthInSteps);
        sb.append(" - Children ");
        for(Node child : children) {
            sb.append(" || ["+child.parentAction+"] ");
            sb.append("r/s: "+child.reward+"/"+child.timesVisited);
        }

        return sb.toString();
    }
}