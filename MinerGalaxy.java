import java.util.List;

import processing.core.PImage;

public class MinerGalaxy
   extends Miner
{
   boolean galaxy = false;
   public MinerGalaxy(String name, Point position, int rate,
         int animation_rate, int resource_limit, List<PImage> imgs)
      {
         super(name, position, rate, animation_rate, resource_limit,
            0, Ore.class, imgs);
      }

   protected Miner transform(WorldModel world)
   {
      return this;
   }
   
   protected boolean move(WorldModel world, WorldEntity ore)
   {  
      if (ore == null)
      {
         return false;
      }

      if (dist_between(getPosition(), world.findNearest(getPosition(), 
         Wyvern.class).getPosition(), world) < 8)
      {
         this.setRate(getRate() + 5);
      }
      
      if (adjacent(getPosition(), ore.getPosition()))
      {
         ore.remove(world);
         return false;
      }
      else
      {
         world.moveEntity(this, nextPosition(this.getPosition(), 
            ore.getPosition(), world));
         return true;
      }
   }
   
   public Action createAction(WorldModel world, ImageStore imageStore)
   {
      Action[] action = { null };
      action[0] = ticks -> {
         removePendingAction(action[0]);

         WorldEntity target = world.findNearest(getPosition(), Ore.class);
         long nextTime = ticks + getRate();
         
         if (target != null)
         {
            if (world.getBackground(target.getPosition()).getName().contains("galaxy")
                  && adjacent(getPosition(), target.getPosition()))
            {
               galaxy = true;
            }

            if (galaxy)
            {
               world.moveEntity(this, target.getPosition());

               Quake quake = OreBlob.createQuake(world, getPosition(), ticks, imageStore);
               world.addEntity(quake);
               return;
            }
         }

         if (move(world, target))
         {
            nextTime = nextTime + getRate();
         }

         scheduleAction(world, this,
            this.createAction(world, imageStore),
            ticks + this.getRate());
      };
      return action[0];
   }
}