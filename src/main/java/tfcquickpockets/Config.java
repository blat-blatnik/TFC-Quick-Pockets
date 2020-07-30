// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

package tfcquickpockets;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Set;

public class Config implements IModGuiFactory {

    public static Configuration config;

    public static boolean invertHotbarCycleDirection = false;
    public static float hotbarCyclePreviewTransparency = 0.6f;
    public static float hotbarCyclePreviewItemIconScale = 0.5f;
    public static float hotbarCyclePreviewHeight = 0.73f;
    public static int hotbarCyclePreviewXOffset = 0;
    public static int hotbarCyclePreviewYOffset = 0;
    public static int waterskinFixDelayTicks = 3;

    public static void initialize(String configDirectory) {
        if (config == null) {
            File path = new File(configDirectory + "/" + QuickPockets.ID + ".cfg");
            config = new Configuration(path);
            load();
        }
    }

    public static void load() {
        invertHotbarCycleDirection = config.getBoolean("Invert Scrolling Direction", Configuration.CATEGORY_GENERAL, false,
                "\nIf set to true, scrolling the mouse wheel up will move you *down* one inventory row, otherwise scrolling up will move you *up*.\n\n");
        hotbarCyclePreviewTransparency = config.getFloat("Hotbar Preview Transparency", Configuration.CATEGORY_GENERAL, 0.6f, 0.0f, 1.0f,
                "\nHow transparent the hotbar cycling preview slots will appear.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewItemIconScale = config.getFloat("Hotbar Preview Item Icon Scale", Configuration.CATEGORY_GENERAL, 0.5f, 0.0f, 1.0f,
                "\nHow big the hotbar cycling preview item icons will appear.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewHeight = config.getFloat("Hotbar Preview Height", Configuration.CATEGORY_GENERAL, 0.73f, 0.0f, 10.0f,
                "\nHow big the hotbar cycling preview slots will appear relative to the main hotbar slots.\n\nThis doesn't affect the current hotbar row, only the preview rows.\n\n");
        hotbarCyclePreviewXOffset = config.getInt("Hotbar Preview X Offset", Configuration.CATEGORY_GENERAL, 0, -10000, +10000,
                "\nWhere on the screen the hotbar cycle preview appears.\n\n");
        hotbarCyclePreviewYOffset = config.getInt("Hotbar Preview Y Offset", Configuration.CATEGORY_GENERAL, 0, -10000, +10000,
                "\nWhere on the screen the hotbar cycle preview appears.\n\n");
        waterskinFixDelayTicks = config.getInt("Waterskin Fix Delay Ticks", Configuration.CATEGORY_GENERAL, 3, 0, 20,
                "\nSince the waterskin fix might not always work depending on your exact setup, you can use this value to tweak it.\n\nLower values will try to fix the waterskin faster, but are less likely to work. Higher values have a higher chance to work but there might be a noticeable delay.\n\nSet this to 0 to disable the waterskin fix.\n\n");

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
                new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                QuickPockets.ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(Config.config.toString()));
        }
    }
}