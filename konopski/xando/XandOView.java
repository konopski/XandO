/*
 * XandOView.java
 */

package konopski.xando;

import java.io.File;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.application.Task;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The application's main frame.
 */
public class XandOView extends FrameView {

    /**
     * Default constructor.
     * @param app SAF application.
     */
    public XandOView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    /**
     * Shows about dialog.
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = XandOApp.getApplication().getMainFrame();
            aboutBox = new XandOAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        XandOApp.getApplication().show(aboutBox);
    }

    /**
     * Prepares to file loading.
     * @return task loading file in working thread.
     */
    @Action
    public Task<Void,Void> load() {
        JFileChooser fc = createFileChooser("loadFileChooser");
        int option = fc.showOpenDialog(getFrame());
        Task<Void,Void> task = null;
        if (JFileChooser.APPROVE_OPTION == option)
        {
            File file=fc.getSelectedFile();
            //get resources here to keep them in one properties file
            String msg = getResourceMap().getString("load.failedMessage", file);
            String title = getResourceMap().getString("load.failedTitle");
            task = new LoadFromFileTask(file, msg, title);
        }
        return task;
    }

    /**
     * Prepares to file saving.
     * @return task saving file in working thread.
     */
    @Action
    public Task<Void,Void> save() {
        JFileChooser fc = createFileChooser("saveFileChooser");
        int option = fc.showSaveDialog(getFrame());
        Task<Void,Void> task = null;
        if (JFileChooser.APPROVE_OPTION == option)
        {
            File file=fc.getSelectedFile();
            String msg = getResourceMap().getString("save.failedMessage", file);
            String title = getResourceMap().getString("save.failedTitle");
            task = new SaveToFileTask(file, msg, title);
        }
        return task;
    }

    /**
     * Base class providing common code for file operations.
     * Override {@code operate()} to implement task's operation.
     */
    abstract class FileOperationTask extends Task<Void, Void>
                                     implements ExceptionListener
    {
        /** file to be operated on  */
        protected File file;
        /** message shown on failure */
        protected String msg;
        /** dialog title shown on failure */
        protected String title;

        /**
         * Constructor
         * @param file the file to be operated on.
         * @param msg message shown on failure 
         * @param title dialog title shown on failure
         */
        FileOperationTask(File file, String msg, String title) {
            super(XandOApp.getApplication());
            this.file=file;
            this.msg=msg;
            this.title=title;
        }

        /**
         * override this method to read or write to file.
         * @throws java.io.IOException when bad things happen to file access.
         */
        public abstract void operate() throws IOException;

        /**
         * Simply calls {@code operate()} from subclass.
         * @return always {@literal null}
         * @throws java.lang.Exception when problem with file.
         */
        @Override
        protected Void doInBackground() throws Exception {            
            operate();
            return null;
        }

        /**
         * Allows exception collected during XML processing
         * to be handled by SAF and call {@code failed()} in
         * event dispatching thread.
         * @param e thrown exception.
         */
        public void exceptionThrown(Exception e) {
            throw new RuntimeException(e);
        }

        /**
         * Called in case of exception. Shows a error message.
         * @param cause the exception caught.
         */
        @Override
        protected void failed(Throwable cause) {
              logger.log(Level.SEVERE, "failed file operation " + file, cause);
              int type = JOptionPane.ERROR_MESSAGE;
              JOptionPane.showMessageDialog(getFrame(), msg, title, type);
        }
    }

    /**
     * Task loading game state from file.
     */
    class LoadFromFileTask extends FileOperationTask
    {        
    	/**
         * Constructor
         * @param file the file to be loaded.
         * @param msg message shown on failure 
         * @param title dialog title shown on failure
         */
        LoadFromFileTask(File file, String msg, String title)
        {
            super(file,msg,title);
        }
        
        /* 
         * @see konopski.xando.XandOView.FileOperationTask#operate()
         */
        @Override
        public void operate() throws IOException {
            XMLDecoder d=new XMLDecoder(
                    new BufferedInputStream(new FileInputStream(file)),
                        null, this);
            board1.read(d);
            d.close();           
        }
    }

