package pacman.entries.pacman.rachid.behaviortree.helpers;

import pacman.entries.pacman.rachid.behaviortree.RachidPacManTree;
import pacman.entries.pacman.rachid.GameState;

/**
 *
 * @author romsahel
 */
public abstract class Leaf extends Node
{
    protected final RachidPacManTree parent;
    protected final GameState state;

    public Leaf(RachidPacManTree parent)
    {
        this.parent = parent;
        this.state = parent.getState();
    }

}
