import processing.core.PImage;

import java.util.List;
import java.util.Random;

public abstract class Miner
   extends MobileAnimatedActor
{
   private int resource_limit;
   private int resource_count;
   private Class<?> seeking;
   
   private static final Random rand = new Random();

   public Miner(String name, Point position, int rate, int animation_rate,
      int resource_limit, int resource_count, Class<?> seeking,
      List<PImage> imgs)
   {
      super(name, position, rate, animation_rate, imgs);
      this.resource_limit = resource_limit;
      this.resource_count = resource_count;
      this.seeking = seeking;
   }

   public void setResourceCount(int count)
   {
      this.resource_count = count;
   }

   public int getResourceCount()
   {
      return this.resource_count;
   }

   public int getResourceLimit()
   {
      return this.resource_limit;
   }
   
   public void setSeeking(Class new_seek)
   {
      this.seeking = new_seek;
   }

   protected boolean canPassThrough(WorldModel world, Point pt)
   {
      return !world.isOccupied(pt);
   }

   protected abstract Miner transform(WorldModel world);
   protected abstract boolean move(WorldModel world, WorldEntity ore);

   public Action createAction(WorldModel world, ImageStore imageStore)
   {
      Action[] action = { null };
      action[0] = ticks -> {
         removePendingAction(action[0]);

         WorldEntity target = world.findNearest(getPosition(), seeking);

         Actor newEntity = this;
         if (move(world, target) || world.getBackground(getPosition()).getName().contains("galaxy"))
         {
            newEntity = tryTransform(world, imageStore);
         }

         scheduleAction(world, newEntity,
            newEntity.createAction(world, imageStore),
            ticks + newEntity.getRate());
      };
      return action[0];
   }

   private Miner tryTransform(WorldModel world, ImageStore imageStore)
   {
      Miner newEntity = transform(world);
      
      if (world.getBackground(getPosition()).getName().contains("galaxy"))
      {
         newEntity = createGalaxyMiner(world, getName(), getPosition(), getRate(),
            imageStore);
      }

      if (this != newEntity && this != null)
      {
         this.remove(world);
         world.addEntity(newEntity);
         newEntity.scheduleAnimation(world);
      }
      return newEntity;
   }
   
   private MinerGalaxy createGalaxyMiner(WorldModel world, String name,
      Point pt, int rate, ImageStore imageStore)
   {
      MinerGalaxy miner = new MinerGalaxy(name, pt, rate, getAnimationRate(), 
         resource_limit, imageStore.get("minergalaxy"));
      return miner;
   }
}
