package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;

public class AutoSwap extends Module {
    public ButtonSetting sameType;
    public ButtonSetting spoofItem;
    public ButtonSetting swapToGreaterStack;
    public AutoSwap() {
        super("AutoSwap", category.player);
        this.registerSetting(new DescriptionSetting("Automatically swaps blocks."));
        this.registerSetting(sameType = new ButtonSetting("Only same type", false));
        this.registerSetting(spoofItem = new ButtonSetting("Spoof item", false));
        this.registerSetting(swapToGreaterStack = new ButtonSetting("Swap to greater stack", true));
        this.canBeEnabled = false;
    }
}
