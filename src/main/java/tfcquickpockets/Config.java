// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

package tfcquickpockets;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Config implements IModGuiFactory {

    public static Configuration config;

    public static String AUTO_REFILL_CATEGORY = "auto-refill";
    public static boolean autoRefillTools = true;
    public static boolean autoRefillWeapons = true;
    public static boolean autoRefillFood = true;
    public static boolean autoRefillBlocks = true;
    public static boolean autoRefillMisc = true;

    public static String WALK_IN_INVENTORY_CATEGORY = "walk-in-inventory";
    public static boolean allowWalkInInventory = true;
    public static boolean allowJumpingInInventory = true;
    public static boolean allowSprintingInInventory = true;
    public static boolean removeDarkFilterInInventory = true;

    public static String HOTBAR_CYCLING_CATEGORY = "hotbar-cycling";
    public static boolean invertHotbarCycleDirection = false;
    public static float hotbarCyclePreviewTransparency = 0.6f;
    public static float hotbarCyclePreviewItemIconScale = 0.5f;
    public static float hotbarCyclePreviewHeight = 0.73f;
    public static int hotbarCyclePreviewXOffset = 0;
    public static int hotbarCyclePreviewYOffset = 0;

    public static String QUICK_ACCESS_CATEGORY = "quick-access";
    public static boolean allowQuickAccessVessel = true;
    public static boolean allowQuickAccessSack = true;
    public static boolean allowQuickAccessHideBag = true;
    public static boolean allowQuickAccessLeatherBag = true;
    public static boolean allowQuickAccessQuiver = true;

    public static String WATERSKIN_FIX_CATEGORY = "waterskin-fix";
    public static int waterskinFixDelayTicks = 3;

    public static String MOB_DROPS_CATEGORY = "mob-drops";
    public static boolean disableRottenFlesh = true;
    public static boolean disableSpiderEyes = true;

    public static void initialize(String configDirectory) {
        if (config == null) {
            File path = new File(configDirectory + "/" + QuickPockets.ID + ".cfg");
            config = new Configuration(path);
            load();
        }
    }

    public static void load() {

        autoRefillTools = config.getBoolean("Auto-refill Tools", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically swap in a new tool when the one you're using breaks - if you have a replacement in your inventory.\n\nExamples of tools are axes, saws, chisels, etc.\n\n");
        autoRefillWeapons = config.getBoolean("Auto-refill Weapons", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically swap in a new weapon when the one you're using breaks - if you have a replacement in your inventory.\n\nExamples of weapons are swords, maces, bows, etc.\n\n");
        autoRefillBlocks = config.getBoolean("Auto-refill Blocks", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically swap in a new stack of blocks when you use up one - if you have some extra in your inventory.\n\nExamples of blocks are wooden planks, smooth stone, dirt, etc.\n\n");
        autoRefillFood = config.getBoolean("Auto-refill Food and Drinks", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically swap in a new stack of food or drinks when you use one up - if you have some extra in your inventory.\n\nExamples of food and drinks are bread, sandwiches, meat, water jugs, milk, etc.\n\n");
        autoRefillMisc = config.getBoolean("Auto-refill Misc. Items", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically swap in a new stack of miscellaneous items when you use one up - if you have some extra in your inventory.\n\nExamples of misc items are flowers, clay, straw, leather, etc.\n\n");

        allowWalkInInventory = config.getBoolean("Enable Walk in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to allow player motion in the player inventory, and bag inventory screens.\n\nIf this is set to false, it will override the settings for jumping and sprinting.\n\n");
        allowJumpingInInventory = config.getBoolean("Enable Jump in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to allow jumping in the player inventory, and bag inventory screens.\n\nThis setting has no effect if Walk in Inventory is disabled.\n\n");
        allowSprintingInInventory = config.getBoolean("Enable Sprint in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to allow sprinting in the player inventory, and bag inventory screens.\n\nThis setting has no effect if Walk in Inventory is disabled.\n\n");
        removeDarkFilterInInventory = config.getBoolean("Remove Dark Filter in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to remove the dark filter that covers the background in the player inventory, and bag inventory screens.\n\n");

        invertHotbarCycleDirection = config.getBoolean("Invert Scrolling Direction", HOTBAR_CYCLING_CATEGORY, false,
                "\nIf set to true, scrolling the mouse wheel up will move you *down* one inventory row, otherwise scrolling up will move you *up*.\n\n");
        hotbarCyclePreviewTransparency = config.getFloat("Hotbar Preview Transparency", HOTBAR_CYCLING_CATEGORY, 0.6f, 0.0f, 1.0f,
                "\nHow transparent the hotbar cycling preview slots will appear.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewItemIconScale = config.getFloat("Hotbar Preview Item Icon Scale", HOTBAR_CYCLING_CATEGORY, 0.5f, 0.0f, 1.0f,
                "\nHow big the hotbar cycling preview item icons will appear.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewHeight = config.getFloat("Hotbar Preview Height", HOTBAR_CYCLING_CATEGORY, 0.73f, 0.0f, 10.0f,
                "\nHow big the hotbar cycling preview slots will appear relative to the main hotbar slots.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewXOffset = config.getInt("Hotbar Preview X Offset", HOTBAR_CYCLING_CATEGORY, 0, -10000, +10000,
                "\nWhere on the screen the hotbar cycle preview appears.\n\n");
        hotbarCyclePreviewYOffset = config.getInt("Hotbar Preview Y Offset", HOTBAR_CYCLING_CATEGORY, 0, -10000, +10000,
                "\nWhere on the screen the hotbar cycle preview appears.\n\n");

        allowQuickAccessVessel = config.getBoolean("Allow Quick-Access on Vessels", QUICK_ACCESS_CATEGORY, true,
                "\nWhether to allow right-clicking on a small vessel to quickly access it from any inventory screen.\n\n");
        allowQuickAccessSack = config.getBoolean("Allow Quick-Access on Burlap Sacks", QUICK_ACCESS_CATEGORY, true,
                "\nWhether to allow right-clicking on a burlap sack to quickly access it from any inventory screen.\n\n");
        allowQuickAccessHideBag = config.getBoolean("Allow Quick-Access on Hide Bags", QUICK_ACCESS_CATEGORY, true,
                "\nWhether to allow right-clicking on a hide bag to quickly access it from any inventory screen.\n\n");
        allowQuickAccessLeatherBag = config.getBoolean("Allow Quick-Access on Leather Bags", QUICK_ACCESS_CATEGORY, true,
                "\nWhether to allow right-clicking on a leather bag to quickly access it from any inventory screen.\n\n");
        allowQuickAccessQuiver = config.getBoolean("Allow Quick-Access on Quivers", QUICK_ACCESS_CATEGORY, true,
                "\nWhether to allow right-clicking on a quiver to quickly access it from any inventory screen.\n\n");

        waterskinFixDelayTicks = config.getInt("Waterskin Fix Delay Ticks", WATERSKIN_FIX_CATEGORY, 3, 0, 20,
                "\nThe waterskin fix might not always work depending on your exact setup, you can tweak it here.\n\nLower values will try to fix the waterskin faster, but are less likely to work. Higher values have a higher chance to work but with a noticeable delay.\n\nSet this to 0 to disable the waterskin fix.\n\n");

        disableRottenFlesh = config.getBoolean("Disable Rotten Flesh", MOB_DROPS_CATEGORY, true,
                "\nWhether to disable rotten flesh drops from zombies. Rotten flesh has no in-game uses.\n\n");
        disableSpiderEyes = config.getBoolean("Disable Spider Eyes", MOB_DROPS_CATEGORY, true,
                "\nWhether to disable spider eye drops from spiders. Spider eyes have no in-game uses.\n\n");


        if (config.hasChanged())
            config.save();
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(QuickPockets.ID))
            load();
    }

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ConfigGUI.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static class ConfigGUI extends GuiConfig {
        public ConfigGUI(GuiScreen screen) {
            super(screen,
                getConfigElements(),
                QuickPockets.ID,
                false,
                false,
                "TFC+ Quick Pockets");
        }

        @SuppressWarnings("rawtypes")
        public static List<IConfigElement> getConfigElements() {
            List<IConfigElement> autoRefill = new ConfigElement(Config.config.getCategory(AUTO_REFILL_CATEGORY)).getChildElements();
            List<IConfigElement> hotbarCycling = new ConfigElement(Config.config.getCategory(HOTBAR_CYCLING_CATEGORY)).getChildElements();
            List<IConfigElement> walkInInventory = new ConfigElement(Config.config.getCategory(WALK_IN_INVENTORY_CATEGORY)).getChildElements();
            List<IConfigElement> quickAccess = new ConfigElement(Config.config.getCategory(QUICK_ACCESS_CATEGORY)).getChildElements();
            List<IConfigElement> waterskinFix = new ConfigElement(Config.config.getCategory(WATERSKIN_FIX_CATEGORY)).getChildElements();
            List<IConfigElement> mobDrops = new ConfigElement(Config.config.getCategory(MOB_DROPS_CATEGORY)).getChildElements();

            List<IConfigElement> elements = new ArrayList<IConfigElement>();
            elements.add(new DummyConfigElement.DummyCategoryElement("Auto Refill", "gui.config.category.auto-refill", autoRefill));
            elements.add(new DummyConfigElement.DummyCategoryElement("Hotbar Cycling", "gui.config.category.hotbar-cycling", hotbarCycling));
            elements.add(new DummyConfigElement.DummyCategoryElement("Walk in Inventory", "gui.config.category.walk-in-inventory", walkInInventory));
            elements.add(new DummyConfigElement.DummyCategoryElement("Quick Access", "gui.config.category.quick-access", quickAccess));
            elements.add(new DummyConfigElement.DummyCategoryElement("Waterskin Fix", "gui.config.category.waterskin-fix", waterskinFix));
            elements.add(new DummyConfigElement.DummyCategoryElement("Useless Mob Drops", "gui.config.category.mob-drops", mobDrops));

            return elements;
        }
    }
}