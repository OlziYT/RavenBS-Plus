package keystrokesmod.module.impl.render;

import keystrokesmod.mixin.impl.accessor.IAccessorEntityRenderer;
import keystrokesmod.mixin.impl.accessor.IAccessorMinecraft;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.minigames.SkyWars;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class Nametags extends Module {
    private SliderSetting scale;
    private ButtonSetting autoScale;
    private ButtonSetting drawBackground;
    private ButtonSetting onlyRenderName;
    private ButtonSetting dropShadow;
    private ButtonSetting showDistance;
    private ButtonSetting showHealth;
    private ButtonSetting showHitsToKill;
    private ButtonSetting showInvis;
    private ButtonSetting removeTags;
    private ButtonSetting renderSelf;
    private ButtonSetting showArmor;
    private ButtonSetting showEnchants;
    private ButtonSetting showDurability;
    private ButtonSetting showStackSize;

    private int backGroundColor = new Color(0, 0, 0, 100).getRGB();
    private int friendColor = new Color(0, 255, 0, 255).getRGB();
    private int enemyColor = new Color(255, 0, 0, 255).getRGB();

    private double normalizedThreshold = 8;

    public Nametags() {
        super("Nametags", category.render, 0);
        this.registerSetting(scale = new SliderSetting("Scale", 1.0, 0.1, 5.0, 0.1));
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(drawBackground = new ButtonSetting("Draw background", true));
        this.registerSetting(onlyRenderName = new ButtonSetting("Only render name", false));
        this.registerSetting(renderSelf = new ButtonSetting("Render self", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", true));
        this.registerSetting(showDistance = new ButtonSetting("Show distance", false));
        this.registerSetting(showHealth = new ButtonSetting("Show health", true));
        this.registerSetting(showHitsToKill = new ButtonSetting("Show hits to kill", false));
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
        this.registerSetting(removeTags = new ButtonSetting("Remove tags", false));
        this.registerSetting(new DescriptionSetting("Armor settings"));
        this.registerSetting(showArmor = new ButtonSetting("Show armor", false));
        this.registerSetting(showEnchants = new ButtonSetting("Show enchants", true));
        this.registerSetting(showDurability = new ButtonSetting("Show durability", true));
        this.registerSetting(showStackSize = new ButtonSetting("Show stack size", true));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderWorldLast(RenderWorldLastEvent ev) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (removeTags.isToggled()) {
            return;
        }
        double interpolatedX;
        double interpolatedY;
        double interpolatedZ;
        if (mc.gameSettings.thirdPersonView > 0) {
            Vec3 thirdPersonPos = Utils.getCameraPos(ev.partialTicks);
            interpolatedX = thirdPersonPos.xCoord;
            interpolatedY = thirdPersonPos.yCoord;
            interpolatedZ = thirdPersonPos.zCoord;
        }
        else {
            interpolatedX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * ev.partialTicks;
            interpolatedY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * ev.partialTicks + mc.thePlayer.getEyeHeight();
            interpolatedZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * ev.partialTicks;
        }
        long strengthDuration = SkyWars.isSkyWarsTeams ? 2 : 5;
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        for (EntityPlayer en : mc.theWorld.playerEntities) {
            if (!showInvis.isToggled() && en.isInvisible()) {
                continue;
            }
            if (en == mc.thePlayer && (!renderSelf.isToggled() || mc.gameSettings.thirdPersonView == 0)) {
                continue;
            }
            if (en.getDisplayNameString().isEmpty() || (en != mc.thePlayer && AntiBot.isBot(en))) {
                continue;
            }
            if (!RenderUtils.isInViewFrustum(en)) {
                continue;
            }
            double playerX = en.lastTickPosX + (en.posX - en.lastTickPosX) * ev.partialTicks;
            double playerY = en.lastTickPosY + (en.posY - en.lastTickPosY) * ev.partialTicks;
            double playerZ = en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * ev.partialTicks;

            double renderHeightOffset = (playerY - mc.getRenderManager().viewerPosY) + (!en.isSneaking() ? en.height : en.height - 0.3) + 0.294;
            double heightOffset = playerY + (!en.isSneaking() ? en.height : en.height - 0.3) + 0.294;

            ((IAccessorEntityRenderer) mc.entityRenderer).callSetupCameraTransform(((IAccessorMinecraft) mc).getTimer().renderPartialTicks, 0);

            Vec3 screenCords = RenderUtils.convertTo2D(scaledResolution.getScaleFactor(), playerX - mc.getRenderManager().viewerPosX, renderHeightOffset, playerZ - mc.getRenderManager().viewerPosZ);
            if (screenCords == null) {
                continue;
            }
            boolean inFrustum = screenCords.zCoord < 1.0003684;
            if (!inFrustum) {
                continue;
            }
            mc.entityRenderer.setupOverlayRendering();

            float scaleSetting = (float) scale.getInput();
            float newScale = scaleSetting;
            if (autoScale.isToggled()) {
                double deltaX = Math.abs(interpolatedX - playerX);
                if (deltaX < normalizedThreshold + 1) {
                    double deltaZ = Math.abs(interpolatedZ - playerZ);
                    if (deltaZ < normalizedThreshold + 1) {
                        double deltaY = Math.abs(interpolatedY - heightOffset);
                        if (deltaY < normalizedThreshold + 1) {
                            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                            if (distance < normalizedThreshold) {
                                newScale = Math.max((float) (scaleSetting * (normalizedThreshold / distance)), scaleSetting);
                            }
                        }
                    }
                }
            }
            else {
                double deltaX = Math.abs(interpolatedX - playerX);
                double deltaZ = Math.abs(interpolatedZ - playerZ);
                double deltaY = Math.abs(interpolatedY - heightOffset);
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                newScale = (float) ((scale.getInput() + 4) / distance);
            }
            String name;
            if (onlyRenderName.isToggled()) {
                String formattedName = Utils.getFirstColorCode(en.getDisplayName().getFormattedText());
                String colorSuffix = "";
                if (formattedName.length() >= 2 && formattedName.startsWith("§")) {
                    colorSuffix = formattedName;
                }
                name = colorSuffix + en.getName();
            }
            else {
                name = en.getDisplayName().getFormattedText();
            }
            if (showHealth.isToggled()) {
                name = name + " " + Utils.getHealthStr(en, false);
            }
            if (showHitsToKill.isToggled()) {
                name = name + " " + Utils.getHitsToKillStr(en, mc.thePlayer.getCurrentEquippedItem());
            }
            if (showDistance.isToggled()) {
                int distance = Math.round(mc.thePlayer.getDistanceToEntity(en));
                String color = "§";
                if (distance < 8) {
                    color += "c";
                }
                else if (distance < 30) {
                    color += "6";
                }
                else if (distance < 60) {
                    color += "e";
                }
                else if (distance < 90) {
                    color += "a";
                }
                else {
                    color += "2";
                }
                name = color + distance + "m§r " + name;
            }
            if (ModuleManager.skyWars.isEnabled() && ModuleManager.skyWars.strengthIndicator.isToggled() && !ModuleManager.skyWars.strengthPlayers.isEmpty() && ModuleManager.skyWars.strengthPlayers.get(en) != null) {
                double startTime = ModuleManager.skyWars.strengthPlayers.get(en);
                double timePassed = (System.currentTimeMillis() - startTime) / 1000;
                double strengthRemaining = Math.max(0, Utils.round(strengthDuration - timePassed, 1));
                String strengthInfo = "§4" + Utils.asWholeNum(strengthRemaining) + "s§r ";
                name = strengthInfo + name;
            }

            int strWidth = (mc.fontRendererObj.getStringWidth(name)) / 2;
            int x1 = -strWidth - 1;
            int y1 = -10;
            int x2 = strWidth + 1;
            int y2 = 8 - 9;
            GlStateManager.pushMatrix();
            GlStateManager.scale(newScale, newScale, newScale);
            GlStateManager.translate(screenCords.xCoord / newScale, screenCords.yCoord / newScale, 0);
            if (drawBackground.isToggled()) {
                RenderUtils.drawRect(x1, y1, x2, y2, backGroundColor);
            }
            if (Utils.isFriended(en)) {
                RenderUtils.drawOutline(x1, y1, x2, y2, 2, friendColor);
            }
            else if (Utils.isEnemy(en)) {
                RenderUtils.drawOutline(x1, y1, x2, y2, 2, enemyColor);
            }
            mc.fontRendererObj.drawString(name, -strWidth, -9, -1, dropShadow.isToggled());
            if (showArmor.isToggled()) {
                renderArmor(en);
            }
            GlStateManager.scale(1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre e) {
        if (e.entity instanceof EntityPlayer && (e.entity != mc.thePlayer || renderSelf.isToggled()) && e.entity.deathTime == 0) {
            EntityPlayer entityPlayer = (EntityPlayer) e.entity;
            if (!showInvis.isToggled() && entityPlayer.isInvisible()) {
                return;
            }
            if (entityPlayer.getDisplayNameString().isEmpty() || (entityPlayer != mc.thePlayer && AntiBot.isBot(entityPlayer))) {
                return;
            }
            e.setCanceled(true);
        }
    }

    private void renderArmor(EntityPlayer e) {
        int pos = 0;
        for (ItemStack is : e.inventory.armorInventory) {
            if (is != null) {
                pos -= 8;
            }
        }
        if (e.getHeldItem() != null) {
            pos -= 8;
            ItemStack item = e.getHeldItem().copy();
            if (item.hasEffect() && (item.getItem() instanceof ItemTool || item.getItem() instanceof ItemArmor)) {
                item.stackSize = 1;
            }
            renderItemStack(item, pos, -20);
            pos += 16;
        }
        for (int i = 3; i >= 0; --i) {
            ItemStack stack = e.inventory.armorInventory[i];
            if (stack != null) {
                renderItemStack(stack, pos, -20);
                pos += 16;
            }
        }
    }

    private void renderItemStack(ItemStack stack, int xPos, int yPos) {
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        mc.getRenderItem().zLevel = -150.0F;
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, xPos, yPos - 8);
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.disableDepth();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.translate(0, -10, 0);
        renderText(stack, xPos, yPos);
        GlStateManager.enableDepth();
        GlStateManager.scale(2, 2, 2);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    private void renderText(ItemStack stack, int xPos, int yPos) {
        int newYPos = yPos - 24;
        if (showDurability.isToggled() && stack.getItem() instanceof ItemArmor) {
            int remainingDurability = stack.getMaxDamage() - stack.getItemDamage();
            mc.fontRendererObj.drawString(String.valueOf(remainingDurability), (float) (xPos * 2), (float) yPos, 16777215, dropShadow.isToggled());
        }
        if (showEnchants.isToggled() && stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() < 6) {
            if (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow || stack.getItem() instanceof ItemArmor) {
                NBTTagList nbttaglist = stack.getEnchantmentTagList();
                for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                    int id = nbttaglist.getCompoundTagAt(i).getShort("id");
                    int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");
                    if (lvl > 0) {
                        String abbreviated = getEnchantmentAbbreviated(id);
                        mc.fontRendererObj.drawString(abbreviated + lvl, (float) (xPos * 2), (float) newYPos, -1, dropShadow.isToggled());
                        newYPos += 8;
                    }
                }
            }
        }
        if (showStackSize.isToggled() && !(stack.getItem() instanceof ItemSword) && !(stack.getItem() instanceof ItemBow) && !(stack.getItem() instanceof ItemTool) && !(stack.getItem() instanceof ItemArmor)) {
            mc.fontRendererObj.drawString(stack.stackSize + "x", (float) (xPos * 2), (float) yPos, -1, dropShadow.isToggled());
        }
    }

    private String getEnchantmentAbbreviated(int id) {
        switch (id) {
            case 0:
                return "pt";   // Protection
            case 1:
                return "frp";   // Fire Protection
            case 2:
                return "ff";    // Feather Falling
            case 3:
                return "blp";   // Blast Protection
            case 4:
                return "prp";   // Projectile Protection
            case 5:
                return "thr";   // Thorns
            case 6:
                return "res";   // Respiration
            case 7:
                return "aa";    // Aqua Affinity
            case 16:
                return "sh";   // Sharpness
            case 17:
                return "smt";   // Smite
            case 18:
                return "ban";   // Bane of Arthropods
            case 19:
                return "kb";    // Knockback
            case 20:
                return "fa";    // Fire Aspect
            case 21:
                return "lot";  // Looting
            case 32:
                return "eff";   // Efficiency
            case 33:
                return "sil";   // Silk Touch
            case 34:
                return "ub";   // Unbreaking
            case 35:
                return "for";   // Fortune
            case 48:
                return "pow";   // Power
            case 49:
                return "pun";   // Punch
            case 50:
                return "flm";   // Flame
            case 51:
                return "inf";   // Infinity
            default:
                return null;
        }
    }
}
