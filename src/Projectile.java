import java.util.ArrayList;

public class Projectile implements GameObject{

    //0 = Right, 1 = Left, 2 = Up, 3 = Down
    private int direction = 0;
    private int speed = 20;
    private Game game;
    public static int delay = 0;

    private int layer=0;
    private Sprite sprite;
    private Rectangle missleRectangle;

    public Projectile(Sprite sprite, int xZoom, int yZoom, Game game){
        this.sprite=sprite;
        this.game =game;
        this.direction =game.getPlayer().getDirection();
        missleRectangle = new Rectangle(this.game.getPlayer().getRectangle().x, this.game.getPlayer().getRectangle().y, 20, 26);
        missleRectangle.generateGraphics(3, 0xFF00FF90);

    }
    public Rectangle getRectangle() {
        return missleRectangle;
    }

    @Override
    public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {
        return false;
    }

    @Override
    public void update(Game game) {
        ArrayList<GameObject> objects =game.getObjects();
        if(direction == 0){
            missleRectangle.x+=speed;
        }
        if(direction == 1){
            missleRectangle.x-=speed;
        }
        if(direction == 2){
            missleRectangle.y-=speed;
        }
        if(direction == 3){
            missleRectangle.y+=speed;
        }

        //16= the size of a tile in pixels
        if(missleRectangle.x> Game.maxX * 16 *game.getXZoom()|| missleRectangle.x < Game.minX * 16 *game.getXZoom()){
            objects.remove(this);
        }
        if( missleRectangle.y > Game.maxY * 16*game.getYZoom() || missleRectangle.y < Game.minY * 16*game.getYZoom()){
            objects.remove(this);
        }

        for(int i=1; i<objects.size(); ++i){
            if(objects.get(i) instanceof Enemy){
                if(objects.get(i).getRectangle().intersects(missleRectangle)){
                    objects.remove(objects.get(i));
                    objects.remove(this);
                    HUD.SCORE+=10;
                }
            }
        }
    }

    @Override
    public void render(RenderHandler renderer, int xZoom, int yZoom) {
        renderer.renderSprite(sprite, missleRectangle.x, missleRectangle.y, xZoom, yZoom, false);
    }

    @Override
    public int getLayer() {
        return 0;
    }
}
