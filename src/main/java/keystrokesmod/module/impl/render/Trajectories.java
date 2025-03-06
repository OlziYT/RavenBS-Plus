package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Trajectories extends Module {
    private ButtonSetting autoScale;
    private ButtonSetting disableUncharged;
    private ButtonSetting highlightOnEntity;
    private int highlightColor = new Color(234, 38, 38).getRGB();
    private int topColor = new Color(46, 255, 22).getRGB();
    public Trajectories() {
        super("Trajectories", category.render);
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(disableUncharged = new ButtonSetting("Disable uncharged bow", true));
        this.registerSetting(highlightOnEntity = new ButtonSetting("Highlight on entity", true));
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (!Utils.nullCheck() || mc.thePlayer.getHeldItem() == null) {
            return;
        }
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (!(heldItem.getItem() instanceof ItemBow) && !(heldItem.getItem() instanceof ItemSnowball) && !(heldItem.getItem() instanceof ItemEgg) && !(heldItem.getItem() instanceof ItemEnderPearl)) {
            return;
        }
        if (heldItem.getItem() instanceof ItemBow && !mc.thePlayer.isUsingItem() && disableUncharged.isToggled()) {
            return;
        }
        boolean bow = heldItem.getItem() instanceof ItemBow;

        float playerYaw = mc.thePlayer.rotationYaw;
        float playerPitch = mc.thePlayer.rotationPitch;

        double posX = mc.getRenderManager().viewerPosX - (double)(MathHelper.cos(playerYaw / 180.0f * (float)Math.PI) * 0.16f);
        double posY = mc.getRenderManager().viewerPosY + (double)mc.thePlayer.getEyeHeight() - (double)0.1f;
        double posZ = mc.getRenderManager().viewerPosZ - (double)(MathHelper.sin(playerYaw / 180.0f * (float)Math.PI) * 0.16f);

        double motionX = (double)(-MathHelper.sin(playerYaw / 180.0f * (float)Math.PI) * MathHelper.cos(playerPitch / 180.0f * (float)Math.PI)) * (bow ? 1.0 : 0.4);
        double motionY = (double)(-MathHelper.sin(playerPitch / 180.0f * (float)Math.PI)) * (bow ? 1.0 : 0.4);
        double motionZ = (double)(MathHelper.cos(playerYaw / 180.0f * (float)Math.PI) * MathHelper.cos(playerPitch / 180.0f * (float)Math.PI)) * (bow ? 1.0 : 0.4);

        int itemInUse = 40;
        if (mc.thePlayer.getItemInUseCount() > 0 && bow) {
            itemInUse = mc.thePlayer.getItemInUseCount();
        }
        int timeInUse = 72000 - itemInUse;
        float strength = (float)timeInUse / 20.0f;
        if ((double)(strength = (strength * strength + strength * 2.0f) / 3.0f) < 0.1) {
            return;
        }
        if (strength > 1.0f) {
            strength = 1.0f;
        }

        RenderUtils.glColor(-1);
        GL11.glPushMatrix();

        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean textureTwoD = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);

        if (depthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        if (textureTwoD) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        }
        if (!blend) {
            GL11.glEnable(GL11.GL_BLEND);
        }
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float velocity = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX /= velocity;
        motionY /= velocity;
        motionZ /= velocity;
        motionX *= (double)(bow ? strength * 2.0f : 1.0f) * 1.5;
        motionY *= (double)(bow ? strength * 2.0f : 1.0f) * 1.5;
        motionZ *= (double)(bow ? strength * 2.0f : 1.0f) * 1.5;

        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        boolean ground = false;
        MovingObjectPosition target = null;
        boolean highlight = false;
        boolean isTop = false;
        double[] transform = new double[]{posX, posY, posZ, motionX, motionY, motionZ};

        for (int k = 0; k <= 100 && !ground; ++k) {
            Vec3 start = new Vec3(transform[0], transform[1], transform[2]);
            Vec3 predicted = new Vec3(transform[0] + transform[3], transform[1] + transform[4], transform[2] + transform[5]);
            MovingObjectPosition rayTraced = mc.theWorld.rayTraceBlocks(start, predicted, false, true, false);
            if (rayTraced == null) {
                rayTraced = getEntityHit(start, predicted);
                if (rayTraced != null) {
                    highlight = true;
                    break;
                }

                float f14 = 0.99f;
                transform[4] *= f14;
                transform[0] += (transform[3] *= f14);
                transform[1] += (transform[4] -= bow ? 0.05 : 0.03);
                transform[2] += (transform[5] *= f14);
            } 
            else if (rayTraced.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && rayTraced.sideHit == EnumFacing.UP) {
                isTop = true;
            }
        }

        for (int k = 0; k <= 100 && !ground; ++k) {
            Vec3 start = new Vec3(posX, posY, posZ);
            Vec3 predicted = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition rayTraced = mc.theWorld.rayTraceBlocks(start, predicted, false, true, false);
            if (rayTraced != null) {
                ground = true;
                target = rayTraced;
            } 
            else {
                MovingObjectPosition entityHit = getEntityHit(start, predicted);
                if (entityHit != null) {
                    target = entityHit;
                    ground = true;
                }
            }

            if (highlight && highlightOnEntity.isToggled()) {
                RenderUtils.glColor(highlightColor);
            } 
            else if (isTop) {
                RenderUtils.glColor(topColor);
            }

            float airResistance = 0.99f;
            motionY *= airResistance;
            GL11.glVertex3d((posX += (motionX *= airResistance)) - mc.getRenderManager().viewerPosX, (posY += (motionY -= bow ? 0.05 : 0.03)) - mc.getRenderManager().viewerPosY, (posZ += (motionZ *= airResistance)) - mc.getRenderManager().viewerPosZ);
        }
        GL11.glEnd();

        GL11.glTranslated(posX - mc.getRenderManager().viewerPosX, posY - mc.getRenderManager().viewerPosY, posZ - mc.getRenderManager().viewerPosZ);
        if (target != null && target.sideHit != null) {
            switch (target.sideHit.getIndex()) {
                case 2:
                case 3:
                    GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
                    break;
                case 4:
                case 5:
                    GL11.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
                    break;
            }
        }
        if (autoScale.isToggled()) {
            double distance = Math.max(mc.thePlayer.getDistance(posX + motionX, posY + motionY, posZ + motionZ) * 0.042830285, 1);
            GL11.glScaled(distance, distance, distance);
        }
        this.drawX();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        if (depthTest) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if (textureTwoD) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        if (!blend) {
            GL11.glDisable(GL11.GL_BLEND);
        }
        GL11.glPopMatrix();
    }

    public MovingObjectPosition getEntityHit(Vec3 origin, Vec3 destination) {
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityLivingBase)) {
                continue;
            }
            if (e instanceof EntityPlayer && AntiBot.isBot(e)) {
                continue;
            }
            if (e != mc.thePlayer) {
                float expand = 0.3f;
                AxisAlignedBB boundingBox = e.getEntityBoundingBox().expand(expand, expand, expand);
                MovingObjectPosition possibleHit = boundingBox.calculateIntercept(origin, destination);
                if (possibleHit != null) {
                    return possibleHit;
                }
            }
        }
        return null;
    }

    public void drawX() {
        GL11.glPushMatrix();
        GL11.glScalef(0.95f, 0.95f, 0.95f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(-0.25, 0.0, 0.25);
        GL11.glVertex3d(0.25, 0.0, -0.25);
        GL11.glVertex3d(-0.25, 0.0, -0.25);
        GL11.glVertex3d(0.25, 0.0, 0.25);
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
