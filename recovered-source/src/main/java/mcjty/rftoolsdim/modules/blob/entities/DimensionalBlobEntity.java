package mcjty.rftoolsdim.modules.blob.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.rftoolsdim.dimension.data.DimensionData;
import mcjty.rftoolsdim.dimension.data.PersistantDimensionManager;
import mcjty.rftoolsdim.modules.blob.BlobConfig;
import mcjty.rftoolsdim.modules.dimlets.data.DimletRarity;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

public class DimensionalBlobEntity extends Monster {
   private float squishAmount;
   public float squishFactor;
   public float prevSquishFactor;
   private final DimletRarity rarity;
   private AABB targetBox = null;
   private int tickCounter = 5;
   private static final TargetingConditions PREDICATE = TargetingConditions.forCombat();

   public DimensionalBlobEntity(EntityType<? extends Monster> type, Level worldIn, DimletRarity rarity) {
      super(type, worldIn);
      this.rarity = rarity;
      this.calculateTargetBox(this.getBoundingBox());
   }

   private static int getDefaultMaxHealth(DimletRarity rarity) {
      return switch (rarity) {
         case COMMON -> BlobConfig.BLOB_COMMON_HEALTH.get();
         case RARE -> BlobConfig.BLOB_RARE_HEALTH.get();
         case LEGENDARY -> BlobConfig.BLOB_LEGENDARY_HEALTH.get();
         case UNCOMMON -> throw new IllegalStateException("There is no uncommon blob!");
      };
   }

   private static int getDefaultMaxHealthSetup(DimletRarity rarity) {
      return switch (rarity) {
         case COMMON -> BlobConfig.BLOB_COMMON_HEALTH.getDefault();
         case RARE -> BlobConfig.BLOB_RARE_HEALTH.getDefault();
         case LEGENDARY -> BlobConfig.BLOB_LEGENDARY_HEALTH.getDefault();
         case UNCOMMON -> throw new IllegalStateException("There is no uncommon blob!");
      };
   }

   private int getRegenLevel() {
      return switch (this.rarity) {
         case COMMON -> BlobConfig.BLOB_COMMON_REGEN.get();
         case RARE -> BlobConfig.BLOB_RARE_REGEN.get();
         case LEGENDARY -> BlobConfig.BLOB_LEGENDARY_REGEN.get();
         case UNCOMMON -> throw new IllegalStateException("There is no uncommon blob!");
      };
   }

   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide()) {
         DimensionData data = PersistantDimensionManager.get(this.level()).getData(this.level().dimension().identifier());
         if (data != null && data.getEnergy() >= (Long)BlobConfig.BLOB_REGENERATION_LEVEL.get()) {
            this.tickCounter--;
            if (this.tickCounter <= 0) {
               this.tickCounter = 5;
               this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, this.getRegenLevel()));
            }
         }
      }
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(
      @Nonnull ServerLevelAccessor worldIn, @Nonnull DifficultyInstance difficultyIn, @Nonnull EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn
   ) {
      AttributeInstance attr = this.getAttributes().getInstance(Attributes.MAX_HEALTH);
      if (attr != null) {
         attr.setBaseValue(getDefaultMaxHealth(this.rarity));
         this.setHealth(getDefaultMaxHealth(this.rarity));
      }

      return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
   }

   public static Builder registerAttributes(DimletRarity rarity) {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.MAX_HEALTH, getDefaultMaxHealthSetup(rarity));
   }

   public float getBlobScale() {
      return switch (this.rarity) {
         case COMMON, UNCOMMON -> 1.5F;
         case RARE -> 2.2F;
         case LEGENDARY -> 4.7F;
      };
   }

   private void calculateTargetBox(AABB bb) {
      if (this.rarity != null) {
         double radius = switch (this.rarity) {
            case COMMON, UNCOMMON -> 5.0;
            case RARE -> 9.0;
            case LEGENDARY -> 15.0;
         };
         this.targetBox = bb.inflate(radius);
      }
   }

   private void infectPlayer(Player player) {
      player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100));
      player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
      switch (this.rarity) {
         case COMMON:
         case UNCOMMON:
         default:
            break;
         case RARE:
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
            break;
         case LEGENDARY:
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100));
      }
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         this.squishFactor = this.squishFactor + (this.squishAmount - this.squishFactor) * 0.5F;
         this.prevSquishFactor = this.squishFactor;
         if (this.random.nextFloat() < 0.03F) {
            this.squishAmount = -0.5F;
         } else if (this.random.nextFloat() < 0.03F) {
            this.squishAmount = 1.0F;
         }

         this.squishAmount *= 0.6F;
      } else if (this.random.nextFloat() < 0.05) {
         for (Player player : this.level().getEntitiesOfClass(Player.class, this.targetBox)) {
            this.infectPlayer(player);
         }
      }
   }

   public DimletRarity getRarity() {
      return this.rarity;
   }
}
