import processing.core.PImage;

import java.util.List;
import java.util.Random;

public class Vein
   extends Actor
{
   private static final int DEFAULT_DISTANCE = 1;
   private int resourceDistance;

   private static final int ORE_CORRUPT_MIN = 20000;
   private static final int ORE_CORRUPT_MAX = 30000;

   private static final int RANDOM_MAX = 3;
   private static final int RANDOM_MIN = 1;
   
   private static final Random rand = new Random();

   public Vein(String name, Point position, int rate, int resourceDistance,
      List<PImage> imgs)
   {
      super(name, position, rate, imgs);
      this.resourceDistance = resourceDistance;
   }

   public Vein(String name, Point position, int rate, List<PImage> imgs)
   {
      this(name, position, rate, DEFAULT_DISTANCE, imgs);
   }

   public String toString()
   {
      return String.format("vein %s %d %d %d %d", this.getName(),
         this.getPosition().x, this.getPosition().y, this.getRate(),
         this.resourceDistance);
   }

   public Action createAction(WorldModel world, ImageStore imageStore)
   {
      Action[] action = { null };
      action[0] = ticks -> {
         removePendingAction(action[0]);

         Point openPt = findOpenAround(world, getPosition(), resourceDistance);
         if (openPt != null && world.getBackground(openPt).getName().equals("grass"))
         {
            Ore ore = createOre(world, "ore - " + getName() + " - " + ticks,
               openPt, ticks, imageStore);
            world.addEntity(ore);
            
            if (openPt.x % 2 != 0 && openPt.y != 0)
            {
               String name = "galaxy" + rand.nextInt(2) + rand.nextInt(2);
               world.setBackground(openPt, new Background("galaxy", imageStore.get(name)));
            }
         }

         scheduleAction(world, this, createAction(world, imageStore),
            ticks + getRate());
      };
      return action[0];
   }

   private Ore createOre(WorldModel world, String name, Point pt,
      long ticks, ImageStore imageStore)
   {
      Ore ore = new Ore(name, pt, 
         ORE_CORRUPT_MIN + rand.nextInt(ORE_CORRUPT_MAX - ORE_CORRUPT_MIN),
         imageStore.get("ore"));
      ore.schedule(world, ticks, imageStore);
      return ore;
   }

   private Point findOpenAround(WorldModel world, Point pt, int distance)
   {
      for (int dy = -distance; dy <= distance; dy++)
      {
         for (int dx = -distance; dx <= distance; dx++)
         {
            Point newPt = new Point(pt.x + dx, pt.y + dy);
            if (world.withinBounds(newPt) && !world.isOccupied(newPt))
            {
               return newPt;
            }
         }
      }

      return null;
   }
}
