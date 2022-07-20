package com.mylo.parkour.config;

import com.mylo.parkour.Parkour;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Parkour.MOD_ID)
public class Configuration {

    @Config.Name("Double jump")
    public static DoubleJump doubleJump = new DoubleJump();

    public static class DoubleJump {
        @Config.Name("Double jump count")
        @Config.Comment("The amount of air jumps you have")
        public int doubleJumps = 1;

        @Config.Name("Double jump Upward force")
        @Config.Comment("The upward force of the double jump")
        public double doubleJumpUpwardForce = 0.5;

        @Config.Name("Double jump Forward force")
        @Config.Comment("The forwards force of the double jump")
        public double doubleJumpForwardsForce = 0.2;
    }

    @Config.Name("Sliding")
    public static Sliding sliding = new Sliding();

    public static class Sliding {
        @Config.Name("Enable Sliding")
        public boolean enableSliding = true;

        @Config.Name("Base multiplier")
        public double baseMultiplier = 2;

        @Config.Name("Directional multiplier")
        public double directionalMultiplier = 0.4;

        @Config.Name("Max speed for boost")
        public double maxBoostSpeed = 1;
    }

    @Config.Name("WallRunning")
    public static WallRunning wallRunning = new WallRunning();

    public static class WallRunning {
        @Config.Name("Enable Wall Running")
        public boolean enableWallRunning = true;

        @Config.Name("Wall Running speed")
        public double wallRunningSpeed = 0.1;

        @Config.Name("Wall Running minimum Speed")
        @Config.Comment("If you move slower than this, you will drop")
        public double wallRunningMinimumSpeed = 0.1;

        @Config.Name("Wall Running Jump Force")
        @Config.Comment("The horizontal jump force")
        public double wallRunningJumpForceHor = 0.4;

        @Config.Name("Wall Running Jump Force Vertical")
        @Config.Comment("The vertical jump force")
        public double wallRunningJumpForceVert = 0.45;
    }

    @Config.Name("Climb Height")
    public static int climbHeight = 2;

    @Config.Name("Air movement")
    public static AirMovement airMovement = new AirMovement();

    public static class AirMovement {
        @Config.Name("Air Movement Add Mult")
        @Config.Comment("Air Movement Additive multiplier")
        public double airMovementAddMul = 0.02;

        @Config.Name("Air Movement Slow fall up")
        @Config.Comment("Air Movement Slow fall force while going up")
        public double airMovementSlowFallUp = 0.1;

        @Config.Name("Air Movement Slow fall down")
        @Config.Comment("Air Movement Slow fall force while going down")
        public double airMovementSlowFallDown = 0.05;
    }

    @Mod.EventBusSubscriber(modid = Parkour.MOD_ID)
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Parkour.MOD_ID)) {
                ConfigManager.sync(Parkour.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
