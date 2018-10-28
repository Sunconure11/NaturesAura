package de.ellpeck.naturesaura.blocks.tiles;

import de.ellpeck.naturesaura.aura.chunk.AuraChunk;
import de.ellpeck.naturesaura.blocks.Multiblocks;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;

public class TileEntityPotionGenerator extends TileEntityImpl implements ITickable {

    @Override
    public void update() {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 10 == 0) {
            if (Multiblocks.POTION_GENERATOR.validate(this.world, this.pos)) {
                boolean addedOne = false;

                List<EntityAreaEffectCloud> clouds = this.world.getEntitiesWithinAABB(EntityAreaEffectCloud.class, new AxisAlignedBB(this.pos).grow(3));
                for (EntityAreaEffectCloud cloud : clouds) {
                    if (cloud.isDead)
                        continue;

                    PotionType type = ReflectionHelper.getPrivateValue(EntityAreaEffectCloud.class, cloud, "field_184502_e", "potion");
                    if (type == null)
                        continue;

                    for (PotionEffect effect : type.getEffects()) {
                        Potion potion = effect.getPotion();
                        if (potion.isBadEffect() || potion.isInstant()) {
                            continue;
                        }

                        if (!addedOne) {
                            int toAdd = (effect.getAmplifier() * 5 + 1) * (effect.getDuration() / 80);
                            for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                                BlockPos offset = this.pos.offset(dir, 8);
                                BlockPos spot = AuraChunk.getClosestSpot(this.world, offset, 10, offset);
                                if (AuraChunk.getAuraInArea(this.world, spot, 10) < 15000) {
                                    AuraChunk chunk = AuraChunk.getAuraChunk(this.world, spot);
                                    chunk.storeAura(spot, toAdd / 4);
                                }
                            }
                            addedOne = true;
                        }

                        float newRadius = cloud.getRadius() - 0.25F;
                        if (newRadius < 0.5F)
                            cloud.setDead();
                        else
                            cloud.setRadius(newRadius);
                    }
                }
            }
        }
    }
}