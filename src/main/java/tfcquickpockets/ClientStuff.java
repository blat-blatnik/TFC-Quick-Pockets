package tfcquickpockets;

import com.dunk.tfc.Core.Player.BodyTempStats;
import com.dunk.tfc.Core.Player.PlayerInventory;
import com.dunk.tfc.Core.TFC_Core;
import com.dunk.tfc.Core.TFC_Textures;
import com.dunk.tfc.Core.TFC_Time;
import com.dunk.tfc.Entities.Mobs.EntityHorseTFC;
import com.dunk.tfc.Food.ItemFoodTFC;
import com.dunk.tfc.GUI.*;
import com.dunk.tfc.Handlers.Client.RenderOverlayHandler;
import com.dunk.tfc.ItemSetup;
import com.dunk.tfc.Items.*;
import com.dunk.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.dunk.tfc.Items.Tools.*;
import com.dunk.tfc.TerraFirmaCraft;
import com.dunk.tfc.TileEntities.*;
import com.dunk.tfc.api.Crafting.*;
import com.dunk.tfc.api.Enums.EnumDamageType;
import com.dunk.tfc.api.Enums.EnumFoodGroup;
import com.dunk.tfc.api.Enums.RuleEnum;
import com.dunk.tfc.api.Food;
import com.dunk.tfc.api.Interfaces.IFood;
import com.dunk.tfc.api.TFCFluids;
import com.dunk.tfc.api.TFCItems;
import com.dunk.tfc.api.TFCOptions;
import com.dunk.tfc.api.TileEntities.TEFireEntity;
import com.dunk.tfc.api.Util.Helper;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.item.ItemShears;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class ClientStuff extends ClientAndServerStuff {

    public static int HOTBAR_SIZE = InventoryPlayer.getHotbarSize();
    public static int INVENTORY_ROW_SIZE = 9;
    public static ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");

    public static Minecraft minecraft;
    public static RenderItem itemRenderer = new RenderItem();
    public static boolean cyclingThroughInventory; // if this is false, then we're cycling through the hotbar
    public static QuickSwapBinding currentSwapKey; // if this is null then we're not cycling at the moment
    public static int cycleHotbarSlotIndex = -1;
    public static int cycleLastSlotIndex = -1;
    public static ItemStack[] lastCycleInventorySlots = new ItemStack[HOTBAR_SIZE + 3 * INVENTORY_ROW_SIZE];
    public static int selectedSlotAtStartOfMouseInput = -1;
    public static int drinkSlotIndexPreUse = -1;
    public static int drinkSlotIndexPostUse = -1;
    public static Item drinkToFix = null;
    public static long drinkFixRemainingDelayTicks = -1;
    public static ItemStack[] inventoryDuringDrink = new ItemStack[HOTBAR_SIZE + 3 * INVENTORY_ROW_SIZE];
    public static Method syncCurrentPlayItem;
    public static Field remainingHighlightTicks;

    // --- for inventory GUIs ---

    public static Field xSizeLow;
    public static Field ySizeLow;
    public static Field xSize;
    public static Field ySize;
    public static Field upperChestInventory;
    public static Field lowerChestInventory;
    public static Field beehiveTE;
    public static Field firepitTE;
    public static Field basketTE;
    public static Field basketGuiTab;
    public static Field foodPrepTE;
    public static Field foodPrepGuiTab;
    public static Field barrelTE;
    public static Field barrelGuiTab;
    public static Field largeVesselTE;
    public static Field largeVesselGuiTab;
    public static Field fireTE;
    public static Field crucibleTE;
    public static Field forgeTE;
    public static Field blastFurnaceTE;
    public static Field anvilX;
    public static Field anvilY;
    public static Field anvilZ;
    public static Field sluiceTE;
    public static Field horse;

    public static KeyBinding swapHotbar = new KeyBinding("key.swapHotbar", Keyboard.KEY_LMENU,"key.categories.inventory");
    public static QuickSwapBinding[] quickSwaps = {
        // it seems like I can't use lambdas or I can't compile this thing.. oh well
        new QuickSwapBinding("key.swapToSword", 0, "key.categories.inventory", new Predicate<Item>() {
            @Override public boolean test(Item item) {
                return ((ItemCustomSword) item).damageType == EnumDamageType.SLASHING;
            }
        }, ItemCustomSword.class),
        new QuickSwapBinding("key.swapToMace", 0, "key.categories.inventory", new Predicate<Item>() {
            @Override public boolean test(Item item) {
                return ((ItemCustomSword) item).damageType == EnumDamageType.CRUSHING;
            }
        }, ItemCustomSword.class),
        new QuickSwapBinding("key.swapToRanged", 0, "key.categories.inventory", ItemBow.class, ItemJavelin.class),
        new QuickSwapBinding("key.swapToFood", 0, "key.categories.inventory", ItemFoodTFC.class),
        new QuickSwapBinding("key.swapToWater", 0, "key.categories.inventory", ItemDrink.class),
        new QuickSwapBinding("key.swapToPickaxe", 0, "key.categories.inventory", ItemPickaxe.class),
        new QuickSwapBinding("key.swapToProPick", 0, "key.categories.inventory", ItemProPick.class),
        new QuickSwapBinding("key.swapToAxe", 0, "key.categories.inventory", ItemAxe.class),
        new QuickSwapBinding("key.swapToSaw", 0, "key.categories.inventory", ItemCustomSaw.class),
        new QuickSwapBinding("key.swapToShovel", 0, "key.categories.inventory", ItemCustomShovel.class),
        new QuickSwapBinding("key.swapToChisel", 0, "key.categories.inventory", ItemChisel.class),
        new QuickSwapBinding("key.swapToHammer", 0, "key.categories.inventory", ItemHammer.class),
        new QuickSwapBinding("key.swapToScythe", 0, "key.categories.inventory", ItemCustomScythe.class),
        new QuickSwapBinding("key.swapToFireStarter", 0, "key.categories.inventory", ItemFirestarter.class, ItemFlintAndSteel.class),
        new QuickSwapBinding("key.swapToKnife", 0, "key.categories.inventory", ItemKnife.class),
        new QuickSwapBinding("key.swapToHoe", 0, "key.categories.inventory", ItemHoe.class),
        new QuickSwapBinding("key.swapToTrowel", 0, "key.categories.inventory", ItemTrowel.class),
        new QuickSwapBinding("key.swapToStaff", 0, "key.categories.inventory", ItemStaff.class),
        new QuickSwapBinding("key.swapToFishingRod", 0, "key.categories.inventory", ItemFishingRod.class),
    };

    @Override
    public void initializeConfig(FMLPreInitializationEvent event) {
        super.initializeConfig(event);
        Config.initialize(event.getModConfigurationDirectory().toString());
        FMLCommonHandler.instance().bus().register(new Config());
    }

    @Override
    public void initialize(FMLInitializationEvent event) {
        super.initialize(event);

        minecraft = Minecraft.getMinecraft();

        ClientRegistry.registerKeyBinding(swapHotbar);
        for (QuickSwapBinding swapBinding : quickSwaps) {
            ClientRegistry.registerKeyBinding(swapBinding);
        }

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        //TODO: Can do this through access transformers as well. It's more clean so it might be worth doing.
        syncCurrentPlayItem = loadMethod(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j");
        remainingHighlightTicks = loadField(GuiIngame.class, "remainingHighlightTicks", "field_92017_k");

        //HACK: Such ugliness.. you just had to set stuff to private Dunk didn't you :P
        //MAINTENANCE: If any of these names change they need to be updated here as well.
        xSize = loadField(GuiContainer.class, "xSize");
        ySize = loadField(GuiContainer.class, "ySize");
        xSizeLow = loadField(GuiInventoryTFC.class, "xSizeLow");
        ySizeLow = loadField(GuiInventoryTFC.class, "ySizeLow");
        upperChestInventory = loadField(GuiChestTFC.class, "upperChestInventory");
        lowerChestInventory = loadField(GuiChestTFC.class, "lowerChestInventory");
        beehiveTE = loadField(GuiBeehive.class, "beehiveTE");
        firepitTE = loadField(GuiFirepit.class, "firepitTE");
        basketTE = loadField(GuiBasket.class, "basketTE");
        basketGuiTab = loadField(GuiBasket.class, "guiTab");
        foodPrepTE = loadField(GuiFoodPrep.class, "foodPrepTE");
        foodPrepGuiTab = loadField(GuiFoodPrep.class, "guiTab");
        barrelTE = loadField(GuiBarrel.class, "barrelTE");
        barrelGuiTab = loadField(GuiBarrel.class, "guiTab");
        largeVesselTE = loadField(GuiLargeVessel.class, "vesselTE");
        largeVesselGuiTab = loadField(GuiLargeVessel.class, "guiTab");
        fireTE = loadField(GuiGrill.class, "fireTE");
        crucibleTE = loadField(GuiCrucible.class, "crucibleTE");
        forgeTE = loadField(GuiForge.class, "forgeTE");
        blastFurnaceTE = loadField(GuiBlastFurnace.class, "blastFurnaceTE");
        anvilX = loadField(GuiAnvil.class, "x");
        anvilY = loadField(GuiAnvil.class, "y");
        anvilZ = loadField(GuiAnvil.class, "z");
        sluiceTE = loadField(GuiSluice.class, "sluiceTE");
        horse = loadField(GuiScreenHorseInventoryTFC.class, "horse");
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void doQuickToolSwap(InputEvent.KeyInputEvent event) {
        for (QuickSwapBinding swapKey : quickSwaps) {
            if (swapKey.isPressed()) {
                cycleToolIntoHotbarSlot(swapKey);
            }
        }
    }

    @SubscribeEvent(priority= EventPriority.HIGHEST) @SuppressWarnings("unused")
    public void recordSelectedItemBeforeMouseScroll(MouseEvent event) { // this fires first, then minecraft code stuff, then onMouseScroll
        selectedSlotAtStartOfMouseInput = minecraft.thePlayer.inventory.currentItem;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST) @SuppressWarnings("unused")
    public void cycleThroughInventoryRowsOnMouseScroll(InputEvent.MouseInputEvent event) {
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            if (swapHotbar.getIsKeyPressed()) {
                if (Config.invertHotbarCycleDirection)
                    delta = -delta;

                sendCycleInventoryRowsToServer(delta < 0);
                setCurrentPlayerItem(selectedSlotAtStartOfMouseInput);
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void autoFillWhenPlayerDestroysItem(PlayerDestroyItemEvent event) {
        if (event.entityPlayer != null && event.original != null) {
            ItemStack original = event.original;

            ItemStack closestMatch = findClosestMatchingStack(original);
            if (closestMatch != null && closestMatch != original) {

                int indexOriginal = -1;
                int indexClosestMatch = -1;

                ItemStack[] inventory = event.entityPlayer.inventory.mainInventory;
                for (int i = 0; i < inventory.length; ++i) {
                    if (inventory[i] == original)
                        indexOriginal = i;
                    if (inventory[i] == closestMatch)
                        indexClosestMatch = i;
                }

                if (indexOriginal < 0)
                    indexOriginal = event.entityPlayer.inventory.currentItem;

                if (indexOriginal >= 0 && indexClosestMatch >= 0 && indexOriginal != indexClosestMatch) {
                    int slot1 = convertInventoryIndexToSlotIndex(indexOriginal);
                    int slot2 = convertInventoryIndexToSlotIndex(indexClosestMatch);
                    //sendSwapPlayerInventorySlotsToServer(slot1, slot2);
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void makeGUIsHaveQuickContainerAccess(GuiOpenEvent event) {
        if (event.gui instanceof GuiContainer) {
            stopCyclingToolsInHotbarSlot();

            GuiContainer gui = (GuiContainer) event.gui;
            Container slots = gui.inventorySlots;

            if (event.gui instanceof GuiInventoryTFC) {

                EntityPlayer player = minecraft.thePlayer;
                event.gui = new InventoryGUIWithFastBagAccess(minecraft.thePlayer);

            } else if (event.gui instanceof GuiBag) {

                event.gui = new ContainerGUIWithFastBagAccess(slots, 176, 85, GuiBag.texture);

            } else if (event.gui instanceof GuiVessel) {

                event.gui = new ContainerGUIWithFastBagAccess(slots, 176, 85, GuiVessel.texture);

            } else if (event.gui instanceof GuiQuiver) {

                event.gui = new ContainerGUIWithFastBagAccess(slots, 176, 49, GuiQuiver.texture);

            } else if (event.gui instanceof GuiChestTFC) {

                try {
                    IInventory upperInventory = (IInventory)upperChestInventory.get(gui);
                    IInventory lowerInventory = (IInventory)lowerChestInventory.get(gui);
                    event.gui = new ChestGUIWithFastBagAccess(slots, upperInventory, lowerInventory);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiBarrel) {

                try {
                    TEBarrel barrel = (TEBarrel)barrelTE.get(gui);
                    int tab = (Integer)barrelGuiTab.get(gui);
                    event.gui = new BarrelGUIWithFastBagAccess(slots, barrel, tab, minecraft.thePlayer);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiBasket) {

                try {
                    TEBasket basket = (TEBasket)basketTE.get(gui);
                    int tab = (Integer)basketGuiTab.get(gui);
                    event.gui = new BasketGUIWithFastBagAccess(slots, basket, tab, minecraft.thePlayer);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiLargeVessel) {

                try {
                    TEVessel vessel = (TEVessel)largeVesselTE.get(gui);
                    int tab = (Integer)largeVesselGuiTab.get(gui);
                    event.gui = new LargeVesselGUIWithFastBagAccess(slots, vessel, tab, minecraft.thePlayer);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiQuern) {

                event.gui = new QuernGUIWithFastBagAccess(slots);

            } else if (event.gui instanceof GuiBeehive) {

                try {
                    TEBeehive beehive = (TEBeehive)beehiveTE.get(gui);
                    event.gui = new BeehiveGUIWithFastBagAccess(slots, beehive, minecraft.thePlayer);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiFirepit) {

                try {
                    TEFirepit firepit = (TEFirepit)firepitTE.get(gui);
                    event.gui = new FirepitGUIWithFastBagAccess(slots, firepit);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiFoodPrep) {

                try {
                    TEFoodPrep foodPrep = (TEFoodPrep)foodPrepTE.get(gui);
                    int tab = (Integer)foodPrepGuiTab.get(gui);
                    event.gui = new FoodPrepGUIWithFastBagAccess(slots, foodPrep, tab);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiLogPile) {

                event.gui = new LogPileGUIWithFastBagAccess(slots);

            } else if (event.gui instanceof GuiHopper) {

                event.gui = new HopperGUIWithFastBagAccess(slots);

            } else if (event.gui instanceof GuiGrill) {

                try {
                    TEFireEntity fire = (TEFireEntity)fireTE.get(gui);
                    event.gui = new GrillGUIWithFastBagAccess(slots, fire);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiCrucible) {

                try {
                    TECrucible crucible = (TECrucible)crucibleTE.get(gui);
                    event.gui = new CrucibleGUIWithFastBagAccess(slots, crucible);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiForge) {

                try {
                    TEForge forge = (TEForge)forgeTE.get(gui);
                    event.gui = new ForgeGUIWithFastBagAccess(slots, forge);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiBlastFurnace) {

                try {
                    TEBlastFurnace furnace = (TEBlastFurnace)blastFurnaceTE.get(gui);
                    event.gui = new BlastFurnaceGUIWithFastBagAccess(slots, furnace);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiAnvil) {

                try {
                    int x = (Integer)anvilX.get(gui);
                    int y = (Integer)anvilY.get(gui);
                    int z = (Integer)anvilZ.get(gui);
                    TEAnvil anvil = ((GuiAnvil)event.gui).anvilTE;
                    event.gui = new AnvilGUIWithFastBagAccess(slots, anvil, x, y, z, minecraft.thePlayer);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiNestBox) {

                event.gui = new NestBoxGUIWithFastBagAccess(slots);

            } else if (event.gui instanceof GuiSluice) {

                try {
                    TESluice sluice = (TESluice)sluiceTE.get(gui);
                    event.gui = new SluiceGUIWithFastBagAccess(slots, sluice);
                } catch (Exception ignored) {}

            } else if (event.gui instanceof GuiScreenHorseInventoryTFC) {

                try {
                    EntityHorseTFC horseEntity = (EntityHorseTFC)horse.get(gui);
                    event.gui = new HorseGuiWithFastBagAccess(slots, horseEntity);
                } catch (Exception ignored) {}

            }
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH) @SuppressWarnings("unused")
    public void drawInventoryPreviewAndStopTFCFromDrawingGUI(RenderGameOverlayEvent.Pre event) {
        if (swapHotbar.getIsKeyPressed()) {
            int width = event.resolution.getScaledWidth();
            int height = event.resolution.getScaledHeight();
            switch (event.type) {
                case HOTBAR:
                    drawHotbarCyclePreview(width, height, event.partialTicks);

                    // disable item highlight text that would render over our beautiful hotbar preview
                    if (remainingHighlightTicks != null) {
                        try {
                            remainingHighlightTicks.set(minecraft.ingameGUI, 0);
                        } catch (Exception tooBad) {
                            tooBad.printStackTrace();
                        }
                    }

                    event.setCanceled(true);
                    break;
                case CROSSHAIRS:
                    drawCrosshairs(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
                    drawTemperatureGauge(width, height);
                    event.setCanceled(true);
                    break;
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void drawChatOverlayAboveInventoryPreview(RenderGameOverlayEvent.Chat event) {
        if (swapHotbar.getIsKeyPressed()) {
            event.posY -= 32;
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void recordWaterskinInventorySlotOnFill(InputEvent.MouseInputEvent event) {
        final int RIGHT_MOUSE_BUTTON = 1;
        EntityPlayer player = minecraft.thePlayer;
        WorldClient world = minecraft.theWorld;

        if (Config.waterskinFixDelayTicks > 0 && player != null && world != null && player.inventory != null && Mouse.getEventButton() == RIGHT_MOUSE_BUTTON) {
            ItemStack[] inventory = player.inventory.mainInventory;
            int currentItemIndex = player.inventory.currentItem;
            ItemStack currentStack = inventory[currentItemIndex];
            if (currentStack != null && currentStack.getItem() == ItemSetup.waterskinEmpty) {

                MovingObjectPosition mop = Helper.getMovingObjectPositionFromPlayer(world, player, true, 4);
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    int x = mop.blockX;
                    int y = mop.blockY;
                    int z = mop.blockZ;
                    int flowX = x;
                    int flowY = y;
                    int flowZ = z;
                    switch(mop.sideHit) {
                        case 0:
                            flowY = y - 1;
                            break;
                        case 1:
                            flowY = y + 1;
                            break;
                        case 2:
                            flowZ = z - 1;
                            break;
                        case 3:
                            flowZ = z + 1;
                            break;
                        case 4:
                            flowX = x - 1;
                            break;
                        case 5:
                            flowX = x + 1;
                    }

                    if (!player.isSneaking() && TFC_Core.isFreshWater(world.getBlock(x, y, z)) || TFC_Core.isFreshWater(world.getBlock(flowX, flowY, flowZ))) {
                        int nextItemSlotIndex = player.inventory.getFirstEmptyStack();
                        if (nextItemSlotIndex >= 0) {
                            drinkSlotIndexPreUse = currentItemIndex;
                            drinkSlotIndexPostUse = nextItemSlotIndex;
                            drinkFixRemainingDelayTicks = Config.waterskinFixDelayTicks;
                            drinkToFix = ItemSetup.waterskin;
                        }
                    }
                }

            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void recordPlayerInventoryWhileDrinkingFromWaterskin(PlayerUseItemEvent.Tick event) {
        if (Config.waterskinFixDelayTicks > 0) {
            ItemStack[] inventory = event.entityPlayer.inventory.mainInventory;
            System.arraycopy(inventory, 0, inventoryDuringDrink, 0, inventory.length);
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void recordWaterskinInventorySlotOnDrink(PlayerUseItemEvent.Finish event) {
        if (Config.waterskinFixDelayTicks > 0 && event.item != null) {
            ItemStack itemStack = event.item;
            Item item = itemStack.getItem();
            if (item instanceof ItemDrink) {
                ItemStack[] items = event.entityPlayer.inventory.mainInventory;

                for (int i = 0; i < items.length; ++i) {
                    if (items[i] != null) {
                        if (items[i] == itemStack) {
                            drinkSlotIndexPreUse = i;
                        } else {
                            Item slotItem = items[i].getItem();
                            if (slotItem == item && items[i].animationsToGo == 5) {
                                drinkSlotIndexPostUse = i;
                                drinkToFix = items[i].getItem();
                            }
                        }
                    }
                }

                if (drinkSlotIndexPreUse >= 0 && drinkSlotIndexPostUse < 0) {
                    // this should only happen if the player fully empties out the waterskin
                    for (int i = 0; i < items.length; ++i) {
                        if (inventoryDuringDrink[i] == null && items[i] != null) {
                            drinkSlotIndexPostUse = i;
                            drinkToFix = ItemSetup.waterskinEmpty;
                            break;
                        }
                    }
                }

                if (drinkSlotIndexPreUse >= 0 && drinkSlotIndexPostUse >= 0) {
                    drinkFixRemainingDelayTicks = Config.waterskinFixDelayTicks;
                } else {
                    drinkSlotIndexPreUse = -1;
                    drinkSlotIndexPostUse = -1;
                    drinkToFix = null;
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void putWaterskinInProperSlotAfterUse(TickEvent.ClientTickEvent event) {
        if (Config.waterskinFixDelayTicks > 0 && event.phase == TickEvent.Phase.END && minecraft.thePlayer != null) {
            ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;
            if (drinkToFix != null && drinkSlotIndexPreUse >= 0 && drinkSlotIndexPostUse >= 0) {
                if (--drinkFixRemainingDelayTicks == 0) {
                    ItemStack preDrink = inventory[drinkSlotIndexPreUse];
                    ItemStack postDrink = inventory[drinkSlotIndexPostUse];
                    if (preDrink == null && postDrink != null && postDrink.getItem() == drinkToFix) {
                        int slot1 = convertInventoryIndexToSlotIndex(drinkSlotIndexPreUse);
                        int slot2 = convertInventoryIndexToSlotIndex(drinkSlotIndexPostUse);
                        sendSwapPlayerInventorySlotsToServer(slot1, slot2);
                        inventory[drinkSlotIndexPreUse].animationsToGo = 0;
                    }
                    drinkSlotIndexPreUse = -1;
                    drinkSlotIndexPostUse = -1;
                    drinkToFix = null;
                }
            }
        }
    }

    public static ItemStack findClosestMatchingStack(final ItemStack target) {
        ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;
        ItemStack[] sorted = new ItemStack[inventory.length];
        System.arraycopy(inventory, 0, sorted, 0, inventory.length);

        //TODO: This is for debugging purposes onlys
        int[] similarity = new int[inventory.length];
        for (int i = 0; i < inventory.length; ++i)
            similarity[i] = getStackSimilarity(target, inventory[i]);

        Arrays.sort(sorted, new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack stack1, ItemStack stack2) {
                int similarity1 = getStackSimilarity(target, stack1);
                int similarity2 = getStackSimilarity(target, stack2);
                return similarity2 - similarity1;
            }
        });

        ItemStack bestMatch = sorted[0];
        ItemStack secondBest = sorted[1];

        ItemStack closestMatch = bestMatch != target ? bestMatch : secondBest;
        if (getStackSimilarity(closestMatch, target) > 0)
            return closestMatch;
        else
            return null;
    }

    public static int getStackSimilarity(ItemStack a, ItemStack b) {
        if (a == null || b == null || a.getItem() == null || b.getItem() == null || (a.stackSize <= 0 && b.stackSize <= 0))
            return -1;
        else {
            Item item1 = a.getItem();
            Item item2 = b.getItem();
            ItemCategory category1 = ItemCategory.get(item1);
            ItemCategory category2 = ItemCategory.get(item2);
            //Class<? extends Item> class1 = item1.getClass();
            //Class<? extends Item> class2 = item2.getClass();

            int similarity = 0;
            if (item1 == item2 || item1.getUnlocalizedName().equals(item2.getUnlocalizedName()))
                similarity += 1;
            if (category1 == ItemCategory.OTHER && category2 == ItemCategory.OTHER && a.getItemDamage() != b.getItemDamage())
                similarity -= 1;
            if (category1.isSame(category2))
                similarity += 1;

            return similarity;
        }
    }

    //TODO: This can probably be done way more simply, by building a list to cycle through
    public static void cycleToolIntoHotbarSlot(QuickSwapBinding swapKey) {
        boolean canExecute =
                swapKey != null &&
                        minecraft != null &&
                        minecraft.thePlayer != null &&
                        minecraft.thePlayer.inventory != null &&
                        minecraft.thePlayer.inventory.mainInventory != null;

        if (canExecute) {
            InventoryPlayer inventory = minecraft.thePlayer.inventory;
            ItemStack[] inventorySlots = inventory.mainInventory;
            int selectedSlot = inventory.currentItem;

            boolean cycleAgain = true;
            while(cycleAgain) {
                cycleAgain = false;

                boolean startFromScratch =
                        swapKey != currentSwapKey ||
                                (cyclingThroughInventory && selectedSlot != cycleHotbarSlotIndex) ||
                                (!cyclingThroughInventory && selectedSlot != cycleLastSlotIndex) ||
                                !arrayShallowEquals(inventorySlots, lastCycleInventorySlots);

                if (startFromScratch) {
                    stopCyclingToolsInHotbarSlot();
                    currentSwapKey = swapKey;
                    cycleHotbarSlotIndex = selectedSlot;
                    cycleLastSlotIndex = selectedSlot;
                }

                if (cyclingThroughInventory) {
                    if (cycleLastSlotIndex < HOTBAR_SIZE) { // check if we just stated to cycle through inventory.
                        cycleLastSlotIndex = HOTBAR_SIZE - 1;
                    } else {
                        sendSwapPlayerInventorySlotsToServer(cycleHotbarSlotIndex + 3 * INVENTORY_ROW_SIZE, cycleLastSlotIndex - HOTBAR_SIZE);
                    }
                    int nextIndex = swapKey.findMatchingSlotIndexInInventory(inventorySlots, cycleLastSlotIndex + 1);
                    if (nextIndex < 0) {
                        // Cycle through hotbar next time through.
                        cyclingThroughInventory = false;
                        cycleLastSlotIndex = cycleHotbarSlotIndex;
                    } else {
                        sendSwapPlayerInventorySlotsToServer(cycleHotbarSlotIndex + 3 * INVENTORY_ROW_SIZE, nextIndex - HOTBAR_SIZE);
                        cycleLastSlotIndex = nextIndex;
                    }
                } else {
                    int nextIndex = swapKey.findMatchingSlotIndexInHotbar(inventorySlots, cycleLastSlotIndex);
                    if (nextIndex < 0 || (nextIndex >= cycleHotbarSlotIndex && nextIndex < cycleLastSlotIndex)) {
                        // Cycle through inventory next time through.
                        setCurrentPlayerItem(cycleHotbarSlotIndex);
                        int nextIndexInInventory = swapKey.findMatchingSlotIndexInInventory(inventorySlots, HOTBAR_SIZE - 1);
                        if (nextIndexInInventory != -1) {
                            cyclingThroughInventory = true;
                            if (cycleLastSlotIndex == cycleHotbarSlotIndex) {
                                // If this is the first time we try to cycle, and we didn't find anything
                                // then just immediately try to cycle through the inventory.
                                cycleAgain = true;
                            } else {
                                cycleLastSlotIndex = cycleHotbarSlotIndex;
                            }
                        } else {
                            cycleLastSlotIndex = cycleHotbarSlotIndex;
                        }
                    } else {
                        setCurrentPlayerItem(nextIndex);
                        cycleLastSlotIndex = nextIndex;
                    }
                }

                System.arraycopy(inventorySlots, 0, lastCycleInventorySlots, 0, inventorySlots.length);

            }
        }
    }

    public static void stopCyclingToolsInHotbarSlot() {
        cyclingThroughInventory = false;
        currentSwapKey = null;
        cycleHotbarSlotIndex = -1;
        cycleLastSlotIndex = -1;
    }

    public static void setCurrentPlayerItem(int index) {
        minecraft.thePlayer.inventory.currentItem = index;

        if (syncCurrentPlayItem != null) {
            try {
                syncCurrentPlayItem.invoke(minecraft.playerController);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // the indices start from the topmost row of the inventory, and *not* the hotbar, and the last row of indices are for the hotbar
    public static void sendSwapPlayerInventorySlotsToServer(int slot1Index, int slot2Index) {
        // I don't think this is possible to get working client side correctly - this whole mod has to be server
        // side so that this can work.. stupid minecraft.
        // I used to do this by calling minecraft.playerController.windowClick() and that works for 99% of cases
        // BUT whenever you had an item like hot food in your inventory it would tick so fast and break this - leading
        // to item duplication and many many other problems..

        if (slot1Index != slot2Index) {
            int slot1 = slot1Index + 9;
            int slot2 = slot2Index + 9;
            swapPlayerInventorySlots(minecraft.thePlayer, slot1, slot2);
            network.sendToServer(new SwapInventorySlotsPacket(slot1, slot2));
        }
    }

    public static void sendCycleInventoryRowsToServer(boolean cycleUp) {
        cyclePlayerInventoryRows(minecraft.thePlayer, cycleUp);
        ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;

        for (int i = 0; i < HOTBAR_SIZE; ++i) {
            ItemStack stack = inventory[i];
            if (stack != null)
                stack.animationsToGo = 3;
        }

        network.sendToServer(new CycleInventoryRowsPacket(cycleUp));
    }

    public static int convertInventoryIndexToSlotIndex(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= HOTBAR_SIZE + 3 * INVENTORY_ROW_SIZE)
            return inventoryIndex;
        else if (inventoryIndex < HOTBAR_SIZE)
            return inventoryIndex + 3 * INVENTORY_ROW_SIZE;
        else
            return inventoryIndex - HOTBAR_SIZE;
    }

    public static int convertSlotIndexToInventoryIndex(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= HOTBAR_SIZE + 3 * INVENTORY_ROW_SIZE)
            return inventoryIndex;
        else if (inventoryIndex >= 3 * INVENTORY_ROW_SIZE)
            return inventoryIndex - 3 * INVENTORY_ROW_SIZE;
        else
            return inventoryIndex + HOTBAR_SIZE;
    }

    public static void drawHotbarCyclePreview(int screenWidth, int screenHeight, float partialTicks) {
        float alpha = Config.hotbarCyclePreviewTransparency;
        float xOffset = Config.hotbarCyclePreviewXOffset;
        float yOffset = -(Config.hotbarCyclePreviewYOffset + 26);
        float itemScale = Config.hotbarCyclePreviewItemIconScale;
        float height = Math.round(22 * Config.hotbarCyclePreviewHeight);

        drawHotbar(3, xOffset, yOffset - height, height, itemScale, alpha, screenWidth, screenHeight, partialTicks);
        drawHotbar(0, xOffset, yOffset + 00, 22, 1, 1, screenWidth, screenHeight, partialTicks);
        drawHotbar(1, xOffset, yOffset + 22, height, itemScale, alpha, screenWidth, screenHeight, partialTicks);
    }

    // adapted from GuiIngameForge.renderHotbar
    public static void drawHotbar(int hotbarRow, float xOffset, float yOffset, float height, float itemScale, float alpha, int screenWidth, int screenHeight, float partialTicks) {
        minecraft.mcProfiler.startSection("actionBar");

        int selectedSlotIndex = minecraft.thePlayer.inventory.currentItem;
        boolean hasSelectedItem = hotbarRow == 0;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1, 1, 1, alpha);
        minecraft.renderEngine.bindTexture(WIDGETS);

        drawTexturedRect256(xOffset + screenWidth / 2.0f - 91, yOffset + screenHeight - 22, 182, height, 0, 0, 182, 22);
        if (hasSelectedItem) {
            drawTexturedRect256(xOffset + screenWidth / 2.0f - 91 - 1 + selectedSlotIndex * 20, yOffset + screenHeight - 22 - 1, 24, 22, 0, 22);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < HOTBAR_SIZE; ++i) {
            float x = xOffset + screenWidth / 2.0f - 90 + i * 20 + 2;
            float y = yOffset + screenHeight - 16 - 3 - 10 * (1 - itemScale);
            drawInventorySlot(i + hotbarRow * INVENTORY_ROW_SIZE, x, y, itemScale, itemScale, partialTicks);
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        minecraft.mcProfiler.endSection();
    }

    // adapted from Gui.renderInventorySlot
    public static void drawInventorySlot(int slotIndex, float x, float y, float scaleX, float scaleY, float partialTicks) {
        ItemStack itemstack = minecraft.thePlayer.inventory.mainInventory[slotIndex];

        if (itemstack != null) {
            float animationTicksLeft = (float)itemstack.animationsToGo - partialTicks;

            GL11.glPushMatrix();
            GL11.glTranslatef(x + 8, y + 12, 0);
            if (animationTicksLeft > 0) {
                float warp = 1 + animationTicksLeft / 5;
                GL11.glScalef(scaleX / warp, scaleY * (warp + 1) / 2, 1);
            } else {
                GL11.glScalef(scaleX, scaleY, 1);
            }
            GL11.glTranslatef(-(x + 8), -(y + 12), 0);
            GL11.glColor4f(1, 1, 1, 0);
            TextureManager textureManager = minecraft.getTextureManager();
            itemRenderer.renderItemAndEffectIntoGUI(minecraft.fontRenderer, textureManager, itemstack, Math.round(x), Math.round(y + 1));
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glScalef(scaleX, scaleY, 1);
            int overlayX = (int)(x / scaleX + (1 - scaleX) * 20);
            int overlayY = (int)((y + 1) / scaleY + (1 - scaleY) * 24);
            itemRenderer.renderItemOverlayIntoGUI(minecraft.fontRenderer, textureManager, itemstack, overlayX, overlayY);
            GL11.glPopMatrix();
        }
    }

    // adapted from GuiIngameForge.renderCrosshairs
    public static void drawCrosshairs(int width, int height) {
        minecraft.getTextureManager().bindTexture(Gui.icons);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
        minecraft.ingameGUI.drawTexturedModalRect(width / 2 - 7, height / 2 - 7, 0, 0, 16, 16);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_BLEND);
    }

    // adapted from RenderOverlayHandler.render
    public static void drawTemperatureGauge(int width, int height) {
        int tempGaugeLeft = width - 20;
        int tempGaugeTop = height - 83;
        TFC_Core.bindTexture(RenderOverlayHandler.temperature);
        drawTexturedRect256(tempGaugeLeft - 1, tempGaugeTop - 1, 7, 65, 6, 0);
        int[] resistances = new int[2];
        BodyTempStats tempStats = TFC_Core.getBodyTempStats(minecraft.thePlayer);
        int timeRemaining = tempStats.timeRemaining;
        int hotZones;
        int normalZones;
        int coldZones;
        int zoneDeltaY;
        if (timeRemaining > -1) {
            hotZones = timeRemaining % 60;
            normalZones = hotZones % 10;
            coldZones = hotZones / 10;
            zoneDeltaY = timeRemaining / 60;
            minecraft.fontRenderer.drawString(zoneDeltaY + ":" + coldZones + normalZones, tempGaugeLeft - 4, tempGaugeTop + 70, Color.white.getRGB());
        }

        TFC_Core.bindTexture(RenderOverlayHandler.temperature);
        resistances[0] = Math.min(tempStats.heatResistance, 5);
        resistances[1] = Math.min(tempStats.coldResistance, 14);
        hotZones = 4 - resistances[0];
        normalZones = 1 + resistances[0] + resistances[1];
        coldZones = 20 - hotZones - normalZones - 2;
        zoneDeltaY = 2;

        int temp;
        for (temp = 0; temp < hotZones; ++temp) {
            drawTexturedRect256(tempGaugeLeft + 1, tempGaugeTop + zoneDeltaY, 3, 2, 24, 14);
            zoneDeltaY += 3;
        }

        if (resistances[0] < 5) {
            drawTexturedRect256(tempGaugeLeft + 1, tempGaugeTop + zoneDeltaY, 3, 2, 24, 11);
            zoneDeltaY += 3;
        }

        for(temp = 0; temp < normalZones; ++temp) {
            drawTexturedRect256(tempGaugeLeft + 1, tempGaugeTop + zoneDeltaY, 3, 2, 24, 8);
            zoneDeltaY += 3;
        }

        if (resistances[1] < 14) {
            drawTexturedRect256(tempGaugeLeft + 1, tempGaugeTop + zoneDeltaY, 3, 2, 24, 20);
            zoneDeltaY += 3;
        }

        for(temp = 0; temp < coldZones; ++temp) {
            drawTexturedRect256(tempGaugeLeft + 1, tempGaugeTop + zoneDeltaY, 3, 2, 24, 17);
            zoneDeltaY += 3;
        }

        temp = (int)tempStats.ambientTemperature;
        int tempDelta;
        if (temp >= 45) {
            tempDelta = 0;
        } else if (temp < -45) {
            tempDelta = 19;
        } else {
            tempDelta = 9 - temp / 5;
        }

        drawTexturedRect256(tempGaugeLeft - 1, tempGaugeTop + 1 + tempDelta * 3, 7, 5, 22, 2);
        if (tempDelta <= hotZones) {
            drawTexturedRect256(tempGaugeLeft - 1, tempGaugeTop - 1, 7, 65, 33, 0);
        } else if (tempDelta >= normalZones + hotZones + 1) {
            drawTexturedRect256(tempGaugeLeft - 1, tempGaugeTop - 1, 7, 65, 40, 0);
        }
    }

    public static void drawTexturedRect256(float x, float y, float width, float height, float u, float v) {
        drawTexturedRect256(x, y, width, height, u, v, width, height);
    }

    // adapted from Gui.drawTexturedModalRect
    public static void drawTexturedRect256(float x, float y, float width, float height, float u, float v, float texWidth, float texHeight) {
        final float UV_SCALE = 1 / 256.0f;
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(u * UV_SCALE, v * UV_SCALE);
        GL11.glVertex2f(x, y);

        GL11.glTexCoord2f(u * UV_SCALE, (v + texHeight) * UV_SCALE);
        GL11.glVertex2f(x, y + height);

        GL11.glTexCoord2f((u + texWidth) * UV_SCALE, (v + texHeight) * UV_SCALE);
        GL11.glVertex2f(x + width, y + height);

        GL11.glTexCoord2f((u + texWidth) * UV_SCALE, v * UV_SCALE);
        GL11.glVertex2f(x + width, y);

        GL11.glEnd();
    }

    public static int findFirstFreeHotbarSlotIndex() {
        ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;
        for (int slot = 0; slot < HOTBAR_SIZE; ++slot)
            if (inventory[slot] == null)
                return slot;
        return -1;
    }

    public static boolean doQuickAccessOnRightClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {

        if (rightClick != 0 && slot != null) {
            if (slot.inventory == minecraft.thePlayer.inventory) { //ASSUMPTION: bags and such can never be inside another bag

                ItemStack clickedStack = slot.getStack();
                if (clickedStack != null) {

                    Item clickedItem = clickedStack.getItem();
                    boolean quickAccessBlockedByConfig =
                            (clickedItem == ItemSetup.leatherBag && !Config.allowQuickAccessLeatherBag) ||
                                    (clickedItem == ItemSetup.hideBag && !Config.allowQuickAccessHideBag) ||
                                    (clickedItem == ItemSetup.potterySmallVessel && !Config.allowQuickAccessVessel) ||
                                    (clickedItem == ItemSetup.burlapSack && !Config.allowQuickAccessSack) ||
                                    (clickedItem == ItemSetup.quiver && !Config.allowQuickAccessQuiver);

                    //ASSUMPTION: all ItemPotterySmallVessels and ItemQuivers have inventories
                    if (!quickAccessBlockedByConfig && clickedItem instanceof ItemPotterySmallVessel || clickedItem instanceof ItemQuiver) {

                        EntityPlayer player = minecraft.thePlayer;
                        if (player.inventory.getItemStack() == null) {

                            player.closeScreen();
                            int inventorySlotIndex = slot.getSlotIndex();

                            int hotbarSlot;
                            if (inventorySlotIndex >= 0 && inventorySlotIndex < HOTBAR_SIZE)
                                hotbarSlot = inventorySlotIndex;
                            else {
                                int freeHotbarSlot = findFirstFreeHotbarSlotIndex(); // 0..8
                                if (freeHotbarSlot >= 0)
                                    hotbarSlot = freeHotbarSlot;
                                else
                                    hotbarSlot = (inventorySlotIndex - HOTBAR_SIZE) % INVENTORY_ROW_SIZE;

                                int hotbarSlotInInventory = hotbarSlot + 3 * INVENTORY_ROW_SIZE;
                                int playerInventorySlotIndex;
                                if (inventorySlotIndex >= 0 && inventorySlotIndex < HOTBAR_SIZE)
                                    playerInventorySlotIndex = inventorySlotIndex + 3 * INVENTORY_ROW_SIZE;
                                else
                                    playerInventorySlotIndex = inventorySlotIndex - HOTBAR_SIZE;

                                sendSwapPlayerInventorySlotsToServer(playerInventorySlotIndex, hotbarSlotInInventory);
                            }

                            setCurrentPlayerItem(hotbarSlot);

                            ItemStack containerItem = player.inventory.mainInventory[hotbarSlot];
                            if (containerItem != null && containerItem.getItem() == clickedItem) { // sanity check
                                int mouseX = Mouse.getX();
                                int mouseY = Mouse.getY();
                                minecraft.playerController.sendUseItem(player, player.worldObj, containerItem);
                                Mouse.setCursorPosition(mouseX, mouseY);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static Field loadField(Class<?> type, String fieldName) {
        return loadField(type, fieldName, fieldName);
    }

    public static Field loadField(Class<?> type, String fieldName, String obfuscatedName) {
        try {
            Field field = type.getDeclaredField(obfuscatedName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            if (!fieldName.equals(obfuscatedName)) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (Exception ignored) {}
            }
            System.err.printf("Couldn't access '%s.%s' through reflection. TFC+ Quick Pockets might not work properly without it!", type, fieldName);
            return null;
        }
    }

    public static Method loadMethod(Class<?> type, String methodName, String obfuscatedName, Class<?>... methodParams) {
        try {
            Method method = type.getDeclaredMethod(obfuscatedName, methodParams);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            if (!methodName.equals(obfuscatedName)) {
                try {
                    Method method = type.getDeclaredMethod(methodName, methodParams);
                    method.setAccessible(true);
                    return method;
                } catch (Exception ignored) {}
            }
            e.printStackTrace();
            System.err.printf("Couldn't access '%s.%s()' through reflection. TFC+ Quick Pockets might not work properly without it!", type, methodName);
            return null;
        }
    }

    public static <T> boolean arrayShallowEquals(T[] a, T[] b) {
        if (a.length != b.length)
            return false;
        else {
            for (int i = 0; i < a.length; ++i)
                if (a[i] != b[i])
                    return false;
            return true;
        }
    }

    public enum ItemCategory {
        NONE,
        SWORD,
        MACE,
        STAFF,
        BOW,
        JAVELIN,
        FOOD,
        DRINK,
        AXE,
        SAW,
        HOE,
        PICKAXE,
        PRO_PICK,
        KNIFE,
        CHISEL,
        HAMMER,
        SHOVEL,
        SCYTHE,
        TROWEL,
        SHEARS,
        FIRE_STARTER,
        FISHING_ROD,
        SEEDS,
        ROCK,
        ROCK_FLAKE,
        CLAY,
        STRAW,
        LOG,
        CONTAINER,
        OTHER;

        public static ItemCategory get(Item item) {
            if (item == null)
                return ItemCategory.NONE;
            else  if (item instanceof ItemCustomSword) {
                    if (((ItemCustomSword)item).damageType == EnumDamageType.CRUSHING)
                        return ItemCategory.MACE;
                    else
                        return ItemCategory.SWORD;
            } else if (item instanceof ItemCustomBow) {
                return ItemCategory.BOW;
            } else if (item instanceof ItemJavelin) {
                return ItemCategory.JAVELIN;
            } else if (item instanceof ItemFoodTFC || item instanceof ItemHoneyBowl) {
                return ItemCategory.FOOD;
            } else if (item instanceof ItemDrink) {
                return ItemCategory.DRINK;
            } else if (item instanceof ItemPickaxe) {
                return ItemCategory.PICKAXE;
            } else if (item instanceof ItemProPick) {
                return ItemCategory.PRO_PICK;
            } else if (item instanceof ItemCustomSaw) {
                return ItemCategory.SAW;
            } else if (item instanceof ItemAxe) {
                return ItemCategory.AXE;
            } else if (item instanceof ItemCustomShovel) {
                return ItemCategory.SHOVEL;
            } else if (item instanceof ItemPotterySmallVessel || item instanceof ItemQuiver) {
                return ItemCategory.CONTAINER;
            } else if (item instanceof ItemChisel) {
                return ItemCategory.CHISEL;
            } else if (item instanceof ItemHammer) {
                return ItemCategory.HAMMER;
            } else if (item instanceof ItemKnife) {
                return ItemCategory.KNIFE;
            } else if (item instanceof ItemCustomScythe) {
                return ItemCategory.SCYTHE;
            } else if (item instanceof ItemTrowel) {
                return ItemCategory.TROWEL;
            } else if (item instanceof ItemStaff) {
                return ItemCategory.STAFF;
            } else if (item instanceof ItemFishingRod) {
                return ItemCategory.FISHING_ROD;
            } else if (item instanceof ItemFirestarter || item instanceof ItemFlintAndSteel) {
                return ItemCategory.FIRE_STARTER;
            } else if (item instanceof ItemStraw) {
                return ItemCategory.STRAW;
            } else if (item instanceof ItemShears) {
                return ItemCategory.SHEARS;
            } else if (item == ItemSetup.stoneFlake) {
                return ItemCategory.ROCK_FLAKE;
            } else if (item instanceof ItemLogs || item instanceof ItemThickLogs) {
                return ItemCategory.LOG;
            } else if (item instanceof ItemCustomSeeds) {
                return ItemCategory.SEEDS;
            } else if (item instanceof ItemClay) {
                return ItemCategory.CLAY;
            } else if (item instanceof ItemLooseRock || item == ItemSetup.flatRock) {
                return ItemCategory.ROCK;
            } else if (item instanceof ItemHoe) {
                return ItemCategory.HOE;
            } else {
                return ItemCategory.OTHER;
            }
        }

        public boolean isSame(ItemCategory other) {
            if (this == NONE || other == NONE || this == OTHER || other == OTHER)
                return false;
            else
                return this == other;
        }

        public boolean isTool() {
            switch (this) {
                case FOOD:
                case DRINK:
                case SWORD:
                case MACE:
                case STAFF:
                case BOW:
                case JAVELIN:
                case AXE:
                case SAW:
                case HOE:
                case PICKAXE:
                case PRO_PICK:
                case KNIFE:
                case CHISEL:
                case HAMMER:
                case SHOVEL:
                case SCYTHE:
                case TROWEL:
                case SHEARS:
                case FIRE_STARTER:
                case FISHING_ROD:
                    return true;
                default:
                case NONE:
                case OTHER:
                case STRAW:
                case LOG:
                case ROCK:
                case ROCK_FLAKE:
                case CONTAINER:
                case SEEDS:
                case CLAY:
                    return false;
            }
        }

        public boolean isBlock() {
            switch (this) {
                case NONE:
                case OTHER:
                case STRAW:
                case LOG:
                case ROCK:
                case ROCK_FLAKE:
                case CONTAINER:
                case SEEDS:
                case CLAY:
                    return true;
                case FOOD:
                case DRINK:
                case SWORD:
                case MACE:
                case STAFF:
                case BOW:
                case JAVELIN:
                case AXE:
                case SAW:
                case HOE:
                case PICKAXE:
                case PRO_PICK:
                case KNIFE:
                case CHISEL:
                case HAMMER:
                case SHOVEL:
                case SCYTHE:
                case TROWEL:
                case SHEARS:
                case FIRE_STARTER:
                case FISHING_ROD:
                default:
                    return false;
            }
        }
    }

    public static class QuickSwapBinding extends KeyBinding {
        public final Class<? extends Item>[] categories;
        public final Predicate<Item> filter;

        public QuickSwapBinding(String name, int keycode, String category, Predicate<Item> additionalFilter, Class<? extends Item>... swapCategories) {
            super(name, keycode, category);
            categories = swapCategories;
            filter = additionalFilter;
        }

        public QuickSwapBinding(String name, int keycode, String category, Class<? extends Item>... swapCategories) {
            this(name, keycode, category, null, swapCategories);
        }

        // startIdx will *not* be checked
        public int findMatchingSlotIndexInHotbar(ItemStack[] slots, int startIdx) {
            int minIdx = 0;
            int maxIdx = HOTBAR_SIZE - 1;
            for (int i = startIdx + 1; i != startIdx; i = (i >= maxIdx) ? minIdx : i + 1) { // this loop wraps around
                if (slotMatches(slots[i])) {
                    return i;
                }
            }
            return -1;
        }

        public int findMatchingSlotIndexInInventory(ItemStack[] slots, int startIdx) {
            int maxIdx = slots.length - 1;
            for (int i = startIdx; i <= maxIdx; ++i) {
                if (slotMatches(slots[i])) {
                    return i;
                }
            }
            return -1;
        }

        public boolean slotMatches(ItemStack slot) {
            if (slot != null) {
                for (Class<?> category : categories) {
                    Item item = slot.getItem();
                    if (category.isInstance(item)) {
                        if (filter == null || filter.test(item)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    // can't use java 1.7 so we have to define our own
    public interface Predicate<T> {
        boolean test(T item);
    }

    public static class InventoryGUIWithFastBagAccess extends GuiInventoryTFC {
        public InventoryGUIWithFastBagAccess(EntityPlayer player) {
            super(player);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float mystery, int mouseX, int mouseY) {
            if (xSizeLow == null || ySizeLow == null) {
                super.drawGuiContainerBackgroundLayer(mystery, mouseX, mouseY);
            } else {

                try {
                    GL11.glColor4f(1, 1, 1, 1);
                    if (player.getEntityData().hasKey("craftingTable")) {
                        TFC_Core.bindTexture(UPPER_TEXTURE);
                    } else {
                        TFC_Core.bindTexture(UPPER_TEXTURE_2X2);
                    }

                    float xsl = (Float)xSizeLow.get(this);
                    float ysl = (Float)ySizeLow.get(this);
                    int k = guiLeft;
                    int l = guiTop;

                    this.drawTexturedModalRect(k, l, 0, 0, xSize, 86);

                    // --- player head rotation fix ---
                    // I just added +52 to horizontalHeadRotation. I don't know if that's the exact number but it seems to work.

                    // drawPlayerModel(x, y, scale, horizontalHeadRotation, verticalHeadRotation, player) ???
                    drawPlayerModel(k + 51 + 54, l + 75, 30, k + 51 - xsl + 52, l + 75 - 50 - ysl, player);

                    PlayerInventory.drawInventory(this, width, height, ySize - PlayerInventory.invYSize);
                    GL11.glColor4f(1, 1, 1, 1);

                } catch (Exception e) {
                    e.printStackTrace();
                    super.drawGuiContainerBackgroundLayer(mystery, mouseX, mouseY);
                }

            }
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }
    }

    public static class ContainerGUIWithFastBagAccess extends GuiContainerTFC {

        public ResourceLocation texture;

        public ContainerGUIWithFastBagAccess(Container container, int xSize, int ySize, ResourceLocation texture) {
            super(container, xSize, ySize);
            this.texture = texture;
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(texture);
        }

        @Override
        public boolean checkHotbarKeys(int par1) {
            if (this.mc.thePlayer.inventory.currentItem != par1 - 2) {
                super.checkHotbarKeys(par1);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }
    }

    public static class ChestGUIWithFastBagAccess extends GuiContainer {

        public static ResourceLocation TEXTURE = new ResourceLocation("terrafirmacraftplus", "textures/gui/gui_chest.png");

        public IInventory upperChestInventory;
        public IInventory lowerChestInventory;
        public int inventoryRows;

        public ChestGUIWithFastBagAccess(Container chestContainer, IInventory upperInventory, IInventory lowerInventory) {
            super(chestContainer);

            upperChestInventory = upperInventory;
            lowerChestInventory = lowerInventory;
            allowUserInput = false;
            short var3 = 222;
            int var4 = var3 - 108;
            inventoryRows = lowerChestInventory.getSizeInventory() / 9;
            ySize = var4 + inventoryRows * 18;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
            TFC_Core.bindTexture(TEXTURE);
            GL11.glColor4f(1, 1, 1, 1);
            int var5 = (width - xSize) / 2;
            int var6 = (height - ySize) / 2;
            drawTexturedModalRect(var5, var6, 0, 0, xSize, inventoryRows * 18 + 17);
            drawTexturedModalRect(var5, var6 + inventoryRows * 18 + 17, 0, 126, xSize, 96);
            PlayerInventory.drawInventory(this, width, height, ySize - PlayerInventory.invYSize + 10);
        }
    }

    public static class QuernGUIWithFastBagAccess extends GuiContainerTFC {
        public QuernGUIWithFastBagAccess(Container quernContainer) {
            super(quernContainer, 176, 85);
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiQuern.texture);
        }
    }

    public static class BeehiveGUIWithFastBagAccess extends GuiContainerTFC {
        public TEBeehive beehiveTE;
        public EntityPlayer player;

        public BeehiveGUIWithFastBagAccess(Container beehiveContainer, TEBeehive beehive, EntityPlayer player) {
            super(beehiveContainer, 176, 85);

            this.player = player;
            beehiveTE = beehive;
            guiLeft = (width - 208) / 2;
            guiTop = (height - 198) / 2;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawTooltip(int mx, int my, String text) {
            drawHoveringText(Arrays.asList(text), mx, my + 15, fontRendererObj);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            drawGui(GuiBeehive.TEXTURE);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

        @Override
        public void drawCenteredString(FontRenderer fontrenderer, String s, int i, int j, int k) {
            fontrenderer.drawString(s, i - fontrenderer.getStringWidth(s) / 2, j, k);
        }
    }

    public static class FirepitGUIWithFastBagAccess extends GuiContainerTFC {
        public TEFirepit firepitTE;

        public FirepitGUIWithFastBagAccess(Container firepitContainer, TEFirepit firepit) {
            super(firepitContainer, 176, 85);
            firepitTE = firepit;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiFirepit.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            if (firepitTE != null) {
                int scale = firepitTE.getTemperatureScaled(49);
                drawTexturedModalRect(guiLeft + 30, guiTop + 65 - scale, 185, 31, 15, 6);
            }

        }
    }

    public static class BasketGUIWithFastBagAccess extends GuiContainerTFC {

        // GuiContainerTFC.TEXTURE has a few white pixels on the left column of slots - this is a fix.
        public static ResourceLocation TEXTURE = new ResourceLocation(QuickPockets.ID, "basket-gui-fixed.png");

        public TEBasket basketTE;
        public EntityPlayer player;
        public int guiTab;

        public BasketGUIWithFastBagAccess(Container basketContainer, TEBasket basket, int tab, EntityPlayer player) {
            super(basketContainer, 176, 85);

            this.player = player;
            basketTE = basket;
            guiLeft = (width - 208) / 2;
            guiTop = (height - 198) / 2;
            guiTab = tab;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawTooltip(int mx, int my, String text) {
            this.drawHoveringText(Arrays.asList(text), mx, my + 15, fontRendererObj);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            TFC_Core.bindTexture(TEXTURE);
            GL11.glColor4f(1, 1, 1, 0.5f);
            int w = (width - xSize) / 2;
            int h = (height - ySize) / 2;
            if (guiTab == 1) {
                drawTexturedModalRect(w, h, 0, 86, xSize, getShiftedYSize());
            }
            PlayerInventory.drawInventory(this, width, height, getShiftedYSize());
        }

        @Override
        public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

        @Override
        public void drawCenteredString(FontRenderer fontrenderer, String s, int i, int j, int k) {
            fontrenderer.drawString(s, i - fontrenderer.getStringWidth(s) / 2, j, k);
        }

        @Override
        public void drawScreen(int x, int y, float par3) {
            super.drawScreen(x, y, par3);
            if (basketTE.getSealed()) {
                GL11.glPushMatrix();
                if (guiTab == 0) {
                    Slot inputSlot = inventorySlots.getSlot(0);
                    drawSlotOverlay(inputSlot);
                } else if (guiTab == 1) {
                    for (int i = 0; i < basketTE.storage.length; ++i) {
                        Slot slot = inventorySlots.getSlot(i);
                        drawSlotOverlay(slot);
                    }
                }

                GL11.glPopMatrix();
            }
        }

        public void drawSlotOverlay(Slot slot) {
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            int xPos = slot.xDisplayPosition + guiLeft - 1;
            int yPos = slot.yDisplayPosition + guiTop - 1;
            GL11.glColorMask(true, true, true, false);
            drawGradientRect(xPos, yPos, xPos + 18, yPos + 18, 1979711487, 1979711487);
            GL11.glColorMask(true, true, true, true);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
        }
    }

    public static class FoodPrepGUIWithFastBagAccess extends GuiContainerTFC {
        public static ResourceLocation TEXTURE = new ResourceLocation("terrafirmacraftplus", "textures/gui/gui_foodprep.png");
        public TEFoodPrep foodPrepTE;
        public int guiTab;

        public FoodPrepGUIWithFastBagAccess(Container foodPrepContainer, TEFoodPrep foodPrep, int tab) {
            super(foodPrepContainer, 176, 85);

            foodPrepTE = foodPrep;
            guiTab = tab;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            bindTexture(TEXTURE);
            guiLeft = (width - xSize) / 2;
            guiTop = (height - ySize) / 2;
            if (guiTab == 0) {
                this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, getShiftedYSize());
            } else if (guiTab == 1) {
                this.drawTexturedModalRect(guiLeft, guiTop, 0, 86, xSize, getShiftedYSize());
            }

            PlayerInventory.drawInventory(this, width, height, getShiftedYSize());
        }

        @Override
        public void initGui() {
            super.initGui();
            this.buttonList.clear();
            if (this.guiTab == 0) {
                buttonList.add(new GuiButton(0, this.guiLeft + 74, this.guiTop + 44, 50, 20, TFC_Core.translate("gui.FoodPrep.CreateMeal")));
                buttonList.add((new FoodPrepGUIWithFastBagAccess.GuiFoodPrepTabButton(2, this.guiLeft + 36, this.guiTop - 21, 31, 21, this, new ItemStack(TFCItems.salad), TFC_Core.translate("gui.FoodPrep.Salad"))).setButtonCoord(31, 172));
                buttonList.add(new FoodPrepGUIWithFastBagAccess.GuiFoodPrepTabButton(1, this.guiLeft + 5, this.guiTop - 21, 31, 21, this, new ItemStack(TFCItems.sandwich), TFC_Core.translate("gui.FoodPrep.Sandwich")));
            } else if (this.guiTab == 1) {
                buttonList.add(new GuiButton(0, this.guiLeft + 74, this.guiTop + 44, 50, 20, TFC_Core.translate("gui.FoodPrep.CreateMeal")));
                buttonList.add(new FoodPrepGUIWithFastBagAccess.GuiFoodPrepTabButton(2, this.guiLeft + 36, this.guiTop - 21, 31, 21, this, new ItemStack(TFCItems.salad), TFC_Core.translate("gui.FoodPrep.Salad")));
                buttonList.add((new FoodPrepGUIWithFastBagAccess.GuiFoodPrepTabButton(1, this.guiLeft + 5, this.guiTop - 21, 31, 21, this, new ItemStack(TFCItems.sandwich), TFC_Core.translate("gui.FoodPrep.Sandwich"))).setButtonCoord(31, 172));
            }

        }

        @Override
        public void actionPerformed(GuiButton guibutton) {
            if (guibutton.id == 0) {
                foodPrepTE.actionCreate(minecraft.thePlayer);
            } else if (guibutton.id == 1 && guiTab != 0) {
                foodPrepTE.actionSwitchTab(0, minecraft.thePlayer);
            } else if (guibutton.id == 2 && guiTab != 1) {
                foodPrepTE.actionSwitchTab(1, minecraft.thePlayer);
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            if (guiTab == 0 && foodPrepTE.validateSandwich()) {
                ((GuiButton)buttonList.get(0)).enabled = true;
            } else if (this.guiTab == 1 && foodPrepTE.validateSalad()) {
                ((GuiButton)buttonList.get(0)).enabled = true;
            } else if (((GuiButton)this.buttonList.get(0)).enabled) {
                ((GuiButton)buttonList.get(0)).enabled = false;
            }

        }

        public static class GuiFoodPrepTabButton extends GuiButton {
            public FoodPrepGUIWithFastBagAccess screen;
            public ItemStack item;
            public int xPos;
            public int yPos = 172;
            public int xSize = 31;
            public int ySize = 24;

            public GuiFoodPrepTabButton(int index, int xPos, int yPos, int width, int height, FoodPrepGUIWithFastBagAccess gui, ItemStack is, String s) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.item = is;
            }

            public GuiFoodPrepTabButton(int index, int xPos, int yPos, int width, int height, FoodPrepGUIWithFastBagAccess gui, String s, int xp, int yp, int xs, int ys) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.xPos = xp;
                this.yPos = yp;
                this.xSize = xs;
                this.ySize = ys;
            }

            public FoodPrepGUIWithFastBagAccess.GuiFoodPrepTabButton setButtonCoord(int x, int y) {
                this.xPos = x;
                this.yPos = y;
                return this;
            }

            public void drawButton(Minecraft mc, int x, int y) {
                if (visible) {
                    TFC_Core.bindTexture(FoodPrepGUIWithFastBagAccess.TEXTURE);
                    GL11.glColor4f(1, 1, 1, 1);
                    RenderHelper.disableStandardItemLighting();
                    GL11.glDisable(2896);
                    GL11.glDisable(2929);
                    zLevel = 301;
                    drawTexturedModalRect(xPosition, yPosition, xPos, yPos, xSize, ySize);
                    field_146123_n = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glPushMatrix();
                    renderInventorySlot(item, xPosition + 8, yPosition + 5);
                    GL11.glPopMatrix();
                    mouseDragged(mc, x, y);
                    if (field_146123_n) {
                        screen.drawTooltip(x, y, displayString);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                }

            }

            public void renderInventorySlot(ItemStack par1, int par2, int par3) {
                if (par1 != null) {
                    itemRenderer.renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(), par1, par2, par3);
                }
            }
        }
    }

    public static class LogPileGUIWithFastBagAccess extends GuiContainerTFC {
        public LogPileGUIWithFastBagAccess(Container logPileContainer) {
            super(logPileContainer, 176, 85);
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiLogPile.texture);
        }
    }

    public static class BarrelGUIWithFastBagAccess extends GuiContainerTFC {
        public TEBarrel barrelTE;
        public EntityPlayer player;
        public int guiTab;

        public BarrelGUIWithFastBagAccess(Container barrelContainer, TEBarrel barrel, int tab, EntityPlayer player) {
            super(barrelContainer, 176, 85);

            this.player = player;
            barrelTE = barrel;
            guiLeft = (width - 208) / 2;
            guiTop = (height - 198) / 2;
            guiTab = tab;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            if (barrelTE.getInvCount() > 0) {
                if (guiTab == 0) {
                    ((GuiButton)buttonList.get(4)).visible = false;
                } else if (guiTab == 1) {
                    ((GuiButton)buttonList.get(1)).visible = false;
                }
            } else if (guiTab == 0) {
                ((GuiButton)buttonList.get(4)).visible = true;
            } else if (guiTab == 1) {
                ((GuiButton)buttonList.get(1)).visible = true;
            }

            if (barrelTE.getFluidLevel() > 0) {
                if (guiTab == 0) {
                    ((GuiButton)buttonList.get(3)).visible = false;
                } else if (guiTab == 1) {
                    ((GuiButton)buttonList.get(0)).visible = false;
                }
            } else if (guiTab == 0) {
                ((GuiButton)buttonList.get(3)).visible = true;
            } else if (guiTab == 1) {
                ((GuiButton)buttonList.get(0)).visible = true;
            }

            if (barrelTE.getSealed() && guiTab == 0) {
                ((GuiButton)buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Unseal");
                ((GuiButton)buttonList.get(1)).enabled = false;
                ((GuiButton)buttonList.get(2)).enabled = false;
            } else if (!this.barrelTE.getSealed() && guiTab == 0) {
                ((GuiButton)buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Seal");
                ((GuiButton)buttonList.get(1)).enabled = true;
                ((GuiButton)buttonList.get(2)).enabled = true;
            }

        }

        @Override
        public void initGui() {
            super.initGui();
            createButtons();
        }

        public void createButtons() {
            buttonList.clear();
            if (guiTab == 0) {
                if (!barrelTE.getSealed()) {
                    buttonList.add(new GuiButton(0, guiLeft + 38, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    buttonList.add(new GuiButton(0, guiLeft + 38, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Unseal")));
                }

                buttonList.add(new GuiButton(1, guiLeft + 88, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Empty")));
                if (this.barrelTE.mode == 0) {
                    buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOn"), 0, 204, 16, 16));
                } else if (this.barrelTE.mode == 1) {
                    buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOff"), 0, 188, 16, 16));
                }

                buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(3, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(4, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
            } else if (this.guiTab == 1) {
                buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(0, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new BarrelGUIWithFastBagAccess.GuiBarrelTabButton(1, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
                if (!this.barrelTE.getSealed()) {
                    buttonList.add(new GuiButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    buttonList.add(new GuiButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Unseal")));
                }
            }

        }

        @Override
        public void drawTooltip(int mx, int my, String text) {
            drawHoveringText(Arrays.asList(text), mx, my + 15, fontRendererObj);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
        }

        @Override
        protected void actionPerformed(GuiButton guibutton) {
            if (guiTab == 0) {
                if (guibutton.id == 0) {
                    if (!barrelTE.getSealed()) {
                        barrelTE.actionSeal(0, player);
                    } else {
                        barrelTE.actionUnSeal(0, player);
                    }
                } else if (guibutton.id == 1) {
                    barrelTE.actionEmpty();
                } else if (guibutton.id == 2) {
                    barrelTE.actionMode();
                    createButtons();
                } else if (guibutton.id == 3 && barrelTE.getFluidLevel() == 0 && barrelTE.getInvCount() == 0) {
                    barrelTE.actionSwitchTab(1, player);
                }
            } else if (guiTab == 1) {
                if (guibutton.id == 1 && barrelTE.getInvCount() == 0) {
                    barrelTE.actionSwitchTab(0, player);
                } else if (guibutton.id == 2) {
                    if (!barrelTE.getSealed()) {
                        barrelTE.actionSeal(1, player);
                    } else {
                        barrelTE.actionUnSeal(1, player);
                    }

                    this.createButtons();
                }
            }

        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            bindTexture(GuiBarrel.TEXTURE);
            guiLeft = (width - xSize) / 2;
            guiTop = (height - ySize) / 2;
            if (guiTab == 0) {
                drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, getShiftedYSize());
                if (barrelTE != null && barrelTE.fluid != null) {
                    int scale = barrelTE.getLiquidScaled(50);
                    IIcon liquidIcon = barrelTE.fluid.getFluid().getIcon(barrelTE.fluid);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    int color = barrelTE.fluid.getFluid().getColor(barrelTE.fluid);
                    GL11.glColor4ub((byte)(color >> 16 & 255), (byte)(color >> 8 & 255), (byte)(color & 255), (byte)-86);
                    int div = (int)Math.floor((double)(scale / 8));
                    int rem = scale - div * 8;
                    drawTexturedModelRectFromIcon(guiLeft + 12, guiTop + 65 - scale, liquidIcon, 8, div > 0 ? 8 : rem);

                    for(int c = 0; div > 0 && c < div; ++c) {
                        this.drawTexturedModelRectFromIcon(guiLeft + 12, guiTop + 65 - (8 + c * 8), liquidIcon, 8, 8);
                    }

                    GL11.glColor3f(0.0F, 0.0F, 0.0F);
                }

                ItemStack inStack = barrelTE.getStackInSlot(0);
                if (barrelTE.getFluidStack() != null) {
                    drawCenteredString(fontRendererObj, barrelTE.fluid.getFluid().getLocalizedName(barrelTE.getFluidStack()), guiLeft + 88, guiTop + 7, 5592405);
                }

                if (barrelTE.sealtime != 0) {
                    drawCenteredString(fontRendererObj, TFC_Time.getDateStringFromHours(barrelTE.sealtime), guiLeft + 88, guiTop + 17, 5592405);
                }

                if (barrelTE.recipe != null) {
                    if (!(barrelTE.recipe instanceof BarrelBriningRecipe)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.Output") + ": " + barrelTE.recipe.getRecipeName(), guiLeft + 88, this.guiTop + 72, 5592405);
                    } else if (barrelTE.getSealed() && barrelTE.getFluidStack() != null && barrelTE.getFluidStack().getFluid() == TFCFluids.BRINE && inStack != null && inStack.getItem() instanceof IFood && (((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || (IFood)inStack.getItem() == TFCItems.cheese) && !Food.isBrined(inStack)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.barrel.brining"), guiLeft + 88, guiTop + 72, 5592405);
                    }
                } else if (barrelTE.recipe == null && barrelTE.getSealed() && barrelTE.getFluidStack() != null && inStack != null && inStack.getItem() instanceof IFood && this.barrelTE.getFluidStack().getFluid() == TFCFluids.VINEGAR && !Food.isPickled(inStack) && Food.getWeight(inStack) / (float)barrelTE.getFluidStack().amount <= 160.0F / (float)barrelTE.getMaxLiquid()) {
                    if ((((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || (IFood)inStack.getItem() == TFCItems.cheese) && Food.isBrined(inStack)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.barrel.pickling"), guiLeft + 88, guiTop + 72, 5592405);
                    }
                } else {
                    BarrelPreservativeRecipe preservative = BarrelManager.getInstance().findMatchingPreservativeRepice(barrelTE, inStack, barrelTE.getFluidStack(), this.barrelTE.getSealed());
                    if (preservative != null) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate(preservative.getPreservingString()), guiLeft + 88, guiTop + 72, 5592405);
                    }
                }
            } else if (guiTab == 1) {
                drawTexturedModalRect(guiLeft, guiTop, 0, 86, xSize, getShiftedYSize());
            }

            PlayerInventory.drawInventory(this, width, height, getShiftedYSize());
        }

        @Override
        public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
            if (guiTab == 0 && mouseInRegion(12, 15, 9, 50, mouseX, mouseY)) {
                ArrayList<String> list = new ArrayList();
                list.add(barrelTE.getFluidLevel() + "mB");
                drawHoveringText(list, mouseX - guiLeft, mouseY - guiTop + 8, fontRendererObj);
            }

        }

        @Override
        public void drawCenteredString(FontRenderer fontrenderer, String s, int i, int j, int k) {
            fontrenderer.drawString(s, i - fontrenderer.getStringWidth(s) / 2, j, k);
        }

        @Override
        public void drawScreen(int x, int y, float par3) {
            super.drawScreen(x, y, par3);
            if (this.barrelTE.getSealed()) {
                GL11.glPushMatrix();
                if (guiTab == 0) {
                    Slot inputSlot = inventorySlots.getSlot(0);
                    drawSlotOverlay(inputSlot);
                } else if (guiTab == 1) {
                    for(int i = 0; i < barrelTE.storage.length; ++i) {
                        Slot slot = inventorySlots.getSlot(i);
                        drawSlotOverlay(slot);
                    }
                }
                GL11.glPopMatrix();
            }
        }

        public void drawSlotOverlay(Slot slot) {
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            int xPos = slot.xDisplayPosition + guiLeft - 1;
            int yPos = slot.yDisplayPosition + guiTop - 1;
            GL11.glColorMask(true, true, true, false);
            drawGradientRect(xPos, yPos, xPos + 18, yPos + 18, 1979711487, 1979711487);
            GL11.glColorMask(true, true, true, true);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
        }

        public static class GuiBarrelTabButton extends GuiButton {
            public BarrelGUIWithFastBagAccess screen;
            public IIcon buttonicon;
            public int xPos;
            public int yPos = 172;
            public int xSize = 31;
            public int ySize = 15;

            public GuiBarrelTabButton(int index, int xPos, int yPos, int width, int height, BarrelGUIWithFastBagAccess gui, IIcon icon, String s) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.buttonicon = icon;
            }

            public GuiBarrelTabButton(int index, int xPos, int yPos, int width, int height, BarrelGUIWithFastBagAccess gui, String s, int xp, int yp, int xs, int ys) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.xPos = xp;
                this.yPos = yp;
                this.xSize = xs;
                this.ySize = ys;
            }

            @Override
            public void drawButton(Minecraft mc, int x, int y) {
                if (visible) {
                    TFC_Core.bindTexture(GuiBarrel.TEXTURE);
                    GL11.glColor4f(1, 1, 1, 1);
                    zLevel = 301.0F;
                    drawTexturedModalRect(xPosition, yPosition, xPos, yPos, xSize, ySize);
                    field_146123_n = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
                    GL11.glColor4f(1, 1, 1, 1);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    if (buttonicon != null) {
                        drawTexturedModelRectFromIcon(xPosition + 12, yPosition + 4, buttonicon, 8, 8);
                    }

                    zLevel = 0.0F;
                    mouseDragged(mc, x, y);
                    if (field_146123_n) {
                        screen.drawTooltip(x, y, displayString);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                }

            }
        }
    }

    public static class LargeVesselGUIWithFastBagAccess extends GuiContainerTFC {
        public static ResourceLocation TEXTURE = new ResourceLocation(QuickPockets.ID, "large-vessel-gui-fixed.png");
        public TEVessel vesselTE;
        public EntityPlayer player;
        public int guiTab;

        public LargeVesselGUIWithFastBagAccess(Container vesselContainer, TEVessel vessel, int tab, EntityPlayer player) {
            super(vesselContainer, 176, 85);

            this.player = player;
            vesselTE = vessel;
            guiLeft = (width - 208) / 2;
            guiTop = (height - 198) / 2;
            guiTab = tab;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            this.inventorySlots.putStackInSlot(0, this.vesselTE.getInputStack());
            if (this.vesselTE.getInvCount() > 0 && this.vesselTE.getDistillationMode() == -1) {
                if (this.guiTab == 0) {
                    ((GuiButton)this.buttonList.get(4)).visible = false;
                } else if (this.guiTab == 1) {
                    ((GuiButton)this.buttonList.get(1)).visible = false;
                }
            } else if (this.guiTab == 0) {
                ((GuiButton)this.buttonList.get(4)).visible = true;
            } else if (this.guiTab == 1) {
                ((GuiButton)this.buttonList.get(1)).visible = true;
            }

            if (this.vesselTE.getFluidLevel() > 0) {
                if (this.guiTab == 0) {
                    ((GuiButton)this.buttonList.get(3)).visible = false;
                } else if (this.guiTab == 1) {
                    ((GuiButton)this.buttonList.get(0)).visible = false;
                }
            } else if (this.guiTab == 0) {
                ((GuiButton)this.buttonList.get(3)).visible = true;
            } else if (this.guiTab == 1) {
                ((GuiButton)this.buttonList.get(0)).visible = true;
            }

            if (this.vesselTE.getSealed() && this.guiTab == 0) {
                ((GuiButton)this.buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Unseal");
                ((GuiButton)this.buttonList.get(1)).enabled = false;
                ((GuiButton)this.buttonList.get(2)).enabled = false;
            } else if (!this.vesselTE.getSealed() && this.guiTab == 0) {
                ((GuiButton)this.buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Seal");
                ((GuiButton)this.buttonList.get(1)).enabled = true;
                ((GuiButton)this.buttonList.get(2)).enabled = true;
            }

        }

        @Override
        public void initGui() {
            super.initGui();
            this.createButtons();
        }

        public void createButtons() {
            this.buttonList.clear();
            if (this.guiTab == 0) {
                if (!this.vesselTE.getSealed()) {
                    this.buttonList.add(new GuiButton(0, this.guiLeft + 38, this.guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    this.buttonList.add(new GuiButton(0, this.guiLeft + 38, this.guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Unseal")));
                }

                this.buttonList.add(new GuiButton(1, this.guiLeft + 88, this.guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Empty")));
                if (this.vesselTE.mode == 0) {
                    this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(2, this.guiLeft + 39, this.guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOn"), 0, 204, 16, 16));
                } else if (this.vesselTE.mode == 1) {
                    this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(2, this.guiLeft + 39, this.guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOff"), 0, 188, 16, 16));
                }

                this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(3, this.guiLeft + 36, this.guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(4, this.guiLeft + 5, this.guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
            } else if (this.guiTab == 1) {
                this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(0, this.guiLeft + 36, this.guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                this.buttonList.add(new LargeVesselGUIWithFastBagAccess.GuiBarrelTabButton(1, this.guiLeft + 5, this.guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
                if (!this.vesselTE.getSealed()) {
                    this.buttonList.add(new GuiButton(2, this.guiLeft + 6, this.guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    this.buttonList.add(new GuiButton(2, this.guiLeft + 6, this.guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Unseal")));
                }
            }

        }

        @Override
        public void drawTooltip(int mx, int my, String text) {
            this.drawHoveringText(Arrays.asList(text), mx, my + 15, this.fontRendererObj);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
        }

        @Override
        public void actionPerformed(GuiButton guibutton) {
            if (this.guiTab == 0) {
                if (guibutton.id == 0) {
                    if (!this.vesselTE.getSealed()) {
                        this.vesselTE.actionSeal(0, this.player);
                    } else {
                        this.vesselTE.actionUnSeal(0, this.player);
                    }
                } else if (guibutton.id == 1) {
                    this.vesselTE.actionEmpty();
                } else if (guibutton.id == 2) {
                    this.vesselTE.actionMode();
                    this.createButtons();
                } else if (guibutton.id == 3 && this.vesselTE.getFluidLevel() == 0 && this.vesselTE.getInvCount() == 0) {
                    this.vesselTE.actionSwitchTab(1, this.player);
                }
            } else if (this.guiTab == 1) {
                if (guibutton.id == 1 && this.vesselTE.getInvCount() == 0) {
                    this.vesselTE.actionSwitchTab(0, this.player);
                } else if (guibutton.id == 2) {
                    if (!this.vesselTE.getSealed()) {
                        this.vesselTE.actionSeal(1, this.player);
                    } else {
                        this.vesselTE.actionUnSeal(1, this.player);
                    }

                    this.createButtons();
                }
            }

        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            TFC_Core.bindTexture(TEXTURE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            int w = (this.width - this.xSize) / 2;
            int h = (this.height - this.ySize) / 2;
            if (this.guiTab == 0) {
                this.drawTexturedModalRect(w, h, 0, 0, this.xSize, this.getShiftedYSize());
                if (this.vesselTE != null && this.vesselTE.fluid != null) {
                    int scale = this.vesselTE.getLiquidScaled(50);
                    IIcon liquidIcon = this.vesselTE.fluid.getFluid().getIcon(this.vesselTE.fluid);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    int color = this.vesselTE.fluid.getFluid().getColor(this.vesselTE.fluid);
                    GL11.glColor4ub((byte)(color >> 16 & 255), (byte)(color >> 8 & 255), (byte)(color & 255), (byte)-86);
                    int div = (int)Math.floor((double)(scale / 8));
                    int rem = scale - div * 8;
                    this.drawTexturedModelRectFromIcon(w + 12, h + 65 - scale, liquidIcon, 8, div > 0 ? 8 : rem);

                    for(int c = 0; div > 0 && c < div; ++c) {
                        this.drawTexturedModelRectFromIcon(w + 12, h + 65 - (8 + c * 8), liquidIcon, 8, 8);
                    }

                    GL11.glColor3f(0.0F, 0.0F, 0.0F);
                }

                ItemStack inStack = this.vesselTE.getStackInSlot(0);
                if (this.vesselTE.getFluidStack() != null) {
                    this.drawCenteredString(this.fontRendererObj, this.vesselTE.fluid.getFluid().getLocalizedName(this.vesselTE.getFluidStack()), this.guiLeft + 88, this.guiTop + 7, 5592405);
                }

                if (this.vesselTE.sealtime != 0) {
                    this.drawCenteredString(this.fontRendererObj, TFC_Time.getDateStringFromHours(this.vesselTE.sealtime), this.guiLeft + 88, this.guiTop + 17, 5592405);
                }

                this.vesselTE.recipe = BarrelManager.getInstance().findMatchingRecipe(this.vesselTE.getInputStack(), this.vesselTE.getFluidStack(), this.vesselTE.getSealed(), this.vesselTE.getTechLevel(), this.vesselTE.isHeated(), this.vesselTE);
                if (this.vesselTE.recipe != null) {
                    if (!(this.vesselTE.recipe instanceof BarrelBriningRecipe)) {
                        this.drawCenteredString(this.fontRendererObj, TFC_Core.translate("gui.Output") + ": " + this.vesselTE.recipe.getRecipeName(), this.guiLeft + 88, this.guiTop + 72, 5592405);
                    } else if (this.vesselTE.getSealed() && this.vesselTE.getFluidStack() != null && this.vesselTE.getFluidStack().getFluid() == TFCFluids.BRINE && inStack != null && inStack.getItem() instanceof IFood && (((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || (IFood)inStack.getItem() == TFCItems.cheese) && !Food.isBrined(inStack)) {
                        this.drawCenteredString(this.fontRendererObj, TFC_Core.translate("gui.barrel.brining"), this.guiLeft + 88, this.guiTop + 72, 5592405);
                    }
                } else if (this.vesselTE.recipe == null && this.vesselTE.getSealed() && this.vesselTE.getFluidStack() != null && inStack != null && inStack.getItem() instanceof IFood && this.vesselTE.getFluidStack().getFluid() == TFCFluids.VINEGAR && !Food.isPickled(inStack) && Food.getWeight(inStack) / (float)this.vesselTE.getFluidStack().amount <= 160.0F / (float)this.vesselTE.getMaxLiquid()) {
                    if ((((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || (IFood)inStack.getItem() == TFCItems.cheese) && Food.isBrined(inStack)) {
                        this.drawCenteredString(this.fontRendererObj, TFC_Core.translate("gui.barrel.pickling"), this.guiLeft + 88, this.guiTop + 72, 5592405);
                    }
                } else {
                    BarrelPreservativeRecipe preservative = BarrelManager.getInstance().findMatchingPreservativeRepice(this.vesselTE, inStack, this.vesselTE.getFluidStack(), this.vesselTE.getSealed());
                    if (preservative != null) {
                        this.drawCenteredString(this.fontRendererObj, TFC_Core.translate(preservative.getPreservingString()), this.guiLeft + 88, this.guiTop + 72, 5592405);
                    }
                }
            } else if (this.guiTab == 1) {
                this.drawTexturedModalRect(w, h, 0, 86, this.xSize, this.getShiftedYSize());
            }

            PlayerInventory.drawInventory(this, this.width, this.height, this.getShiftedYSize());
        }

        @Override
        public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
            if (this.guiTab == 0 && this.mouseInRegion(12, 15, 9, 50, mouseX, mouseY)) {
                ArrayList<String> list = new ArrayList();
                list.add(this.vesselTE.getFluidLevel() + "mB");
                this.drawHoveringText(list, mouseX - this.guiLeft, mouseY - this.guiTop + 8, this.fontRendererObj);
            }

        }

        @Override
        public void drawCenteredString(FontRenderer fontrenderer, String s, int i, int j, int k) {
            fontrenderer.drawString(s, i - fontrenderer.getStringWidth(s) / 2, j, k);
        }

        @Override
        public void drawScreen(int x, int y, float par3) {
            super.drawScreen(x, y, par3);
            if (this.vesselTE.getSealed()) {
                GL11.glPushMatrix();
                if (this.guiTab == 0) {
                    Slot inputSlot = this.inventorySlots.getSlot(0);
                    this.drawSlotOverlay(inputSlot);
                } else if (this.guiTab == 1) {
                    for(int i = 0; i < this.vesselTE.storage.length; ++i) {
                        Slot slot = this.inventorySlots.getSlot(i);
                        this.drawSlotOverlay(slot);
                    }
                }

                GL11.glPopMatrix();
            }

        }

        public void drawSlotOverlay(Slot slot) {
            GL11.glDisable(2896);
            GL11.glDisable(2929);
            int xPos = slot.xDisplayPosition + this.guiLeft - 1;
            int yPos = slot.yDisplayPosition + this.guiTop - 1;
            GL11.glColorMask(true, true, true, false);
            this.drawGradientRect(xPos, yPos, xPos + 18, yPos + 18, 1979711487, 1979711487);
            GL11.glColorMask(true, true, true, true);
            GL11.glEnable(2896);
            GL11.glEnable(2929);
        }

        public static class GuiBarrelTabButton extends GuiButton {
            private LargeVesselGUIWithFastBagAccess screen;
            private IIcon buttonicon;
            private int xPos;
            private int yPos = 172;
            private int xSize = 31;
            private int ySize = 15;

            public GuiBarrelTabButton(int index, int xPos, int yPos, int width, int height, LargeVesselGUIWithFastBagAccess gui, IIcon icon, String s) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.buttonicon = icon;
            }

            public GuiBarrelTabButton(int index, int xPos, int yPos, int width, int height, LargeVesselGUIWithFastBagAccess gui, String s, int xp, int yp, int xs, int ys) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.xPos = xp;
                this.yPos = yp;
                this.xSize = xs;
                this.ySize = ys;
            }

            public void drawButton(Minecraft mc, int x, int y) {
                if (this.visible) {
                    TFC_Core.bindTexture(GuiLargeVessel.TEXTURE);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.zLevel = 301.0F;
                    this.drawTexturedModalRect(this.xPosition, this.yPosition, this.xPos, this.yPos, this.xSize, this.ySize);
                    this.field_146123_n = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    if (this.buttonicon != null) {
                        this.drawTexturedModelRectFromIcon(this.xPosition + 12, this.yPosition + 4, this.buttonicon, 8, 8);
                    }

                    this.zLevel = 0.0F;
                    this.mouseDragged(mc, x, y);
                    if (this.field_146123_n) {
                        this.screen.drawTooltip(x, y, this.displayString);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }

            }
        }
    }

    public static class HopperGUIWithFastBagAccess extends GuiContainerTFC {
        public HopperGUIWithFastBagAccess(Container hopperContainer) {
            super(hopperContainer, 176, 49);
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiHopper.texture);
        }
    }

    public static class GrillGUIWithFastBagAccess extends GuiContainerTFC {
        public TEFireEntity fireTE;

        public GrillGUIWithFastBagAccess(Container grillContainer, TEFireEntity fire) {
            super(grillContainer, 176, 85);
            fireTE = fire;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            this.drawGui(GuiGrill.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            int scale = 0;
            if (fireTE != null) {
                scale = fireTE.getTemperatureScaled(49);
            }
            drawTexturedModalRect(guiLeft + 7, guiTop + 65 - scale, 0, 86, 15, 6);
        }
    }

    public static class CrucibleGUIWithFastBagAccess extends GuiContainerTFC {

        public TECrucible crucibleTE;

        public CrucibleGUIWithFastBagAccess(Container crucibleContainer, TECrucible crucible) {
            super(crucibleContainer, 176, 113);
            this.crucibleTE = crucible;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiCrucible.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            int scale = crucibleTE.getTemperatureScaled(49);
            drawTexturedModalRect(guiLeft + 153, guiTop + 80 - scale, 185, 0, 15, 6);
            scale = crucibleTE.getOutCountScaled(100);
            drawTexturedModalRect(guiLeft + 129, guiTop + 106 - scale, 177, 6, 8, scale);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int i, int j) {
            if (TFCOptions.enableDebugMode) {
                fontRendererObj.drawString("Temp: " + crucibleTE.temperature, 178, 8, 16777215);
            }

            if (crucibleTE.currentAlloy != null) {
                if (crucibleTE.currentAlloy.outputAmount == 0.0F) {
                    fontRendererObj.drawString(EnumChatFormatting.UNDERLINE + TFC_Core.translate("gui.empty"), 7, 7, 0);
                    return;
                }

                if (crucibleTE.currentAlloy.outputType != null) {
                    fontRendererObj.drawString(EnumChatFormatting.UNDERLINE + TFC_Core.translate("gui.metal." + crucibleTE.currentAlloy.outputType.name.replace(" ", "")), 7, 7, 0);
                } else {
                    fontRendererObj.drawString(EnumChatFormatting.UNDERLINE + TFC_Core.translate("gui.metal.Unknown"), 7, 7, 0);
                }

                for (int c = 0; c < crucibleTE.currentAlloy.alloyIngred.size(); ++c) {
                    double m = this.crucibleTE.currentAlloy.alloyIngred.get(c).metal;
                    m = Math.round(m * 100.0) / 100.0;
                    if (crucibleTE.currentAlloy.alloyIngred.get(c).metalType != null) {
                        fontRendererObj.drawString(EnumChatFormatting.DARK_GRAY + TFC_Core.translate("gui.metal." + crucibleTE.currentAlloy.alloyIngred.get(c).metalType.name.replace(" ", "")) + ": " + EnumChatFormatting.DARK_GREEN + m + "%", 7, 18 + 10 * c, 0);
                    }
                }
            }

        }

        @Override
        public void drawScreen(int par1, int par2, float par3) {
            super.drawScreen(par1, par2, par3);
            if (crucibleTE.currentAlloy != null) {
                int w = (width - xSize) / 2;
                int h = (height - ySize) / 2;
                if (par1 >= 129 + w && par2 >= 6 + h && par1 <= 137 + w && par2 <= 106 + h) {
                    String[] text = new String[]{String.format("%2.0f", crucibleTE.currentAlloy.outputAmount)};
                    this.drawHoveringText(Arrays.asList(text), par1, par2, fontRendererObj);
                }
            }

        }

    }

    public static class ForgeGUIWithFastBagAccess extends GuiContainerTFC {
        public TEForge forgeTE;

        public ForgeGUIWithFastBagAccess(Container forgeContainer, TEForge forge) {
            super(forgeContainer, 176, 85);
            forgeTE = forge;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiForge.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            if (forgeTE != null) {
                int scale = forgeTE.getTemperatureScaled(49);
                drawTexturedModalRect(guiLeft + 8, guiTop + 65 - scale, 185, 31, 15, 6);
            }
        }

        @Override
        public boolean checkHotbarKeys(int keycode) {
            return false;
        }
    }

    public static class BlastFurnaceGUIWithFastBagAccess extends GuiContainerTFC {
        public TEBlastFurnace blastFurnaceTE;

        public BlastFurnaceGUIWithFastBagAccess(Container blastFurnaceContainer, TEBlastFurnace blastFurnace) {
            super(blastFurnaceContainer, 176, 85);
            blastFurnaceTE = blastFurnace;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiBlastFurnace.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            int scale = blastFurnaceTE.getTemperatureScaled(49);
            drawTexturedModalRect(guiLeft + 8, guiTop + 65 - scale, 185, 31, 15, 6);
            scale = blastFurnaceTE.getOreCountScaled(80);
            drawTexturedModalRect(guiLeft + 40, guiTop + 25, 176, 0, scale + 1, 8);
            scale = blastFurnaceTE.getCharcoalCountScaled(80);
            drawTexturedModalRect(guiLeft + 40, guiTop + 43, 176, 0, scale + 1, 8);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int i, int j) {
            fontRendererObj.drawString(TFC_Core.translate("gui.Bloomery.Ore"), 40, 17, 0);
            fontRendererObj.drawString(TFC_Core.translate("gui.Bloomery.Charcoal"), 40, 35, 0);
            if (TFCOptions.enableDebugMode) {
                fontRendererObj.drawString("Temp : " + blastFurnaceTE.fireTemp, 40, 71, 0);
            }
        }
    }

    public static class AnvilGUIWithFastBagAccess extends GuiContainerTFC {
        public TEAnvil anvilTE;
        public EntityPlayer player;
        public int x;
        public int y;
        public int z;
        public String plan = "";
        public ItemStack input;
        public ItemStack input2;

        public AnvilGUIWithFastBagAccess(Container anvilContainer, TEAnvil anvil, int x, int y, int z, EntityPlayer player) {
            super(anvilContainer, 208, 117);
            this.player = player;
            this.x = x;
            this.y = y;
            this.z = z;
            anvilTE = anvil;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void initGui() {
            super.initGui();
            buttonList.clear();
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(7, guiLeft + 123, guiTop + 82, 16, 16, TFC_Textures.anvilShrink, 208, 17, 16, 16, this, TFC_Core.translate("gui.Anvil.Shrink")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(6, guiLeft + 105, guiTop + 82, 16, 16, TFC_Textures.anvilUpset, 208, 17, 16, 16, this, TFC_Core.translate("gui.Anvil.Upset")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(5, guiLeft + 123, guiTop + 64, 16, 16, TFC_Textures.anvilBend, 208, 17, 16, 16, this, TFC_Core.translate("gui.Anvil.Bend")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(4, guiLeft + 105, guiTop + 64, 16, 16, TFC_Textures.anvilPunch, 208, 17, 16, 16, this, TFC_Core.translate("gui.Anvil.Punch")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(3, guiLeft + 87, guiTop + 82, 16, 16, TFC_Textures.anvilDraw, 208, 33, 16, 16, this, TFC_Core.translate("gui.Anvil.Draw")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(2, guiLeft + 69, guiTop + 82, 16, 16, TFC_Textures.anvilHitHeavy, 208, 33, 16, 16, this, TFC_Core.translate("gui.Anvil.HeavyHit")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(1, guiLeft + 87, guiTop + 64, 16, 16, TFC_Textures.anvilHitMedium, 208, 33, 16, 16, this, TFC_Core.translate("gui.Anvil.MediumHit")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(0, guiLeft + 69, guiTop + 64, 16, 16, TFC_Textures.anvilHitLight, 208, 33, 16, 16, this, TFC_Core.translate("gui.Anvil.LightHit")));
            buttonList.add(new GuiButton(8, guiLeft + 13, guiTop + 53, 36, 20, TFC_Core.translate("gui.Anvil.Weld")));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(9, guiLeft + 113, guiTop + 7, 19, 21, 208, 49, 19, 21, this, 2, TFCOptions.anvilRuleColor2[0], TFCOptions.anvilRuleColor2[1], TFCOptions.anvilRuleColor2[2]));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(10, guiLeft + 94, guiTop + 7, 19, 21, 208, 49, 19, 21, this, 1, TFCOptions.anvilRuleColor1[0], TFCOptions.anvilRuleColor1[1], TFCOptions.anvilRuleColor1[2]));
            buttonList.add(new AnvilGUIWithFastBagAccess.Button(11, guiLeft + 75, guiTop + 7, 19, 21, 208, 49, 19, 21, this, 0, TFCOptions.anvilRuleColor0[0], TFCOptions.anvilRuleColor0[1], TFCOptions.anvilRuleColor0[2]));
            buttonList.add(new AnvilGUIWithFastBagAccess.PlanButton(12, guiLeft + 122, guiTop + 45, 18, 18, this, TFC_Core.translate("gui.Anvil.Plans")));
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            if (anvilTE != null) {
                String craftingPlan = anvilTE.craftingPlan;
                ItemStack stack1 = anvilTE.anvilItemStacks[1];
                ItemStack stack2 = anvilTE.anvilItemStacks[5];
                if (craftingPlan != null && craftingPlan != plan || stack1 != null && stack1 != input || stack2 != null && stack2 != input2) {
                    plan = anvilTE.craftingPlan;
                    anvilTE.updateRecipe();
                    input = anvilTE.anvilItemStacks[1];
                    input2 = anvilTE.anvilItemStacks[5];
                }
            }

        }

        @Override
        public void actionPerformed(GuiButton guibutton) {
            if (guibutton.id == 0) {
                anvilTE.actionLightHammer();
            } else if (guibutton.id == 2) {
                anvilTE.actionHeavyHammer();
            } else if (guibutton.id == 1) {
                anvilTE.actionHammer();
            } else if (guibutton.id == 3) {
                anvilTE.actionDraw();
            } else if (guibutton.id == 4) {
                anvilTE.actionPunch();
            } else if (guibutton.id == 5) {
                anvilTE.actionBend();
            } else if (guibutton.id == 6) {
                anvilTE.actionUpset();
            } else if (guibutton.id == 7) {
                anvilTE.actionShrink();
            } else if (guibutton.id == 8) {
                anvilTE.actionWeld();
            } else if (guibutton.id == 12 && anvilTE.anvilItemStacks[1] != null) {
                player.openGui(TerraFirmaCraft.instance, 24, player.worldObj, x, y, z);
            }

            inventorySlots.detectAndSendChanges();
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiAnvil.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            if (anvilTE != null) {
                int i1 = anvilTE.getCraftingValue();
                drawTexturedModalRect(guiLeft + 27 + i1, guiTop + 103, 213, 10, 5, 5);
                i1 = anvilTE.getItemCraftingValue();
                drawTexturedModalRect(guiLeft + 27 + i1, guiTop + 108, 208, 10, 5, 6);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
                if (anvilTE.workRecipe != null) {
                    int s0 = (int)(anvilTE.workRecipe.getSkillMult(player) * 1000.0F);
                    float s1 = (float)s0 / 10.0F;
                    fontRendererObj.drawString("Skill: " + s1 + "%", guiLeft + 150, guiTop + 8, 16736256);
                }

                drawItemRulesImages(guiLeft, guiTop);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
                drawRulesImages(guiLeft, guiTop);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
            }

        }

        @Override
        public void drawTooltip(int mx, int my, String text) {
            drawHoveringText(Arrays.asList(text), mx, my + 15, fontRendererObj);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(2896);
            GL11.glDisable(2929);
        }

        public void drawItemRulesImages(int w, int h) {
            if (anvilTE != null && anvilTE.itemCraftingRules != null) {
                PlanRecipe p = AnvilManager.getInstance().getPlan(anvilTE.craftingPlan);
                if (p == null) {
                    return;
                }

                RuleEnum[] rules = anvilTE.workRecipe != null ? p.rules : null;
                int[] itemRules = anvilTE.getItemRules();
                mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                drawTexturedModelRectFromIcon(w + 80, h + 31, getIconFromRule(itemRules[0]), 10, 10);
                drawTexturedModelRectFromIcon(w + 99, h + 31, getIconFromRule(itemRules[1]), 10, 10);
                drawTexturedModelRectFromIcon(w + 118, h + 31, getIconFromRule(itemRules[2]), 10, 10);
                mc.getTextureManager().bindTexture(GuiAnvil.texture);
                if (rules != null && rules[0].matches(itemRules, 0)) {
                    GL11.glColor4f(0, 1, 0, 1);
                }

                this.drawTexturedModalRect(w + 77, h + 28, 210, 115, 15, 15);
                GL11.glColor4f(1, 1, 1, 1);
                if (rules != null && rules[1].matches(itemRules, 1)) {
                    GL11.glColor4f(0, 1, 0, 1);
                }

                this.drawTexturedModalRect(w + 96, h + 28, 210, 115, 15, 15);
                GL11.glColor4f(1, 1, 1, 1);
                if (rules != null && rules[2].matches(itemRules, 2)) {
                    GL11.glColor4f(0, 1, 0, 1);
                }

                this.drawTexturedModalRect(w + 115, h + 28, 210, 115, 15, 15);
                GL11.glColor4f(1, 1, 1, 1);
            }

        }

        public void drawRulesImages(int w, int h) {
            if (anvilTE.workRecipe != null) {
                PlanRecipe p = AnvilManager.getInstance().getPlan(anvilTE.craftingPlan);
                if (p == null) {
                    return;
                }

                RuleEnum[] rules = p.rules;
                TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                drawTexturedModelRectFromIcon(w + 80, h + 10, getIconFromRule(rules[0].Action), 10, 10);
                drawTexturedModelRectFromIcon(w + 99, h + 10, getIconFromRule(rules[1].Action), 10, 10);
                drawTexturedModelRectFromIcon(w + 118, h + 10, getIconFromRule(rules[2].Action), 10, 10);
                TFC_Core.bindTexture(GuiAnvil.texture);
                GL11.glColor4ub(TFCOptions.anvilRuleColor0[0], TFCOptions.anvilRuleColor0[1], TFCOptions.anvilRuleColor0[2], (byte)-1);
                if (rules[0].Min == 0) {
                    drawTexturedModalRect(w + 75, h + 26, 228, 68, 19, 3);
                }

                if (rules[0].Max > 0 && (rules[0].Min <= 1 || rules[0].Max == 1)) {
                    drawTexturedModalRect(w + 94, h + 26, 228, 68, 19, 3);
                }

                if (rules[0].Max > 1 && (rules[0].Min <= 2 || rules[0].Max == 2)) {
                    drawTexturedModalRect(w + 113, h + 26, 228, 68, 19, 3);
                }

                GL11.glColor4ub(TFCOptions.anvilRuleColor1[0], TFCOptions.anvilRuleColor1[1], TFCOptions.anvilRuleColor1[2], (byte)-1);
                if (rules[1].Min == 0) {
                    drawTexturedModalRect(w + 75, h + 24, 228, 68, 19, 3);
                }

                if (rules[1].Max > 0 && (rules[1].Min <= 1 || rules[1].Max == 1)) {
                    drawTexturedModalRect(w + 94, h + 24, 228, 68, 19, 3);
                }

                if (rules[1].Max > 1 && (rules[1].Min <= 2 || rules[1].Max == 2)) {
                    drawTexturedModalRect(w + 113, h + 24, 228, 68, 19, 3);
                }

                GL11.glColor4ub(TFCOptions.anvilRuleColor2[0], TFCOptions.anvilRuleColor2[1], TFCOptions.anvilRuleColor2[2], (byte)-1);
                if (rules[2].Min == 0) {
                    drawTexturedModalRect(w + 75, h + 22, 228, 68, 19, 3);
                }

                if (rules[2].Max > 0 && (rules[2].Min <= 1 || rules[2].Max == 1)) {
                    drawTexturedModalRect(w + 94, h + 22, 228, 68, 19, 3);
                }

                if (rules[2].Max > 1 && (rules[2].Min <= 2 || rules[2].Max == 2)) {
                    drawTexturedModalRect(w + 113, h + 22, 228, 68, 19, 3);
                }
            }

        }

        public IIcon getIconFromRule(int action) {
            switch(action) {
                case 0:
                    return TFC_Textures.anvilHit;
                case 1:
                    return TFC_Textures.anvilDraw;
                case 2:
                default:
                    return TFC_Textures.invisibleTexture;
                case 3:
                    return TFC_Textures.anvilPunch;
                case 4:
                    return TFC_Textures.anvilBend;
                case 5:
                    return TFC_Textures.anvilUpset;
                case 6:
                    return TFC_Textures.anvilShrink;
            }
        }

        @Override
        public boolean func_146978_c(int slotX, int slotY, int sizeX, int sizeY, int clickX, int clickY) {
            int k1 = guiLeft;
            int l1 = guiTop;
            clickX -= k1;
            clickY -= l1;
            return clickX >= slotX - 1 && clickX < slotX + sizeX + 1 && clickY >= slotY - 1 && clickY < slotY + sizeY + 1;
        }

        @Override
        public void drawTexturedModelRectFromIcon(int x, int y, IIcon par3Icon, int width, int height) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(x + 0, y + height, zLevel, par3Icon.getMinU(), par3Icon.getMaxV());
            tessellator.addVertexWithUV(x + width, y + height, zLevel, par3Icon.getMaxU(), par3Icon.getMaxV());
            tessellator.addVertexWithUV(x + width, y + 0, zLevel, par3Icon.getMaxU(), par3Icon.getMinV());
            tessellator.addVertexWithUV(x + 0, y + 0, zLevel, par3Icon.getMinU(), par3Icon.getMinV());
            tessellator.draw();
        }

        public static class Button extends GuiButton {
            public IIcon icon;
            public int bX;
            public int bY;
            public int bW;
            public int bH;
            public int ruleIndex;
            public AnvilGUIWithFastBagAccess screen;
            public byte red = -1;
            public byte blue = -1;
            public byte green = -1;

            public Button(int index, int xPos, int yPos, int width, int height, IIcon ico, int buttonX, int buttonY, int buttonW, int buttonH, AnvilGUIWithFastBagAccess gui, String s) {
                super(index, xPos, yPos, width, height, s);
                this.icon = ico;
                this.bX = buttonX;
                this.bY = buttonY;
                this.bW = buttonW;
                this.bH = buttonH;
                this.screen = gui;
            }

            public Button(int index, int xPos, int yPos, int width, int height, int buttonX, int buttonY, int buttonW, int buttonH, AnvilGUIWithFastBagAccess gui, int i, byte r, byte g, byte b) {
                super(index, xPos, yPos, width, height, "");
                this.bX = buttonX;
                this.bY = buttonY;
                this.bW = buttonW;
                this.bH = buttonH;
                this.screen = gui;
                this.ruleIndex = i;
                this.red = r;
                this.green = g;
                this.blue = b;
            }

            @Override
            public void drawButton(Minecraft mc, int x, int y) {
                if (visible) {
                    int k = getHoverState(field_146123_n) - 1;
                    if (icon == null) {
                        k = 0;
                        if (screen.anvilTE != null && screen.anvilTE.workRecipe != null) {
                            PlanRecipe p = AnvilManager.getInstance().getPlan(screen.anvilTE.craftingPlan);
                            if (p == null) {
                                return;
                            }

                            RuleEnum[] rules = p.rules;
                            displayString = TFC_Core.translate(rules[ruleIndex].Name);
                        }
                    }

                    TFC_Core.bindTexture(GuiAnvil.texture);
                    GL11.glColor4ub(red, green, blue, (byte)-1);
                    drawTexturedModalRect(xPosition, yPosition, bX + k * 16, bY + ruleIndex * 22, bW, bH);
                    field_146123_n = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
                    GL11.glColor4f(1, 1, 1, 1);
                    if (icon != null) {
                        TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                        drawTexturedModelRectFromIcon(xPosition, yPosition, icon, width, height);
                    }

                    mouseDragged(mc, x, y);
                    if (field_146123_n) {
                        screen.drawTooltip(x, y, displayString);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                }
            }
        }

        public static class PlanButton extends GuiButton {
            public AnvilGUIWithFastBagAccess screen;

            public PlanButton(int index, int xPos, int yPos, int width, int height, AnvilGUIWithFastBagAccess gui, String s) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
            }

            @Override
            public void drawButton(Minecraft mc, int x, int y) {
                if (visible) {
                    int k = getHoverState(field_146123_n) - 1;
                    TFC_Core.bindTexture(GuiAnvil.texture);
                    GL11.glColor4f(1, 1, 1, 1);
                    zLevel = 300;
                    drawTexturedModalRect(xPosition, yPosition, 0, 205 + k * 18, 18, 18);
                    field_146123_n = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
                    GL11.glColor4f(1, 1, 1, 1);
                    if (screen.anvilTE != null && !screen.anvilTE.craftingPlan.equals("") && screen.anvilTE.workRecipe != null) {
                        renderInventorySlot(screen.anvilTE.workRecipe.getCraftingResult(), xPosition + 1, yPosition + 1);
                    } else {
                        this.renderInventorySlot(new ItemStack(TFCItems.blueprint), xPosition + 1, yPosition + 1);
                    }

                    zLevel = 0;
                    mouseDragged(mc, x, y);
                    if (field_146123_n) {
                        screen.drawTooltip(x, y, displayString);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                }

            }

            public void renderInventorySlot(ItemStack is, int x, int y) {
                if (is != null) {
                    itemRenderer.renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(), is, x, y);
                }
            }
        }
    }

    public static class NestBoxGUIWithFastBagAccess extends GuiContainerTFC {
        public NestBoxGUIWithFastBagAccess(Container nestBoxContainer) {
            super(nestBoxContainer, 176, 85);
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiNestBox.texture);
        }
    }

    public static class SluiceGUIWithFastBagAccess extends GuiContainerTFC {
        public TESluice sluiceTE;

        public SluiceGUIWithFastBagAccess(Container sluiceContainer, TESluice sluice) {
            super(sluiceContainer, 176, 85);
            sluiceTE = sluice;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
            drawGui(GuiSluice.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            if (sluiceTE.waterInput && sluiceTE.waterOutput) {
                int l = 12;
                drawTexturedModalRect(guiLeft + 80, guiTop + 36 + 12 - 19 - l, 176, 12 - l, 14, l + 2);
            }

            int scale = sluiceTE.getProcessScaled(24);
            drawTexturedModalRect(guiLeft + 76, guiTop + 34, 176, 14, scale + 1, 16);
        }

        @Override
        public void drawGuiContainerForegroundLayer(int par1, int par2) {
            if (sluiceTE.soilAmount != -1) {
                fontRendererObj.drawString(TFC_Core.translate("gui.Sluice.Soil") + ": " + sluiceTE.soilAmount + "/50", 15, 39, 4210752);
            } else {
                fontRendererObj.drawString(TFC_Core.translate("gui.Sluice.Overworked"), 10, 39, 4210752);
            }

        }
    }

    public static class HorseGuiWithFastBagAccess extends GuiContainerTFC {
        public EntityHorseTFC horse;
        public float xSize;
        public float ySize;

        public HorseGuiWithFastBagAccess(Container horseContainer, EntityHorseTFC horse) {
            super(horseContainer, 176, 85);
            this.horse = horse;
            allowUserInput = false;
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {
                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void drawGuiContainerForegroundLayer(int par1, int par2) {
            if (horse.hasCustomNameTag()) {
                fontRendererObj.drawString(horse.getCustomNameTag(), 8, 6, 4210752);
            }
        }

        @Override
        public void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
            drawGui(GuiScreenHorseInventoryTFC.texture);
        }

        @Override
        public void drawForeground(int guiLeft, int guiTop) {
            if (horse.isChested()) {
                drawTexturedModalRect(guiLeft + 79, guiTop + 17, 0, getShiftedYSize() + 1, 90, 54);
            }
            if (horse.func_110259_cr()) {
                drawTexturedModalRect(guiLeft + 7, guiTop + 35, 0, getShiftedYSize() + 55, 18, 18);
            }

            float rY = horse.renderYawOffset;
            float pRY = horse.prevRenderYawOffset;
            horse.renderYawOffset = 0.0F;
            horse.prevRenderYawOffset = 0.0F;
            GuiInventory.func_147046_a(guiLeft + 51, guiTop + 60, 17, (float)(guiLeft + 51) - xSize, (float)(guiTop + 75 - 50) - ySize, horse);
            horse.renderYawOffset = rY;
            horse.prevRenderYawOffset = pRY;
        }

        @Override
        public void drawScreen(int par1, int par2, float par3) {
            xSize = (float)par1;
            ySize = (float)par2;
            super.drawScreen(par1, par2, par3);
        }
    }
}