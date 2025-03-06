package keystrokesmod.module.impl.render;

import keystrokesmod.mixin.impl.accessor.IAccessorEntityRenderer;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;

public class ExtendCamera extends Module {
    public SliderSetting distance;
    private float lastDistance;
    public ExtendCamera() {
        super("ExtendCamera", category.render);
        this.registerSetting(new DescriptionSetting("Extends camera in third person."));
        this.registerSetting(new DescriptionSetting("Default is 4 blocks."));
        this.registerSetting(distance = new SliderSetting("Distance", " block", 4, 1, 40, 0.5));
    }

    public void onEnable() {
        setThirdPersonDistance((float) distance.getInput());
    }

    public void onUpdate() {
        try {
            float input = (float) distance.getInput();
            if (lastDistance != input) {
                setThirdPersonDistance(lastDistance = input);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Utils.sendMessage("&cThere was an issue setting third person distance.");
        }
    }

    public void onDisable() {
        setThirdPersonDistance(4.0f);
    }

    private void setThirdPersonDistance(float distance) {
        ((IAccessorEntityRenderer) mc.entityRenderer).setThirdPersonDistance(distance);
    }
}
