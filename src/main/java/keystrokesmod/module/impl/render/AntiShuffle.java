package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;

public class AntiShuffle extends Module {
    private static String shuffleStr = "§k";

    public AntiShuffle() {
        super("AntiShuffle", Module.category.render, 0);
        this.registerSetting(new DescriptionSetting("Removes obfuscation (" + shuffleStr + "hey" + "§" + "r)."));
    }

    public static String removeObfuscation(String s) {
        return s.replace(shuffleStr, "");
    }
}