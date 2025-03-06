package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TPAura extends Module {
    private SliderSetting range;
    private ButtonSetting weaponOnly;
    private double x = 0;
    private double z = 0;
    private double y = 0;
    public TPAura() {
        super("TPAura", category.combat);
        this.registerSetting(range = new SliderSetting("Range", 0, 0, 50, 1));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
        if (Utils.nullCheck() && mc.thePlayer.maxHurtTime > 0 && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime) {
            this.updatePosition();
        }
    }

    private void updatePosition() {
        this.x = Utils.randomizeInt(-15, 15) / 10.0;
        this.y = Utils.randomizeInt(10, 15) / 10.0;
        this.z = Utils.randomizeInt(-15, 15) / 10.0;
    }

    @Override
    public void onEnable() {
        if (range.getInput() == 0.0) {
            Utils.sendMessage("&cTPAura range values are set to 0.");
            this.disable();
            return;
        }
        this.updatePosition();
    }

    public void onUpdate() {
        if (weaponOnly.isToggled() && !Utils.holdingWeapon()) {
            return;
        }
        double rangeSq = range.getInput() * range.getInput();
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                if (mc.thePlayer.getDistanceSqToEntity(entityPlayer) > rangeSq) {
                    continue;
                }
                if (AntiBot.isBot(entityPlayer) || Utils.isFriended(entityPlayer)) {
                    continue;
                }
                mc.thePlayer.setPosition(entityPlayer.posX + this.x, entityPlayer.posY + this.y, entityPlayer.posZ + this.z);
                Utils.attackEntity(entityPlayer, true, false);
                break;
            }
        }
    }
}
