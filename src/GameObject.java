public interface GameObject
{

    //Call every time physically possible.
    void render(RenderHandler renderer, int xZoom, int yZoom);

    //Call at 60 fps rate.
    void update(Game game);

    //Call whenever mouse is clicked on Canvas.
    //Return true to stop checking other clicks.
    boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom);

    int getLayer();

    Rectangle getRectangle();
}