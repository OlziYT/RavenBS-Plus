package keystrokesmod.module.impl.other;

import keystrokesmod.event.AntiCheatFlagEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.PlayerData;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class Anticheat extends Module {
    private SliderSetting interval;
    private ButtonSetting enemyAdd;
    private ButtonSetting autoReport;
    private ButtonSetting ignoreTeammates;
    private ButtonSetting atlasSuspect;
    private ButtonSetting shouldPing;
    private ButtonSetting autoBlock;
    private ButtonSetting noFall;
    private ButtonSetting noSlow;
    private ButtonSetting scaffold;
    private ButtonSetting legitScaffold;

    private HashMap<UUID, HashMap<ButtonSetting, Long>> flags = new HashMap<>();
    private HashMap<UUID, PlayerData> players = new HashMap<>();

    private long lastAlert;
    private long lastClientBoundPacket;

    public Anticheat() {
        super("Anticheat", category.other);
        this.registerSetting(new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(interval = new SliderSetting("Flag interval", " second", 20.0, 0.0, 60.0, 1.0));
        this.registerSetting(enemyAdd = new ButtonSetting("Add cheaters as enemy", false));
        this.registerSetting(autoReport = new ButtonSetting("Auto report", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        this.registerSetting(atlasSuspect = new ButtonSetting("Only atlas suspect", false));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(new DescriptionSetting("Detected cheats"));
        this.registerSetting(autoBlock = new ButtonSetting("Autoblock", true));
        this.registerSetting(noFall = new ButtonSetting("NoFall", true));
        this.registerSetting(noSlow = new ButtonSetting("NoSlow", true));
        this.registerSetting(scaffold = new ButtonSetting("Scaffold", true));
        this.registerSetting(legitScaffold = new ButtonSetting("Legit scaffold", true));
        this.closetModule = true;
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        lastClientBoundPacket = System.currentTimeMillis();
    }

    private void alert(final EntityPlayer entityPlayer, ButtonSetting mode) {
        if (Utils.isFriended(entityPlayer) || (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))) {
            return;
        }
        if (atlasSuspect.isToggled()) {
            if (!entityPlayer.getName().equals("Suspect§r")) {
                return;
            }
        }
        else if (enemyAdd.isToggled()) {
            Utils.addEnemy(entityPlayer.getName());
        }
        final long currentTimeMillis = System.currentTimeMillis();
        if (interval.getInput() > 0.0) {
            HashMap<ButtonSetting, Long> hashMap = flags.get(entityPlayer.getUniqueID());
            if (hashMap == null) {
                hashMap = new HashMap<>();
            }
            else {
                final Long n = hashMap.get(mode);
                if (n != null && Utils.timeBetween(n, currentTimeMillis) <= interval.getInput() * 1000.0) {
                    return;
                }
            }
            hashMap.put(mode, currentTimeMillis);
            flags.put(entityPlayer.getUniqueID(), hashMap);
        }
        final ChatComponentText chatComponentText = new ChatComponentText(Utils.formatColor("&7[&dR&7]&r " + entityPlayer.getDisplayName().getUnformattedText() + " &7detected for &d" + mode.getName()));
        final ChatStyle chatStyle = new ChatStyle();
        chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wdr " + entityPlayer.getName()));
        chatComponentText.appendSibling(new ChatComponentText(Utils.formatColor(" §7[§cWDR§7]")).setChatStyle(chatStyle));
        mc.thePlayer.addChatMessage(chatComponentText);
        postAntiCheatFlagEvent(mode.getName(), entityPlayer);
        if (shouldPing.isToggled() && Utils.timeBetween(lastAlert, currentTimeMillis) >= 1500L) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
            lastAlert = currentTimeMillis;
        }
        if (autoReport.isToggled() && !Utils.isFriended(entityPlayer)) {
            mc.thePlayer.sendChatMessage("/wdr " + Utils.stripColor(entityPlayer.getGameProfile().getName()));
        }
    }

    public void onUpdate() {
        if (mc.isSingleplayer()) {
            return;
        }
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer == null) {
                continue;
            }
            if (entityPlayer == mc.thePlayer) {
                continue;
            }
            if (AntiBot.isBot(entityPlayer)) {
                continue;
            }
            PlayerData data = players.get(entityPlayer.getUniqueID());
            if (data == null) {
                data = new PlayerData();
            }
            data.update(entityPlayer);
            this.performCheck(entityPlayer, data);
            data.updateServerPos(entityPlayer);
            data.updateSneak(entityPlayer);
            players.put(entityPlayer.getUniqueID(), data);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            players.clear();
            flags.clear();
        }
    }

    public void onDisable() {
        players.clear();
        flags.clear();
        lastAlert = 0L;
    }

    private void performCheck(EntityPlayer entityPlayer, PlayerData playerData) {
        if (autoBlock.isToggled() && playerData.autoBlockTicks >= 10) {
            alert(entityPlayer, autoBlock);
            return;
        }
        if (legitScaffold.isToggled() && playerData.sneakTicks >= 3) {
            alert(entityPlayer, legitScaffold);
            return;
        }
        if (noSlow.isToggled() && playerData.noSlowTicks == 11 && playerData.speed >= 0.08) {
            alert(entityPlayer, noSlow);
            return;
        }
        if (scaffold.isToggled() && entityPlayer.isSwingInProgress && entityPlayer.rotationPitch >= 70.0f && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof ItemBlock && playerData.fastTick >= 20 && entityPlayer.ticksExisted - playerData.lastSneakTick >= 30 && entityPlayer.ticksExisted - playerData.aboveVoidTicks >= 20) {
            boolean overAir = true;
            BlockPos blockPos = entityPlayer.getPosition().down(2);
            for (int i = 0; i < 4; ++i) {
                if (!(BlockUtils.getBlock(blockPos) instanceof BlockAir)) {
                    overAir = false;
                    break;
                }
                blockPos = blockPos.down();
            }
            if (overAir) {
                alert(entityPlayer, scaffold);
                return;
            }
        }
        if (noFall.isToggled() && !entityPlayer.capabilities.isFlying && Utils.timeBetween(System.currentTimeMillis(), lastClientBoundPacket) <= 150) {
            double serverPosX = entityPlayer.serverPosX / 32;
            double serverPosY = entityPlayer.serverPosY / 32;
            double serverPosZ= entityPlayer.serverPosZ / 32;
            double deltaX = Math.abs(playerData.serverPosX - serverPosX);
            double deltaY = playerData.serverPosY - serverPosY;
            double deltaZ = Math.abs(playerData.serverPosZ - serverPosZ);
            if (deltaY >= 5 && deltaX <= 10 && deltaZ <= 10 && deltaY <= 40) {
                if (!Utils.overVoid(serverPosX, serverPosY, serverPosZ) && Utils.distanceToGround(entityPlayer) > 3 && !Utils.onLadder(entityPlayer) && !entityPlayer.isInWater() && !entityPlayer.isInLava()) {
                    alert(entityPlayer, noFall);
                }
            }
        }
    }

    public void postAntiCheatFlagEvent(String flag, Entity entity) {
        MinecraftForge.EVENT_BUS.post(new AntiCheatFlagEvent(flag, entity));
    }
}
