import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * This RenderHandler class has the following fields
 * A camera that is an rectangle
 * An array of pixels
 * The max screen width and the max screen height
 * A BufferedImage view
 */

public class RenderHandler
{
    private BufferedImage view;
    private Rectangle camera;
    private int[] pixels;
    private int maxScreenWidth, maxScreenHeight;

    /**
     * The constructor of this class
     * @param width the width of the camera
     * @param height the height of the camera
     */
    public RenderHandler(int width, int height)
    {
        //here we get the max screen resolution
        GraphicsDevice[] graphicsDevices = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        maxScreenHeight=height;
        maxScreenWidth=width;
        /*
        for(int i = 0; i < graphicsDevices.length; i++) {
            if(maxScreenWidth < graphicsDevices[i].getDisplayMode().getWidth())
                maxScreenWidth = graphicsDevices[i].getDisplayMode().getWidth();

            if(maxScreenHeight < graphicsDevices[i].getDisplayMode().getHeight())
                maxScreenHeight = graphicsDevices[i].getDisplayMode().getHeight();
        */



        //Create a BufferedImage that will represent our view.
        view = new BufferedImage(maxScreenWidth, maxScreenHeight, BufferedImage.TYPE_INT_RGB);

        camera = new Rectangle(0, 0, width, height);

        //Create an array for pixels
        pixels = ((DataBufferInt) view.getRaster().getDataBuffer()).getData();

    }

    //Render our array of pixels to the screen
    public void render(Graphics graphics)
    {
        graphics.drawImage(view.getSubimage(0, 0, camera.w, camera.h), 0, 0, camera.w, camera.h, null);
    }


    /**
     * This method renders an string to the canvas
     * @param graphics an graphics class
     * @param string = the string that needs to be output on the screen
     * @param x = the x position on screen
     * @param y = the y position on the screen
     */
    public void renderString(Graphics graphics, String string, int x, int y){

        graphics.setFont(new Font("Arial", 10, 30));
        graphics.drawString(string, x, y);
    }

    //Render our image to our array of pixels.
    public void renderImage(BufferedImage image, int xPosition, int yPosition, int xZoom, int yZoom, boolean fixed)
    {
        int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        renderArray(imagePixels, image.getWidth(), image.getHeight(), xPosition, yPosition, xZoom, yZoom, fixed);
    }

    public void renderSprite(Sprite sprite, int xPosition, int yPosition, int xZoom, int yZoom, boolean fixed) {
        renderArray(sprite.getPixels(), sprite.getWidth(), sprite.getHeight(), xPosition, yPosition, xZoom, yZoom, fixed);
    }

    /**
     * This method is used to render an rectangle on the screen
     * There are no x and y positions becausee the rectangle already has those fields
     * @param rectangle we pass an rectangle that will get rendered on the screen
     * @param xZoom = the xZoom of the object
     * @param yZoom = the yZoom of the object
     * @param fixed = whether the rectangle is fixed or not on the screen
     */
    public void renderRectangle(Rectangle rectangle, int xZoom, int yZoom, boolean fixed)
    {
        int[] rectanglePixels = rectangle.getPixels();
        if(rectanglePixels != null)
            renderArray(rectanglePixels, rectangle.w, rectangle.h, rectangle.x, rectangle.y, xZoom, yZoom, fixed);
    }

    public void renderRectangle(Rectangle rectangle, Rectangle offset, int xZoom, int yZoom, boolean fixed)
    {
        int[] rectanglePixels = rectangle.getPixels();
        if(rectanglePixels != null)
            renderArray(rectanglePixels, rectangle.w, rectangle.h, rectangle.x + offset.x, rectangle.y + offset.y, xZoom, yZoom, fixed);
    }

    /**
     * This renders and array of pixels int
     * It calls the method setPixel to render all the pixels
     * @param renderPixels an array of pixels that represents the rectangle to be rendered
     * @param renderWidth = the render width ( the size in pixels of the image rendered on the screen as width)
     * @param renderHeight = the render height ( the size in pixels of the image rendered on the screen as height)
     * @param xPosition = the x position of the object
     * @param yPosition = the y position of the object
     * @param xZoom
     * @param yZoom
     * @param fixed = this tells the renderer whether the rectangle is fixed on screen or not
     */
    public void renderArray(int[] renderPixels, int renderWidth, int renderHeight, int xPosition, int yPosition, int xZoom, int yZoom, boolean fixed)
    {
        for(int y = 0; y < renderHeight; y++)
            for(int x = 0; x < renderWidth; x++)
                for(int yZoomPosition = 0; yZoomPosition < yZoom; yZoomPosition++)
                    for(int xZoomPosition = 0; xZoomPosition < xZoom; xZoomPosition++)
                        setPixel(renderPixels[x + y * renderWidth], (x * xZoom) + xPosition + xZoomPosition, ((y * yZoom) + yPosition + yZoomPosition), fixed);
    }

    /**
     * This method renders an pixel on the screen based on his x and y position
     * Also the pixel can have an fixed position or not
     * @param pixel = this one represents one pixel to be rendered
     * @param x = the x position of the pixel
     * @param y = the y position of the pixel
     * @param fixed = whether the pixel is fixed on screen or not
     */
    private void setPixel(int pixel, int x, int y, boolean fixed)
    {
        int pixelIndex = 0;
        if(!fixed)
        {
            if(x >= camera.x && y >= camera.y && x <= camera.x + camera.w && y <= camera.y + camera.h)
                pixelIndex = (x - camera.x) + (y - camera.y) * view.getWidth();
        }
        else
        {
            if(x >= 0 && y >= 0 && x <= camera.w && y <= camera.h)
                pixelIndex = x + y * view.getWidth();
        }

        if(pixels.length > pixelIndex && pixel != Game.alpha)
            pixels[pixelIndex] = pixel;
    }

    /**
     * Getter method which returns the camera as an rectangle
     * @return the camera
     */
    public Rectangle getCamera()
    {
        return camera;
    }

    /**
     * Getter method which returns the max width of the screen
     * @return an int max screen width
     */
    public int getMaxWidth() {
        return maxScreenWidth;
    }

    public int getMaxHeight() {
        return maxScreenHeight;
    }

    /**
     * We use this method to clear the pixels
     */
    public void clear()
    {
        for(int i = 0; i < pixels.length; i++)
            pixels[i] = 0;
    }

}