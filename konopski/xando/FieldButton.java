package konopski.xando;

import javax.swing.JButton;

/**
 * Represents game field.
 * @author Łukasz Konopski
 */
public class FieldButton extends JButton {
    /**
	 * serialization UID 
	 */
	private static final long serialVersionUID = - 5364454262782476185L;
	
	/** position of field in game */
    private int number;
    /** {@code null} for not clicked, 'x' for clicked by user,
     * 'o' for marked by computer player */
    private Character value;

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the value
     */
    public Character getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Character value) {
        this.value = value;
        if(null!=value)
            setText(""+value); //show it
        else
            setText("");
    }

    
}
