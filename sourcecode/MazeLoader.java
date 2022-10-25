// File             : MazeLoader.java
// Author           : David W. Collins Jr., Caleb Probst
// Date Created     : 03/01/2016
// Last Modified    : 03/21/2022
// Description      : This is the MazeLoader file for Math 271 where students
//                    will implement the recursive routine to "solve" the maze.

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


/** This is the main class that defines the window to load the maze
 * 
 * @author collindw and cprobst
 */
public class MazeLoader {
    
    private JFrame window;
    private Scanner fileToRead;
    private JPanel[][] grid;
    private static final Color WALL_COLOR = Color.BLUE.darker();
    private static final Color PATH_COLOR = Color.GREEN.brighter();
    private static final Color OPEN_COLOR = Color.WHITE;
    private static final Color BAD_PATH_COLOR  = Color.RED;
    private static int ROW;
    private static int COL;
    private String data;
    private Point start;
    private boolean allowMazeUpdate;
    private JMenuBar menuBar;
    private JMenu menu, saveMenu, changeMenu;
    private JMenuItem[] loadMaze;
    private JMenuItem save;
    private Timer timer;
    private JFileChooser mazeFile, pictureFile;
    private ArrayList<Point> pointArray;
    private ArrayList<Color> colorArray;
    private int init=0;
    
    /** Default constructor - initializes all private values
     * 
     */
    public MazeLoader() {
        // Intialize other "stuff"
        // Edited by student - Caleb Probst
        mazeFile = new JFileChooser();
        pictureFile = new JFileChooser();
        int option1 = JOptionPane.showOptionDialog(window, "Would you like to open a maze from a file or a picture?", "Opening Option", JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.PLAIN_MESSAGE, null, new String[]{"Picture","File"},0);
        if (option1 == 0){
            int option2 = JOptionPane.showOptionDialog(window, "Please use maze pictures from the website\n"
                        + "https://keesiemeijer.github.io/maze-generator/#generate.\n"+"Would you like to open this page?", "Open maze from a picture", 
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,null, new String[]{"Yes", "No"}, 0);
            if(option2 == 0){
                try {
                    Desktop.getDesktop().browse(new URL("https://keesiemeijer.github.io/maze-generator/#generate").toURI());
                }
                catch (Exception ex) {}
            }
                pictureFile.showOpenDialog(window);
                if(pictureFile.getSelectedFile()==null){
                    throw new IllegalArgumentException("Please choose a file to select");
                }
                // First decode the image
                // Read image file and create a new file
                BufferedImage img = null;
                FileWriter fileStream = null;
                try{
                    img = ImageIO.read(pictureFile.getSelectedFile());
                    fileStream = new FileWriter("CreatedMaze.maze");
                }
                catch (IOException h){
                    System.out.println("Error caught, exiting");
                    System.exit(0);
                }
                BufferedWriter picOut = new BufferedWriter(fileStream);
                int width = img.getWidth();
                int height = img.getHeight();
                try{
                    picOut.write(""+(height)/10+" "+(width)/10);
                    picOut.newLine();
                }
                catch(IOException h){}

                for (int y = 0; y <= (height-10); y+=10) {
                    for (int x = 0; x <= (width-10); x+=10) {
                        int c = img.getRGB(x,y);
                        int  red = (c & 0x0000FFFF) >> 16;
                        int  green = (c & 0x0000FFFF) >> 8;
                        int  blue = c & 0x0000FFFF;
                        if(red<10 && blue<10 && green<10){
                            try{
                                picOut.write("*");
                            }
                            catch(IOException d){}
                        }
                        else{
                            try{
                                picOut.write(" ");
                            }
                            catch(IOException d){}
                        }
                    }
                    try{
                        picOut.newLine();
                    }
                    catch(IOException u){}

                }


                try{
                    picOut.close();
                }
                catch(IOException h){}
                mazeFile.setSelectedFile(new File("CreatedMaze.maze"));  
        }
        else{
            JOptionPane.showMessageDialog(window, "Please choose a file to load the maze from", 
                "Choose maze", JOptionPane.PLAIN_MESSAGE);
            mazeFile.showOpenDialog(window);
        }
        // End of edit
        start = new Point();
        allowMazeUpdate = true;
        timer = new Timer(100, new TimerListener());
        
        // Create the maze window
        window = new JFrame("Maze Program");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Need to define the layout - as a grid depending on the number
        // of grid squares to use. Open the file and read in the size.
        try {
            
            fileToRead = new Scanner(mazeFile.getSelectedFile());
            ROW = fileToRead.nextInt();
            COL = fileToRead.nextInt();
        }
        catch(FileNotFoundException e) {
            JOptionPane.showMessageDialog(window,"Default maze not found. " +
                    "\nSelect a maze to solve from the menu," +
                    "\nor rename maze to maze.txt", "Error", JOptionPane.ERROR_MESSAGE);
            allowMazeUpdate = false;
        }

        if(allowMazeUpdate) {
            // Now establish the Layout - appropriate to the grid size
            window.setLayout(new GridLayout(ROW, COL));
            grid= new JPanel[ROW][COL];
            data = fileToRead.nextLine();
            for(int i=0; i<ROW; i++) {
                data = fileToRead.nextLine();
                for(int j=0; j<COL; j++) {
                    grid[i][j] = new JPanel();
                    grid[i][j].setName("" + i + ":" + j);
                    if(data.charAt(j) == '*') 
                        grid[i][j].setBackground(WALL_COLOR);
                    else {
                        grid[i][j].setBackground(OPEN_COLOR);
                             grid[i][j].addMouseListener(new MazeListener());
                    }
                    window.add(grid[i][j]);
                }
            }
            fileToRead.close();
            window.pack();
        }

        // Add the menu to the window
        menuBar = new JMenuBar();
        menu = new JMenu("Load Maze...");
        // Edited by student - Caleb Probst
        changeMenu = new JMenu("Change...");
        loadMaze = new JMenuItem[5];
        // End of edit
        loadMaze[0] = new JMenuItem("Load New Maze from another file...");
        loadMaze[0].addActionListener(new LoadMazeFromFile());
        loadMaze[1] = new JMenuItem("Load New Maze from current maze...");
        loadMaze[1].addActionListener(new ReloadCurrentMaze());
        // Edited by student - Caleb Probst
        loadMaze[2] = new JMenuItem("Load New Maze from a PICTURE...");
        loadMaze[2].addActionListener(new PictureMaze());
        loadMaze[3] = new JMenuItem("Create a new maze!!!");
        loadMaze[3].addActionListener(new CreateAMaze());
        loadMaze[4] = new JMenuItem("Change speed of timer");
        loadMaze[4].addActionListener(new ChangeSpeed());
        // End of edit
        menu.add(loadMaze[0]);
        menu.add(loadMaze[1]);
        // Edited by student - Caleb Probst
        menu.add(loadMaze[2]);
        menu.add(loadMaze[3]);
        changeMenu.add(loadMaze[4]);
        // End of edit
        
        menuBar.add(menu);
        menuBar.add(changeMenu);
        window.setJMenuBar(menuBar);
        
        if(!allowMazeUpdate)
            window.setSize(100,50);
       
        // Finally, show the maze
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        // Edited by student - Caleb Probst
        pointArray = new ArrayList<>();
        colorArray = new ArrayList<>();
        // End of edit
    }
    
