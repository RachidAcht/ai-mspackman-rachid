package pacman.entries.pacman.rachid.behaviortree.tasks;

import java.awt.Color;

import pacman.entries.pacman.rachid.behaviortree.RachidPacManTree;
import pacman.entries.pacman.rachid.behaviortree.helpers.Leaf;
import pacman.game.Constants;
import pacman.game.Game;
import pacman.game.GameView;

/**
 * A condition task: returns true if the path from current position 
 * to the nearest powerpill is safe.
 * 
 * @author romsahel
 */
public class IsPathSafeTask extends Leaf
{

    public IsPathSafeTask(RachidPacManTree parent)
    {
        super(parent);
    }

    @Override
    public boolean DoAction(Game game)
    {
        final int nearestPowerPill = state.getNearestPowerPill();
        if (nearestPowerPill == -1)
            return false;
        
        for (int node : game.getShortestPath(state.getCurrent(), nearestPowerPill))
        {
            if (game.getDistance(node, state.getNearestGhost().getIndex(), Constants.DM.PATH) < 2)
                return false;
        }

        GameView.addPoints(game, Color.RED, game.getShortestPath(state.getCurrent(), nearestPowerPill));


        return true;
    }

}
