import java.util.Random;

public class Enemy implements GameObject {

    private Rectangle enemyRectangle;
    private int speed = 3;

    //0 = Right, 1 = Left, 2 = Up, 3 = Down
    private int layer = 0;
    private Sprite sprite;

    Random r= new Random();
    public Enemy(Sprite sprite, int xZoom, int yZoom)
    {
        this.sprite = sprite;

        speed+=HUD.level;

        updateDirection();
        //rx= random x position to spawn an enemy
        //ry = random x position to spawn an enemy
        int rx=r.nextInt(Game.maxX*xZoom*sprite.getWidth()), ry=r.nextInt(Game.maxY*yZoom*sprite.getHeight());
        enemyRectangle = new Rectangle(rx, ry, 20, 26);
        enemyRectangle.generateGraphics(3, 0xFF00FF90);
    }
    private void updateDirection()
    {

    }

    //Call every time physically possible.
    public void render(RenderHandler renderer, int xZoom, int yZoom)
    {
         if(sprite != null)
            renderer.renderSprite(sprite, enemyRectangle.x, enemyRectangle.y, xZoom, yZoom, false);
        else
            renderer.renderRectangle(enemyRectangle, xZoom, yZoom, false);

    }

    //Call at 60 fps rate.
    public void update(Game game)
    {
        Player player =game.getPlayer();
        if(player.getRectangle().x > enemyRectangle.x) {
            enemyRectangle.x +=speed;
        }
        if(player.getRectangle().x < enemyRectangle.x) {
            enemyRectangle.x -=speed;
        }
        if(player.getRectangle().y > enemyRectangle.y) {
            enemyRectangle.y +=speed;
        }
        if(player.getRectangle().y < enemyRectangle.y) {
            enemyRectangle.y -=speed;
        }
        if(enemyRectangle.intersects(player.getRectangle())){
            HUD.HEALTH--;
        }
    }
    

    public int getLayer() {
        return layer;
    }

    public Rectangle getRectangle() {
        return enemyRectangle;
    }

    //Call whenever mouse is clicked on Canvas.
    public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) { return false; }
}
