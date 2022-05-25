package massim.javaagents.pathfinding;

import java.awt.Point;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.javaagents.general.NextConstants.*;

/**
 * Wegfindung ganz Simpel
 * @author miwo
 *
 */
public class NextSpiralPath implements INextPath
{
	// #fields
	private int changeSteps = 0;
	private int steps = 0;
	private int stepsToGo = 1;
	private ECardinals lastDirection = ECardinals.n;
	
	// #methods
	@Override
	public Action GenerateNextMove() 
	{
		if(stepsToGo == steps)
		{
			findNewDirection();
			steps = 0; // reset
		}
		
		// Alle 2 Richtungswechsel die Schrittweite aendern
		if(changeSteps == 2) 
		{
			stepsToGo++;
			changeSteps = 0;
		}
		
		steps++;
		return new Action("move", new Identifier(lastDirection.toString()));
	}
	
	private void findNewDirection() 
	{
		ECardinals newDirection = null;
		boolean isNewDirection = false;
		while(!isNewDirection) 
		{			
			switch(lastDirection)
			{
				case n:
					newDirection = ECardinals.e;
				break;
				case e:
					newDirection = ECardinals.s;
				break;
				case s:
					newDirection = ECardinals.w;
				break;
				case w:
					newDirection = ECardinals.n;
				break;
			}
			
			if(newDirection != lastDirection) {
				lastDirection = newDirection;
				isNewDirection = true;
			}
		}
		changeSteps++;
	}	
}
