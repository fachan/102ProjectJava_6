import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;

public class WorldView
{
   private PApplet screen;
   private WorldModel world;
   private int tileWidth;
   private int tileHeight;
   private Viewport viewport;
   private int tilePixels = 32;
   
   private static final int WYVERN_RATE = 780;
   private static final int WYVERN_ANIMATION_MAX = 4;
   private static final int WYVERN_ANIMATION_MIN = 1;
   private static final int WYVERN_ANIMATION_RATE_SCALE = 4;
   
   private static final int RANDOM_MAX = 4;
   private static final int RANDOM_MIN = 1;
   
   private static final Random rand = new Random();

   public WorldView(int numCols, int numRows, PApplet screen, WorldModel world,
      int tileWidth, int tileHeight)
   {
      this.screen = screen;
      this.world = world;
      this.tileWidth = tileWidth;
      this.tileHeight = tileHeight;
      this.viewport = new Viewport(numRows, numCols);
   }
 
   public void drawViewport()
   {
      drawBackground();
      drawEntities();
      isEntity(world);
   }
   
   public void drawView(ImageStore images, int cx, int cy)
   {
      Point location = new Point(cy / tileHeight, cx / tileWidth);
      
      int radius = 4;
      for (int y = -2; y < 3; y++)
      {
         for (int x = -2; x < 3; x++)
         {
            String name = "galaxy" + y + x;
            Point surrounding = new Point(location.x + x, location.y + y);
            Point shift_surrounding = viewportToWorld(viewport, location.y + y, 
               location.x + x);

            if ((Math.abs(surrounding.x - location.x) + 
               Math.abs(surrounding.y - location.y)) < radius &&
               (!name.equals("galaxy1-2") && !name.equals("galaxy-12") &&
               !name.equals("galaxy21") && !name.equals("galaxy-2-1")))
            {
               world.setBackground(shift_surrounding, new Background(name, 
                  images.get(name)));

               if (world.isOccupied(shift_surrounding) && 
                  !(world.getTileOccupant(shift_surrounding) instanceof Miner))
               {
                  WorldEntity entity = world.getTileOccupant(shift_surrounding);
                  world.removeEntity(entity);
                  entity.remove(world);
               }
            }
         }
      }
      
      Point position = viewportToWorld(viewport, 
            location.y, location.x);
      if (position.x % (RANDOM_MIN + rand.nextInt(RANDOM_MAX - RANDOM_MIN)) 
         != 0 && position.y % (RANDOM_MIN) + rand.nextInt(RANDOM_MAX - 
         RANDOM_MIN) != 0)
      {
         Wyvern wyvern = createWyvern(world, "wyvern", viewportToWorld(viewport, 
            location.y, location.x), 5, images);
         world.addEntity(wyvern);
      }
   }
   
   private static Wyvern createWyvern(WorldModel world, String name,
         Point pt, long ticks, ImageStore imageStore)
      {
         Wyvern wyvern = new Wyvern(name, pt, WYVERN_RATE,
            WYVERN_ANIMATION_RATE_SCALE * (WYVERN_ANIMATION_MIN + 
            rand.nextInt(WYVERN_ANIMATION_MAX - WYVERN_ANIMATION_MIN)),
            imageStore.get("wyvern"));
         wyvern.schedule(world, ticks, imageStore);
         return wyvern;
      }
   
   private void drawBackground()
   {
      for (int row = 0; row < viewport.getNumRows(); row++)
      {
         for (int col = 0; col < viewport.getNumCols(); col++)
         {
            Point wPt = viewportToWorld(viewport, col, row);
            PImage img = world.getBackground(wPt).getImage();
            screen.image(img, col * tileWidth, row * tileHeight);
         }
      }
   }

   public void isEntity(WorldModel world)
   {
      int x = screen.mouseX / tilePixels;
      int y = screen.mouseY / tilePixels;
      
      WorldObject entity = world.getTileOccupant(new Point(x, y));
      
      if (entity instanceof MobileAnimatedActor)
      {
         MobileAnimatedActor actor = (MobileAnimatedActor) entity;
         markPath(actor.findPath());
      }

   }
   
   public void markPath(List<Point> path)
   {
      for (Point pt : path)
      {
         markPoint(pt);
      }
   }
   
 /*  public void markSuccess(List<Point> path)
   {
      for 
   }*/
   
   public void markPoint(Point pt)
   {
      screen.fill(0, 0, 0, 150);
      screen.rect(pt.x * tilePixels, pt.y * tilePixels, tilePixels, tilePixels);
   }

   
   private void drawEntities()
   {
      for (WorldEntity entity : world.getEntities())
      {
         Point pt = entity.getPosition();
         if (viewport.contains(pt))
         {
            Point vPt = worldToViewport(viewport, pt.x, pt.y);
            screen.image(entity.getImage(), vPt.x * tileWidth,
               vPt.y * tileHeight);
         }
      }
   }

   public void updateView(int dx, int dy)
   {
      int new_x = clamp(viewport.getCol() + dx, 0,
         world.getNumCols() - viewport.getNumCols());
      int new_y = clamp(viewport.getRow() + dy, 0,
         world.getNumRows() - viewport.getNumRows());
      viewport.shift(new_y, new_x);
   }

   private static int clamp(int v, int min, int max)
   {
      return Math.min(max, Math.max(v, min));
   }

   private static Point viewportToWorld(Viewport viewport, int col, int row)
   {
      return new Point(col + viewport.getCol(), row + viewport.getRow());
   }

   private static Point worldToViewport(Viewport viewport, int col, int row)
   {
      return new Point(col - viewport.getCol(), row - viewport.getRow());
   }
}
