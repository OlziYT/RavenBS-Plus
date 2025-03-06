package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderPlayerEvent.Post;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;

public class Chams extends Module {
    private ButtonSetting ignoreBots;
    private ButtonSetting renderSelf;
    private ButtonSetting hidePlayers;
    private HashSet<Entity> bots = new HashSet<>();

    public Chams() {
        super("Chams", Module.category.render, 0);
        this.registerSetting(ignoreBots = new ButtonSetting("Ignore bots", false));
        this.registerSetting(hidePlayers = new ButtonSetting("Hide players", false));
        this.registerSetting(renderSelf = new ButtonSetting("Render self", false));
    }

    @SubscribeEvent
    public void onPreRender(Pre e) {
        Entity entity = e.entity;
        if (entity == mc.thePlayer && !renderSelf.isToggled()) {
            return;
        }
        if (hidePlayers.isToggled() && !(entity == mc.thePlayer && renderSelf.isToggled())) {
            e.setCanceled(true);
            return;
        }
        if (ignoreBots.isToggled()) {
            if (AntiBot.isBot(entity)) {
                return;
            }
            bots.add(entity);
        }
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0f, -2500000.0f);
    }

    @SubscribeEvent
    public void onPostRender(Post e) {
        Entity entity = e.entity;
        if (entity == mc.thePlayer && !renderSelf.isToggled()) {
            return;
        }
        if (hidePlayers.isToggled() && !(entity == mc.thePlayer && renderSelf.isToggled())) {
            return;
        }
        if (ignoreBots.isToggled()) {
            if (!bots.contains(entity)) {
                return;
            }
            bots.remove(entity);
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0f, 2500000.0f);
    }

    @Override
    public void onDisable() {
        bots.clear();
    }
}