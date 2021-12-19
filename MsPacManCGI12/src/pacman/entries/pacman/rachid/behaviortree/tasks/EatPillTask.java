package pacman.entries.pacman.rachid.behaviortree.tasks;

import pacman.entries.pacman.rachid.behaviortree.RachidPacManTree;
import pacman.entries.pacman.rachid.behaviortree.helpers.Leaf;
import pacman.game.Constants;
import pacman.game.Game;

/**
 * Action task: sets the next move to go eat the nearest pill
 * @author romsahel
 */
public class EatPillTask extends Leaf
{

    public EatPillTask(RachidPacManTree parent)
    {
        super(parent);
    }

    @Override
    public boolean DoAction(Game game)
    {
        parent.setMove(game.getNextMoveTowardsTarget(
                state.getCurrent(),
                state.getNearestPill(),
                Constants.DM.PATH)
        );
        return true;
    }
}
