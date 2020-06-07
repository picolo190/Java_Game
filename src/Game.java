import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import java.lang.Runnable;
import java.lang.Thread;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class Game extends JFrame implements Runnable
{

    private static Game gameInstance = null;
    //Alpha represents an hexa number representing the background color of the spreadsheets
    //We need this to be saved so we make the sprites have a transparent background
    public static int alpha = 0xFFFF00DC;
    private Connection c = null;

    private Canvas canvas = new Canvas();
    private RenderHandler renderer;

    private SpriteSheet playerSheet;

    /**
     * Sprite fields for loading menu buttons
     */
    private Sprite startMenuSprite;
    private Sprite helpMenuSprite;
    private Sprite quitMenuSprite;
    private Sprite backMenuSprite;
    private Sprite gameOverSprite;
    private Sprite highscoreSprite;
    private Graphics lastImage=null;

    public enum STATE{
        MENU, GAME, HELP, GAMEOVER, HIGHSCORE

    }
    private STATE State = STATE.MENU;


    private int delay=0;
    private Map map;
    private HUD hud;
    private Sprite enemySprite;
    private JPanel panel1;
    private JTextField textField = new JTextField("Enter your name here:");
    private JButton buttonBack = new JButton("Back");
    private JButton buttonSave = new JButton("Save Score");


    private ArrayList<GameObject> objects= new ArrayList<>();
    private KeyBoardListener keyListener = new KeyBoardListener(this);
    private MouseEventListener mouseListener = new MouseEventListener(this);
    private static int WIDTH =1400;
    private static int HEIGHT= WIDTH/16*9;

    private Player player;
    private AnimatedSprite playerAnimations;

    private int xZoom = 3;
    private int yZoom = 3;

    public static int minY=-10, maxY=20, maxX=53, minX=-16;


    /**
     * The Game constructor which initializes all the graphics, connections, etc.
     */
    public Game()
    {
        renderer=new RenderHandler(WIDTH, HEIGHT);
        this.setResizable(false);
        //Creating the connection with the database
        createConnection();

        //Initializing the button panel for the highscore menu
        panel1 = new JPanel();
        panel1.setLayout(null);
        panel1.setVisible(true);
        panel1.setBounds(0,0,WIDTH,HEIGHT);
        panel1.setBackground(Color.gray);
        getHighScore(panel1, c);

        //Here we create some JButtons
        buttonSave.setBounds(WIDTH/4,400,300,100);

        buttonSave.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String nume=textField.getText();
                textField.setText("");
                try {
                    Statement stmt = c.createStatement();

                    String sql="INSERT INTO HighScore VALUES("+HUD.level+","+HUD.SCORE+",'"+nume+"');";
                    stmt.execute(sql);
                    c.commit();


                }
            catch ( Exception e1) {
                System.err.println( e.getClass().getName() + ": " + e1.getMessage() );
                System.exit(0);
            }


            }
        });
        textField.setBounds(WIDTH/4,300, 300, 50);
        buttonBack.setBounds(WIDTH/4, 550, 300, 100);
        buttonBack.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                State=STATE.GAMEOVER;
                canvas= new Canvas();
                gameInstance.remove(panel1);
                gameInstance.add(canvas);
                gameInstance.setVisible(true);
                init(canvas);
            }
        });

        //Adding the JButtons and the JTextField to the panel
        panel1.add(textField);
        panel1.add(buttonBack);
        panel1.add(buttonSave);

        //Loading Sprites
        SpriteSheet sheet = new SpriteSheet(loadImage("image/Tiles1.png"));
        //16 represents the size of the sprites within the sprite sheet
        sheet.loadSprites(16, 16);

        String tilesPath=null;
        String mapPath=null;

        //Connecting to the local SQLite
        try {
            Statement stmt=c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM FILEPATH");
            while(rs.next()){
                String name = rs.getString("File");
                String path = rs.getString("Path");
                if(name.equals("Tiles")){
                    tilesPath=path;
                }
                if(name.equals("Map")){
                    mapPath=path;
                }
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        Tiles tiles=null;

        if(tilesPath!=null){
             tiles= new Tiles(new File(tilesPath),sheet);
        }

        if(mapPath!=null){
            map = new Map(new File("files/Map.txt"), tiles);
        }
        //Make our program shutdown when we exit out.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set the position and size of our frame.
        setBounds(0,0, WIDTH, HEIGHT);

        //Put our frame in the center of the screen.
        setLocationRelativeTo(null);

        //Add our graphics component
        add(canvas);

        //Make our frame visible.
        setVisible(true);

        //Create our object for buffer strategy.
        renderer = new RenderHandler(getWidth(), getHeight());

        //load the player sheet
        //BufferedImage playerSheetImage = loadImage("image/Player.png");
        playerSheet = new SpriteSheet(loadImage("image/Player.png"));
        //20 and 26 are the width and the height of the player sprite
        playerSheet.loadSprites(20, 26);
        enemySprite = new Sprite(loadImage("image/1.png"));
        hud = new HUD();
        playerAnimations = new AnimatedSprite(playerSheet, 5);

        startMenuSprite = new Sprite(loadImage("image/startSprite.png"));
        quitMenuSprite = new Sprite(loadImage("image/quitSprite.png"));
        helpMenuSprite = new Sprite(loadImage("image/helpSprite.png"));
        backMenuSprite = new Sprite(loadImage("image/backSprite.png"));
        gameOverSprite = new Sprite(loadImage("image/playagainSprite.png"));
        highscoreSprite = new Sprite(loadImage("image/highscoreSprite.png"));

        //Player Animated Sprites
        //5 represents the speed that the sprites change to create the animated sprite
        AnimatedSprite playerAnimations = new AnimatedSprite(playerSheet, 5);

        //Load Objects
        player = new Player(playerAnimations, xZoom, yZoom);
        objects.add(player);

        //Add Listeners
        init(canvas);
        gameInstance = this;
    }

    /**
     * This method helps us initialize the canvas so we don't need to write this piece of code multiple times
     * @param canvas is the canvas to be initialized
     */
    private void init(Canvas canvas){
        canvas.createBufferStrategy(3);
        canvas.addKeyListener(keyListener);
        canvas.addFocusListener(keyListener);
        canvas.addMouseListener(mouseListener);
        canvas.addMouseMotionListener(mouseListener);
        canvas.requestFocus();
    }

    /**
     * This method gets the records from the database and prints it into the panel to be displayed
     * @param panel the panel which gets to render the highscore state
     * @param c is the connection made for the database
     */
    private void getHighScore(JPanel panel, Connection c){
        Statement stmt;
        //First we get rid of all the labels in the panel so we avoid overlapping
        if(panel.getComponents()!=null){
            Component[] temp= panel.getComponents();
            for(int i=0; i<temp.length; ++i){
                if(temp[i] instanceof JLabel){
                    panel.remove(temp[i]);
                }
            }
        }
        JLabel aux= new JLabel("HIGHSCORES");
        aux.setBounds(WIDTH*3/4, 25, 500,25);
        panel.add(aux);
        try {
            stmt = c.createStatement();
            int i=1;

            //Select all the fields from the HighScore table and print the records
            ResultSet rs = stmt.executeQuery("SELECT * FROM HighScore ORDER BY Level DESC, Score DESC");
            while(rs.next() && i<=15){
                String name = rs.getString("Name");
                int score = rs.getInt("Score");
                int level = rs.getInt("Level");
                JLabel temp= new JLabel(i+") Nume: "+name+" Level: "+level+" Scor: "+score);
                temp.setBounds(WIDTH*3/4, 25+(i*25), 500,25);
                panel.add(temp);
                temp.setVisible(true);
                ++i;
            }
        } catch ( Exception e ) {
            //Exception handler
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }

    /**
     * Calling this method to make the connection with the local database
     */
    private void createConnection(){
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:gameDB.db");
            c.setAutoCommit(false);
        }
        catch(Exception e){
            //Exception handler
            e.printStackTrace();
        }
    }

    /**
     * The update method gets called every time possible to update the screen
     */
    private void update()
    {
        if(State == STATE.GAME){
            if(HUD.HEALTH<=0){
                State=STATE.GAMEOVER;
            }
            delay++;
            Projectile.delay++;
            if (delay > 100) {
                objects.add(new Enemy(enemySprite, xZoom, yZoom));
                delay = 0;
            }
            for (int i = 0; i < objects.size(); i++) {
                objects.get(i).update(this);
            }
        }

    }

    /**
     * This method helps us load a image from a given path
     * @param path is a string that represents the path of the image
     * @return is returning the image as a BufferedImage
     */
    public BufferedImage loadImage(String path)
    {
        try
        {
            BufferedImage loadedImage = ImageIO.read(Game.class.getResource(path));
            BufferedImage formattedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            formattedImage.getGraphics().drawImage(loadedImage, 0, 0, null);

            return formattedImage;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    public void handleCTRL(boolean[] keys)
    {
        if(keys[KeyEvent.VK_S])
            map.saveMap();
    }

    /**
     * This method gets called whenever there is an event on the left click
     * @param x is the x position of the mouse on the canvas
     * @param y is the y position of the mouse on the canvas
     */
    public void leftClick(int x, int y)
    {
        //The leftClick is doing things based on the state of the game
        //After we know the state of the game we check the position of the mouse and do the according code
        //Check if the state is MENU
        if(State == STATE.MENU){
            if(startMenuSprite.getWidth()+WIDTH/2-32*3>=x && WIDTH/2-32*3<=x && 200<=y && 200+startMenuSprite.getHeight()>=y){
                //pressed start
                State =STATE.GAME;
                objects.add(hud);
            }
            if(startMenuSprite.getWidth()+WIDTH/2-32*3>=x && WIDTH/2-32*3<=x && 400<=y && 400+startMenuSprite.getHeight()>=y){
                //pressed help
                State =STATE.HELP;
            }
            if(startMenuSprite.getWidth()+WIDTH/2-32*3>=x && WIDTH/2-32*3<=x && 600<=y && 600+startMenuSprite.getHeight()>=y){
                //pressed quit
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            }
        }
        //Check if the state is GAMEOVER
        if(State == STATE.GAMEOVER){
            if(startMenuSprite.getWidth()+WIDTH/2-32*3>=x && WIDTH/2-32*3<=x && 400<=y && 400+startMenuSprite.getHeight()>=y){
                //pressed back
                State =STATE.MENU;
                int i=objects.size()-1;
                while(objects.size()!=0){
                    objects.remove(objects.get(i));
                    i--;
                }
                HUD.level=1;
                HUD.HEALTH=100;
                HUD.SCORE=0;
                player=new Player(playerAnimations, xZoom, yZoom);
                objects.add(player);
                hud = new HUD();
            }
            if(gameOverSprite.getWidth()+WIDTH/2-32*5>=x && WIDTH/2-32*5<=x && 250<=y && 250+gameOverSprite.getHeight()>=y){
                //pressed play again
                State =STATE.GAME;
                int i=objects.size()-1;
                while(objects.size()!=0){
                    objects.remove(objects.get(i));
                    i--;
                }
                HUD.level=1;
                HUD.HEALTH=100;
                HUD.SCORE=0;
                player=new Player(playerAnimations, xZoom, yZoom);
                objects.add(player);
                hud = new HUD();
                objects.add(hud);
            }
            if(highscoreSprite.getWidth()+WIDTH/2-32*4 >=x && WIDTH/2-32*4<=x && 550<=y && 550 + highscoreSprite.getHeight() >=y){
                State = STATE.HIGHSCORE;
                getHighScore(panel1, c);
                this.remove(canvas);
                this.add(panel1);
            }
        }

        //Check if the state is HELP
        if(State == STATE.HELP){
            if(startMenuSprite.getWidth()+50>=x && 50<=x && 50<=y && 50+startMenuSprite.getHeight()>=y){
                //pressed back
                State =STATE.MENU;
            }
        }
    }

    /**
     * This method gets called whenever the user right clicks
     * @param x is the x position of the mouse
     * @param y is the y position of the mouse
     */
    public void rightClick(int x, int y)
    {
        System.out.println(player.getRectangle().x/xZoom+" "+player.getRectangle().y/yZoom);
    }

    /**
     * This method is used to render all the objects in the game base on the state of the game
     */
    private void render()
    {
        if(State == STATE.MENU)
        {
            BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            Graphics graphics = bufferStrategy.getDrawGraphics();
            super.paint(graphics);

            map.render(renderer, objects, xZoom, yZoom);

            renderer.renderSprite(startMenuSprite,WIDTH/2-32*3, 200, 1, 1,true);
            renderer.renderSprite(helpMenuSprite,WIDTH/2-32*3, 400, 1, 1,true);
            renderer.renderSprite(quitMenuSprite,WIDTH/2-32*3, 600, 1, 1,true);
            renderer.render(graphics);
            renderer.renderString(graphics, "MENU", WIDTH/2-32,100);


            graphics.dispose();
            bufferStrategy.show();
            renderer.clear();
        }
        if(State==STATE.GAME){

            BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            Graphics graphics = bufferStrategy.getDrawGraphics();
            super.paint(graphics);

            map.render(renderer, objects, xZoom, yZoom);

            renderer.render(graphics);

            //Update the HUD to display the correct score and level
            graphics.setColor(Color.white);
            renderer.renderString(graphics, "SCORE:" + HUD.SCORE, 10, 80);
            renderer.renderString(graphics, "LEVEL:" + HUD.level, 10, 110);

            graphics.dispose();
            bufferStrategy.show();
            renderer.clear();
        }
        if(State == STATE.HELP){

            BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            Graphics graphics = bufferStrategy.getDrawGraphics();
            super.paint(graphics);

            map.render(renderer, objects, xZoom, yZoom);

            renderer.renderSprite(enemySprite, 500, 500,xZoom,yZoom, true);
            renderer.renderSprite(playerSheet.getSprite(0,3), 500,400,xZoom, yZoom, true);

            renderer.renderSprite(backMenuSprite,50, 50, 1, 1,true);
            renderer.render(graphics);

            renderer.renderString(graphics, "Press SPACE to shoot", 400,200);
            renderer.renderString(graphics, "To pass the level you must get the points; each enemy killed is +10 points", 400, 240);
            renderer.renderString(graphics, "-> Player", 600, 450);
            renderer.renderString(graphics, "-> Enemy", 600, 550);

            graphics.dispose();
            bufferStrategy.show();
            renderer.clear();
        }
        if(State==STATE.GAMEOVER){
                BufferStrategy bufferStrategy = canvas.getBufferStrategy();
                Graphics graphics;
                try{
                    graphics = bufferStrategy.getDrawGraphics();
                    lastImage=graphics;
                }
                catch (Exception e){
                    graphics=lastImage;
                }

                super.paint(graphics);

                map.render(renderer, objects, xZoom, yZoom);

                renderer.renderSprite(backMenuSprite, WIDTH / 2 - 32 * 2, 400, 1, 1, true);
                renderer.renderSprite(gameOverSprite, WIDTH / 2 - 32 * 5, 250, 1, 1, true);
                renderer.renderSprite(highscoreSprite, WIDTH / 2 - 32 * 4, 550, 1, 1, true);
                renderer.render(graphics);
                renderer.renderString(graphics, "GAME OVER", WIDTH / 2 - 32 * 2, 200);
                graphics.dispose();
                bufferStrategy.show();
                renderer.clear();

        }
    }

    /**
     * The Game class implements a runnable and there needs to be a run method in order to start it
     */
    public void run()
    {
        long lastTime = System.nanoTime(); //long 2^63
        double nanoSecondConversion = 1000000000.0 / 60; //60 frames per second
        double changeInSeconds = 0;

        while(true)
        {
            long now = System.nanoTime();

            changeInSeconds += (now - lastTime) / nanoSecondConversion;
            while(changeInSeconds >= 1) {
                update();
                changeInSeconds--;
            }

            render();
            lastTime = now;
        }
    }

    public static void main(String[] args)
    {
        Game game = new Game();
        Thread gameThread = new Thread(game);
        gameThread.start();
    }


    /**
     * Getter methods for the main class
     */
    public KeyBoardListener getKeyListener()
    {
        return keyListener;
    }

    public RenderHandler getRenderer()
    {
        return renderer;
    }

    public Map getMap() {
        return map;
    }
    public Player getPlayer(){return player;}

    public int getXZoom() {
        return xZoom;
    }

    public int getYZoom() {
        return yZoom;
    }

    public ArrayList<GameObject> getObjects(){
        return objects;
    }

}