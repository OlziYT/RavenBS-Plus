package keystrokesmod.utility;

import keystrokesmod.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class ScaffoldBlockCount {
    private final Minecraft mc;
    private Timer fadeTimer;
    private Timer fadeInTimer;
    private float previousAlpha;

    public ScaffoldBlockCount(Minecraft mc) {
        this.mc = mc;
        this.fadeTimer = null;
        (this.fadeInTimer = new Timer(150)).start();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (previousAlpha <= 10 && fadeInTimer == null) {
            onDisable();
            return;
        }
        if (!Utils.nullCheck() || !ModuleManager.scaffold.showBlockCount.isToggled()) {
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                return;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            int blocks = ModuleManager.scaffold.totalBlocks();
            String color = "§";
            if (blocks <= 5) {
                color += "c";
            }
            else if (blocks <= 15) {
                color += "6";
            }
            else if (blocks <= 25) {
                color += "e";
            }
            else {
                color = "";
            }
            float alpha = fadeTimer == null ? 255 : (255 - fadeTimer.getValueInt(0, 255, 1));
            if (fadeInTimer != null) {
                alpha = fadeInTimer.getValueFloat(10, 255, 1);
                if (alpha == 255) {
                    fadeInTimer = null;
                }
            }
            previousAlpha = alpha;
            int colorAlpha = Utils.mergeAlpha(-1, (int) previousAlpha);
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            mc.fontRendererObj.drawStringWithShadow(color + blocks + " §rblock" + (blocks == 1 ? "" : "s"), scaledResolution.getScaledWidth()/2 + 8, scaledResolution.getScaledHeight()/2 + 4, colorAlpha);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    public void beginFade() {
        (this.fadeTimer = new Timer(150)).start();
        this.fadeInTimer = null;
    }

    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        fadeInTimer = null;
        fadeTimer = null;
    }
}
