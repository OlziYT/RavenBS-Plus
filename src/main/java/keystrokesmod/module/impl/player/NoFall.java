package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.mixin.impl.accessor.IAccessorMinecraft;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall extends Module {
    public SliderSetting mode;
    private SliderSetting minFallDistance;
    private ButtonSetting disableAdventure;
    private ButtonSetting ignoreVoid;
    private SliderSetting hypixelATimer;
    private String[] modes = new String[]{"Spoof", "NoGround", "Hypixel A", "Hypixel B"};

    private double initialY;
    private double dynamic;
    private boolean isFalling;

    public NoFall() {
        super("NoFall", category.player);
        this.registerSetting(mode = new SliderSetting("Mode", 2, modes));
        this.registerSetting(hypixelATimer = new SliderSetting("Game speed", 0.6, 0.1, 1.0, 0.05));
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3, 0, 10, 0.1));
        this.registerSetting(disableAdventure = new ButtonSetting("Disable adventure", false));
        this.registerSetting(ignoreVoid = new ButtonSetting("Ignore void", true));
    }

    public void guiUpdate() {
        hypixelATimer.setVisible(mode.getInput() == 2, this);
    }

    public void onDisable() {
        Utils.resetTimer();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (reset()) {
            Utils.resetTimer();
            initialY = mc.thePlayer.posY;
            isFalling = false;
            return;
        }
        else if ((double) mc.thePlayer.fallDistance >= minFallDistance.getInput()) {
            isFalling = true;
        }
        double predictedY = mc.thePlayer.posY + mc.thePlayer.motionY;
        double distanceFallen = initialY - predictedY;
        if (mc.thePlayer.motionY >= -1.0) {
            dynamic = 3.0;
        }
        if (mc.thePlayer.motionY < -1.0) {
            dynamic = 4.0;
        }
        if (mc.thePlayer.motionY < -2.0) {
            dynamic = 5.0;
        }
        if (isFalling && mode.getInput() == 2) {
            if (distanceFallen >= dynamic) {
                ((IAccessorMinecraft) mc).getTimer().timerSpeed = (float) (hypixelATimer.getInput() + Utils.randomizeDouble(-0.012, 0.012));
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                initialY = mc.thePlayer.posY;
            }
        }
        if (isFalling && mode.getInput() == 3) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                ((IAccessorMinecraft) mc).getTimer().timerSpeed = (float) Utils.randomizeDouble(0.5, 0.50201);
            }
            else {
                ((IAccessorMinecraft) mc).getTimer().timerSpeed = (float) 1;
            }
            if (distanceFallen >= 3) {
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                initialY = mc.thePlayer.posY;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        switch ((int) mode.getInput()) {
            case 0:
                e.setOnGround(true);
                break;
            case 1:
                e.setOnGround(false);
                break;
        }
    }

    @Override
    public String getInfo() {
        int modeIndex = (int) mode.getInput();
        if (modeIndex == 2 || modeIndex == 3) {
            return "Hypixel";
        }
        return modes[modeIndex];
    }

    private boolean isVoid() {
        return Utils.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private boolean reset() {
        if (disableAdventure.isToggled() && mc.playerController.getCurrentGameType().isAdventure()) {
            return true;
        }
        if (ignoreVoid.isToggled() && isVoid()) {
            return true;
        }
        if (Utils.isBedwarsPracticeOrReplay()) {
            return true;
        }
        if (Utils.spectatorCheck()) {
            return true;
        }
        if (mc.thePlayer.onGround) {
            return true;
        }
        if (BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)) != Blocks.air) {
            return true;
        }
        if (mc.thePlayer.motionY > -0.0784) {
            return true;
        }
        if (mc.thePlayer.capabilities.isCreativeMode) {
            return true;
        }
        if (isVoid() && mc.thePlayer.posY <= 41) {
            return true;
        }
        if (mc.thePlayer.capabilities.isFlying) {
            return true;
        }
        return false;
    }
}