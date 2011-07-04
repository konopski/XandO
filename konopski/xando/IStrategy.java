
package konopski.xando;

/**
 * Computer play strategy interface.
 * @author Łukasz Konopski
 */
public interface IStrategy {

    /**
     * Calculates next move according to state of game provided
     * as argument
     * @param g game state
     * @return next move
     */
    int getNextMove(FieldContainer g);

}
