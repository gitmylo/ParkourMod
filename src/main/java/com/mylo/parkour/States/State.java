package com.mylo.parkour.States;

import com.mylo.parkour.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.vecmath.Vector3d;

public class State {
    public enum baseState
    {
        Walking, Running, Jumping, Falling, Idle, Climbing, Swimming, Sliding, Dead, WallRunning
    }

    public baseState currentState = baseState.Idle;

    public float lastEyeHeight = 1.62f;
    public float eyeHeight = 1.62f;

    public int airJumpsLeft;
    public boolean jumpLastTick;

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
        switch (currentState) {
            case Walking:
                break;
            case Running:
                break;
            case Jumping:
                mc.player.motionX *= 1.02f;
                mc.player.motionZ *= 1.02f;
                break;
            case Falling:
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
                mc.player.onGround = false;
                mc.player.motionX *= 1.05f;
                mc.player.motionZ *= 1.05f;
                if (Math.abs(Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)) < 0.05f)
                {
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
                if (!mc.player.collidedHorizontally || Math.abs(Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)) < 0.3f){
                    setState(baseState.Falling);
                }
                mc.player.addVelocity(x * 0.1f, 0, z * 0.1f);
                break;
        }

        float targetEyeHeight = isSliding() ? 1.2F : 1.62F;
        if (mc.player.getCollisionBoundingBox() != null)
        {
            mc.player.getCollisionBoundingBox().setMaxY(mc.player.getCollisionBoundingBox().minY + (isSliding() ? .9F : 1.8F));
        }
        eyeHeight = (float) MathUtils.lerp(eyeHeight, targetEyeHeight, 0.3F);
        currentPow = (float) MathUtils.lerp(currentPow, isSliding()?10:0, 0.3F);
        lastEyeHeight = mc.player.eyeHeight;
        lastPow = currentPow;

        if (isWalking() || isRunning() || isWallRunning()) {
            airJumpsLeft = 1;
        }
        jumpLastTick = mc.player.movementInput.jump;
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

        float speed = (float) Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);

        if (mc.player.isDead){
            setState(baseState.Dead);
        } else if ((isRunning() || (speed > 0.18 && (isFalling() || isWalking()))) && mc.player.isSneaking() && mc.player.onGround) {
            setState(baseState.Sliding);
        } else if (mc.player.isInWater()) {
            setState(baseState.Swimming);
        } else if (mc.player.isOnLadder()) {
            setState(baseState.Climbing);
        } else if (mc.player.posX == 0 && mc.player.posY == 0 && mc.player.posZ == 0) {
            setState(baseState.Idle);
        } else if (mc.player.collidedHorizontally && !mc.player.onGround && (isFalling() || isWallRunning())) {
            setState(baseState.WallRunning);
        } else if (mc.player.motionY < 0 && !mc.player.onGround && !isSliding()) {
            setState(baseState.Falling);
        } else if (mc.player.motionY > 0 && !mc.player.onGround) {
            setState(baseState.Jumping);
        } else if (mc.player.isSprinting() && !isSliding()) {
            setState(baseState.Running);
        } else if (!isSliding()){
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


    double currentPow, lastPow;

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        if (mc.player.getEntityBoundingBox() == null) return;

    }

    @SubscribeEvent
    public void render(EntityViewRenderEvent.CameraSetup e) {
        Minecraft mc = Minecraft.getMinecraft();
        //e.setRoll ((float) (e.getRoll() + MathUtils.lerp(lastPow, currentPow, e.getRenderPartialTicks())));
    }
}
