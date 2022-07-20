package com.mylo.parkour.States;

import com.mylo.parkour.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.vecmath.Vector3d;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class State {
    public enum baseState {
        Walking, Running, Jumping, Falling, Idle, Climbing, Swimming, Sliding, Dead, WallRunning, WallClimbing
    }

    public baseState currentState = baseState.Idle;

    public float lastEyeHeight = 1.62f;
    public float eyeHeight = 1.62f;

    public int airJumpsLeft;
    public boolean jumpLastTick;

    public double lastX, lastY, lastZ;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        setCurrentState();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;
        double x = MathUtils.getXDir();
        double z = MathUtils.getZDir();
        double lastToCurrentSpeed = Math.sqrt(Math.pow(mc.player.posX - lastX, 2) + Math.pow(mc.player.posZ - lastZ, 2));

        if (currentState == baseState.WallClimbing) {
            int jumpInFront = MathUtils.heightOfJumpInFront(2);
            if (jumpInFront == 0) currentState = baseState.Falling;
            else
            mc.player.motionY = MathUtils.HeightToMotion(jumpInFront);

            ArrayList<Vec3d> collidingWalls = MathUtils.WhichWallAmICollidingWith();
            float xVel = 0, zVel = 0;
            for (Vec3d wall : collidingWalls) {
                xVel += wall.x / 10;
                zVel += wall.z / 10;
            }
            mc.player.motionX = xVel;
            mc.player.motionZ = zVel;
        }

        switch (currentState) {
            case Walking:
                break;
            case Running:
                break;
            case Jumping:
                if (!mc.player.capabilities.isFlying)
                    mc.player.motionY += Math.abs(mc.player.motionY - 0.42) * 0.1;
                mc.player.motionX *= 1.02f;
                mc.player.motionZ *= 1.02f;
                mc.player.fallDistance = -10 + 3;
                break;
            case Falling:
                if (!mc.player.capabilities.isFlying)
                    mc.player.motionY += Math.abs(mc.player.motionY - 0.42) * 0.05;
                mc.player.motionX *= 1.02f;
                mc.player.motionZ *= 1.02f;
                if (!jumpLastTick && mc.player.movementInput.jump && airJumpsLeft > 0) {
                    airJumpsLeft--;
                    mc.player.motionX += MathUtils.getXDir() * 0.2F;
                    mc.player.motionZ += MathUtils.getZDir() * 0.2F;
                    mc.player.motionY = 0.5;
                }
                break;
            case Idle:
                break;
            case Climbing:
                break;
            case Swimming:
                break;
            case Sliding:
                mc.player.fallDistance = -10 + 3;
                mc.player.onGround = false;
                mc.player.motionX *= 1.05f;
                mc.player.motionZ *= 1.05f;
                if (Math.abs(Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)) < 0.05f) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }
                break;
            case Dead:
                break;
            case WallRunning:
                mc.player.motionY = 0;
                mc.player.onGround = true;
                for (Vec3d vec : MathUtils.WhichWallAmICollidingWith()) {
                    if (vec.x == 0 && vec.z == 0)
                        continue;
                    double xDiff = vec.x;
                    double zDiff = vec.z;
                    double xDir = Math.abs(xDiff) > Math.abs(zDiff) ? xDiff : 0;
                    double zDir = Math.abs(xDiff) > Math.abs(zDiff) ? 0 : zDiff;
                    if (xDir != 0)
                        mc.player.motionX = xDir * 0.1;
                    if (zDir != 0)
                        mc.player.motionZ = zDir * 0.1;
                }
                if (mc.player.movementInput.jump) {
                    for (Vec3d vec : MathUtils.WhichWallAmICollidingWith()) {
                        if (vec.x == 0 && vec.z == 0)
                            continue;
                        double xDiff = vec.x;
                        double zDiff = vec.z;
                        double xDir = Math.abs(xDiff) > Math.abs(zDiff) ? xDiff : 0;
                        double zDir = Math.abs(xDiff) > Math.abs(zDiff) ? 0 : zDiff;
                        if (xDir != 0)
                            mc.player.motionX = -xDir * 0.4;
                        if (zDir != 0)
                            mc.player.motionZ = -zDir * 0.4;
                        mc.player.motionY = 0.45;
                    }
                }
                mc.player.setSprinting(true);
                if (!mc.player.collidedHorizontally || Math.abs(Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)) < 0.3f || lastToCurrentSpeed < 0.1) {
                    setState(baseState.Falling);
                }
                mc.player.addVelocity(x * 0.1f, 0, z * 0.1f);
                break;
        }

        ArrayList<Vec3d> positions = MathUtils.WhichWallAmICollidingWith();
        float xDir = 0;
        float zDir = 0;
        for (Vec3d vec : positions) {
            xDir += vec.x * 10;
            zDir += vec.z * 10;
        }
        double dir = MathUtils.getDirectionOfMovement(zDir, xDir);
        double playerRotation = mc.player.rotationYaw;
        dir = dir - playerRotation;
        double[] posDiff = MathUtils.directionToPosDiff(dir);
        double targetRoll;
        double targetPitch;
        if ((xDir != 0 || zDir != 0) && (isFalling() || isWallRunning()) && mc.player.collidedHorizontally && lastToCurrentSpeed > 0.1) {
            targetRoll = posDiff[0] * 10;
            targetPitch = posDiff[1] * 10;
        }
        else {
            targetRoll = 0;
            targetPitch = 0;
        }
        smoothRoll = MathUtils.lerp(smoothRoll, targetRoll, 0.3);
        smoothPitch = MathUtils.lerp(smoothPitch, targetPitch, 0.3);

        float targetEyeHeight = isSliding() ? 1.2F : 1.62F;
        if (mc.player.getCollisionBoundingBox() != null) {
            mc.player.getCollisionBoundingBox().setMaxY(mc.player.getCollisionBoundingBox().minY + (isSliding() ? .9F : 1.8F));
        }
        eyeHeight = (float) MathUtils.lerp(eyeHeight, targetEyeHeight, 0.3F);
        currentPow = (float) MathUtils.lerp(currentPow, isSliding() ? 10 : 0, 0.3F);
        lastEyeHeight = mc.player.eyeHeight;
        lastPow = currentPow;

        if (isWalking() || isRunning() || isWallRunning()) {
            airJumpsLeft = 1;
        }
        jumpLastTick = mc.player.movementInput.jump;
        lastX = mc.player.posX;
        lastY = mc.player.posY;
        lastZ = mc.player.posZ;
        lastSmoothPitch = smoothPitch;
        lastSmoothRoll = smoothRoll;
    }

    @SubscribeEvent
    public void onRenderView(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        //smoothly change eye height using lastEyeHeight
        mc.player.eyeHeight = (float) MathUtils.lerp(lastEyeHeight, eyeHeight, event.renderTickTime);
    }

    public void setCurrentState() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        double lastToCurrentSpeed = Math.sqrt(Math.pow(mc.player.posX - lastX, 2) + Math.pow(mc.player.posZ - lastZ, 2));

        float speed = (float) Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);

        int jumpInFront = MathUtils.heightOfJumpInFront(2);

        if (isWallClimbing() && mc.player.collidedHorizontally) return;

        if (mc.player.isDead) {
            setState(baseState.Dead);
        } else if ((isRunning() || (speed > 0.18 && (isFalling() || isWalking()))) && mc.player.isSneaking() && mc.player.onGround) {
            setState(baseState.Sliding);
        } else if (mc.player.isInWater()) {
            setState(baseState.Swimming);
        } else if (mc.player.isOnLadder()) {
            setState(baseState.Climbing);
        } else if (mc.player.posX == 0 && mc.player.posY == 0 && mc.player.posZ == 0) {
            setState(baseState.Idle);
        } else if (mc.player.collidedHorizontally && !mc.player.onGround && (isFalling() || isWallRunning()) && lastToCurrentSpeed >= 0.1) {
            setState(baseState.WallRunning);
        } else if (mc.player.motionY < 0 && !mc.player.onGround && !isSliding()) {
            setState(baseState.Falling);
        } else if (mc.player.movementInput.jump && mc.player.collidedHorizontally && jumpInFront != 0) {
            setState(baseState.WallClimbing);
        } else if (mc.player.motionY > 0 && !mc.player.onGround) {
            setState(baseState.Jumping);
        } else if (mc.player.isSprinting() && !isSliding()) {
            setState(baseState.Running);
        } else if (!isSliding()) {
            setState(baseState.Walking);
        } else if (!mc.player.isSneaking() && isSliding()) {
            setState(baseState.Idle);
        }
    }


    public void setState(baseState state) {
        Minecraft mc = Minecraft.getMinecraft();
        currentState = state;

        switch (state) {
            case Walking:
                break;
            case Running:
                break;
            case Jumping:
                break;
            case Falling:
                break;
            case Idle:
                break;
            case Climbing:
                break;
            case Swimming:
                break;
            case Sliding:
                mc.player.motionX *= 2;
                mc.player.motionX += MathUtils.getXDir() * 0.4;
                mc.player.motionX = Math.min(mc.player.motionX, 1);
                mc.player.motionZ *= 2;
                mc.player.motionZ += MathUtils.getZDir() * 0.4;
                mc.player.motionZ = Math.min(mc.player.motionZ, 1);
                break;
            case Dead:
                break;
        }
    }

    public boolean isWalking() {
        return currentState == baseState.Walking;
    }

    public boolean isRunning() {
        return currentState == baseState.Running;
    }

    public boolean isJumping() {
        return currentState == baseState.Jumping;
    }

    public boolean isFalling() {
        return currentState == baseState.Falling;
    }

    public boolean isIdle() {
        return currentState == baseState.Idle;
    }

    public boolean isClimbing() {
        return currentState == baseState.Climbing;
    }

    public boolean isSwimming() {
        return currentState == baseState.Swimming;
    }

    public boolean isSliding() {
        return currentState == baseState.Sliding;
    }

    public boolean isDead() {
        return currentState == baseState.Dead;
    }

    public boolean isWallRunning() {
        return currentState == baseState.WallRunning;
    }

    public boolean isWallClimbing() {
        return currentState == baseState.WallClimbing;
    }


    double currentPow, lastPow;

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        if (mc.player.getEntityBoundingBox() == null) return;

    }

    @SubscribeEvent
    public void renderStart(RenderPlayerEvent.Pre e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (e.getEntityPlayer() != mc.player) return;

        GlStateManager.pushMatrix();

        ModelPlayer player = e.getRenderer().getMainModel();

        player.isSneak = false;

        double rot = MathUtils.getDirectionOfMovement(mc.player.motionZ, mc.player.motionX);

        if (rot == 0 && mc.player.moveStrafing == 0)
            rot = mc.player.rotationYaw;
        rot += 90;

        float speed = (float) Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);

        float x = (float) Math.cos(rot * Math.PI / 180);
        float z = (float) Math.sin(rot * Math.PI / 180);

        if (isSliding() && speed > 0.2) {
            //GlStateManager.translate(0, 0.2, 0);
            //GlStateManager.rotate(90, x, 0, z);
        } else if (isFalling()) {
            ArrayList<Vec3d> positions = MathUtils.WhichWallAmICollidingWith();
            float xDir = 0;
            float zDir = 0;
            for (Vec3d vec : positions) {
                xDir += vec.x;
                zDir += vec.z;
            }
            if (positions.size() >= 1) {
                xDir /= positions.size();
                zDir /= positions.size();
                GlStateManager.rotate(10, -zDir, 0, xDir);
                GlStateManager.translate(0, -0.1, 0);
            }
        } else {

        }
    }

    @SubscribeEvent
    public void renderEnd(RenderPlayerEvent.Post e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (e.getEntityPlayer() != mc.player) return;

        GlStateManager.popMatrix();
    }

    public double smoothPitch, smoothRoll, lastSmoothPitch, lastSmoothRoll;
    @SubscribeEvent
    public void setupCamera(EntityViewRenderEvent.CameraSetup e) {
        Minecraft mc = Minecraft.getMinecraft();
        e.setPitch((float) (e.getPitch() + MathUtils.lerp(lastSmoothPitch, smoothPitch, e.getRenderPartialTicks())));
        e.setRoll((float) (e.getRoll() + MathUtils.lerp(lastSmoothRoll, smoothRoll, e.getRenderPartialTicks())));
    }

    public State() {

    }
}