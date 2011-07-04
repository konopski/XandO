
package konopski.xando;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

/**
 * Holds the game state.
 * @author Lukasz Konopski
 */
public class Game implements FieldContainer {

    /** default size (one dimension) */
    public final static int DEFAULT_SIZE=3;

    /** actual size */
    int size=DEFAULT_SIZE;

    /** game fields container */
    private Character [] fields=new Character[size*size];

    /** last move (number of field last marked)*/
    int lastMove=-1;

    /** support for notifications to UI */
    PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /** true if game is finished */
    private boolean finished;

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(int size) {
        assert(size>0);
        this.size = size;
        setFields(new Character[size * size]);
    }

    //the below methods are synchronized
    //further tests needed to determine if this can be removed
    
    /* 
     * @see konopski.xando.FieldContainer#getFields(int)
     */
    public synchronized Character getFields(int index) {
        return getFields()[index];
    }

    /* 
     * @see konopski.xando.FieldContainer#setFields(int, java.lang.Character)
     */
    public synchronized void setFields(int index, Character value) {
        //check range
        assert(null==value || value.equals('o') || value.equals('x'));
        
        Character oldVal=getFields()[index];
        if(null!=oldVal)
        {
            return;
        }
        
        getFields()[index]=value;
        setLastMove(index);
        fireFieldChange(index, oldVal, value);        
    }

    /**
     * @return index of last move
     */
    public int getLastMove() {
        return lastMove;
    }

    /**
     * @param lastMove index of last move to set
     */
    public void setLastMove(int lastMove){
        this.lastMove=lastMove;
    }

    /**
     * this method is to introduce indexed property.
     * @return the fields
     */
    public Character[] getFields() {
        return fields;
    }

    /**
     * this method is to introduce indexed property.
     * @param fields the fields to set
     */
    public void setFields(Character[] fields) {
        this.fields = fields;
    }

    /**
     * Adds listener to field changes
     * @param listener object willing to receive events
     */
    public void addFieldChangeListener(PropertyChangeListener listener) {
        if (listener == null)
            return;
       
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * sends event notification to listeners
     *
     * @param index index of changed field
     * @param oldVal old field value
     * @param newVal new field value
     */
    void fireFieldChange(final int index, final Character oldVal, final Character newVal){
        
        //TODO this check should be rather done in the change support object,
        //as a result Game depends now on Swing
        if(SwingUtilities.isEventDispatchThread())
        {
            changeSupport.fireIndexedPropertyChange(
                                    "fields", index, oldVal, newVal);
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    changeSupport.fireIndexedPropertyChange(
                                    "fields", index, oldVal, newVal);
                }
            });
        }
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
