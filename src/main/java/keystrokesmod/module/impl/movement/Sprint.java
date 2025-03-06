package keystrokesmod.module.impl.movement;

import keystrokesmod.mixin.impl.accessor.IAccessorEntityPlayerSP;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;

public class Sprint extends Module {
    private ButtonSetting displayText;
    private ButtonSetting rainbow;
    public ButtonSetting disableBackwards;
    public String text = "[Sprint (Toggled)]";
    public float posX = 5;
    public float posY = 5;
    private float limit;

    public Sprint() {
        super("Sprint", category.movement, 0);
        this.registerSetting(new DescriptionSetting("Command: '§esprint [msg]§r'"));
        this.registerSetting(new ButtonSetting("Edit text position", () -> {
            mc.displayGuiScreen(new EditScreen());
        }));
        this.registerSetting(displayText = new ButtonSetting("Display text", false));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
        this.registerSetting(disableBackwards = new ButtonSetting("Disable backwards", false));
        this.closetModule = true;
    }

    public void onUpdate() {
        if (Utils.nullCheck() && mc.inGameHasFocus) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !displayText.isToggled() || !Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }
        mc.fontRendererObj.drawStringWithShadow(text, posX, posY, rainbow.isToggled() ? Utils.getChroma(2, 0) : -1);
    }

    public boolean disableBackwards() {
        limit = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - ((IAccessorEntityPlayerSP) mc.thePlayer).getLastReportedYaw());
        double limitVal = 125;
        if (!disableBackwards.isToggled()) {
            return false;
        }
        if (exceptions()) {
            return false;
        }
        if ((limit <= -limitVal || limit >= limitVal)) {
            return true;
        }
        if (ModuleManager.bHop.isEnabled() && KillAura.target != null && mc.thePlayer.moveForward <= 0.5) {
            return true;
        }
        return false;
    }

    private boolean exceptions() {
        return ModuleManager.scaffold.isEnabled || mc.thePlayer.hurtTime > 0 || !mc.thePlayer.onGround;
    }

    static class EditScreen extends GuiScreen {
        GuiButtonExt resetPosition;
        boolean d = false;
        int miX = 0;
        int miY = 0;
        int maX = 0;
        int maY = 0;
        float aX = 5;
        float aY = 5;
        int laX = 0;
        int laY = 0;
        int lmX = 0;
        int lmY = 0;
        int clickMinX = 0;

        public void initGui() {
            super.initGui();
            this.buttonList.add(this.resetPosition = new GuiButtonExt(1, this.width - 90, this.height - 25, 85, 20, "Reset position"));
            this.aX = ModuleManager.sprint.posX;
            this.aY =ModuleManager.sprint.posY;
        }

        public void drawScreen(int mX, int mY, float pt) {
            drawRect(0, 0, this.width, this.height, -1308622848);
            int miX = (int) this.aX;
            int miY = (int) this.aY;
            String text = ModuleManager.sprint.text;
            int maX = miX + this.mc.fontRendererObj.getStringWidth(text);
            int maY = miY + this.mc.fontRendererObj.FONT_HEIGHT;
            this.mc.fontRendererObj.drawStringWithShadow(text, this.aX, this.aY, -1);
            this.miX = miX;
            this.miY = miY;
            this.maX = maX;
            this.maY = maY;
            this.clickMinX = miX;
            ModuleManager.sprint.posX = miX;
            ModuleManager.sprint.posY = miY;
            ScaledResolution res = new ScaledResolution(this.mc);
            int x = res.getScaledWidth() / 2 - 84;
            int y = res.getScaledHeight() / 2 - 20;
            RenderUtils.drawColoredString("Edit the HUD position by dragging.", '-', x, y, 2L, 0L, true, this.mc.fontRendererObj);

            try {
                this.handleInput();
            }
            catch (IOException var12) {
            }

            super.drawScreen(mX, mY, pt);
        }

        protected void mouseClickMove(int mX, int mY, int b, long t) {
            super.mouseClickMove(mX, mY, b, t);
            if (b == 0) {
                if (this.d) {
                    this.aX = this.laX + (mX - this.lmX);
                    this.aY = this.laY + (mY - this.lmY);
                }
                else if (mX > this.clickMinX && mX < this.maX && mY > this.miY && mY < this.maY) {
                    this.d = true;
                    this.lmX = mX;
                    this.lmY = mY;
                    this.laX = (int) this.aX;
                    this.laY = (int) this.aY;
                }

            }
        }

        protected void mouseReleased(int mX, int mY, int s) {
            super.mouseReleased(mX, mY, s);
            if (s == 0) {
                this.d = false;
            }
        }

        public void actionPerformed(GuiButton b) {
            if (b == this.resetPosition) {
                this.aX = ModuleManager.sprint.posX = 5;
                this.aY = ModuleManager.sprint.posY = 5;
            }

        }

        public boolean doesGuiPauseGame() {
            return false;
        }
    }
}
