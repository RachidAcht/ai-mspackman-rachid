package pacman.entries.pacman.rachid.behaviortree.tasks;

import pacman.entries.pacman.rachid.behaviortree.RachidPacManTree;
import pacman.entries.pacman.rachid.behaviortree.helpers.Leaf;
import pacman.game.Constants;
import pacman.game.Game;

/**
 * Action task: sets the next move to go to the nearest powerpill
 * @author romsahel
 */
public class ChasePowerPillTask extends Leaf
{

    public ChasePowerPillTask(RachidPacManTree parent)
    {
        super(parent);
    }

    @Override
    public boolean DoAction(Game game)
    {
        parent.setMove(game.getNextMoveTowardsTarget(
                state.getCurrent(), 
                state.getNearestPowerPill(), 
                Constants.DM.PATH));
        return true;
    }

}
