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

public final class RaidCaptainBottles implements ModInitializer {
    public static final String MOD_ID = "raidcaptainbottles";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof Raider raider)) {
                return;
            }

            LOGGER.info("[raidcaptainbottles] {} died. isPatrolLeader={}", raider.getType(), raider.isPatrolLeader());

            if (!raider.isPatrolLeader()) {
                LOGGER.info("[raidcaptainbottles] -> skipped: not a patrol leader at death time");
                return;
            }

            Entity attacker = damageSource.getEntity();
            LOGGER.info("[raidcaptainbottles] attacker={}", attacker);
            if (!(attacker instanceof ServerPlayer)) {
                LOGGER.info("[raidcaptainbottles] -> skipped: killer was not a player");
                return;
            }

            if (!(entity.level() instanceof ServerLevel serverLevel)) {
                LOGGER.info("[raidcaptainbottles] -> skipped: not a ServerLevel");
                return;
            }

            Raid raid = serverLevel.getRaidAt(entity.blockPosition());
            LOGGER.info("[raidcaptainbottles] getRaidAt result: {}", raid);
            if (raid == null) {
                LOGGER.info("[raidcaptainbottles] -> skipped: no raid found at death position (vanilla should have dropped it instead)");
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

            LOGGER.info("[raidcaptainbottles] -> DROPPED ominous bottle, amplifier {}", amplifier);
        });
    }
}
