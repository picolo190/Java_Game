import java.awt.image.BufferedImage;

public class SpriteSheet
{
    private int[] pixels;//An array of all the pixels in the image
    private BufferedImage image;//The image
    public final int SIZEX;//Width of the image
    public final int SIZEY;//Height of the image

    private Sprite[] loadedSprites = null;
    private boolean spritesLoaded = false;

    private int spriteSizeX;


    /**
     * Public constructor of SpriteSheet
     * @param sheetImage = The image loaded into the program
     */
    public SpriteSheet(BufferedImage sheetImage)
    {
        image = sheetImage;
        SIZEX = sheetImage.getWidth();
        SIZEY = sheetImage.getHeight();

        pixels = new int[SIZEX*SIZEY];

        //getRGB returns an array of pixels
        pixels = sheetImage.getRGB(0, 0, SIZEX, SIZEY, pixels, 0, SIZEX);
    }

    /**
     * This method loads all the sprites within the sprite sheet image
     * @param spriteSizeX = the pixel size of the sprite as Width
     * @param spriteSizeY = the pixel size of the sprite as Height
     */

    public void loadSprites(int spriteSizeX, int spriteSizeY)
    {
        this.spriteSizeX = spriteSizeX;
        loadedSprites = new Sprite[(SIZEX / spriteSizeX) * (SIZEY / spriteSizeY)];

        int spriteID = 0;
        for(int y = 0; y < SIZEY; y += spriteSizeY)
        {
            for(int x = 0; x < SIZEX; x += spriteSizeX)
            {
                loadedSprites[spriteID] = new Sprite(this, x, y, spriteSizeX, spriteSizeY);
                spriteID++;
            }
        }

        spritesLoaded = true;
    }


    /**
     * This method gets a sprite loaded as a image
     * @param x = = the x position of the sprite
     * @param y = the y position of the sprite
     * @return as a return the method returns the sprite at the position x, y; otherwise returns null
     */
    public Sprite getSprite(int x, int y)
    {
        //First we check if the sprite sheet loaded correct
        if(spritesLoaded)
        {
            //Divide the whole image Width with the sprite Width and multiply by the layer y
            int spriteID = x + y * (SIZEX / spriteSizeX);

            if(spriteID < loadedSprites.length)
                return loadedSprites[spriteID];
            else
                System.out.println("SpriteID of " + spriteID + " is out of the range with a length of " + loadedSprites.length + ".");
        }
        else
            System.out.println("SpriteSheet could not get a sprite with no loaded sprites.");

        return null;
    }

    public Sprite[] getLoadedSprites()
    {
        return loadedSprites;
    }

    public int[] getPixels()
    {
        return pixels;
    }

    public BufferedImage getImage()
    {
        return image;
    }

}