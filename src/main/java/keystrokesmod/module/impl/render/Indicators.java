package keystrokesmod.module.impl.render;

import keystrokesmod.mixin.impl.accessor.IAccessorEntityArrow;
import keystrokesmod.mixin.impl.accessor.IAccessorEntityRenderer;
import keystrokesmod.mixin.impl.accessor.IAccessorMinecraft;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.GroupSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Indicators extends Module {
    private GroupSetting items;
    private ButtonSetting renderArrows;
    private ButtonSetting renderPearls;
    private ButtonSetting renderFireballs;
    private ButtonSetting renderEggs;
    private ButtonSetting renderSnowballs;

    private SliderSetting arrow;
    private SliderSetting radius;
    private ButtonSetting itemColors;
    private ButtonSetting renderItem;
    private ButtonSetting renderDistance;
    private ButtonSetting threatsOnly;
    private ButtonSetting renderOnlyOffScreen;

    private HashSet<Entity> threats = new HashSet<>();
    private Map<String, String> lastHeldItems = new ConcurrentHashMap<>();

    private String[] arrowTypes = new String[] { "Caret", "Greater than", "Triangle" };

    public Indicators() {
        super("Indicators", category.render);
        this.registerSetting(items = new GroupSetting("Items"));
        this.registerSetting(renderArrows = new ButtonSetting(items, "Render arrows", true));
        this.registerSetting(renderPearls = new ButtonSetting(items, "Render ender pearls", true));
        this.registerSetting(renderFireballs = new ButtonSetting(items, "Render fireballs", true));
        this.registerSetting(renderEggs = new ButtonSetting(items, "Render eggs", false));
        this.registerSetting(renderSnowballs = new ButtonSetting(items, "Render snowballs", false));
        this.registerSetting(arrow = new SliderSetting("Arrow", 0, arrowTypes));
        this.registerSetting(radius = new SliderSetting("Circle radius", 50, 30, 200, 5));
        this.registerSetting(itemColors = new ButtonSetting("Item colors", true));
        this.registerSetting(renderItem = new ButtonSetting("Render item", true));
        this.registerSetting(renderDistance = new ButtonSetting("Render distance", true));
        this.registerSetting(threatsOnly = new ButtonSetting("Render only threats", true));
        this.registerSetting(renderOnlyOffScreen = new ButtonSetting("Render only offscreen", false));
    }

    public void onDisable() {
        this.threats.clear();
        this.lastHeldItems.clear();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (mc.currentScreen != null || !Utils.nullCheck()) {
            return;
        }
        try {
            for (Entity en : mc.theWorld.loadedEntityList) {
                if (en == null || en == mc.thePlayer) {
                    continue;
                }
                ItemStack itemStack = null;
                if (en instanceof EntityArrow) {
                    if (((IAccessorEntityArrow) en).getInGround()) {
                        threats.remove(en);
                        continue;
                    }
                    itemStack = new ItemStack(Items.arrow);
                }
                else if (en instanceof EntityFireball) {
                    itemStack = new ItemStack(Items.fire_charge);
                }
                else if (en instanceof EntityEnderPearl) {
                    itemStack = new ItemStack(Items.ender_pearl);
                }
                else if (en instanceof EntityEgg) {
                    itemStack = new ItemStack(Items.egg);
                }
                else if (en instanceof EntitySnowball) {
                    itemStack = new ItemStack(Items.snowball);
                }
                if (!threats.contains(en)) {
                    continue;
                }
                if (!mc.theWorld.loadedEntityList.contains(en) || !canRender(en)) {
                    threats.remove(en);
                    continue;
                }
                this.renderIndicatorFor(en, itemStack, event.renderTickTime);
            }
        }
        catch (Exception e) {}
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (e.entity == mc.thePlayer) {
            this.threats.clear();
        }
        else if (canRender(e.entity) && (mc.thePlayer.getDistanceSqToEntity(e.entity) > 36 || !threatsOnly.isToggled())) {
            this.threats.add(e.entity);
        }
    }

    private boolean canRender(Entity entity) {
        if (entity instanceof EntityArrow && !((IAccessorEntityArrow) entity).getInGround() && renderArrows.isToggled()) {
            return true;
        }
        else if (entity instanceof EntityLargeFireball && renderFireballs.isToggled()) {
            return true;
        }
        else if (entity instanceof EntityEnderPearl && renderPearls.isToggled()) {
            return true;
        }
        else if (entity instanceof EntityEgg && renderEggs.isToggled()) {
            return true;
        }
        else if (entity instanceof EntitySnowball && renderSnowballs.isToggled()) {
            return true;
        }
        return false;
    }

    private void renderIndicatorFor(Entity en, ItemStack itemStack, float partialTicks) {
        if (!this.canRender(en)) {
            return;
        }
        if (!this.shouldRender(en, itemStack)) {
            return;
        }
        if (renderOnlyOffScreen.isToggled() && RenderUtils.isInViewFrustum(en)) {
            return;
        }
        Color colorForStack = getColorForItem(itemStack);
        int color = itemColors.isToggled() ? colorForStack.getRGB() : -1;

        double x = en.lastTickPosX + (en.posX - en.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = en.lastTickPosY + (en.posY - en.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY + en.height / 2;
        double z = en.lastTickPosZ + (en.posZ - en.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        ((IAccessorEntityRenderer) mc.entityRenderer).callSetupCameraTransform(((IAccessorMinecraft) mc).getTimer().renderPartialTicks, 0);

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        Vec3 vec = RenderUtils.convertTo2D(scaledResolution.getScaleFactor(), x, y, z);

        if (vec != null) {
            mc.entityRenderer.setupOverlayRendering();
            ScaledResolution res = new ScaledResolution(mc);

            double dx = vec.xCoord - res.getScaledWidth() / 2.0;
            double dy = vec.yCoord - res.getScaledHeight() / 2.0;
            boolean inFrustum = vec.zCoord < 1.0003684;

            if (!inFrustum) {
                dx *= -1.0;
                dy *= -1.0;
            }

            double angle1 = Math.atan2(dx, dy);
            double angle2 = Math.atan2(dy, dx) * 57.295780181884766 + 90.0;
            double hypotenuse = Math.hypot(dx, dy);
            double radiusInput = radius.getInput();

            if (renderItem.isToggled()) {
                radiusInput += 20.0;
            }

            if (inFrustum && hypotenuse < radiusInput + 15.0) {
                return;
            }

            double baseX = res.getScaledWidth() / 2.0;
            double baseY = res.getScaledHeight() / 2.0;
            double sinAng = Math.sin(angle1);
            double cosAng = Math.cos(angle1);
            double renderX = baseX + radiusInput * sinAng;
            double renderY = baseY + radiusInput * cosAng;

            GlStateManager.pushMatrix();
            GlStateManager.translate(renderX, renderY, 0.0);
            GlStateManager.rotate((float) angle2, 0.0f, 0.0f, 1.0f);
            GlStateManager.scale(1.0f, 1.0f, 1.0f);

            int arrowInput = (int) arrow.getInput();

            if (arrowInput == 0) {
                if (color == -1) {
                    GL11.glColor3d(1.0, 1.0, 1.0);
                }
                else {
                    GL11.glColor3d(colorForStack.getRed(), colorForStack.getGreen(), colorForStack.getBlue());
                }

                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                double halfAngle = 0.6108652353286743;
                double size = 9.0;
                double offsetY = 5.0;
                GL11.glLineWidth(3.0f);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                GL11.glVertex2d(Math.sin(-halfAngle) * size, Math.cos(-halfAngle) * size - offsetY);
                GL11.glVertex2d(0.0, -offsetY);
                GL11.glVertex2d(Math.sin(halfAngle) * size, Math.cos(halfAngle) * size - offsetY);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
            }
            else if (arrowInput == 1) {
                GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f);
                GlStateManager.scale(1.5, 1.5, 1.5);
                mc.fontRendererObj.drawString(">", -2.0f, -4.0f, color, false);
            }
            else if (arrowInput == 2) {
                RenderUtils.draw2DPolygon(0.0, 0.0, 5.0, 3, Utils.mergeAlpha(color, 255));
            }

            GlStateManager.popMatrix();

            renderX = baseX + (radiusInput - 13.0) * sinAng;
            renderY = baseY + (radiusInput - 13.0) * cosAng;

            GlStateManager.pushMatrix();
            GlStateManager.translate(renderX, renderY, 0.0);
            GlStateManager.scale(0.8, 0.8, 0.8);

            if (renderDistance.isToggled()) {
                String text = (int) mc.thePlayer.getDistanceToEntity(en) + "m";
                mc.fontRendererObj.drawString(text, (float) (-mc.fontRendererObj.getStringWidth(text) / 2), -4.0f, -1, true);
            }

            GlStateManager.popMatrix();

            if (renderItem.isToggled() && itemStack != null) {
                GlStateManager.pushMatrix();
                if (itemStack.getItem() == Items.arrow) {
                    renderX = baseX + (radiusInput - 26.0) * sinAng;
                    renderY = baseY + (radiusInput - 26.0) * cosAng;
                    GlStateManager.translate(renderX, renderY, 0.0);
                    GlStateManager.scale(1.0f, 1.0f, 1.0f);
                    GlStateManager.rotate((float) angle2 - 45.0f, 0.0f, 0.0f, 1.0f);
                    mc.getRenderItem().renderItemIntoGUI(itemStack, -12, -4);
                }
                else {
                    renderX = baseX + (radiusInput - 29.0) * sinAng;
                    renderY = baseY + (radiusInput - 29.0) * cosAng;
                    GlStateManager.translate(renderX, renderY, 0.0);
                    GlStateManager.scale(1.0f, 1.0f, 1.0f);
                    mc.getRenderItem().renderItemIntoGUI(itemStack, -8, -9);
                }
                GlStateManager.popMatrix();
            }
        }
    }

    private Color getColorForItem(ItemStack itemStack) {
        if (itemStack == null) {
            return Color.WHITE;
        }
        if (itemStack.getItem() == Items.ender_pearl) {
            return new Color(210, 0, 255);
        }
        else if (itemStack.getItem() == Items.fire_charge) {
            return new Color(255, 150, 0);
        }
        else if (itemStack.getItem() == Items.egg) {
            return new Color(255, 238, 154);
        }
        else {
            return Color.WHITE;
        }
    }

    private boolean shouldRender(Entity en, ItemStack stack) {
        if (threatsOnly.isToggled() && stack != null && stack.getItem() == Items.fire_charge) {
            double x = en.posX;
            double y = en.posY;
            double z = en.posZ;
            final double dx = x - en.lastTickPosX;
            final double dy = y - en.lastTickPosY;
            final double dz = z - en.lastTickPosZ;
            if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
                for (int i = 0; i < 400.0; ++i) {
                    final double dist = mc.thePlayer.getDistanceSq(x, y, z);
                    if (dist <= 36) {
                        return true;
                    }
                    final Block block = BlockUtils.getBlock(new BlockPos(x, y, z));
                    if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid) && !(block instanceof BlockFire)) {
                        break;
                    }
                    x += dx * 0.5;
                    y += dy * 0.5;
                    z += dz * 0.5;
                }
            }
            return false;
        }
        return true;
    }
}