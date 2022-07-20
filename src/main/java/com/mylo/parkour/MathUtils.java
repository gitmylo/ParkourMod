package com.mylo.parkour;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class MathUtils {
    /**
     * @param z The z coordinate
     * @param x The x coordinate
     * @return The angle in degrees
     */
    public static double getDirectionOfMovement(double z, double x) {
        return Math.atan2(z, x) / Math.PI * 180;
    }

    /**
     * Converts a direction to a difference in x and z
     * @param direction The direction to convert
     * @return The difference in x and z
     */
    public static double[] directionToPosDiff(double direction) {
        return new double[]{
                Math.cos(direction/180 * Math.PI),
                Math.sin(direction/180 * Math.PI)
        };
    }

    /**
     * Linear interpolation
     * @param a The first value
     * @param b The second value
     * @param t The interpolation value
     * @return The interpolated value
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double getXDir() {
        Minecraft mc = Minecraft.getMinecraft();
        double rot = (Math.atan2(mc.player.moveForward, mc.player.moveStrafing) / Math.PI * 180);
        if(rot == 0 && mc.player.moveStrafing == 0)
            rot = 90;
        rot += mc.player.rotationYaw;
        if(!moveKeysDown())
            return 0;
        return Math.cos(rot * Math.PI / 180);
    }

    public static double getZDir() {
        Minecraft mc = Minecraft.getMinecraft();
        double rot = (Math.atan2(mc.player.moveForward, mc.player.moveStrafing) / Math.PI * 180);
        if(rot == 0 && mc.player.moveStrafing == 0)
            rot = 90;
        rot += mc.player.rotationYaw;
        if(!moveKeysDown())
            return 0;
        return Math.sin(rot * Math.PI / 180);
    }

    public static boolean moveKeysDown() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0;
    }

    public static ArrayList<Vec3d> WhichWallAmICollidingWith() {
        Minecraft mc = Minecraft.getMinecraft();
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;
        ArrayList<Vec3d> out = new ArrayList<Vec3d>();
        for (int i = 0; i < 4; i++) {
            Vec3d direction = new Vec3d(0, 0, 0);
            switch (i) {
                case 0:
                    direction = new Vec3d(0, 0, .1);
                    break;
                case 1:
                    direction = new Vec3d(0, 0, -.1);
                    break;
                case 2:
                    direction = new Vec3d(.1, 0, 0);
                    break;
                case 3:
                    direction = new Vec3d(-.1, 0, 0);
                    break;
            }
            //Check if the player is colliding with a wall
            if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(direction)).isEmpty()) {
                out.add(direction.normalize());
            }
        }
        return out;
    }

    public static int heightOfJumpInFront(int maxHeight) {
        Minecraft mc = Minecraft.getMinecraft();
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;
        int heightDiff = 0;
        double[] forwards = directionToPosDiff(mc.player.rotationYaw + 90);
        double directionX = forwards[0];
        double directionZ = forwards[1];
        if (Math.abs(directionX) > Math.abs(directionZ)) {
            directionZ = 0;
        }
        else {
            directionX = 0;
        }
        boolean finished = false;
        for (int i = 0; i < maxHeight; i++) {
            Vec3d direction = new Vec3d(forwards[0] * 0.1, i, forwards[1] * 0.1);
            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(direction)).isEmpty()) {
                finished = true;
                break;
            } else {
                heightDiff++;
            }
        }
        if (!finished) return 0;
        return heightDiff;
    }

    public static float HeightToMotion(int height) {
        return (float) Math.sqrt(height) / 6;
    }
}
