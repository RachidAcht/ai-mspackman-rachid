package pacman.entries.pacman.rachid.mcts;

public class Params {
    public final int MAX_PATH_LENGTH;
    public final double explorationCoefficient;
    public final int MIN_VISIT_COUNT;

    public final int ghostSimulationTimeMS;

    public Params() {
        MAX_PATH_LENGTH = 140;
        explorationCoefficient = 1.0/Math.sqrt(2);
        MIN_VISIT_COUNT = 5;
        ghostSimulationTimeMS = 8;
    }

    public Params(int MAX_PATH_LENGTH, double explorationCoefficient, int MIN_VISIT_COUNT, int ghostSimulationTimeMS) {
        this.MAX_PATH_LENGTH = MAX_PATH_LENGTH;
        this.explorationCoefficient = explorationCoefficient;
        this.MIN_VISIT_COUNT = MIN_VISIT_COUNT;
        this.ghostSimulationTimeMS = ghostSimulationTimeMS;
    }
}