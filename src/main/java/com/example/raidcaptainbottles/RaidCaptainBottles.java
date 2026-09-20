package com.example.raidcaptainbottles;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As of the "ominous bottle" rework, a raid captain (pillager, vindicator,
 * evoker or illusioner - whichever is carrying the ominous banner) already
 * drops an Ominous Bottle when a player kills it OUTSIDE an active raid.
 * That vanilla path is left completely untouched here.
 *
 * The one gap in vanilla is: a captain killed WHILE an active raid is
 * happening does not drop a bottle. This mod fills in exactly that gap,
 * for every raider type that can be a captain, so you always get a bottle
 * no matter which illager happened to be carrying the banner.
 */
public final class RaidCaptainBottles implements ModInitializer {
    public static final String MOD_ID = "raidcaptainbottles";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Only raid captains (Raider covers pillager/vindicator/evoker/illusioner).
            if (!(entity instanceof Raider raider) || !raider.isPatrolLeader()) {
                return;
            }

            // Only count kills credited to a player (directly or via arrows/etc).
            Entity attacker = damageSource.getEntity();
            if (!(attacker instanceof ServerPlayer)) {
                return;
            }

            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            // If there's no active raid here, vanilla already handled the
            // bottle drop itself - don't double it up.
            Raid raid = serverLevel.getRaidAt(entity.blockPosition());
            if (raid == null) {
                return;
            }

            int amplifier = raid.getRaidOmenLevel() - 1;
            amplifier = Math.max(OminousBottleAmplifier.MIN_AMPLIFIER,
                    Math.min(OminousBottleAmplifier.MAX_AMPLIFIER, amplifier));

            ItemStack bottle = new ItemStack(Items.OMINOUS_BOTTLE);
            bottle.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(amplifier));

            ItemEntity itemEntity = new ItemEntity(
                    serverLevel, entity.getX(), entity.getY(), entity.getZ(), bottle);
            itemEntity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(itemEntity);

            LOGGER.debug("{} dropped an ominous bottle (amplifier {}) after being killed mid-raid",
                    raider.getType(), amplifier);
        });
    }
}
