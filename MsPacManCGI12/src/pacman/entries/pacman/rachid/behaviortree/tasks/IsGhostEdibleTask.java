package pacman.entries.pacman.rachid.behaviortree.tasks;

import pacman.entries.pacman.rachid.behaviortree.RachidPacManTree;
import pacman.entries.pacman.rachid.behaviortree.helpers.Leaf;
import pacman.game.Game;

/**
 * A condition task: returns true if the nearest ghost is edible
 * @author romsahel
 */
public class IsGhostEdibleTask extends Leaf
{

    public IsGhostEdibleTask(RachidPacManTree parent)
    {
        super(parent);
    }

    @Override
    public boolean DoAction(Game game)
    {
        final int edibleTime = game.getGhostEdibleTime(state.getNearestGhost().getType());
        return (edibleTime > 1);
    }

}
