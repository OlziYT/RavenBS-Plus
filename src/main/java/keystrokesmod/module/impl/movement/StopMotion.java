package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;

public class StopMotion extends Module {
    private ButtonSetting stopX;
    private ButtonSetting stopY;
    private ButtonSetting stopZ;

    public StopMotion() {
        super("Stop Motion", Module.category.movement, 0);
        this.registerSetting(stopX = new ButtonSetting("Stop X", true));
        this.registerSetting(stopY = new ButtonSetting("Stop Y", true));
        this.registerSetting(stopZ = new ButtonSetting("Stop Z", true));
    }

    public void onEnable() {
        if (stopX.isToggled()) {
            mc.thePlayer.motionX = 0;
        }
        if (stopY.isToggled()) {
            mc.thePlayer.motionY = 0;
        }
        if (stopZ.isToggled()) {
            mc.thePlayer.motionZ = 0;
        }
        this.disable();
    }
}
