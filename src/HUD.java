

public class HUD implements GameObject{

    public static int HEALTH = 100;
    public static int SCORE=0;
    public static int level=1;
    private Rectangle bar;
    private Rectangle healthRectangle;


    public HUD(){
        bar=new Rectangle(10, 10, 210, 42);
        bar.generateGraphics(0xA0A0A0);
        healthRectangle=new Rectangle(15, 15, HEALTH *2, 32);
        healthRectangle.generateGraphics(0x00FF00);
    }

    private int clamp(int var, int min, int max){
        if(var >= max){
            return max;
        }
        else if(var <=min ){
            return min;
        }
        return var;
    }

    //Call every time physically possible.
    public void render(RenderHandler renderer, int xZoom, int yZoom)
    {
        renderer.renderRectangle(bar, 1, 1,true);
        healthRectangle.generateGraphics(0x00FF00);
        renderer.renderRectangle(healthRectangle, 1,1,true);


    }

    //Call at 60 fps rate.
    public void update(Game game){
        HEALTH= clamp(HEALTH, 0, 100);
        healthRectangle.w = HEALTH *2;
        if(SCORE>100*level){
            level++;
            SCORE=0;
        }
    }

    //Call whenever mouse is clicked on Canvas.
    //Return true to stop checking other clicks.
    public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom){
        return false;
    }

    public int getLayer(){
        return Integer.MAX_VALUE;
    }

    public Rectangle getRectangle(){
        return null;
    }

}
