package keystrokesmod.module.impl.player;

import keystrokesmod.Raven;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.GroupSetting;
import keystrokesmod.module.setting.impl.KeySetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class InvManager extends Module {
    private ButtonSetting closeChest;
    private ButtonSetting closeInventory;
    private ButtonSetting disableInLobby;

    private SliderSetting autoArmor;

    private SliderSetting autoSort;

    private ButtonSetting customChest;
    private SliderSetting chestStealer;

    private GroupSetting inventoryCleaner;
    private ButtonSetting inventoryCleanerEnabled;
    private SliderSetting inventoryCleanerDelay;
    private KeySetting cleanKey;
    private ButtonSetting cleanBuckets;
    private SliderSetting maxBlockStacks;
    private SliderSetting maxProjectileStacks;

    private SliderSetting swordSlot;
    private SliderSetting blocksSlot;
    private SliderSetting goldenAppleSlot;
    private SliderSetting projectileSlot;
    private SliderSetting speedPotionSlot;
    private SliderSetting pearlSlot;

    private String[] trashItems = { "stick", "bed", "sapling", "pressureplate", "weightedplate", "book", "glassbottle", "reeds", "sugar", "expbottle", "flesh", "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather", "shears", "anvil", "torch", "seeds", "leather", "skull", "record", "flower", "minecart", "waterlily", "wheat", "sulphur", "boat", "dyepowder", "frame", "writingbook", "comparator", "banner", "diode", "item.redstone", "ghasttear", "goldnugget", "netherstalkseeds" };

    private int lastStole;
    private int lastSort;
    private int lastArmor;
    private int lastClean;
    private boolean receivedInventoryData;

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(closeChest = new ButtonSetting("Close chest", true));
        this.registerSetting(closeInventory = new ButtonSetting("Close inventory", false));
        this.registerSetting(disableInLobby = new ButtonSetting("Disable in lobby", true));
        this.registerSetting(autoArmor = new SliderSetting("Auto armor", true, 3, 0, 20, 1));
        this.registerSetting(autoSort = new SliderSetting("Auto sort", true,3, 0, 20, 1));
        this.registerSetting(chestStealer = new SliderSetting("Chest stealer", true, 2, 0, 20, 1));
        this.registerSetting(customChest = new ButtonSetting("Steal from custom chests", false));

        this.registerSetting(inventoryCleaner = new GroupSetting("Inventory cleaner"));
        this.registerSetting(inventoryCleanerEnabled = new ButtonSetting(inventoryCleaner, "Enabled", true));
        this.registerSetting(inventoryCleanerDelay = new SliderSetting(inventoryCleaner, "Delay", " tick", 3, 0, 20, 1));
        this.registerSetting(cleanKey = new KeySetting(inventoryCleaner, "Clean key", 1002));
        this.registerSetting(cleanBuckets = new ButtonSetting(inventoryCleaner,"Clean buckets", false));
        this.registerSetting(maxBlockStacks = new SliderSetting(inventoryCleaner,"Max block stacks", 5, 1, 20, 1));
        this.registerSetting(maxProjectileStacks = new SliderSetting(inventoryCleaner,"Max projectile stacks", 5, 1, 20, 1));

        this.registerSetting(swordSlot = new SliderSetting("Sword slot", true, -1, 1, 9, 1));
        this.registerSetting(blocksSlot = new SliderSetting("Blocks slot", true, -1, 1, 9, 1));
        this.registerSetting(goldenAppleSlot = new SliderSetting("Golden apple slot", true, -1, 1, 9, 1));
        this.registerSetting(projectileSlot = new SliderSetting("Projectile slot", true,-1, 1, 9, 1));
        this.registerSetting(speedPotionSlot = new SliderSetting("Speed potion slot", true,-1, 1, 9, 1));
        this.registerSetting(pearlSlot = new SliderSetting("Pearl slot", true,-1, 1, 9, 1));

        this.chestStealer.setSuffix(" tick");
        this.autoArmor.setSuffix(" tick");
        this.autoSort.setSuffix(" tick");
    }

    public void onEnable() {
        resetDelay();
        receivedInventoryData = false;
    }

    public void onUpdate() {
        if ((disableInLobby.isToggled() && Utils.isLobby()) || (ModuleManager.skyWars.isEnabled() && ModuleManager.invmove.isEnabled() && ModuleManager.invmove.inventory.getInput() == 3 && Utils.getSkyWarsStatus() != 2)) {
            resetDelay();
            return;
        }
        if (Utils.inInventory() || (ModuleManager.invmove.isEnabled() && ModuleManager.invmove.inventory.getInput() == 3 && mc.currentScreen == null && !Raven.packetsHandler.sent())) {
            if (autoArmor.getInput() != -1 && lastArmor++ >= autoArmor.getInput()) {
                for (int i = 0; i < 4; i++) {
                    int bestSlot = getBestArmor(i, null);
                    if (bestSlot == i + 5) {
                        continue;
                    }
                    if (bestSlot != -1) {
                        if (getItemStack(i + 5) != null) {
                            drop(i + 5);
                        } else {
                            click(bestSlot, 0, true);
                            lastArmor = 0;
                        }
                        return;
                    }
                }
            }
            if (autoSort.getInput() != -1 && ++lastSort >= autoSort.getInput()) {
                if (swordSlot.getInput() != -1) {
                    if (sort(getBestSword(null, (int) swordSlot.getInput()), (int) swordSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (goldenAppleSlot.getInput() != -1) {
                    if (sort(getBiggestStack(Items.golden_apple, (int) goldenAppleSlot.getInput()), (int) goldenAppleSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (blocksSlot.getInput() != -1) {
                    if (sort(getMostBlocks(), (int) blocksSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (projectileSlot.getInput() != -1) {
                    if (sort(getMostProjectiles((int) projectileSlot.getInput()), (int) projectileSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (pearlSlot.getInput() != -1) {
                    if (sort(getBiggestStack(Items.ender_pearl, (int) pearlSlot.getInput()), (int) pearlSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (speedPotionSlot.getInput() != -1) {
                    if (sort(getBestPotion((int) speedPotionSlot.getInput(), null), (int) speedPotionSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
            }
            if (inventoryCleanerEnabled.isToggled()) {
                if (cleanKey.getKey() != 0 && !cleanKey.isPressed()) {
                    return;
                }
                if (++lastClean >= inventoryCleanerDelay.getInput()) {
                    for (int i = 5; i < 45; i++) {
                        ItemStack stack = getItemStack(i);
                        if (stack == null) {
                            continue;
                        }
                        if (!canDrop(stack, i)) {
                            continue;
                        }
                        drop(i);
                        lastClean = 0;
                        return;
                    }
                }
            }
            if ((lastClean > inventoryCleanerDelay.getInput() || lastClean == 0) && (lastArmor > autoArmor.getInput() || lastArmor == 0) && (lastSort > autoSort.getInput() || lastSort == 0)) {
                if (closeInventory.isToggled()) {
                    mc.thePlayer.closeScreen();
                }
            }
        }
        else if (chestStealer.getInput() != -1 && mc.thePlayer.openContainer instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            if (chest == null || inventoryFull()) {
                autoClose(chest);
                return;
            }
            String name = Utils.stripColor(chest.getLowerChestInventory().getName());
            if (!customChest.isToggled() && !name.equals("Chest") && !name.equals("Ender Chest") && !name.equals("Large Chest")) {
                return;
            }
            boolean notEmpty = false;
            boolean stolen = false;
            int size = chest.getLowerChestInventory().getSizeInventory();
            for (int i = 0; i < size; i++) {
                ItemStack item = chest.getLowerChestInventory().getStackInSlot(i);
                if (item == null) {
                    continue;
                }
                if (Arrays.stream(trashItems).anyMatch(item.getUnlocalizedName().toLowerCase()::contains)) {
                    continue;
                }
                IInventory inventory = chest.getLowerChestInventory();
                notEmpty = true;
                if (item.getItem() instanceof ItemSword) {
                    if (getBestSword(inventory, (int) swordSlot.getInput()) != i) {
                        continue;
                    }
                    if (++lastStole >= chestStealer.getInput()) {
                        if (swordSlot.getInput() != -1) {
                            mc.playerController.windowClick(chest.windowId, i, (int) swordSlot.getInput() - 1, 2, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemBlock) {
                    if (!Utils.canBePlaced((ItemBlock) item.getItem())) {
                        continue;
                    }
                    if (++lastStole >= chestStealer.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemAppleGold) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (goldenAppleSlot.getInput() == -1) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, (int) (goldenAppleSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemSnowball || item.getItem() instanceof ItemEgg) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (projectileSlot.getInput() == -1) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, (int) (projectileSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemEnderPearl) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (pearlSlot.getInput() == -1) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, (int) (pearlSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemArmor) {
                    if (getBestArmor(((ItemArmor) item.getItem()).armorType, inventory) != i) {
                        continue;
                    }
                    if (++lastStole >= chestStealer.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemPotion) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (!isSpeedPot(item)) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        } else {
                            if (getBestPotion((int) speedPotionSlot.getInput(), inventory) != i || speedPotionSlot.getInput() == -1) {
                                mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            }
                            else {
                                mc.playerController.windowClick(chest.windowId, i, (int) (speedPotionSlot.getInput() - 1), 2, mc.thePlayer);
                            }
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemTool) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (getBestTool(item, inventory) != i) {
                            continue;
                        }
                        if (++lastStole >= chestStealer.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemBow) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (getBestBow(inventory) != i) {
                            continue;
                        }
                        if (++lastStole >= chestStealer.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemFishingRod) {
                    if (++lastStole >= chestStealer.getInput()) {
                        if (getBestRod(inventory) != i) {
                            continue;
                        }
                        if (++lastStole >= chestStealer.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                else {
                    if (++lastStole >= chestStealer.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
            }

            if (inventoryFull() || !notEmpty || !stolen) {
                autoClose(null);
            }
        }
        else {
            resetDelay();
            receivedInventoryData = false;
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof S30PacketWindowItems) {
            receivedInventoryData = true;
        }
    }

    private int getProtection(final ItemStack itemStack) {
        return ((ItemArmor)itemStack.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[] { itemStack }, DamageSource.generic);
    }

    private void click(int slot, int mouseButton, boolean shiftClick) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, mouseButton, shiftClick ? 1 : 0, mc.thePlayer);
    }

    private boolean sort(int bestSlot, int desiredSlot) {
        if (bestSlot != -1 && bestSlot != desiredSlot + 35) {
            swap(bestSlot, desiredSlot - 1);
            return true;
        }
        return false;
    }

    private void drop(int slot) {
        mc.playerController.windowClick(0, slot, 1, 4, mc.thePlayer);
    }

    private void swap(int slot, int hSlot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hSlot, 2, mc.thePlayer);
    }

    private boolean isSpeedPot(ItemStack item) {
        List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
        if (list == null) {
            return false;
        }
        for (PotionEffect effect : list) {
            if (effect.getEffectName().equals("potion.moveSpeed")) {
                return true;
            }
        }
        return false;
    }

    private boolean inventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    private void resetDelay() {
        lastStole = lastArmor = lastClean = lastSort = 0;
    }

    private void autoClose(ContainerChest chest) {
        if (closeChest.isToggled() && receivedInventoryData) {
            if (chest != null) {
                String name = Utils.stripColor(chest.getLowerChestInventory().getName());
                if (!customChest.isToggled() && !name.equals("Chest") && !name.equals("Ender Chest") && !name.equals("Large Chest")) {
                    return;
                }
            }
            mc.thePlayer.closeScreen();
            receivedInventoryData = false;
        }
    }

    private int getBestSword(IInventory inventory, int desiredSlot) {
        int bestSword = -1;
        double lastDamage = -1;
        double damageInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && itemStackInSlot.getItem() instanceof ItemSword) {
                damageInSlot = Utils.getDamageLevel(itemStackInSlot);
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemSword)) {
                continue;
            }
            double damage = Utils.getDamageLevel(item);
            if (damage > lastDamage && damage > damageInSlot) {
                lastDamage = damage;
                bestSword = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemSword)) {
                    continue;
                }
                double damage = Utils.getDamageLevel(item);
                if (damage > lastDamage && damage > damageInSlot) {
                    lastDamage = damage;
                    bestSword = i;
                }
            }
        }
        if (bestSword == -1) {
            bestSword = desiredSlot + 35;
        }
        return bestSword;
    }

    private int getBestArmor(int armorType, IInventory inventory) {
        int bestArmor = -1;
        double lastProtection = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                continue;
            }
            double protection = getProtection(item);
            if (protection > lastProtection) {
                lastProtection = protection;
                bestArmor = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                    continue;
                }
                double protection = getProtection(item);
                if (protection > lastProtection) {
                    lastProtection = protection;
                    bestArmor = i;
                }
            }
        }
        return bestArmor;
    }

    private boolean dropPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion) stack.getItem();
            if (potion.getEffects(stack) == null) {
                return true;
            }
            for (PotionEffect effect : potion.getEffects(stack)) {
                if (effect.getPotionID() == Potion.moveSlowdown.getId() || effect.getPotionID() == Potion.weakness.getId() || effect.getPotionID() == Potion.poison.getId() || effect.getPotionID() == Potion.harm.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getBestBow(IInventory inventory) {
        int bestBow = -1;
        double lastPower = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemBow)) {
                continue;
            }
            double protection = getPower(item);
            if (protection > lastPower) {
                lastPower = protection;
                bestBow = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemBow)) {
                    continue;
                }
                double power = getPower(item);
                if (power > lastPower) {
                    lastPower = power;
                    bestBow = i;
                }
            }
        }
        return bestBow;
    }

    private float getPower(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1;
        }
        return score;
    }

    private int getBestRod(IInventory inventory) {
        int bestRod = -1;
        double lastKnocback = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                continue;
            }
            double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
            if (knockback > lastKnocback) {
                lastKnocback = knockback;
                bestRod = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                    continue;
                }
                double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
                if (knockback > lastKnocback) {
                    lastKnocback = knockback;
                    bestRod = i;
                }
            }
        }
        return bestRod;
    }

    private int getBestTool(ItemStack tool, IInventory inventory) {
        if (tool == null || !(tool.getItem() instanceof ItemTool)) {
            return -1;
        }

        Block blockType = Blocks.dirt;
        if (tool.getItem() instanceof ItemAxe) {
            blockType = Blocks.log;
        }
        else if (tool.getItem() instanceof ItemPickaxe) {
            blockType = Blocks.stone;
        }
        else if (tool.getItem() instanceof ItemSpade) {
            blockType = Blocks.dirt;
        }

        Class<?> toolClass = tool.getItem().getClass();

        int bestSlot = -1;
        double bestEfficiency = -1.0;

        for (int slot = 5; slot < 45; slot++) {
            ItemStack stack = getItemStack(slot);
            if (stack == null || !(stack.getItem() instanceof ItemTool)) {
                continue;
            }

            if (toolClass.isInstance(stack.getItem())) {
                double efficiency = Utils.getEfficiency(stack, blockType);
                if (efficiency > bestEfficiency) {
                    bestEfficiency = efficiency;
                    bestSlot = slot;
                }
            }
        }

        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null || !(stack.getItem() instanceof ItemTool)) {
                    continue;
                }
                if (toolClass.isInstance(stack.getItem())) {
                    double efficiency = Utils.getEfficiency(stack, blockType);
                    if (efficiency > bestEfficiency) {
                        bestEfficiency = efficiency;
                        bestSlot = slot;
                    }
                }
            }
        }
        return bestSlot;
    }

    private int getBestPotion(int desiredSlot, IInventory inventory) {
        int bestScore = -1;
        int bestPotion = -1;
        int bestStackSize = -1;

        double amplifierInSlot = -1;
        ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
        if (itemStackInSlot != null && itemStackInSlot.getItem() instanceof ItemPotion) {
            amplifierInSlot = getPotionScore(itemStackInSlot);
        }

        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemPotion)) {
                continue;
            }

            int score = getPotionScore(item);
            if (score <= 0) {
                continue;
            }

            if (score > bestScore && score > amplifierInSlot) {
                bestPotion = i;
                bestScore = score;
                bestStackSize = item.stackSize;
            }
            else if (score == bestScore && item.stackSize > bestStackSize && score > amplifierInSlot) {
                bestPotion = i;
                bestScore = score;
                bestStackSize = item.stackSize;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemPotion)) {
                    continue;
                }

                int score = getPotionScore(item);
                if (score <= 0) {
                    continue;
                }

                if (score > bestScore && score > amplifierInSlot) {
                    bestPotion = i;
                    bestScore = score;
                    bestStackSize = item.stackSize;
                }
                else if (score == bestScore && item.stackSize > bestStackSize && score > amplifierInSlot) {
                    bestPotion = i;
                    bestScore = score;
                    bestStackSize = item.stackSize;
                }
            }
        }

        return bestPotion;
    }

    private int getPotionScore(ItemStack item) {
        if (!(item.getItem() instanceof ItemPotion)) {
            return -1;
        }
        List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
        if (list == null) {
            return -1;
        }
        for (PotionEffect effect : list) {
            if ("potion.moveSpeed".equals(effect.getEffectName())) {
                return effect.getAmplifier() + effect.getDuration();
            }
        }
        return -1;
    }

    private int getBiggestStack(Item targetItem, int desiredSlot) {
        int stack = 0;
        int biggestSlot = -1;
        int stackInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && itemStackInSlot.getItem() == targetItem) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() == targetItem && item.stackSize > stack && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private boolean canDrop(ItemStack itemStack, int slot) {
        if (Arrays.stream(trashItems).anyMatch(itemStack.getUnlocalizedName().toLowerCase()::contains)) {
            return true;
        }
        if (dropPotion(itemStack)) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemSword && getBestSword(null, (int) swordSlot.getInput()) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemArmor && getBestArmor(((ItemArmor) itemStack.getItem()).armorType, null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemTool && getBestTool(itemStack, null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemBow && getBestBow(null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemFishingRod && getBestRod(null) != slot) {
            return true;
        }
        if (cleanBuckets.isToggled() && (itemStack.getItem() instanceof ItemBucket || itemStack.getItem() instanceof ItemBucketMilk)) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemBlock) {
            int stacksInInventory = getFullStacksOfBlocks();
            if (stacksInInventory > maxBlockStacks.getInput() || !Utils.canBePlaced((ItemBlock) itemStack.getItem())) {
                return true;
            }
        }
        if (itemStack.getItem() instanceof ItemSnowball || itemStack.getItem() instanceof ItemEgg) {
            int stacksInInventory = getFullStacksOfProjectiles();
            if (stacksInInventory > maxProjectileStacks.getInput()) {
                return true;
            }
        }
        return false;
    }

    private int getMostProjectiles(int desiredSlot) {
        int biggestSnowballSlot = getBiggestStack(Items.snowball, desiredSlot);
        int biggestEggSlot = getBiggestStack(Items.egg, desiredSlot);

        int snowballStackSize = (biggestSnowballSlot != -1) ? getItemStack(biggestSnowballSlot).stackSize : 0;
        int eggStackSize = (biggestEggSlot != -1) ? getItemStack(biggestEggSlot).stackSize : 0;

        int stackInSlot = 0;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && (itemStackInSlot.getItem() instanceof ItemEgg || itemStackInSlot.getItem() instanceof ItemSnowball)) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }

        if (stackInSlot >= snowballStackSize && stackInSlot >= eggStackSize) {
            return -1;
        }

        if (eggStackSize > snowballStackSize) {
            return biggestEggSlot;
        }
        else if (snowballStackSize > eggStackSize) {
            return biggestSnowballSlot;
        }
        else {
            if (snowballStackSize != 0 && eggStackSize != 0) {
                return biggestSnowballSlot;
            }
        }
        return -1;
    }

    private int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        ItemStack itemStackInSlot = getItemStack((int) (blocksSlot.getInput() + 35));
        int stackInSlot = 0;
        if (itemStackInSlot != null) {
            stackInSlot = itemStackInSlot.stackSize;
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemBlock && item.stackSize > stack && Utils.canBePlaced((ItemBlock) item.getItem()) && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private ItemStack getItemStack(int i) {
        Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
        if (slot == null) {
            return null;
        }
        ItemStack item = slot.getStack();
        if (item == null) {
            return null;
        }
        return item;
    }

    private int getFullStacksOfBlocks() {
        int fullStacks = 0;
        int maxStackSize = 64;

        InventoryPlayer inventory = mc.thePlayer.inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack currentStack = inventory.mainInventory[i];

            if (currentStack != null) {
                if (!(currentStack.getItem() instanceof ItemBlock)) {
                    continue;
                }
                if (!Utils.canBePlaced((ItemBlock) currentStack.getItem())) {
                    continue;
                }
                if (currentStack.stackSize >= maxStackSize) {
                    fullStacks += currentStack.stackSize / maxStackSize;
                }
            }
        }

        return fullStacks;
    }

    private int getFullStacksOfProjectiles() {
        int fullStacks = 0;
        int maxStackSize = 16;

        InventoryPlayer inventory = mc.thePlayer.inventory;

        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack currentStack = inventory.mainInventory[i];

            if (currentStack != null) {
                if (!(currentStack.getItem() instanceof ItemEgg) && !(currentStack.getItem() instanceof ItemSnowball)) {
                    continue;
                }
                if (currentStack.stackSize >= maxStackSize) {
                    fullStacks += currentStack.stackSize / maxStackSize;
                }
            }
        }

        return fullStacks;
    }
}