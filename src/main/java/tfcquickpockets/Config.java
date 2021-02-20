// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

package tfcquickpockets;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.*;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
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

    public static String CLIENT_ONLY_CATEGORY = "client-only";
    public static boolean clientOnlyMode = true;

    public static String AUTO_REFILL_CATEGORY = "auto-refill";
    public static boolean autoRefillTools = true;
    public static boolean autoRefillWeapons = true;
    public static boolean autoRefillFood = true;
    public static boolean autoRefillBlocks = true;
    public static boolean autoRefillMisc = true;
    public static boolean autoStackFoodOnPickup = true;

    public static String WALK_IN_INVENTORY_CATEGORY = "walk-in-inventory";
    public static boolean allowWalkInInventory = true;
    public static boolean allowJumpingInInventory = true;
    public static boolean removeDarkFilterInInventory = true;
    public static boolean onlyMoveWhenSprintKeyIsPressed = true;

    public static String HOTBAR_CYCLING_CATEGORY = "hotbar-cycling";
    public static boolean enableHotbarCyclePreview = true;
    public static boolean invertHotbarCycleDirection = false;
    public static boolean skipEmptyInventorySlots = true;
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

    public static String SOUNDS_CATEGORY = "sounds";
    public static boolean enablePlayerInventorySound = true;
    public static boolean enableSwitchHotbarSlotSound = false;
    public static boolean enableHotbarCycleSound = true;
    public static boolean enableAutoRefillSound = true;
    public static boolean enableChestClosingSound = true;
    public static boolean enableBarrelSealSound = true;
    public static boolean enableBarrelEmptySound = true;
    public static boolean enableBarrelItemSoakSound = true;
    public static boolean enableLargeVesselSealSound = true;
    public static boolean enableLargeVesselEmptySound = true;
    public static boolean enableLargeVesselItemSoakSound = true;
    public static boolean enableBeehiveOpenSound = true;
    public static boolean enableBeehiveHoneySound = true;
    public static boolean enableMeleeWeaponSounds = true;
    public static boolean enableBowWeaponSounds = true;
    public static boolean enableBucketSounds = true;
    public static boolean enableFirepitSounds = true;
    public static boolean enableBloomerySounds = true;
    public static boolean enableBlastFurnaceSounds = true;
    public static boolean enableFishingRodSounds = true;
    public static boolean enableRopeSounds = true;

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
        clientOnlyMode = config.getBoolean("Enable Client-Only Mode", CLIENT_ONLY_CATEGORY, false,
                "\nDisables all features that require server-side assistance.\n\n" +
                "Set this to TRUE only when you are playing on a server that does not have " + QuickPockets.NAME + " installed. If you do not do so, you could crash your game - or the entire server.\n\n" +
                "Features that require server assistance:\n" +
                "* auto-refill\n" +
                "* auto-stack food\n" +
                "* hotbar cycling\n" +
                "* quick access\n" +
                "* waterskin fix\n" +
                "* no useless drops\n\n");

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
        autoStackFoodOnPickup = config.getBoolean("Auto-stack Food on Pickup", AUTO_REFILL_CATEGORY, true,
                "\nWhether to automatically stack food when picked up off of the ground.\n\n");

        allowWalkInInventory = config.getBoolean("Enable Walk in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to allow player motion in the player inventory, and bag inventory screens when the sprint button is pressed.\n\nIf this is set to false, it will override the setting for jumping.\n\n");
        allowJumpingInInventory = config.getBoolean("Enable Jump in Inventory", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether to allow jumping in the player inventory, and bag inventory screens.\n\nThis setting has no effect if Walk in Inventory is disabled.\n\n");
        removeDarkFilterInInventory = config.getBoolean("Remove Dark Filter When Moving", WALK_IN_INVENTORY_CATEGORY, false,
                "\nWhether to remove the dark filter that covers the background in the player inventory, and bag inventory screens, when the sprint button is pressed.\n\n");
        onlyMoveWhenSprintKeyIsPressed = config.getBoolean("Only Move If Sprint Is Pressed", WALK_IN_INVENTORY_CATEGORY, true,
                "\nWhether the sprint key needs to be pressed in order to move in the inventory screen.\n\nNote that if you disable this you will also move whenever you cut decay off or stack food with D/S.\n\n");

        enableHotbarCyclePreview = config.getBoolean("Enable Hotbar Cycle Preview", HOTBAR_CYCLING_CATEGORY, true,
                "\nWhether to show a preview of the next and previous inventory slots when cycling through the hotbar.\n\n");
        invertHotbarCycleDirection = config.getBoolean("Invert Scrolling Direction", HOTBAR_CYCLING_CATEGORY, false,
                "\nIf set to true, scrolling the mouse wheel up will move you *down* one inventory row, otherwise scrolling up will move you *up*.\n\n");
        skipEmptyInventorySlots = config.getBoolean("Skip Empty Inventory Slots", HOTBAR_CYCLING_CATEGORY, true,
            "\nWhether to skip over empty inventory slots when scrolling through them.\n\n");
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

        enablePlayerInventorySound = config.getBoolean("Enable Player Inventory Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when opening the player inventory.\n\n");
        enableSwitchHotbarSlotSound = config.getBoolean("Enable Switch Hotbar Slot Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when changing the current hotbar slot.\n\n");
        enableHotbarCycleSound = config.getBoolean("Enable Hotbar Cycle Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when cycling through the hotbar.\n\n");
        enableAutoRefillSound = config.getBoolean("Enable Auto-Refill Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when an auto-refill occurs.\n\n");
        enableBarrelSealSound = config.getBoolean("Enable Chest Closing Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when closing a chest.\n\n");
        enableBarrelSealSound = config.getBoolean("Enable Barrel Seal Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when sealing or unsealing a barrel.\n\n");
        enableBarrelEmptySound = config.getBoolean("Enable Barrel Empty Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when emptying a barrel.\n\n");
        enableBarrelItemSoakSound = config.getBoolean("Enable Barrel Soaking Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when placing items in a barrel full of liquid.\n\n");
        enableLargeVesselSealSound = config.getBoolean("Enable Large Vessel Seal Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when sealing or unsealing a vessel.\n\n");
        enableLargeVesselEmptySound = config.getBoolean("Enable Large Vessel Empty Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when emptying a large vessel.\n\n");
        enableLargeVesselItemSoakSound = config.getBoolean("Enable Large Vessel Soaking Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when placing items in a large vessel full of liquid.\n\n");
        enableBeehiveOpenSound = config.getBoolean("Enable Beehive Open Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when opening and closing beehives.\n\n");
        enableBeehiveHoneySound = config.getBoolean("Enable Beehive Honey Sound", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when taking honey from a beehive.\n\n");
        enableMeleeWeaponSounds = config.getBoolean("Enable Melee Weapon Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when attacking with swords, maces, axes, knives, etc.\n\n");
        enableBowWeaponSounds = config.getBoolean("Enable Bow Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds while nocking an arrow.\n\n");
        enableBucketSounds = config.getBoolean("Enable Bucket Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when filling or emptying buckets.\n\n");
        enableFirepitSounds = config.getBoolean("Enable Firepit Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when near a burning firepit.\n\n");
        enableBloomerySounds = config.getBoolean("Enable Bloomery Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when near a lit bloomery.\n\n");
        enableBlastFurnaceSounds = config.getBoolean("Enable Blast Furnace Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when near a lit blast furnace.\n\n");
        //enableFishingRodSounds = config.getBoolean("Enable Fishing Rod Sounds", SOUNDS_CATEGORY, true,
        //        "\nWhether to play sounds when casting and reeling in with a fishing rod.\n\n");
        enableRopeSounds = config.getBoolean("Enable Rope Sounds", SOUNDS_CATEGORY, true,
                "\nWhether to play sounds when tying a rope to a fence, attach it to an animal, or when the rope snaps.\n\n");

        disableRottenFlesh = config.getBoolean("Disable Rotten Flesh", MOB_DROPS_CATEGORY, true,
                "\nWhether to disable rotten flesh drops from zombies. Rotten flesh has no in-game uses.\n\n");
        disableSpiderEyes = config.getBoolean("Disable Spider Eyes", MOB_DROPS_CATEGORY, true,
                "\nWhether to disable spider eye drops from spiders. Spider eyes have no in-game uses.\n\n");

        if (clientOnlyMode) {
            autoRefillBlocks = false;
            autoRefillWeapons = false;
            autoRefillTools = false;
            autoRefillFood = false;
            autoRefillMisc = false;
            autoStackFoodOnPickup = false;
            enableAutoRefillSound = false;
            allowQuickAccessQuiver = false;
            allowQuickAccessSack = false;
            allowQuickAccessVessel = false;
            allowQuickAccessHideBag = false;
            allowQuickAccessLeatherBag = false;
            disableSpiderEyes = false;
            disableRottenFlesh = false;
            enableHotbarCyclePreview = false;
        }

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
            List<IConfigElement> elements = new ArrayList<IConfigElement>();
            elements.add(new CategoryElement(CLIENT_ONLY_CATEGORY, "Client-Only Mode", false));
            elements.add(new CategoryElement(AUTO_REFILL_CATEGORY, "Auto Refill", true));
            elements.add(new CategoryElement(HOTBAR_CYCLING_CATEGORY, "Hotbar Cycling", true));
            elements.add(new CategoryElement(WALK_IN_INVENTORY_CATEGORY, "Walk in Inventory", false));
            elements.add(new CategoryElement(QUICK_ACCESS_CATEGORY, "Quick Access", true));
            elements.add(new CategoryElement(SOUNDS_CATEGORY, "Sounds", false));
            elements.add(new CategoryElement(MOB_DROPS_CATEGORY, "Useless Mob Drops", true));
            return elements;
        }
    }

    @SuppressWarnings("rawtypes")
    public static class CategoryElement extends DummyConfigElement.DummyCategoryElement {
        public boolean serverSide;

        public CategoryElement(String categoryName, String name, boolean serverSide) {
            super(name, "gui.config.category." + categoryName, getCategoryElements(categoryName), CategoryEntry.class);
            this.serverSide = serverSide;
        }

        @SuppressWarnings("unchecked")
        public static List<IConfigElement> getCategoryElements(String categoryName) {
            return new ConfigElement(Config.config.getCategory(categoryName)).getChildElements();
        }

        public boolean enabled() {
            return !serverSide || !Config.clientOnlyMode;
        }
    }

    public static class CategoryEntry extends GuiConfigEntries.CategoryEntry {
        CategoryElement element;

        public CategoryEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
            super(owningScreen, owningEntryList, configElement);

            if (configElement instanceof CategoryElement)
                element = (CategoryElement)configElement;
            else
                element = null;
        }

        @Override
        public boolean enabled() {
            if (element == null)
                return true;
            else
                return element.enabled();
        }

        @Override @SuppressWarnings("unchecked")
        protected GuiScreen buildChildScreen() {
            return new CustomCategoryGUI(owningScreen, configElement.getChildElements(), owningScreen.modID, owningScreen.configID,
                    owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                    owningScreen.allRequireMcRestart || configElement.requiresMcRestart(), owningScreen.title,
                    ((owningScreen.titleLine2 == null ? "" : owningScreen.titleLine2) + " > " + name));
        }

        public static class CustomCategoryGUI extends GuiConfig {
            public CustomCategoryGUI(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
                super(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
            }

            @Override
            public void actionPerformed(GuiButton button) {
                if (button.id == 2000) {
                    boolean flag = true;
                    try {
                        if ((configID != null || parentScreen instanceof GuiConfig) && (entryList.hasChangedEntry(true))) {
                            boolean requiresMcRestart = entryList.saveConfigElements();

                            if (Loader.isModLoaded(modID)) {
                                ConfigChangedEvent event = new ConfigChangedEvent.OnConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart);
                                FMLCommonHandler.instance().bus().post(event);
                                if (!event.getResult().equals(Event.Result.DENY))
                                    FMLCommonHandler.instance().bus().post(new ConfigChangedEvent.PostConfigChangedEvent(modID, configID, isWorldRunning, requiresMcRestart));

                                if (requiresMcRestart) {
                                    flag = false;
                                    mc.displayGuiScreen(new GuiMessageDialog(parentScreen, "fml.configgui.gameRestartTitle",
                                            new ChatComponentText(I18n.format("fml.configgui.gameRestartRequired")), "fml.configgui.confirmRestartMessage"));
                                }

                                if (this.parentScreen instanceof GuiConfig)
                                    ((GuiConfig)parentScreen).needsRefresh = true;
                            }
                        }
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                    }

                    if (flag)
                        mc.displayGuiScreen(parentScreen);
                }
                else
                    super.actionPerformed(button);
            }
        }
    }
}