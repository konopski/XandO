package konopski.xando;

/**
 * Interface providing game state.
 * @author Łukasz Konopski
 */
public interface FieldContainer {

    /**
     * @return the game size.
     */
    int getSize();
    
    /**
     * Gets field value.
     * @param index number of field.
     * @return  a {@code null} for not clicked, 
     * {@value 'x'} for clicked by user,
     * {@value 'o'} for marked by computer player
     */
    Character getFields(int index);

    /**
     * Sets field at given position.
     * @param index number of field. 
     * @param value should be {@value 'x'}  or {@value 'o'}  
     */
    void setFields(int index, Character value);

    /**
     * @return position of last move.
     */
    int getLastMove();
    
}
