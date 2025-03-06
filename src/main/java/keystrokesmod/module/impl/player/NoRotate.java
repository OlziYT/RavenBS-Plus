package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class NoRotate extends Module {
    private float prevPitch = 0f;
    private float prevYaw = 0f;

    public NoRotate() {
        super("NoRotate", category.player);
    }

    public void handlePlayerPosLookPre() {
        if (!this.isEnabled()) {
            return;
        }
        if (mc.thePlayer == null || mc.thePlayer.rotationPitch == 0) {
            return;
        }
        prevPitch = mc.thePlayer.rotationPitch % 360;
        prevYaw = mc.thePlayer.rotationYaw % 360;
    }

    public void handlePlayerPosLook(S08PacketPlayerPosLook packet) {
        if (!this.isEnabled()) {
            return;
        }
        if (packet.getPitch() == 0 || mc.thePlayer == null) {
            return;
        }
        mc.thePlayer.prevRotationYaw = prevYaw;
        mc.thePlayer.prevRotationPitch = prevPitch;
        mc.thePlayer.rotationPitch = prevPitch;
        mc.thePlayer.rotationYaw = prevYaw;
    }
}