    /**
     * Task saving game state to file.
     */
    class SaveToFileTask extends FileOperationTask
    {
    	/**
         * Constructor
         * @param file the file to be loaded.
         * @param msg message shown on failure 
         * @param title dialog title shown on failure
         */
        SaveToFileTask(File file, String msg, String title)
        {
            super(file,msg,title);
        }

        /* 
         * @see konopski.xando.XandOView.FileOperationTask#operate()
         */
        @Override
        public void operate() throws IOException {
            XMLEncoder e=new XMLEncoder(new FileOutputStream(file));
            e.setExceptionListener(this);
            board1.write(e);
            e.close();
        }
    }

    /**
     * @param name title resource name prefix.
     * @return file chooser component accepting XML files.
     */
    private JFileChooser createFileChooser(String name) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(getResourceMap().getString(name + ".dialogTitle"));
        String xmlFilesDesc = getResourceMap().getString("xmlFileExtensionDescription");
        fc.setFileFilter(new FileNameExtensionFilter(xmlFilesDesc,"xml"));
        return fc;
    }

    /**
     * Starts a new game with confirmation dialog.
     * Registers current application as user.
     */
    @Action
    public void startNewGame() {
        String msg=getResourceMap().getString("game.startNewMsg");
        String title=getResourceMap().getString("game.starNewTitle");
        
        int ret=JOptionPane.showConfirmDialog(getFrame(),
                msg,title, JOptionPane.YES_NO_OPTION);
        if(ret==JOptionPane.YES_OPTION)
        {
            board1.setUserApp(XandOApp.getApplication());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        board1 = new konopski.xando.Board();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        loadMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        board1.setName("board1"); // NOI18N
        board1.setUserApp(
            org.jdesktop.application.Application.getInstance(
                konopski.xando.XandOApp.class));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(board1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(board1, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(konopski.xando.XandOApp.class).getContext().getResourceMap(XandOView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(konopski.xando.XandOApp.class).getContext().getActionMap(XandOView.class, this);
        newMenuItem.setAction(actionMap.get("startNewGame")); // NOI18N
        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        newMenuItem.setText(resourceMap.getString("newMenuItem.text")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);
        newMenuItem.getAccessibleContext().setAccessibleName(resourceMap.getString("newMenuItem.AccessibleContext.accessibleName")); // NOI18N
        newMenuItem.getAccessibleContext().setAccessibleDescription(resourceMap.getString("newMenuItem.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        loadMenuItem.setAction(actionMap.get("load")); // NOI18N
        loadMenuItem.setText(resourceMap.getString("loadMenuItem.text")); // NOI18N
        loadMenuItem.setName("loadMenuItem"); // NOI18N
        fileMenu.add(loadMenuItem);
        loadMenuItem.getAccessibleContext().setAccessibleName(resourceMap.getString("loadMenuItem.AccessibleContext.accessibleName")); // NOI18N
        loadMenuItem.getAccessibleContext().setAccessibleDescription(resourceMap.getString("loadMenuItem.AccessibleContext.accessibleDescription")); // NOI18N

        saveMenuItem.setAction(actionMap.get("save")); // NOI18N
        saveMenuItem.setText(resourceMap.getString("saveMenuItem.text")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        fileMenu.add(saveMenuItem);
        saveMenuItem.getAccessibleContext().setAccessibleName(resourceMap.getString("saveMenuItem.AccessibleContext.accessibleName")); // NOI18N
        saveMenuItem.getAccessibleContext().setAccessibleDescription(resourceMap.getString("saveMenuItem.AccessibleContext.accessibleDescription")); // NOI18N

        jSeparator2.setName("jSeparator2"); // NOI18N
        fileMenu.add(jSeparator2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private konopski.xando.Board board1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private final Logger logger = Logger.getLogger(getClass().getName());
}
