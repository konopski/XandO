
package konopski.xando;

import java.awt.event.ActionEvent;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 * Visual JavaBean which allows to embed the X and O game in a Swing applications.
 * To use it first {@code setUserApp() } must be called. 
 * @author Lukasz Konopski
 */
public class Board extends javax.swing.JPanel implements PropertyChangeListener
{

    /**
	 * serialization UID
	 */
	private static final long serialVersionUID = - 8494815334926790485L;

	/** Creates new form Board */
    public Board() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setLayout(new java.awt.GridLayout(game.getSize(), game.getSize(), 4, 4));
        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    /** Game state holder */
    Game game;
    /** buttons - fields visual representation */
    FieldButton [] buttons;

    /** decides if game is finished */
    Arbiter judge=new Arbiter();

    /** generates next move */
    private IStrategy strategy=new RandomSlowStrategy();

    /** SAF application instance in which this runs */
    private Application userApp;
   
    /**
     * Sets the game size. For 3x3 game 3.
     * Changing this property causes the game to reset its state.
     * @param n size of game to be played.
     */
    public void setSize(int n)
    {
        game = new Game();
        game.setSize(n);
        restoreGame();
    }

    /**
     * Called after new game is created or game is loaded from file.
     */
    void restoreGame()
    {
        if(jPanel1!=null)
        {
            jPanel1.setVisible(false);
            removeAll();
        }
        jPanel1=null;
        initComponents();
        initFields(game.getSize());
        game.addFieldChangeListener(this);
    }

    /**
     * Initializes field buttons.
     * @param n size of game.
     */
    public void initFields(int n)
    {
        buttons = new FieldButton[n*n];
        for(int i = 0 ; i < n*n ; ++i)
        {
            FieldButton b=new FieldButton();
            b.setNumber(i);
            
            ActionMap actionMap=getUserApp().getContext().getActionMap(getClass(), this);
            b.setAction(actionMap.get("handleUserClick"));
            b.setValue(game.getFields(i)); //game could be stored in file
            buttons[i]=b;
            jPanel1.add(b);
            if(game.isFinished())
            {
                b.setEnabled(false);
            }
        }
    }

    /**
     * Handles user clicking on fields. Sets 'x' at field.
     * 
     * @param e event containing source button reference.
     * @return SAF will run the returned AnswerUserClickTask task. 
     */
    @Action (block=Task.BlockingScope.APPLICATION)
    public Task<Void,Void> handleUserClick(ActionEvent e)
    {
        FieldButton b=(FieldButton)e.getSource();
        //ignore click in already marked field
        if(b.getValue()!=null)
            return null;

        game.setFields(b.getNumber(), 'x');

        Task<Void,Void> task=new AnswerUserClickTask();
        return task;
    }

    
    /**
     * Handles change on field event sent from game object. 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        IndexedPropertyChangeEvent e=(IndexedPropertyChangeEvent)evt;
        FieldButton b=buttons[e.getIndex()];
        if(! e.getNewValue().equals(b.getValue()))
        {
            b.setValue((Character) e.getNewValue());
        }
    }

    /**
     * @return the strategy
     */
    public IStrategy getStrategy() {
        return strategy;
    }

    /**
     * @param strategy the strategy to set
     */
    public void setStrategy(IStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * @return the userApp
     */
    public Application getUserApp() {
        return userApp;
    }

    /**
     * @param userApp the userApp to set
     */
    public void setUserApp(org.jdesktop.application.Application userApp) {
        this.userApp = userApp;
        setSize(Game.DEFAULT_SIZE);
    }
    
    /**
     * Writes current {@code game } to stream as XML using standard bean persistence model.
     * Strategy is also included in file to ensure easy changes in future versions. 
     * @param e an encoder.
     */
    public void write(XMLEncoder e) {
        e.writeObject(game);
        e.writeObject(strategy);
    }

    /**
     * Loads and restores saved game.
     * Event dispatching thread is used to reinitialize components.
     * @param d a decoder.
     */
    public void read(XMLDecoder d) {
        game = (Game)d.readObject();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                restoreGame();
            }
        });
        strategy = (IStrategy)d.readObject();
    }

    
    /**
     * Called when game is finished. Shows confirmation dialog.
     */
    @Action
    public void finishGame()
    {
        String msg=getUserApp().getContext().getResourceMap(getClass())
                    .getString("game.lostMsg");
        if(game.getFields(game.getLastMove()).equals('x'))
        {
            msg=getUserApp().getContext().getResourceMap(getClass())
                    .getString("game.wonMsg");
        }
        msg+=getUserApp().getContext().getResourceMap(getClass())
                    .getString("game.playAgainMsg");
        
        String title=getUserApp().getContext().getResourceMap(getClass())
                    .getString("game.playAgainTitle");
        int ret=JOptionPane.showConfirmDialog(this,
                msg,title, JOptionPane.YES_NO_OPTION);
        if(ret!=JOptionPane.YES_OPTION)
        {
            for(FieldButton b:buttons)
            {
                b.setEnabled(false);
            }
        }
        else
        {
            int gameSize=game.getSize();
            setSize(gameSize);
        }
    }

    /**
     * Helper class run as task to avoid EDT blocking.
     * Performs all actions answering to user move:
     * checks if game is finished,
     * gets the next move of computer,
     * checks again for finish.
     */
    class AnswerUserClickTask extends Task<Void,Void>{

        /**
         * Default constructor. Task is embedded in user application (SAF).
         */
        public AnswerUserClickTask() {
            super(getUserApp());
            this.setUserCanCancel(false);            
        }

        /**
         * This method runs in working thread.
         */
        @Override
        protected Void doInBackground() throws Exception {

            message("startMessage");
            setDescription("description");
            setTitle("title");
            
            if(judge.isFinished(game))
            {
                game.setFinished(true);
                return null;
            }
            
            //game is not finished, now computer plays
            int nextMove=strategy.getNextMove(game);
            assert(nextMove>=0);
            assert(nextMove<buttons.length);

            game.setFields(nextMove, 'o');

            //check if finished
            if(judge.isFinished(game))
            {
                game.setFinished(true);
            }
            return null;
        }

        /** 
         * Called after successful execution of {@code doInBackground()}.
         */
        @Override
        protected void succeeded(Void result) {
            if(game.isFinished())
            {
                finishGame();
            }
        }


    }
}
