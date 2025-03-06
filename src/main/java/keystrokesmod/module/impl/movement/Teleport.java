package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;

public class Teleport extends Module {
    private ButtonSetting rightClick;
    private ButtonSetting highlightTarget;
    private ButtonSetting highlightPath;
    private BlockPos targetPos;
    private ArrayList<Vec3> path = new ArrayList<>();
    public Teleport() {
        super("Teleport", category.movement);
        this.registerSetting(rightClick = new ButtonSetting("Right click teleport", true));
        this.registerSetting(highlightTarget = new ButtonSetting("Highlight target", true));
        this.registerSetting(highlightPath = new ButtonSetting("Highlight path", false));
    }

    public void teleport(BlockPos targetBlock, boolean sendMessage) {
        targetBlock = targetBlock.up(1);
        ArrayList<Vec3> pathList = this.path = getPath(targetBlock);
        int packetsSent = 0;
        for (Vec3 pathPos : pathList) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(pathPos.xCoord, pathPos.yCoord, pathPos.zCoord, true));
            if (++packetsSent >= 175) {
                if (sendMessage) {
                    Utils.sendMessage("&eToo many packets, ending loop.");
                    break;
                }
                break;
            }
        }
        mc.thePlayer.setPosition(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        if (sendMessage) {
            Utils.sendMessage("&eTeleported to &d(" + targetBlock.getX() + ", " + targetBlock.getY() + ", " + targetBlock.getZ() + ") &ewith &b" + packetsSent + " &epackets.");
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!rightClick.isToggled() || !highlightTarget.isToggled() || this.targetPos == null || !Utils.nullCheck()) {
            return;
        }
        RenderUtils.renderBlock(targetPos, Color.orange.getRGB(), true, true);
        if (highlightPath.isToggled()) {
            int positions = 0;
            for (Vec3 pos : this.path) {
                if (positions >= 175) {
                    break;
                }
                RenderUtils.renderBlock(new BlockPos(pos.xCoord, pos.yCoord, pos.zCoord), Color.yellow.getRGB(), false, true);
                ++positions;
            }
        }
    }

    private ArrayList getPath(BlockPos target) {
        ArrayList<Vec3> path = new ArrayList<>();
        double newX = (double)target.getX() + 0.5;
        double newY = target.getY() + 1;
        double newZ = (double)target.getZ() + 0.5;
        double distance = this.mc.thePlayer.getDistance(newX, newY, newZ);
        double d = 0;
        while (d < distance) {
            path.add(new Vec3(this.mc.thePlayer.posX + (newX - (double)this.mc.thePlayer.getHorizontalFacing().getFrontOffsetX() - this.mc.thePlayer.posX) * d / distance, this.mc.thePlayer.posY + (newY - this.mc.thePlayer.posY) * d / distance, this.mc.thePlayer.posZ + (newZ - (double)this.mc.thePlayer.getHorizontalFacing().getFrontOffsetZ() - this.mc.thePlayer.posZ) * d / distance));
            d += 2.0;
        }
        return path;
    }

    @SubscribeEvent
    public void onMouse(MouseEvent mouseEvent) {
        if (mouseEvent.button != 1 || !mouseEvent.buttonstate || !rightClick.isToggled() || !Utils.nullCheck()) {
            return;
        }
        MovingObjectPosition rayCast = RotationUtils.rayCast(150.0, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true);
        if (rayCast == null || rayCast.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        final BlockPos getBlockPos = rayCast.getBlockPos();
        this.targetPos = getBlockPos;
        teleport(getBlockPos, true);
    }

    @Override
    public void onEnable() {
        this.targetPos = null;
        this.path.clear();
        if (rightClick.isToggled()) {
            return;
        }
        MovingObjectPosition rayCast = RotationUtils.rayCast(150.0, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true);
        if (rayCast == null || rayCast.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        teleport(rayCast.getBlockPos(), true);
        this.disable();
    }
}
