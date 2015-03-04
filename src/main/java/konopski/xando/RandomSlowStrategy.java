
package konopski.xando;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Slow random strategy.
 * @author Lukasz Konopski
 */
public class RandomSlowStrategy implements IStrategy {

    /** provides random numbers    */
    Random rand=new Random();

    /* 
     * @see konopski.xando.IStrategy#getNextMove(konopski.xando.FieldContainer)
     */
    public int getNextMove(FieldContainer g) {
        try 
        {
        	//simulate long algorithm execution
            Thread.sleep(rand.nextInt(5000)); //makes slow
        }
        catch (InterruptedException ex) 
        {
            Logger.getLogger(RandomSlowStrategy.class.getName())
                                        .log(Level.SEVERE, null, ex);
        }
        int nextMove=-1;
        do
        {
            nextMove=rand.nextInt(g.getSize()*g.getSize());            
        }
        while(null!=g.getFields(nextMove));
                    
        return nextMove;
    }

}
