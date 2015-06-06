import java.util.List;

import processing.core.PImage;

public class Wyvern
   extends MobileAnimatedActor
{
   
   boolean visited = false;
   public Wyvern(String name, Point position, int rate, int animation_rate, List<PImage> imgs)
   {
      super(name, position, rate, animation_rate, imgs);
   }
   
   protected boolean canPassThrough(WorldModel world, Point pt)
   {
      return !world.isOccupied(pt);
   }
   
   private boolean move(WorldModel world, WorldEntity target)
   {
      if (target == null)
      {
         return false;
      }
      
      if (adjacent(getPosition(), target.getPosition()))
      {
         target.remove(world);
         return true;
      }
      else
      {
         Point new_pt = nextPosition(this.getPosition(), target.getPosition(), world);
         WorldEntity old_entity = world.getTileOccupant(new_pt);
         if (old_entity != null && old_entity != this)
         {
            old_entity.remove(world);
         }
         world.moveEntity(this, new_pt);
         return false;
      }
   }
   
   public Action createAction(WorldModel world, ImageStore imageStore)
   {
      Action[] action = { null };
      action[0] = ticks -> {
         removePendingAction(action[0]);

         WorldEntity target = world.findNearest(getPosition(), MinerGalaxy.class);
         long nextTime = ticks + getRate();

         if (target != null)
         {
            if (move(world, target))
            {          
               nextTime = nextTime + getRate();
            }
            
            if (!world.getBackground(getPosition()).getName().contains("galaxy"))
            {
               visited = true;
            }
               
            if (world.getBackground(getPosition()).getName().contains("galaxy"))
            {
               if (visited)
               {
                  Quake quake = OreBlob.createQuake(world, getPosition(), ticks, imageStore);
                  world.addEntity(quake);
                  return;
               }   
            }
         }
         
         target = world.findNearest(getPosition(), MinerGalaxy.class);  
         scheduleAction(world, this, createAction(world, imageStore),
            nextTime);
      };
      
      return action[0];
   }
}