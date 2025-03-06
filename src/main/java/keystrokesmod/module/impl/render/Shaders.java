package keystrokesmod.module.impl.render;

import keystrokesmod.mixin.impl.accessor.IAccessorEntityRenderer;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class Shaders extends Module {
    private SliderSetting shader;
    private String[] shaderNames;
    private ResourceLocation[] shaderLocations;

    public Shaders() {
        super("Shaders", category.render);
        shaderLocations = ((IAccessorEntityRenderer) mc.entityRenderer).getShaderResourceLocations();
        if (shaderLocations == null) {
            return;
        }
        shaderNames = new String[shaderLocations.length];
        for (int i = 0; i < shaderLocations.length; ++i) {
            shaderNames[i] = ((String[]) shaderLocations[i].getResourcePath().replaceFirst("shaders/post/", "").split("\\.json"))[0].toUpperCase();
        }
        this.registerSetting(shader = new SliderSetting("Shader", 0, shaderNames));
    }

    public void onUpdate() {
        if (!Utils.nullCheck() || mc.entityRenderer == null || shaderLocations == null) {
            return;
        }
        try {
            if (((IAccessorEntityRenderer) mc.entityRenderer).getShaderIndex() != (int) shader.getInput()) {
                ((IAccessorEntityRenderer) mc.entityRenderer).setShaderIndex((int) shader.getInput());
                ((IAccessorEntityRenderer) mc.entityRenderer).callLoadShader(shaderLocations[(int) shader.getInput()]);
            }
            else if (!((IAccessorEntityRenderer) mc.entityRenderer).getUseShader()) {
                ((IAccessorEntityRenderer) mc.entityRenderer).setUseShader(true);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Utils.sendMessage("&cError loading shader.");
            this.disable();
        }
    }

    public void onDisable() {
        mc.entityRenderer.stopUseShader();
    }

    public void onEnable() {
        if (!OpenGlHelper.shadersSupported) {
            Utils.sendMessage("&cShaders not supported.");
            this.disable();
        }
    }
}