    /** MazeListener class reacts to mouse presses - only when the current
     *  block that is clicked is a valid starting point within the maze.
     */
    private class MazeListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        /** mousePressed method defines the (x,y) coordinate of the starting
         *  square within the maze. Note: the start Point object does NOT
         *  reference the pixel location, rather the matrix location.
         * @param e - the MouseEvent created upon mouse click.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if(((JPanel)e.getSource()).getBackground().equals(OPEN_COLOR) &&
                    !timer.isRunning()) {
                data = ((JPanel)e.getSource()).getName();
                start.x = Integer.parseInt(data.substring(0,data.indexOf(":")));
                start.y = Integer.parseInt(data.substring(data.indexOf(":")+1));
              
                // Find the maze solution
                if(!findPath(start)){
                    // Reset board before it shows
                    for(int i=0; i<ROW; i++)
                        for(int j=0; j<COL; j++)
                            if(grid[i][j].getBackground().equals(PATH_COLOR) ||
                               grid[i][j].getBackground().equals(BAD_PATH_COLOR))
                               grid[i][j].setBackground(OPEN_COLOR);
                    // Use timer to show the path of the maze (take the ArrayList and run it backwards)
                    JOptionPane.showMessageDialog(window,"Cannot exit maze.");
                    timer.start();
                }
                else{
                    // Reset board before reset
                    for(int i=0; i<ROW; i++)
                        for(int j=0; j<COL; j++)
                            if(grid[i][j].getBackground().equals(PATH_COLOR) ||
                               grid[i][j].getBackground().equals(BAD_PATH_COLOR))
                               grid[i][j].setBackground(OPEN_COLOR);
                    JOptionPane.showMessageDialog(window, "Maze Exited!");
                    timer.start();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
        
    }
    
    /** findPath is the recursive routine to find the solution through the maze
     * 
     * @param p - the current Point in the maze
     * @return whether or not a solution has been found.
     */
    public boolean findPath(Point p)  {
        try{
            boolean foundSolution = false;

            // STUDENTS FINISH CODE HERE
            // Move up, left, down, right
            pointArray.add(p);
            colorArray.add(PATH_COLOR);
            grid[(int)p.getX()][(int)p.getY()].setBackground(PATH_COLOR);

            if(!foundSolution && ((p.getX()-1==0 && grid[(int)p.getX()-1][(int)p.getY()].getBackground().equals(OPEN_COLOR)) || 
                                 (grid[(int)p.getX()+1][(int)p.getY()].getBackground().equals(OPEN_COLOR) && p.getX()+1==ROW-1) ||
                                 (p.getY()-1==0 && grid[(int)p.getX()][(int)p.getY()-1].getBackground().equals(OPEN_COLOR)) ||
                                 (grid[(int)p.getX()][(int)p.getY()+1].getBackground().equals(OPEN_COLOR) && p.getY()+1==COL-1))){
                if(p.getX()-1==0 && grid[(int)p.getX()-1][(int)p.getY()].getBackground().equals(OPEN_COLOR)){
                    grid[(int)p.getX()-1][(int)p.getY()].setBackground(PATH_COLOR);
                    pointArray.add(new Point((int)p.getX()-1,(int)p.getY()));
                    colorArray.add(PATH_COLOR);
                }
                else if(grid[(int)p.getX()+1][(int)p.getY()].getBackground().equals(OPEN_COLOR) && p.getX()+1==ROW-1){
                    grid[(int)p.getX()+1][(int)p.getY()].setBackground(PATH_COLOR);
                    pointArray.add(new Point((int)p.getX()+1,(int)p.getY()));
                    colorArray.add(PATH_COLOR);
                }
                else if(p.getY()-1==0 && grid[(int)p.getX()][(int)p.getY()-1].getBackground().equals(OPEN_COLOR)){
                    grid[(int)p.getX()][(int)p.getY()-1].setBackground(PATH_COLOR);
                    pointArray.add(new Point((int)p.getX(),(int)p.getY()-1));
                    colorArray.add(PATH_COLOR);
                }
                else if(grid[(int)p.getX()][(int)p.getY()+1].getBackground().equals(OPEN_COLOR) && p.getY()+1==COL-1){
                    grid[(int)p.getX()][(int)p.getY()+1].setBackground(PATH_COLOR);
                    pointArray.add(new Point((int)p.getX(),(int)p.getY()+1));
                    colorArray.add(PATH_COLOR);
                }
                foundSolution = true;
            }
            else if (!foundSolution){
                // Move up
                if(grid[(int)p.getX()-1][(int)p.getY()].getBackground().equals(OPEN_COLOR) && !foundSolution){
                    foundSolution = findPath(new Point((int)p.getX()-1, (int)p.getY()));
                }
                // Move left
                if(grid[(int)p.getX()][(int)p.getY()-1].getBackground().equals(OPEN_COLOR) && !foundSolution){
                    foundSolution = findPath(new Point((int)p.getX(), (int)p.getY()-1));
                }
                // Move down
                if(grid[(int)p.getX()+1][(int)p.getY()].getBackground().equals(OPEN_COLOR) && !foundSolution){
                    foundSolution = findPath(new Point((int)p.getX()+1, (int)p.getY()));
                }
                // Move right
                if(grid[(int)p.getX()][(int)p.getY()+1].getBackground().equals(OPEN_COLOR) && !foundSolution){
                    foundSolution = findPath(new Point((int)p.getX(), (int)p.getY()+1));
                }
                // If haven't found solution yet, color path as bad path
                if (!foundSolution){
                    grid[(int)p.getX()][(int)p.getY()].setBackground(BAD_PATH_COLOR);
                    pointArray.add(p);
                    colorArray.add(BAD_PATH_COLOR);
                }
            }
            return foundSolution;
        }
        catch(StackOverflowError ex){
            JOptionPane.showMessageDialog(null, "A Stack Overflow Error has occured.\nPlease use the Java command -Xss1024m and -Xms2048m and try again.", "Stack Overflow Error", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
            return false;
        }
    }
    
    /** ReloadCurrentMaze class listens to menu clicks - simply
     *  wipes the current state of the maze.
     */
    private class ReloadCurrentMaze implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            for(int i=0; i<ROW; i++)
                for(int j=0; j<COL; j++)
                    if(grid[i][j].getBackground().equals(PATH_COLOR) ||
                       grid[i][j].getBackground().equals(BAD_PATH_COLOR))
                         grid[i][j].setBackground(OPEN_COLOR);
            timer.stop();
            colorArray.clear();
            pointArray.clear();
            init=0;
        }
    }
    
    /** LoadMazeFromFile class listens to menu clicks - if the student
     *  wishes to earn extra credit, implement this method by utilizing a
     *  FileChooser to allow the user to choose the maze file, rather than
     *  have it hard-coded in the program as "maze.txt"
     */
    private class LoadMazeFromFile implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            window.dispose();
            new MazeLoader();
        }
    } // end of LoadMazeFromFile class
    /** 
     * PictureMaze class listens to menu clicks - This specifically looks
     * at the "Load from a picture" menu option. This takes a picture of
     * a certain picture from the website https://keesiemeijer.github.io/maze-generator/#generate
     * and is able to convert it into asterisks and spaces, which is what the 
     * main object is able to read. 
     */
    private class PictureMaze implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            window.dispose();
            int option = JOptionPane.showOptionDialog(window, "Please use maze pictures from the website\n"
                    + "https://keesiemeijer.github.io/maze-generator/#generate.\n"+"Would you like to open this page?", "Open maze from a picture", 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,null, new String[]{"Yes", "No"}, 0);
            if(option == 0){
                try {
                Desktop.getDesktop().browse(new URL("https://keesiemeijer.github.io/maze-generator/#generate").toURI());
            } catch (Exception ex) {}
            }
            
            pictureFile.showOpenDialog(window);
            if(pictureFile.getSelectedFile()==null){
                throw new IllegalArgumentException("Please select a file");
            }
            // First decode the image
            // Read image file and create a new file
            BufferedImage img = null;
            FileWriter fileStream = null;
            try{
                img = ImageIO.read(pictureFile.getSelectedFile());
                fileStream = new FileWriter("CreatedMaze.maze");
            }
            catch (IOException h){
                System.out.println("Error caught, exiting");
                System.exit(0);
            }
            BufferedWriter picOut = new BufferedWriter(fileStream);
            int width = img.getWidth();
            int height = img.getHeight();
            try{
                picOut.write(""+(height)/10+" "+(width)/10);
                picOut.newLine();
            }
            catch(IOException h){}
            
            for (int y = 0; y <= (height-10); y+=10) {
                for (int x = 0; x <= (width-10); x+=10) {
                    int c = img.getRGB(x,y);
                    int  red = (c & 0x0000FFFF) >> 16;
                    int  green = (c & 0x0000FFFF) >> 8;
                    int  blue = c & 0x0000FFFF;
                    if(red<10 && blue<10 && green<10){
                        try{
                            picOut.write("*");
                        }
                        catch(IOException d){}
                    }
                    else{
                        try{
                            picOut.write(" ");
                        }
                        catch(IOException d){}
                    }
                }
                try{
                    picOut.newLine();
                }
                catch(IOException u){}
            }
            try{
                picOut.close();
            }
            catch(IOException h){}
            
            // Load the created file into the maze
            // Intialize other "stuff"
        start = new Point();
        allowMazeUpdate = true;
        timer = new Timer(100, new TimerListener());
        
        // Create the maze window
        window = new JFrame("Maze Program");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Need to define the layout - as a grid depending on the number
        // of grid squares to use. Open the file and read in the size.
        try {
            
            fileToRead = new Scanner(new File("CreatedMaze.maze"));
            ROW = fileToRead.nextInt();
            COL = fileToRead.nextInt();
        }
        catch(FileNotFoundException u) {
            JOptionPane.showMessageDialog(window,"Default maze not found. " +
                    "\nSelect a maze to solve from the menu," +
                    "\nor rename maze to maze.txt", "Error", JOptionPane.ERROR_MESSAGE);
            allowMazeUpdate = false;
        }

        if(allowMazeUpdate) {
            // Now establish the Layout - appropriate to the grid size
            window.setLayout(new GridLayout(ROW, COL));
            grid= new JPanel[ROW][COL];
            data = fileToRead.nextLine();
            for(int i=0; i<ROW; i++) {
                data = fileToRead.nextLine();
                for(int j=0; j<COL; j++) {
                    grid[i][j] = new JPanel();
                    grid[i][j].setName("" + i + ":" + j);
                    if(data.charAt(j) == '*') 
                        grid[i][j].setBackground(WALL_COLOR);
                    else {
                        grid[i][j].setBackground(OPEN_COLOR);
                             grid[i][j].addMouseListener(new MazeListener());
                    }
                    window.add(grid[i][j]);
                }
            }
            fileToRead.close();
            window.pack();
        }

        // Add the menu to the window
        menuBar = new JMenuBar();
        menu = new JMenu("Load Maze...");
        changeMenu = new JMenu("Change...");
        loadMaze[0] = new JMenuItem("Load New Maze from another file...");
        loadMaze[0].addActionListener(new LoadMazeFromFile());
        loadMaze[1] = new JMenuItem("Load New Maze from current maze...");
        loadMaze[1].addActionListener(new ReloadCurrentMaze());
        loadMaze[2] = new JMenuItem("Load New Maze from a PICTURE...");
        loadMaze[2].addActionListener(new PictureMaze());
        loadMaze[3] = new JMenuItem("Create a new maze!!!");
        loadMaze[3].addActionListener(new CreateAMaze());
        loadMaze[4] = new JMenuItem("Change speed of timer");
        loadMaze[4].addActionListener(new ChangeSpeed());
        menu.add(loadMaze[0]);
        menu.add(loadMaze[1]);
        menu.add(loadMaze[2]);
        menu.add(loadMaze[3]);
        changeMenu.add(loadMaze[4]);
        
        menuBar.add(menu);
        menuBar.add(changeMenu);
        window.setJMenuBar(menuBar);
        
        if(!allowMazeUpdate)
            window.setSize(100,50);
       
        // Finally, show the maze
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        pointArray = new ArrayList<>();
        colorArray = new ArrayList<>();
        }
        
    }
    /**
     * This class listens to menu clicks - Specifically listens to the "Create a new maze"
     * menu item. This creates a new window of blank JPanels which are all filled
     * with the OPEN_COLOR color of ROW x COL. With the specific instructions
     * given by the message, you can now create and color certain mazes.
     */
    private class CreateAMaze implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            window.dispose();
            // Send message to get dims of the maze you are wanting to create
            String dims = JOptionPane.showInputDialog(window,"Please enter the dimensions of the maze you are wanting to create.\n"
                + "Enter in dimensions as \"(Width)x(Height)\"","Create a maze");
            int width = 0, height = 0;
            for (int i = 0; i < dims.length(); i++) {
                if(dims.charAt(i)=='x' || dims.charAt(i)=='X'){
                    StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
                    for (int j = 0; j < i; j++) {
                        sb1.append(""+dims.charAt(j));
                    }
                    for (int j = i; j < dims.length()-1; j++) {
                        sb2.append(""+dims.charAt(j+1));
                    }
                    width = Integer.parseInt(sb1.toString());
                    height = Integer.parseInt(sb2.toString());
                }
            }
            COL = height;
            ROW = width;
            JOptionPane.showMessageDialog(window, "Instructions:\nUse Ctrl key and hover to create an open path\nUse Shift key and hover to create a wall","Instructions",JOptionPane.PLAIN_MESSAGE);
            // Intialize other "stuff"
            start = new Point();
            allowMazeUpdate = true;
            timer = new Timer(100, new TimerListener());

            // Create the maze window
            window = new JFrame("Maze Program");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Need to define the layout - as a grid depending on the number
            // of grid squares to use. Open the file and read in the size.
            if(allowMazeUpdate) {
                window.setLayout(new GridLayout(ROW, COL));
                grid= new JPanel[ROW][COL];
                for(int i=0; i<ROW; i++) {
                    for(int j=0; j<COL; j++) {
                        grid[i][j] = new JPanel();
                        grid[i][j].setName("" + i + ":" + j);
                        grid[i][j].setBackground(OPEN_COLOR);
                        grid[i][j].addMouseListener(new CreateMazeListener());
                        window.add(grid[i][j]);
                    }
                }
            }
            window.pack();

            // Add the menu to the window
            menuBar = new JMenuBar();
            menu = new JMenu("Load Maze...");
            saveMenu = new JMenu("Save Maze...");
            loadMaze[0] = new JMenuItem("Load New Maze from another file...");
            loadMaze[0].addActionListener(new LoadMazeFromFile());
            loadMaze[1] = new JMenuItem("Load New Maze from current maze...");
            loadMaze[1].addActionListener(new ReloadCurrentMaze());
            loadMaze[2] = new JMenuItem("Load New Maze from a PICTURE...");
            loadMaze[2].addActionListener(new PictureMaze());
            loadMaze[3] = new JMenuItem("Create a new maze!!!");
            loadMaze[3].addActionListener(new CreateAMaze());
            save = new JMenuItem("Save maze as a file...");
            save.addActionListener(new SaveMaze());
            menu.add(loadMaze[0]);
            menu.add(loadMaze[1]);
            menu.add(loadMaze[2]);
            menu.add(loadMaze[3]);
            saveMenu.add(save);
            

            menuBar.add(menu);
            menuBar.add(saveMenu);
            window.setJMenuBar(menuBar);
            

            if(!allowMazeUpdate)
                window.setSize(100,50);

            // Finally, show the maze
            window.setResizable(false);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            pointArray = new ArrayList<>();
            colorArray = new ArrayList<>();
        }
        /**
        * This class listens to mouse movement - Specifically, this is attatched to each
        * individual JPanel that is displayed when the "Create a maze" screen is
        * visible. This will listen to the hovering of the mouse, and which key is
        * pressed, either Ctrl or Shift.
        */
        private class CreateMazeListener implements MouseListener{

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if(e.isShiftDown()){
                    e.getComponent().setBackground(WALL_COLOR);
                }
                if(e.isControlDown()){
                    e.getComponent().setBackground(OPEN_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
            
        }
        /**
        * This class listens to menu clicks - Specifically listens to the "Save maze to file" 
        * menu item. This will take the colors that are displayed from the grid and update a
        *  file that is named by the user, with asterisks and spaces.
        */
        private class SaveMaze implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(window, "Please input the name of the file.\nThe file will end in .maze when created", 
                        "Name of the file", JOptionPane.PLAIN_MESSAGE);
                FileWriter fileStream = null;
                try{
                    fileStream = new FileWriter(name +".maze");
                }
                catch (IOException h){
                    System.out.println("Error caught, exiting");
                    System.exit(0);
                }
                BufferedWriter fileOut = new BufferedWriter(fileStream);
                try{
                    fileOut.write(ROW + " " + COL);
                    fileOut.newLine();
                    for (int i = 0; i < ROW; i++) {
                        for (int j = 0; j < COL; j++) {
                            if(grid[i][j].getBackground().equals(WALL_COLOR)){
                                fileOut.write("*");
                            }
                            if(grid[i][j].getBackground().equals(OPEN_COLOR)){
                                fileOut.write(" ");
                            }
                        }
                        fileOut.newLine();
                    }
                    fileOut.close();
                }
                catch (IOException h){}
                JOptionPane.showMessageDialog(window, "File has been saved!", "File created", JOptionPane.PLAIN_MESSAGE);
                window.dispose();
                new MazeLoader();
            }
            
        }
    }
    /**
     * This class listens to menu clicks - Specifically listens to the "Change speed" menu item. 
     * This simply just updates the speed of the timer.
     */
    private class ChangeSpeed implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                int speed = Integer.parseInt(JOptionPane.showInputDialog(window, 
                        "Please enter in a value from 1-1000","Change Speed",
                        JOptionPane.PLAIN_MESSAGE));
                if (speed<1){
                    throw new IllegalArgumentException("Speed cannot be less than 1");
                }
                else if (speed>=1000){
                    throw new IllegalArgumentException("Speed cannot be greater than 1000");
                }
                else{
                    timer.setDelay(speed);
                }
            }
            
        }
    /** TimerListener class - Extra credit for students: instead of simply
     *  showing the solution path, show the solution path & any incorrect
     *  paths (and backtracking) by saving the Points in the maze visited
     *  in a "solutionArray", and in this timer method, each time the "timer"
     *  goes off, print the new state of the board according to the 
     *  solution Array. This will give the user a slowed down visualization
     *  of your recursive routine (although it would have finished already)
	 *  Additionally, you're welcome to use a container class to not only 
	 *  track the solution, but all the incorrect paths and display not only 
	 *  the correct path, but all the incorrect path choices made in the
	 *  recursive steps.
     */
    private class TimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(init == pointArray.size()){
                timer.stop();
                init = 0;
                pointArray.clear();
                colorArray.clear();
            }
            else{
                grid[(int)pointArray.get(init).getX()][(int)pointArray.get(init).getY()].setBackground(colorArray.get(init));
                init++;
            }
        }
    }
}
