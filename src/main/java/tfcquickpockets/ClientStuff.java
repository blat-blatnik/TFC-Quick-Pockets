// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

// CHANGES
// - Fixed potential bug with some mods rebinding the keys to weird values.

package tfcquickpockets;

import com.dunk.tfc.Core.Player.BodyTempStats;
import com.dunk.tfc.Core.Player.PlayerInventory;
import com.dunk.tfc.Core.Player.PlayerManagerTFC;
import com.dunk.tfc.Core.TFC_Core;
import com.dunk.tfc.Core.TFC_Textures;
import com.dunk.tfc.Core.TFC_Time;
import com.dunk.tfc.Entities.Mobs.EntityCowTFC;
import com.dunk.tfc.Entities.Mobs.EntityHorseTFC;
import com.dunk.tfc.Food.ItemFoodTFC;
import com.dunk.tfc.Food.ItemMeal;
import com.dunk.tfc.GUI.*;
import com.dunk.tfc.GUI.GuiHopper;
import com.dunk.tfc.Handlers.Client.RenderOverlayHandler;
import com.dunk.tfc.ItemSetup;
import com.dunk.tfc.Items.*;
import com.dunk.tfc.Items.ItemBlocks.ItemFlowers;
import com.dunk.tfc.Items.ItemBlocks.ItemSapling;
import com.dunk.tfc.Items.ItemCoal;
import com.dunk.tfc.Items.Pottery.ItemPotteryJug;
import com.dunk.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.dunk.tfc.Items.Tools.*;
import com.dunk.tfc.Render.TESR.TESRChest;
import com.dunk.tfc.TerraFirmaCraft;
import com.dunk.tfc.TileEntities.*;
import com.dunk.tfc.api.Crafting.*;
import com.dunk.tfc.api.Entities.IAnimal;
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
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.*;
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
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.item.ItemShears;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class ClientStuff extends ClientAndServerStuff {

    public static int HOTBAR_SIZE = 9; // InventoryPlayer.getHotbarSize() causes a very strange crash <.<
    public static int INVENTORY_ROW_SIZE = 9;
    public static double BACKGROUND_FILTER_TRANSITION_SECONDS = 0.15;
    public static int SOUND_TICKS_BEFORE_REPEAT = 2;

    public static ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
    public static ResourceLocation BAG_OPEN = new ResourceLocation("tfcquickpockets", "bag.open");
    public static ResourceLocation FISHING_CAST = new ResourceLocation("tfcquickpockets", "fishing.cast");
    public static ResourceLocation FISHING_REEL = new ResourceLocation("tfcquickpockets", "fishing.reel");
    public static ResourceLocation FISHING_RETRIEVE = new ResourceLocation("tfcquickpockets", "fishing.retrieve");
    public static ResourceLocation LEASH_PLACE = new ResourceLocation("tfcquickpockets", "leash.place");
    public static ResourceLocation LEASH_BREAK = new ResourceLocation("tfcquickpockets", "leash.break");
    public static ResourceLocation FIRE_CRACKLE = new ResourceLocation("tfcquickpockets", "fire.crackle");
    public static ResourceLocation FIRE = new ResourceLocation("fire.fire");
    public static ResourceLocation ATTACK_SWORD = new ResourceLocation("tfcquickpockets", "attack.sword");
    public static ResourceLocation ATTACK_MACE = new ResourceLocation("tfcquickpockets", "attack.mace");
    public static ResourceLocation ATTACK_AXE = new ResourceLocation("tfcquickpockets", "attack.axe");
    public static ResourceLocation ATTACK_STRONG = new ResourceLocation("tfcquickpockets", "attack.strong");
    public static ResourceLocation ATTACK_WEAK = new ResourceLocation("tfcquickpockets", "attack.weak");
    public static ResourceLocation BOW_NOCK = new ResourceLocation("tfcquickpockets", "bow.nock");
    public static ResourceLocation BUCKET_FILL = new ResourceLocation("tfcquickpockets", "bucket.fill");
    public static ResourceLocation BUCKET_FILL_VISCOUS = new ResourceLocation("tfcquickpockets", "bucket.fill.viscous");
    public static ResourceLocation BUCKET_EMPTY = new ResourceLocation("tfcquickpockets", "bucket.fill");
    public static ResourceLocation BUCKET_EMPTY_VISCOUS = new ResourceLocation("tfcquickpockets", "bucket.fill.viscous");
    public static ResourceLocation COW_MILK = new ResourceLocation("tfcquickpockets", "cow.milk");
    public static ResourceLocation FLUID_SOAK = new ResourceLocation("tfcquickpockets", "fluid.soak");
    public static ResourceLocation FLUID_SOAK_VISCOUS = new ResourceLocation("tfcquickpockets", "fluid.soak.viscous");
    public static ResourceLocation FLUID_UNSOAK = new ResourceLocation("tfcquickpockets", "fluid.unsoak");
    public static ResourceLocation FLUID_UNSOAK_VISCOUS = new ResourceLocation("tfcquickpockets", "fluid.unsoak.viscous");
    public static ResourceLocation FLUID_EMPTY = new ResourceLocation("tfcquickpockets", "fluid.empty");
    public static ResourceLocation FLUID_EMPTY_VISCOUS = new ResourceLocation("tfcquickpockets", "fluid.empty.viscous");
    public static ResourceLocation BARREL_UNSEAL = new ResourceLocation("tfcquickpockets", "barrel.unseal");
    public static ResourceLocation BARREL_SEAL = new ResourceLocation("tfcquickpockets", "barrel.seal");
    public static ResourceLocation LARGE_VESSEL_UNSEAL = new ResourceLocation("tfcquickpockets", "large.vessel.unseal");
    public static ResourceLocation LARGE_VESSEL_SEAL = new ResourceLocation("tfcquickpockets", "large.vessel.seal");
    public static ResourceLocation BEEHIVE_OPEN = new ResourceLocation("tfcquickpockets", "beehive.open");
    public static ResourceLocation BEEHIVE_CLOSE = new ResourceLocation("tfcquickpockets", "beehive.close");
    public static ResourceLocation BEEHIVE_DRIP = new ResourceLocation("tfcquickpockets", "beehive.drip");
    public static ResourceLocation CHEST_CLOSE = new ResourceLocation("tfcquickpockets", "chest.close");

    public static Minecraft minecraft;
    public static RenderItem itemRenderer = new RenderItem();
    public static ToolSwapBinding currentToolSwapKey; // if this is null then we're not cycling at the moment
    public static int[] toolSwapList = new int[64];
    public static int toolSwapListLength = 0;
    public static int toolSwapCurrentIndex = -1;
    public static int lastToolSwapSelectedSlot = -1;
    public static ItemStack[] lastToolSwapInventorySlots = new ItemStack[HOTBAR_SIZE + 3 * INVENTORY_ROW_SIZE];
    public static int selectedSlotAtStartOfMouseInput = -1;
    public static ItemStack lastHeldStack = null;
    public static int lastSelectedItem = -1;
    public static int switchHotbarSlotSoundWaitTicks = 0;
    public static int bucketSoundWaitTicks = 0;
    public static int bowNockSoundWaitTicks = 0;
    public static boolean skipNextBucketSound = false;
    public static ISound bowNockSound = null;
    public static HashSet<EntityLiving> leashedEntities = new HashSet<EntityLiving>();
    public static HashSet<EntityLiving> leashedEntitiesSwap = new HashSet<EntityLiving>();
    public static Random rng = new Random();
    public static Method syncCurrentPlayItem;
    public static Field remainingHighlightTicks;

    // --- for chest textures ---

    public static Field texNormal;
    public static Field texNormalDouble;

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

    public static KeyBinding cycleHotbar = new KeyBinding("key.swapHotbar", Keyboard.KEY_LMENU,"key.categories.inventory");
    public static ToolSwapBinding[] quickToolSwaps = {
        new ToolSwapBinding("key.swapToSword", 0, "key.categories.inventory", ItemCategory.SWORD),
        new ToolSwapBinding("key.swapToRanged", 0, "key.categories.inventory", ItemCategory.BOW, ItemCategory.JAVELIN),
        new ToolSwapBinding("key.swapToFood", 0, "key.categories.inventory", ItemCategory.FOOD),
        new ToolSwapBinding("key.swapToWater", 0, "key.categories.inventory", ItemCategory.DRINK),
        new ToolSwapBinding("key.swapToPickaxe", 0, "key.categories.inventory", ItemCategory.PICKAXE),
        new ToolSwapBinding("key.swapToProPick", 0, "key.categories.inventory", ItemCategory.PRO_PICK),
        new ToolSwapBinding("key.swapToAxe", 0, "key.categories.inventory", ItemCategory.AXE),
        new ToolSwapBinding("key.swapToSaw", 0, "key.categories.inventory", ItemCategory.SAW),
        new ToolSwapBinding("key.swapToShovel", 0, "key.categories.inventory", ItemCategory.SHOVEL),
        new ToolSwapBinding("key.swapToChisel", 0, "key.categories.inventory", ItemCategory.CHISEL),
        new ToolSwapBinding("key.swapToHammer", 0, "key.categories.inventory", ItemCategory.HAMMER),
        new ToolSwapBinding("key.swapToScythe", 0, "key.categories.inventory", ItemCategory.SCYTHE),
        new ToolSwapBinding("key.swapToFireStarter", 0, "key.categories.inventory", ItemCategory.FIRE_STARTER),
        new ToolSwapBinding("key.swapToKnife", 0, "key.categories.inventory", ItemCategory.KNIFE),
        new ToolSwapBinding("key.swapToHoe", 0, "key.categories.inventory", ItemCategory.HOE),
        new ToolSwapBinding("key.swapToTrowel", 0, "key.categories.inventory", ItemCategory.TROWEL),
        new ToolSwapBinding("key.swapToStaff", 0, "key.categories.inventory", ItemCategory.STAFF),
        new ToolSwapBinding("key.swapToFishingRod", 0, "key.categories.inventory", ItemCategory.FISHING_ROD),
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

        KeyBinding.unPressAllKeys();

        minecraft = Minecraft.getMinecraft();

        ClientRegistry.registerKeyBinding(cycleHotbar);
        for (ToolSwapBinding swapBinding : quickToolSwaps) {
            ClientRegistry.registerKeyBinding(swapBinding);
        }

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        //TODO: Can do this through access transformers as well. It's more clean so it might be worth doing.
        syncCurrentPlayItem = loadMethod(PlayerControllerMP.class, "syncCurrentPlayItem", "func_78750_j");
        remainingHighlightTicks = loadField(GuiIngame.class, "remainingHighlightTicks", "field_92017_k");

        //HACK: Such ugliness.. you just had to set stuff to private Dunk didn't you :P
        //MAINTENANCE: If any of these names change they need to be updated here as well.
        texNormal = loadField(TESRChest.class, "texNormal");
        texNormalDouble = loadField(TESRChest.class, "texNormalDouble");
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

        try {
            ResourceLocation[] singleChestTextures = (ResourceLocation[])texNormal.get(null);
            ResourceLocation[] doubleChestTextures = (ResourceLocation[])texNormalDouble.get(null);

            for (int i = 0; i < singleChestTextures.length; ++i) {
                String path = singleChestTextures[i].toString();
                if (path.contains("terrafirmacraftplus")) {
                    if (path.contains("Mahogany"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-mahogany-fixed.png");
                    else if (path.contains("Gingko"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-gingko-fixed.png");
                    else if (path.contains("Fruitwood"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-fruitwood-fixed.png");
                    else if (path.contains("Fever"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-fever-fixed.png");
                    else if (path.contains("Ebony"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-ebony-fixed.png");
                    else if (path.contains("Palm"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-palm-fixed.png");
                    else if (path.contains("Limba"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-limba-fixed.png");
                    else if (path.contains("Mangrove"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-mangrove-fixed.png");
                    else if (path.contains("Baobab"))
                        singleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-single-baobab-fixed.png");
                }
            }

            for (int i = 0; i < doubleChestTextures.length; ++i) {
                String path = doubleChestTextures[i].toString();
                if (path.contains("terrafirmacraftplus")) {
                    if (path.contains("Mahogany"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-mahogany-fixed.png");
                    else if (path.contains("Gingko"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-gingko-fixed.png");
                    else if (path.contains("Fruitwood"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-fruitwood-fixed.png");
                    else if (path.contains("Fever"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-fever-fixed.png");
                    else if (path.contains("Ebony"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-ebony-fixed.png");
                    else if (path.contains("Palm"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-palm-fixed.png");
                    else if (path.contains("Limba"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-limba-fixed.png");
                    else if (path.contains("Mangrove"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-mangrove-fixed.png");
                    else if (path.contains("Baobab"))
                        doubleChestTextures[i] = new ResourceLocation("tfcquickpockets", "chest-double-baobab-fixed.png");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void doQuickToolSwap(InputEvent.KeyInputEvent event) {
        if (!Config.clientOnlyMode) {
            for (ToolSwapBinding swapKeyBind : quickToolSwaps) {
                if (swapKeyBind.isPressed()) {
                    swapToolIntoHotbarSlot(swapKeyBind);
                    break;
                }
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
            if (!Config.clientOnlyMode && cycleHotbar.getIsKeyPressed()) {
                if (Config.invertHotbarCycleDirection)
                    delta = -delta;

                sendCycleInventoryRowsToServer(delta < 0);
                setCurrentPlayerItem(selectedSlotAtStartOfMouseInput);
            } else if (Config.skipEmptyInventorySlots) {

                ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;

                boolean wholeHotbarIsEmpty = true;
                for (int i = 0; i < 9; ++i)
                    if (inventory[i] != null) {
                        wholeHotbarIsEmpty = false;
                        break;
                    }

                // Don't skip when whole hotbar is empty --- it's sometimes fun to flip inventory spaces randomly :)
                if (!wholeHotbarIsEmpty) {
                    int slotIndex = selectedSlotAtStartOfMouseInput;
                    int direction = delta > 0 ? -1 : +1;
                    do {
                        slotIndex = slotIndex + direction;
                        if (slotIndex < 0)
                            slotIndex = 8;
                        if (slotIndex > 8)
                            slotIndex = 0;
                    } while (inventory[slotIndex] == null && slotIndex != selectedSlotAtStartOfMouseInput);
                    setCurrentPlayerItem(slotIndex);
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void makeGUIsHaveQuickContainerAccess(GuiOpenEvent event) {

        if (event.gui instanceof GuiContainer) {
            stopSwappingToolsInHotbarSlot();

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
        if (Config.enableHotbarCyclePreview && cycleHotbar.getIsKeyPressed()) {
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
        if (Config.enableHotbarCyclePreview && cycleHotbar.getIsKeyPressed()) {
            event.posY -= 32;
        }
    }

    //@SubscribeEvent @SuppressWarnings("unused")
    //public void playFishingRodSounds(PlayerUseItemEvent.Tick event) {
    //    if (Config.enableFishingRodSounds && event.entityPlayer != null && event.item != null && event.item.getItem() instanceof ItemCustomFishingRod) {
    //        ItemStack fishingRod = event.item;
    //        NBTTagCompound tag = fishingRod.getTagCompound();
    //        System.out.printf("using fishing rod: %s", event.item);
    //        if (tag != null) {
    //            boolean fishing = tag.hasKey("fishing") && tag.getBoolean("fishing");
    //            boolean lineSet = tag.hasKey("lineSet") && tag.getBoolean("lineSet");
    //            if (fishing && lineSet) {
    //                playSound(FISHING_REEL, event.entityPlayer, 1, 1);
    //            }
    //            if (tag.hasKey("lineSet"))
    //                System.out.printf(", lineSet: %b", tag.getBoolean("lineSet"));
    //            if (tag.hasKey("tickReeledIn"))
    //                System.out.printf(", tickReeledIn: %d", tag.getLong("tickReeledIn"));
    //            if (tag.hasKey("fishing"))
    //                System.out.printf(", fishing: %b", tag.getBoolean("fishing"));
    //        }
    //        System.out.printf("\n");
    //    }
    //}

    @SubscribeEvent @SuppressWarnings("unused")
    public void doAutoRefillAndFixWaterskin(TickEvent.ClientTickEvent event) {
        if (minecraft.thePlayer != null) {

            if (event.phase == TickEvent.Phase.START) {
                --bucketSoundWaitTicks;
                if (bucketSoundWaitTicks < 0)
                    bucketSoundWaitTicks = 0;
                --bowNockSoundWaitTicks;
                if (bowNockSoundWaitTicks < 0)
                    bowNockSoundWaitTicks = 0;
                --switchHotbarSlotSoundWaitTicks;
                if (switchHotbarSlotSoundWaitTicks < 0)
                    switchHotbarSlotSoundWaitTicks = 0;

                if (allowWalkInInventoryScreen(minecraft.currentScreen)) {
                    GameSettings settings = minecraft.gameSettings;

                    int forward = settings.keyBindForward.getKeyCode();
                    int back = settings.keyBindBack.getKeyCode();
                    int left = settings.keyBindLeft.getKeyCode();
                    int right = settings.keyBindRight.getKeyCode();

                    int sprint = settings.keyBindSprint.getKeyCode();
                    boolean sprintIsDown = isKeyDown(sprint);

                    KeyBinding.setKeyBindState(forward, sprintIsDown && isKeyDown(forward));
                    KeyBinding.setKeyBindState(back, sprintIsDown && isKeyDown(back));
                    KeyBinding.setKeyBindState(left, sprintIsDown && isKeyDown(left));
                    KeyBinding.setKeyBindState(right, sprintIsDown && isKeyDown(right));

                    KeyBinding.onTick(forward);
                    KeyBinding.onTick(back);
                    KeyBinding.onTick(left);
                    KeyBinding.onTick(right);

                    if (Config.allowJumpingInInventory) {
                        int jump = settings.keyBindJump.getKeyCode();
                        KeyBinding.setKeyBindState(jump, isKeyDown(jump));
                        KeyBinding.onTick(jump);
                    }
                }

                checkIfNeedToDoAutoFill();

            } else if (event.phase == TickEvent.Phase.END) {

                checkIfNeedToDoAutoFill();

                if (Config.enableRopeSounds) {
                    leashedEntitiesSwap.clear();
                    for (EntityLiving entity : leashedEntities)
                        if (entity.getLeashed())
                            leashedEntitiesSwap.add(entity);

                    HashSet<EntityLiving> temp = leashedEntitiesSwap;
                    leashedEntitiesSwap = leashedEntities;
                    leashedEntities = leashedEntitiesSwap;
                }

                boolean canPlayFireSounds =
                        minecraft != null &&
                        minecraft.theWorld != null &&
                        minecraft.thePlayer != null &&
                        !minecraft.isGamePaused() &&
                        (Config.enableFirepitSounds || Config.enableBloomerySounds || Config.enableBlastFurnaceSounds);

                if (canPlayFireSounds) {
                    @SuppressWarnings("unchecked")
                    List<TileEntity> loadedTileEntities = minecraft.theWorld.loadedTileEntityList;
                    for (TileEntity entity : loadedTileEntities) {

                        int x = entity.xCoord;
                        int y = entity.yCoord;
                        int z = entity.zCoord;
                        double distanceToPlayer = minecraft.thePlayer.getDistanceSq(x + 0.5f, y + 0.5f, z + 0.5f);

                        if (distanceToPlayer < 16*16) {
                            if (Config.enableFirepitSounds && rng.nextFloat() > 0.95 && entity instanceof TEFirepit) {
                                TEFirepit fire = (TEFirepit)minecraft.theWorld.getTileEntity(x, y, z);
                                if (fire != null) {

                                    int meta = fire.getBlockMetadata();
                                    if (meta > 0) { //ASSUMPTION: meta > 0 means the firepit is lit right now.
                                        float tempFactor = fire.fireTemp / 700.0f;
                                        float loudness = 0.05f + tempFactor;
                                        float pitch = 1.2f - 0.4f * tempFactor;

                                        //System.out.printf("Crackle meta: %d, fireTemp: %.1f, loudness: %.2f, pitch: %.2f\n", meta, fire.fireTemp, loudness, pitch);
                                        playSound(FIRE_CRACKLE, fire, loudness, pitch);
                                    }
                                }
                            } else if (Config.enableBloomerySounds && rng.nextFloat() > 0.95 && entity instanceof TEBloomery) {
                                TEBloomery bloomery = (TEBloomery)entity;
                                int meta = bloomery.getBlockMetadata();
                                if (bloomery.bloomeryLit || (meta & 4) != 0) { //ASSUMPTION: meta & 0b100 means the bloomery is lit
                                    float loudness = 0.4f + rng.nextFloat() / 2;
                                    float pitch = 0.4f + rng.nextFloat() / 2;
                                    playSound(FIRE, bloomery, loudness, pitch);
                                }
                            } else if (Config.enableBlastFurnaceSounds && rng.nextFloat() > 0.95 && entity instanceof TEBlastFurnace) {
                                TEBlastFurnace furnace = (TEBlastFurnace)entity;
                                int meta = furnace.getBlockMetadata();
                                //float temp = furnace.fireTemp; TODO: Why doesn't this work unless inventory is open???
                                //System.out.printf("Furnace meta: %d, temp: %.1f\n", meta, temp);
                                if ((meta & 4) != 0) { //ASSUMPTION: meta & 0b100 means the bloomery is lit
                                    float loudness = 0.4f + rng.nextFloat() / 2;
                                    float pitch = 0.4f + rng.nextFloat();
                                    playSound(FIRE, furnace, loudness, pitch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void playMeleeWeaponSound(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (Config.enableMeleeWeaponSounds && player != null) {
            if (PlayerManagerTFC.getInstance().getPlayerInfoFromPlayer(player).canAttack()) {

                ItemStack weapon = player.inventory.getCurrentItem();
                ItemCategory category = ItemCategory.OTHER;
                if (weapon != null)
                    category = ItemCategory.get(weapon);

                switch (category) {
                    case SWORD:
                    case STAFF:
                        playSound(ATTACK_SWORD, minecraft.thePlayer, 1, 1);
                        break;
                    case AXE:
                    case JAVELIN:
                        playSound(ATTACK_AXE, minecraft.thePlayer, 1, 1);
                        break;
                    case MACE:
                        playSound(ATTACK_MACE, minecraft.thePlayer, 1, 1);
                        break;
                    default:
                        int broad = category.getBroadCategoryFlags();
                        if ((broad & ItemCategory.WEAPON_FLAG) != 0 || (broad & ItemCategory.TOOL_FLAG) != 0)
                            playSound(ATTACK_STRONG, minecraft.thePlayer, 1, 1);
                        else
                            playSound(ATTACK_WEAK, minecraft.thePlayer, 1, 1);
                        break;
                }

            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void playBowNockSound(ArrowNockEvent event) {
        if (Config.enableBowWeaponSounds && bowNockSoundWaitTicks <= 0) {
            bowNockSoundWaitTicks = SOUND_TICKS_BEFORE_REPEAT;
            //System.out.println("\nArrow nock");
            EntityPlayer player = event.entityPlayer;

            ItemStack[] inventory = player.inventory.mainInventory;
            for (int i = 0; i < HOTBAR_SIZE; ++i) {
                ItemStack stack = inventory[i];
                if (stack != null && stack.getItem() instanceof ItemArrow && stack.stackSize > 0) {
                    bowNockSound = playSound(BOW_NOCK, player, 1, 1);
                    break;
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void stopBowNockSound(ArrowLooseEvent event) {
        System.out.println("\nArrow fire");
        stopSound(bowNockSound);
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void playCowMilkingSound(EntityInteractEvent event) {

        EntityPlayer player = event.entityPlayer;
        Entity target = event.target;
        if (player != null && target != null && bucketSoundWaitTicks <= 0) {

            ItemStack currentStack = player.inventory.getCurrentItem();
            if (currentStack != null) {
                Item currentItem = currentStack.getItem();
                if (currentItem == ItemSetup.woodenBucketEmpty || currentItem == ItemSetup.clayBucketEmpty) {

                    boolean canMilkEntity = false;

                    if (target instanceof EntityCowTFC) {
                        EntityCowTFC cow = (EntityCowTFC)target;

                        canMilkEntity =
                                cow.isMilkable() &&
                                !(player.isSneaking() && !cow.getFamiliarizedToday() && cow.canFamiliarize()) &&
                                cow.getGender() == IAnimal.GenderEnum.FEMALE &&
                                cow.isAdult() &&
                                cow.getHasMilkTime() < TFC_Time.getTotalTicks() &&
                                cow.checkFamiliarity(IAnimal.InteractionEnum.MILK, player);

                    }

                    if (canMilkEntity) {
                        skipNextBucketSound = true;
                        playSound(COW_MILK, target, 1, 1);
                        bucketSoundWaitTicks = SOUND_TICKS_BEFORE_REPEAT;
                    }
                }
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void playLeashPlacedSound(EntityJoinWorldEvent event) {
        if (Config.enableRopeSounds) {
            Entity entity = event.entity;
            if (entity instanceof EntityLeashKnot) {
                playSound(LEASH_PLACE, entity, 0.9f, 1);
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void playLeashBreakSound(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entityBase = event.entityLiving;
        if (entityBase instanceof EntityLiving) {
            EntityLiving entity = (EntityLiving)entityBase;

            boolean isKnownToBeLeashed = leashedEntities.contains(entity);

            if (!entity.getLeashed() && isKnownToBeLeashed) {
                System.out.println("\nnum leashed entities: " + leashedEntities.size());
                playSound(LEASH_BREAK, entity, 0.9f, 1);
                leashedEntities.remove(entity);
            } else if (entity.getLeashed() && !isKnownToBeLeashed) {
                leashedEntities.add(entity);
            }
        }
    }

    @SubscribeEvent @SuppressWarnings("unused")
    public void stackFoodOnPickup(EntityItemPickupEvent event) {
        if (Config.autoStackFoodOnPickup && event.item != null && event.entityPlayer != null) {
            ItemStack stack = event.item.getEntityItem();
            if (stack != null) {
                Item item = stack.getItem();
                if (item != null) {
                    if (item instanceof ItemFoodTFC) {

                        EntityPlayer player = event.entityPlayer;
                        int nextFreeSlot = player.inventory.getFirstEmptyStack();
                        if (nextFreeSlot >= 0) {
                            int foodSlotIndex = 9 + convertInventoryIndexToSlotIndex(player.inventory.getFirstEmptyStack());
                            sendStackFoodToServer(foodSlotIndex);
                        }
                    }
                }
            }
        }
    }

    public static void checkIfNeedToDoAutoFill() {
        if (minecraft.thePlayer != null) {
            InventoryPlayer playerInventory = minecraft.thePlayer.inventory;

            if (playerInventory != null) {

                int currentSelectedItem = playerInventory.currentItem;
                ItemStack[] inventory = playerInventory.mainInventory;
                ItemStack currentHeldStack = inventory[playerInventory.currentItem];

                boolean hotbarSlotSwitched = lastSelectedItem != currentSelectedItem && lastSelectedItem >= 0;

                if (hotbarSlotSwitched && switchHotbarSlotSoundWaitTicks <= 0 && Config.enableSwitchHotbarSlotSound) {

                    switchHotbarSlotSoundWaitTicks = SOUND_TICKS_BEFORE_REPEAT;
                    playSound(BAG_OPEN, minecraft.thePlayer, 0.5f, 1.2f);

                } else if (lastSelectedItem == currentSelectedItem) {

                    boolean heldItemSuddenlyChanged =
                            (lastHeldStack != null && currentHeldStack == null) ||
                            (lastHeldStack != null && lastHeldStack.getItem() != currentHeldStack.getItem());

                    if (heldItemSuddenlyChanged) {
                        GameSettings settings = minecraft.gameSettings;

                        boolean itWasBecauseHeldItemGotUsedOrDestroyed =
                                minecraft.currentScreen == null && !settings.keyBindDrop.getIsKeyPressed() &&
                                (settings.keyBindAttack.getIsKeyPressed() || settings.keyBindUseItem.getIsKeyPressed());

                        if (itWasBecauseHeldItemGotUsedOrDestroyed) {

                            boolean canCheckForBucketSounds =
                                    Config.enableBucketSounds &&
                                    bucketSoundWaitTicks <= 0 &&
                                    lastHeldStack != null &&
                                    lastHeldStack.getItem() != null &&
                                    currentHeldStack != null &&
                                    currentHeldStack.getItem() != null;

                            if (canCheckForBucketSounds) {
                                Item lastItem = lastHeldStack.getItem();
                                Item currItem = currentHeldStack.getItem();

                                boolean lastItemIsBucket =
                                        lastItem == ItemSetup.woodenBucketEmpty || lastItem == ItemSetup.clayBucketEmpty ||
                                        lastItem == ItemSetup.woodenBucketWater || lastItem == ItemSetup.clayBucketWater ||
                                        lastItem == ItemSetup.woodenBucketSaltWater || lastItem == ItemSetup.clayBucketSaltWater ||
                                        lastItem == ItemSetup.woodenBucketMilk || lastItem == ItemSetup.clayBucketMilk ||
                                        lastItem == ItemSetup.woodenBucketVinegar || lastItem == ItemSetup.clayBucketVinegar ||
                                        lastItem == ItemSetup.woodenBucketHoney || lastItem == ItemSetup.clayBucketHoney ||
                                        lastItem == ItemSetup.woodenBucketPitch || lastItem == ItemSetup.clayBucketPitch;

                                boolean currItemIsBucket =
                                        currItem == ItemSetup.woodenBucketEmpty || currItem == ItemSetup.clayBucketEmpty ||
                                        currItem == ItemSetup.woodenBucketWater || currItem == ItemSetup.clayBucketWater ||
                                        currItem == ItemSetup.woodenBucketSaltWater || currItem == ItemSetup.clayBucketSaltWater ||
                                        currItem == ItemSetup.woodenBucketMilk || currItem == ItemSetup.clayBucketMilk ||
                                        currItem == ItemSetup.woodenBucketVinegar || currItem == ItemSetup.clayBucketVinegar ||
                                        currItem == ItemSetup.woodenBucketHoney || currItem == ItemSetup.clayBucketHoney ||
                                        currItem == ItemSetup.woodenBucketPitch || currItem == ItemSetup.clayBucketPitch;

                                if (lastItemIsBucket && currItemIsBucket) {

                                    boolean lastItemIsClayBucket =
                                            lastItem == ItemSetup.clayBucketEmpty ||
                                            lastItem == ItemSetup.clayBucketWater ||
                                            lastItem == ItemSetup.clayBucketSaltWater ||
                                            lastItem == ItemSetup.clayBucketMilk ||
                                            lastItem == ItemSetup.clayBucketVinegar ||
                                            lastItem == ItemSetup.clayBucketHoney ||
                                            lastItem == ItemSetup.clayBucketPitch;
                                    boolean lastItemIsRedSteelBucket =
                                            lastItem == ItemSetup.redSteelBucketEmpty ||
                                            lastItem == ItemSetup.redSteelBucketWater ||
                                            lastItem == ItemSetup.redSteelBucketSaltWater;
                                    boolean lastItemIsBlueSteelBucket =
                                            lastItem == ItemSetup.blueSteelBucketEmpty ||
                                            lastItem == ItemSetup.blueSteelBucketLava;

                                    boolean currItemIsClayBucket =
                                            currItem == ItemSetup.clayBucketEmpty ||
                                            currItem == ItemSetup.clayBucketWater ||
                                            currItem == ItemSetup.clayBucketSaltWater ||
                                            currItem == ItemSetup.clayBucketMilk ||
                                            currItem == ItemSetup.clayBucketVinegar ||
                                            currItem == ItemSetup.clayBucketHoney ||
                                            currItem == ItemSetup.clayBucketPitch;
                                    boolean currItemIsRedSteelBucket =
                                            currItem == ItemSetup.redSteelBucketEmpty ||
                                            currItem == ItemSetup.redSteelBucketWater ||
                                            currItem == ItemSetup.redSteelBucketSaltWater;
                                    boolean currItemIsBlueSteelBucket =
                                            currItem == ItemSetup.blueSteelBucketEmpty ||
                                            currItem == ItemSetup.blueSteelBucketLava;

                                    int lastBucketMaterial =
                                            lastItemIsClayBucket ? 1 :
                                            lastItemIsBlueSteelBucket ? 2 :
                                            lastItemIsRedSteelBucket ? 3 : 4;
                                    int currBucketMaterial =
                                            currItemIsClayBucket ? 1 :
                                            currItemIsBlueSteelBucket ? 2 :
                                            currItemIsRedSteelBucket ? 3 : 4;

                                    if (lastBucketMaterial == currBucketMaterial) {

                                        boolean lastItemIsEmptyBucket =
                                                lastItem == ItemSetup.woodenBucketEmpty ||
                                                lastItem == ItemSetup.clayBucketEmpty ||
                                                lastItem == ItemSetup.blueSteelBucketEmpty ||
                                                lastItem == ItemSetup.redSteelBucketEmpty;

                                        boolean currItemIsEmptyBucket =
                                                currItem == ItemSetup.woodenBucketEmpty ||
                                                currItem == ItemSetup.clayBucketEmpty ||
                                                currItem == ItemSetup.blueSteelBucketEmpty ||
                                                currItem == ItemSetup.redSteelBucketEmpty;;

                                        boolean fluidIsViscous =
                                                lastItem == ItemSetup.woodenBucketPitch || lastItem == ItemSetup.clayBucketPitch ||
                                                lastItem == ItemSetup.woodenBucketHoney || lastItem == ItemSetup.clayBucketHoney ||
                                                currItem == ItemSetup.woodenBucketPitch || currItem == ItemSetup.clayBucketPitch ||
                                                currItem == ItemSetup.woodenBucketHoney || currItem == ItemSetup.clayBucketHoney ||
                                                currItem == ItemSetup.blueSteelBucketLava;

                                        ResourceLocation bucketSound = null;


                                        if (lastItemIsEmptyBucket && !currItemIsEmptyBucket) {
                                            if (fluidIsViscous)
                                                bucketSound = BUCKET_FILL_VISCOUS;
                                            else
                                                bucketSound = BUCKET_FILL;
                                        } else if (!lastItemIsEmptyBucket && currItemIsEmptyBucket) {
                                            if (fluidIsViscous)
                                                bucketSound = BUCKET_EMPTY_VISCOUS;
                                            else
                                                bucketSound = BUCKET_EMPTY;
                                        }

                                        if (bucketSound != null) {
                                            bucketSoundWaitTicks = SOUND_TICKS_BEFORE_REPEAT;
                                            if (skipNextBucketSound)
                                                skipNextBucketSound = false;
                                            else
                                                playSound(bucketSound, minecraft.thePlayer, 1, 1);
                                        }
                                    }
                                }
                            }

                            if (!Config.clientOnlyMode) {
                                ItemCategory category = ItemCategory.get(lastHeldStack);
                                int broadCategory = category.getBroadCategoryFlags();

                                boolean doRefill = false;
                                if ((broadCategory & ItemCategory.TOOL_FLAG) != 0 && Config.autoRefillTools)
                                    doRefill = true;
                                else if ((broadCategory & ItemCategory.WEAPON_FLAG) != 0 && Config.autoRefillWeapons)
                                    doRefill = true;
                                else if ((broadCategory & ItemCategory.CONSUMABLE_FLAG) != 0 && Config.autoRefillFood)
                                    doRefill = true;
                                else if ((broadCategory & ItemCategory.BLOCK_FLAG) != 0 && Config.autoRefillBlocks)
                                    doRefill = true;
                                else if ((broadCategory & ItemCategory.MISC_FLAG) != 0 && Config.autoRefillMisc)
                                    doRefill = true;

                                if (doRefill)
                                    doAutoRefill(lastHeldStack);
                            }
                        }
                    }
                }

                lastSelectedItem = currentSelectedItem;
                lastHeldStack = currentHeldStack;
            }
        }
    }

    public static void doAutoRefill(ItemStack originalStack) {
        ItemStack closestMatch = findClosestMatchingStack(originalStack);
        if (closestMatch != null && closestMatch != originalStack) {

            EntityPlayer player = minecraft.thePlayer;

            int indexOriginal = -1;
            int indexClosestMatch = -1;

            ItemStack[] inventory = player.inventory.mainInventory;
            for (int i = 0; i < inventory.length; ++i) {
                if (inventory[i] == originalStack)
                    indexOriginal = i;
                if (inventory[i] == closestMatch)
                    indexClosestMatch = i;
            }

            if (indexOriginal < 0)
                indexOriginal = player.inventory.currentItem;

            if (indexOriginal >= 0 && indexClosestMatch >= 0 && indexOriginal != indexClosestMatch) {

                if (Config.enableAutoRefillSound)
                    playSound(BAG_OPEN, minecraft.thePlayer, 0.5f, 1.2f);

                int slot1 = convertInventoryIndexToSlotIndex(indexOriginal);
                int slot2 = convertInventoryIndexToSlotIndex(indexClosestMatch);
                sendSwapPlayerInventorySlotsToServer(slot1, slot2);
            }
        }
    }

    public static ItemStack findClosestMatchingStack(final ItemStack target) {
        ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;
        ItemStack[] sorted = new ItemStack[inventory.length];
        System.arraycopy(inventory, 0, sorted, 0, inventory.length);

        //TODO: This is for debugging purposes only
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
            ItemCategory category1 = ItemCategory.get(a);
            ItemCategory category2 = ItemCategory.get(b);

            int similarity = 0;
            if (item1 == item2 && item1.getUnlocalizedName(a).equals(item2.getUnlocalizedName(b)))
                similarity += 1;
            if (category1.matches(category2))
                similarity += 1;

            //if (item1 == item2 || item1.getUnlocalizedName().equals(item2.getUnlocalizedName()))
            //    similarity += 1;
            //if ((category1 == category2 && (category1 == ItemCategory.BLOCK || category1 == ItemCategory.OTHER)) && a.getItemDamage() != b.getItemDamage())
            //    similarity -= 1;
            //if (category1.matches(category2))
            //    similarity += 1;

            return similarity;
        }
    }

    public static void swapToolIntoHotbarSlot(ToolSwapBinding swapKey) {
        boolean canExecute =
                swapKey != null &&
                minecraft != null &&
                minecraft.thePlayer != null &&
                minecraft.thePlayer.inventory != null &&
                minecraft.thePlayer.inventory.mainInventory != null;

        if (canExecute) {

            InventoryPlayer playerInventory = minecraft.thePlayer.inventory;
            ItemStack[] inventory = playerInventory.mainInventory;
            int selectedSlot = playerInventory.currentItem;

            if (swapKey != currentToolSwapKey || selectedSlot != lastToolSwapSelectedSlot || !itemStacksHaveTheSameItems(lastToolSwapInventorySlots, inventory)) {
                stopSwappingToolsInHotbarSlot();
            }

            if (toolSwapCurrentIndex == -1) {

                // rebuild the toolCycleList

                toolSwapCurrentIndex = 0;
                toolSwapListLength = 0;
                toolSwapList[toolSwapListLength++] = selectedSlot;

                for (int i = selectedSlot + 1; i < HOTBAR_SIZE; ++i)
                    if (swapKey.slotMatches(inventory[i]))
                        toolSwapList[toolSwapListLength++] = i;

                for (int i = 0; i < selectedSlot; ++i)
                    if (swapKey.slotMatches(inventory[i]))
                        toolSwapList[toolSwapListLength++] = i;

                if (toolSwapListLength > 1)
                    toolSwapList[toolSwapListLength++] = selectedSlot;

                for (int i = inventory.length - 1; i >= HOTBAR_SIZE; --i)
                    if (swapKey.slotMatches(inventory[i]))
                        toolSwapList[toolSwapListLength++] = i;

                if (toolSwapListLength > 1 && toolSwapList[0] == toolSwapList[toolSwapListLength - 1])
                    toolSwapListLength--;

                if (toolSwapListLength >= toolSwapList.length) { // sanity check
                    stopSwappingToolsInHotbarSlot();
                    return;
                }
            }

            currentToolSwapKey = swapKey;
            int lastIndex = toolSwapList[toolSwapCurrentIndex];
            toolSwapCurrentIndex = (toolSwapCurrentIndex + 1) % toolSwapListLength;
            int nextIndex = toolSwapList[toolSwapCurrentIndex];

            if (lastIndex >= HOTBAR_SIZE) {
                int firstIndex = toolSwapList[0];
                int slot1 = convertInventoryIndexToSlotIndex(firstIndex);
                int slot2 = convertInventoryIndexToSlotIndex(lastIndex);
                sendSwapPlayerInventorySlotsToServer(slot1, slot2);
            }

            if (nextIndex < 0 || nextIndex >= inventory.length) {
                stopSwappingToolsInHotbarSlot();
            } else {
                if (nextIndex >= HOTBAR_SIZE) {
                    int slot1 = convertInventoryIndexToSlotIndex(nextIndex);
                    int slot2 = convertInventoryIndexToSlotIndex(selectedSlot);
                    sendSwapPlayerInventorySlotsToServer(slot1, slot2);
                    nextIndex = selectedSlot;
                }

                setCurrentPlayerItem(nextIndex);
                lastToolSwapSelectedSlot = nextIndex;
                System.arraycopy(inventory, 0, lastToolSwapInventorySlots, 0, inventory.length);
            }
        }
    }

    public static void stopSwappingToolsInHotbarSlot() {
        toolSwapCurrentIndex = -1;
        lastToolSwapSelectedSlot = -1;
        toolSwapListLength = 0;
        currentToolSwapKey = null;
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

        if (!Config.clientOnlyMode && slot1Index != slot2Index) {
            int slot1 = slot1Index + 9;
            int slot2 = slot2Index + 9;
            swapPlayerInventorySlots(minecraft.thePlayer, slot1, slot2);
            network.sendToServer(new SwapInventorySlotsPacket(slot1, slot2));
        }
    }

    public static boolean allowWalkInInventoryScreen(GuiScreen screen) {
        return Config.allowWalkInInventory && (screen instanceof GuiInventoryTFC || screen instanceof ContainerGUIWithFastBagAccess);
    }

    public static void sendStackFoodToServer(int foodSlot) {
        if (!Config.clientOnlyMode) {
            stackPlayerFood(minecraft.thePlayer, foodSlot);
            network.sendToServer(new StackFoodPacket(foodSlot));
        }
    }

    public static void sendCycleInventoryRowsToServer(boolean cycleUp) {
        if (!Config.clientOnlyMode) {
            cyclePlayerInventoryRows(minecraft.thePlayer, cycleUp);
            ItemStack[] inventory = minecraft.thePlayer.inventory.mainInventory;

            if (Config.enableHotbarCycleSound)
                playSound(BAG_OPEN, minecraft.thePlayer, 0.8f, 1.2f);

            for (int i = 0; i < HOTBAR_SIZE; ++i) {
                ItemStack stack = inventory[i];
                if (stack != null)
                    stack.animationsToGo = 3;
            }

            network.sendToServer(new CycleInventoryRowsPacket(cycleUp));
        }
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

    public static float getFluidViscosity(FluidStack stack) {
        if (stack != null) {
            Fluid fluid = stack.getFluid();
            if (fluid != null) {

                // these are all more or less completely arbitrary for now
                if (fluid == TFCFluids.PITCH)
                    return 10000;
                else if (fluid == TFCFluids.WAX)
                    return 5000;
                else if (fluid == TFCFluids.HONEY)
                    return 6000;
                else
                    return fluid.getViscosity(stack);
            }
        }

        return -1;
    }

    public static void playFluidSound(ResourceLocation sound, ResourceLocation viscousSound, TileEntity source, float viscosity, float loudness, float pitch) {
        final float VISCOSITY_THRESHOLD = 5000;
        if (viscosity >= VISCOSITY_THRESHOLD)
            playSound(viscousSound, source, loudness, pitch);
        else
            playSound(sound, source, loudness, pitch);
    }

    public static ISound playSound(ResourceLocation sound, float x, float y, float z, float loudness, float pitch) {
        PositionedSoundRecord record = new PositionedSoundRecord(sound, loudness, pitch, x, y, z);
        minecraft.getSoundHandler().playSound(record);
        return record;
    }

    public static ISound playSound(ResourceLocation sound, TileEntity source, float loudness, float pitch) {
        float x = source.xCoord + 0.5f;
        float y = source.yCoord + 0.5f;
        float z = source.zCoord + 0.5f;
        source.updateEntity();
        return playSound(sound, x, y, z, loudness, pitch);
    }

    public static ISound playSound(ResourceLocation sound, Entity source, float loudness, float pitch) {
        float x = (float)source.posX;
        float y = (float)source.posY - source.yOffset;
        float z = (float)source.posZ;
        return playSound(sound, x, y, z, loudness, pitch);
    }

    public static void stopSound(ISound sound) {
        minecraft.getSoundHandler().stopSound(sound);
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

    public static boolean itemStacksHaveTheSameItems(ItemStack[] a, ItemStack[] b) {
        if (a.length != b.length)
            return false;
        else {
            for (int i = 0; i < a.length; ++i) {
                if ((a[i] == null) != (b[i] == null))
                    return false;
                else if (a[i] != null && a[i].getItem() != b[i].getItem())
                    return false;
            }
            return true;
        }
    }

    public static boolean isKeyDown(int keycode) {
        // This looks a bit complicated because it's supposed to be able to handle both mouse buttons and keyboard keys.
        // Mouse button key codes from minecraft KeyBindings are always encoded as mouse button number - 100, so left
        // mouse button would be 0-100 = -100, right mouse button would be 1-100 = -99, etc.

        try {
            return Keyboard.isKeyDown(keycode);
        } catch (Exception notAKeyboardKey) {
            try {
                return Mouse.isButtonDown(100 + keycode);
            } catch (Exception notAMouseButtonEither) {
                return false;
            }
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
        PLASTER_BUCKET,
        SHEARS,
        FIRE_STARTER,
        FISHING_ROD,
        SEEDS,
        SAPLING,
        ROCK,
        ROCK_FLAKE,
        COAL,
        CLAY,
        STRAW,
        LOG,
        CONTAINER,
        FLOWERS,
        BLOCK,
        OTHER;

        public static int CONSUMABLE_FLAG = 1;
        public static int TOOL_FLAG       = 1 << 1;
        public static int WEAPON_FLAG     = 1 << 2;
        public static int BLOCK_FLAG      = 1 << 3;
        public static int MISC_FLAG       = 1 << 4;

        public static ItemCategory get(ItemStack stack) {
            Item item = stack.getItem();
            if (item == null)
                return ItemCategory.NONE;
            else if (item instanceof ItemCustomSword) {
                if (((ItemCustomSword) item).damageType == EnumDamageType.CRUSHING)
                    return ItemCategory.MACE;
                else
                    return ItemCategory.SWORD;
            } else if (item instanceof ItemCustomBow) {
                return ItemCategory.BOW;
            } else if (item instanceof ItemJavelin) {
                return ItemCategory.JAVELIN;
            } else if (item instanceof ItemFoodTFC || item instanceof ItemHoneyBowl || item instanceof ItemMeal) {
                return ItemCategory.FOOD;
            } else if (item instanceof ItemDrink || (item instanceof ItemPotteryJug && stack.getItemDamage() == 2)) {
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
            } else if (item instanceof ItemSapling || item instanceof ItemFruitTreeSapling) {
                return ItemCategory.SAPLING;
            } else if (item instanceof ItemCustomSeeds) {
                return ItemCategory.SEEDS;
            } else if (item instanceof ItemClay) {
                return ItemCategory.CLAY;
            } else if (item instanceof ItemLooseRock || item == ItemSetup.flatRock) {
                return ItemCategory.ROCK;
            } else if (item instanceof ItemHoe) {
                return ItemCategory.HOE;
            } else if (item instanceof ItemPlasterBucket) {
                return ItemCategory.PLASTER_BUCKET;
            } else if (item instanceof ItemCoal) {
                return ItemCategory.COAL;
            } else if (item instanceof ItemFlowers) {
                return ItemCategory.FLOWERS;
            } else if (item instanceof ItemBlock) {
                return ItemCategory.BLOCK;
            } else {
                return ItemCategory.OTHER;
            }
        }

        public boolean requiresExactMatch() {
            switch (this) {
                case AXE:
                case KNIFE:
                case HAMMER:
                case SAW:
                case HOE:
                case PICKAXE:
                case PRO_PICK:
                case CHISEL:
                case SHOVEL:
                case SCYTHE:
                case TROWEL:
                case SHEARS:
                case FIRE_STARTER:
                case FISHING_ROD:
                case PLASTER_BUCKET:
                case FOOD:
                case DRINK:
                case LOG:
                case ROCK:
                case ROCK_FLAKE:
                case SEEDS:
                case SAPLING:
                case CLAY:
                case STRAW:
                case COAL:
                case FLOWERS:
                    return false;
                case CONTAINER:
                case BLOCK:
                case OTHER:
                case NONE:
                default:
                    return true;
            }
        }

        public boolean matches(ItemCategory other) {
            if (requiresExactMatch() || other.requiresExactMatch())
                return false;
            else
                return this == other;
        }

        public int getBroadCategoryFlags() {
            switch (this) {
                case AXE:
                case KNIFE:
                case HAMMER:
                    return WEAPON_FLAG | TOOL_FLAG;
                case SAW:
                case HOE:
                case PICKAXE:
                case PRO_PICK:
                case CHISEL:
                case SHOVEL:
                case SCYTHE:
                case TROWEL:
                case SHEARS:
                case FIRE_STARTER:
                case FISHING_ROD:
                case PLASTER_BUCKET:
                    return TOOL_FLAG;
                case FOOD:
                case DRINK:
                    return CONSUMABLE_FLAG;
                case LOG:
                case ROCK:
                case ROCK_FLAKE:
                case CONTAINER:
                case SEEDS:
                case SAPLING:
                case CLAY:
                case STRAW:
                case COAL:
                case FLOWERS:
                case OTHER:
                    return MISC_FLAG;
                case BLOCK:
                    return BLOCK_FLAG;
                case NONE:
                default:
                    return 0;
            }
        }
    }

    public static class ToolSwapBinding extends KeyBinding {
        public final ItemCategory[] categories;
        public final Predicate<Item> filter;

        public ToolSwapBinding(String name, int keycode, String category, Predicate<Item> additionalFilter, ItemCategory... swapCategories) {
            super(name, keycode, category);
            categories = swapCategories;
            filter = additionalFilter;
        }

        public ToolSwapBinding(String name, int keycode, String category, ItemCategory... swapCategories) {
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
                Item item = slot.getItem();
                ItemCategory slotCategory = ItemCategory.get(slot);
                for (ItemCategory matchCategory : categories) {
                    if (matchCategory.matches(slotCategory)) {
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

        public float backgroundFilterAlpha;
        public long lastTimeNanosecs;

        public InventoryGUIWithFastBagAccess(EntityPlayer player) {
            super(player);
            backgroundFilterAlpha = isKeyDown(minecraft.gameSettings.keyBindSprint.getKeyCode()) ? 0 : 1;
            lastTimeNanosecs = System.nanoTime();

            if (Config.enablePlayerInventorySound)
                playSound(BAG_OPEN, minecraft.thePlayer, 0.8f, 0.8f);
        }

        @Override
        public void drawWorldBackground(int p_146270_1_) {
            if (minecraft.theWorld == null || !Config.removeDarkFilterInInventory) {
                super.drawWorldBackground(p_146270_1_);
            } else if (Config.onlyMoveWhenSprintKeyIsPressed) {

                int alpha1 = Math.round(0xC0 * backgroundFilterAlpha);
                int alpha2 = Math.round(0xD0 * backgroundFilterAlpha);
                int color1 = 0x101010 | (alpha1 << 24);
                int color2 = 0x101010 | (alpha2 << 24);

                drawGradientRect(0, 0, width, height, color1, color2);

                long nanosecs = System.nanoTime();
                double dt = (nanosecs - lastTimeNanosecs) / 1e9;
                lastTimeNanosecs = nanosecs;

                if (isKeyDown(minecraft.gameSettings.keyBindSprint.getKeyCode()))
                    backgroundFilterAlpha -= dt / BACKGROUND_FILTER_TRANSITION_SECONDS;
                else
                    backgroundFilterAlpha += dt / BACKGROUND_FILTER_TRANSITION_SECONDS;
                backgroundFilterAlpha = Math.min(Math.max(backgroundFilterAlpha, 0), 1);
            }
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
        public float backgroundFilterAlpha;
        public long lastTimeNanosecs;

        public ContainerGUIWithFastBagAccess(Container container, int xSize, int ySize, ResourceLocation texture) {
            super(container, xSize, ySize);
            this.texture = texture;
            backgroundFilterAlpha = isKeyDown(minecraft.gameSettings.keyBindSprint.getKeyCode()) ? 0 : 1;
            lastTimeNanosecs = System.nanoTime();
        }

        @Override
        public void drawWorldBackground(int p_146270_1_) {
            if (minecraft.theWorld == null || !Config.removeDarkFilterInInventory) {
                super.drawWorldBackground(p_146270_1_);
            } else if (Config.onlyMoveWhenSprintKeyIsPressed) {

                int alpha1 = Math.round(0xC0 * backgroundFilterAlpha);
                int alpha2 = Math.round(0xD0 * backgroundFilterAlpha);
                int color1 = 0x101010 | (alpha1 << 24);
                int color2 = 0x101010 | (alpha2 << 24);

                drawGradientRect(0, 0, width, height, color1, color2);

                long nanosecs = System.nanoTime();
                double dt = (nanosecs - lastTimeNanosecs) / 1e9;
                lastTimeNanosecs = nanosecs;

                if (isKeyDown(minecraft.gameSettings.keyBindSprint.getKeyCode()))
                    backgroundFilterAlpha -= dt / BACKGROUND_FILTER_TRANSITION_SECONDS;
                else
                    backgroundFilterAlpha += dt / BACKGROUND_FILTER_TRANSITION_SECONDS;
                backgroundFilterAlpha = Math.min(Math.max(backgroundFilterAlpha, 0), 1);
            }
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

        @Override
        public void onGuiClosed() {
            //TODO: Sometimes the chest open sound plays when you close the chest. No idea why...
            if (Config.enableChestClosingSound)
                playSound(CHEST_CLOSE, minecraft.thePlayer, 0.9f, 1);
            super.onGuiClosed();
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

            if (Config.enableBeehiveOpenSound)
                playSound(BEEHIVE_OPEN, beehiveTE, 1, 1);
        }

        @Override
        public void handleMouseClick(Slot slot, int slotIndex, int rightClick, int shiftDown) {
            if (!doQuickAccessOnRightClick(slot, slotIndex, rightClick, shiftDown)) {

                if (Config.enableBeehiveHoneySound && slot != null && slotIndex >= 0 && slotIndex < 6) { // beehive slots
                    ItemStack stack = slot.getStack();
                    if (stack != null) {
                        Item item = stack.getItem();
                        if (item == ItemSetup.honeycomb || item == ItemSetup.fertileHoneycomb) {
                            playSound(BEEHIVE_DRIP, beehiveTE, 1, 1);
                        }
                    }
                }

                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void onGuiClosed() {
            if (Config.enableBeehiveOpenSound)
                playSound(BEEHIVE_CLOSE, beehiveTE, 1, 1);
            super.onGuiClosed();
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
                if (Config.enableBarrelItemSoakSound && guiTab == 0 && slot != null && slotIndex >= 0 && barrelTE.fluid != null) {

                    float viscosity = getFluidViscosity(barrelTE.fluid);
                    Slot soakedSlot = inventorySlots.getSlot(0);
                    ItemStack soakedStack = soakedSlot.getStack();
                    ItemStack slotStack = slot.getStack();
                    ItemStack playerStack = player.inventory.getItemStack();

                    boolean placingStackIntoBarrel = slotIndex == 0 && playerStack != null;
                    boolean shiftClickingStackIntoBarrel = slotIndex != 0 && soakedStack == null && slotStack != null && shiftDown != 0 && soakedSlot.isItemValid(slotStack);
                    boolean stackingWithSoakedStack = slotIndex != 0 && soakedStack != null && slotStack != null && shiftDown != 0 && soakedStack.isStackable() && soakedStack.isItemEqual(slotStack);

                    if (placingStackIntoBarrel || shiftClickingStackIntoBarrel || stackingWithSoakedStack) {
                        playFluidSound(FLUID_SOAK, FLUID_SOAK_VISCOUS, barrelTE, viscosity, 1, 1);
                    } else if (slotIndex == 0 && slotStack != null) {
                        playFluidSound(FLUID_UNSOAK, FLUID_UNSOAK_VISCOUS, barrelTE, viscosity, 1, 1);
                    }
                }

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
                    buttonList.add(new TabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOn"), 0, 204, 16, 16));
                } else if (this.barrelTE.mode == 1) {
                    buttonList.add(new TabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOff"), 0, 188, 16, 16));
                }

                buttonList.add(new TabButton(3, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new TabButton(4, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
            } else if (this.guiTab == 1) {
                buttonList.add(new TabButton(0, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new TabButton(1, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
                if (!this.barrelTE.getSealed()) {
                    buttonList.add(new SealButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    buttonList.add(new SealButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Unseal")));
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
            float loudness = 1.0f;
            float pitch = 1.0f;

            if (guiTab == 0) {
                if (guibutton.id == 0) {
                    if (!barrelTE.getSealed()) {
                        if (Config.enableBarrelSealSound)
                            playSound(BARREL_SEAL, barrelTE, loudness, pitch);
                        barrelTE.actionSeal(0, player);
                    } else {
                        if (Config.enableBarrelSealSound)
                            playSound(BARREL_UNSEAL, barrelTE, loudness, pitch);
                        barrelTE.actionUnSeal(0, player);
                    }
                } else if (guibutton.id == 1) {
                    if (Config.enableBarrelEmptySound && barrelTE.fluid != null) {
                        float viscosity = getFluidViscosity(barrelTE.fluid);
                        playFluidSound(FLUID_EMPTY, FLUID_EMPTY_VISCOUS, barrelTE, viscosity, 1, 1);
                    }
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
                        playSound(BARREL_SEAL, barrelTE, loudness, pitch);
                        barrelTE.actionSeal(1, player);
                    } else {
                        playSound(BARREL_UNSEAL, barrelTE, loudness, pitch);
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

        public static class TabButton extends GuiButton {
            public BarrelGUIWithFastBagAccess screen;
            public IIcon buttonicon;
            public int xPos;
            public int yPos = 172;
            public int xSize = 31;
            public int ySize = 15;

            public TabButton(int index, int xPos, int yPos, int width, int height, BarrelGUIWithFastBagAccess gui, IIcon icon, String s) {
                super(index, xPos, yPos, width, height, s);
                this.screen = gui;
                this.buttonicon = icon;
            }

            public TabButton(int index, int xPos, int yPos, int width, int height, BarrelGUIWithFastBagAccess gui, String s, int xp, int yp, int xs, int ys) {
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

        public static class SealButton extends GuiButton {
            public SealButton(int index, int x, int y, int width, int height, String text) {
                super(index, x, y, width, height, text);
            }

            @Override
            public void func_146113_a(SoundHandler p_146113_1_) {
                // Don't play any sound..
                //TODO: This doesn't seem to actually stop the button click sound from playing...
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
                if (Config.enableLargeVesselItemSoakSound && guiTab == 0 && slot != null && slotIndex >= 0 && vesselTE.fluid != null) {

                    float viscosity = getFluidViscosity(vesselTE.fluid);
                    ItemStack liquidStack = inventorySlots.getSlot(0).getStack();
                    ItemStack slotStack = slot.getStack();
                    ItemStack playerStack = player.inventory.getItemStack();

                    if ((slotIndex == 0 && playerStack != null) || (liquidStack == null && slotStack != null && shiftDown != 0)) {
                        playFluidSound(FLUID_SOAK, FLUID_SOAK_VISCOUS, vesselTE, viscosity, 1, 1);
                    } else if (slotIndex == 0 && slotStack != null) {
                        playFluidSound(FLUID_UNSOAK, FLUID_UNSOAK_VISCOUS, vesselTE, viscosity, 1, 1);
                    }
                }

                super.handleMouseClick(slot, slotIndex, rightClick, shiftDown);
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            inventorySlots.putStackInSlot(0, vesselTE.getInputStack());
            if (vesselTE.getInvCount() > 0 && vesselTE.getDistillationMode() == -1) {
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

            if (vesselTE.getFluidLevel() > 0) {
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

            if (this.vesselTE.getSealed() && guiTab == 0) {
                ((GuiButton)buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Unseal");
                ((GuiButton)buttonList.get(1)).enabled = false;
                ((GuiButton)buttonList.get(2)).enabled = false;
            } else if (!vesselTE.getSealed() && guiTab == 0) {
                ((GuiButton)buttonList.get(0)).displayString = TFC_Core.translate("gui.Barrel.Seal");
                ((GuiButton)buttonList.get(1)).enabled = true;
                ((GuiButton)buttonList.get(2)).enabled = true;
            }

        }

        @Override
        public void initGui() {
            super.initGui();
            this.createButtons();
        }

        public void createButtons() {
            buttonList.clear();
            if (guiTab == 0) {
                if (!vesselTE.getSealed()) {
                    buttonList.add(new GuiButton(0, guiLeft + 38, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    buttonList.add(new GuiButton(0, guiLeft + 38, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Unseal")));
                }

                buttonList.add(new GuiButton(1, guiLeft + 88, guiTop + 50, 50, 20, TFC_Core.translate("gui.Barrel.Empty")));
                if (vesselTE.mode == 0) {
                    buttonList.add(new TabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOn"), 0, 204, 16, 16));
                } else if (vesselTE.mode == 1) {
                    buttonList.add(new TabButton(2, guiLeft + 39, guiTop + 29, 16, 16, this, TFC_Core.translate("gui.Barrel.ToggleOff"), 0, 188, 16, 16));
                }

                buttonList.add(new TabButton(3, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new TabButton(4, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
            } else if (guiTab == 1) {
                buttonList.add(new TabButton(0, guiLeft + 36, guiTop - 12, 31, 15, this, TFC_Textures.guiSolidStorage, TFC_Core.translate("gui.Barrel.Solid")));
                buttonList.add(new TabButton(1, guiLeft + 5, guiTop - 12, 31, 15, this, TFC_Textures.guiLiquidStorage, TFC_Core.translate("gui.Barrel.Liquid")));
                if (!vesselTE.getSealed()) {
                    buttonList.add(new BarrelGUIWithFastBagAccess.SealButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Seal")));
                } else {
                    buttonList.add(new BarrelGUIWithFastBagAccess.SealButton(2, guiLeft + 6, guiTop + 33, 44, 20, TFC_Core.translate("gui.Barrel.Unseal")));
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
        public void actionPerformed(GuiButton guibutton) {

            float loudness = 1;
            float pitch = 1;

            if (guiTab == 0) {
                if (guibutton.id == 0) {
                    if (!vesselTE.getSealed()) {
                        if (Config.enableLargeVesselSealSound)
                            playSound(LARGE_VESSEL_SEAL, vesselTE, loudness, pitch);
                        vesselTE.actionSeal(0, player);
                    } else {
                        if (Config.enableLargeVesselSealSound)
                            playSound(LARGE_VESSEL_UNSEAL, vesselTE, loudness, pitch);
                        vesselTE.actionUnSeal(0, player);
                    }
                } else if (guibutton.id == 1) {
                    if (Config.enableLargeVesselEmptySound && vesselTE.fluid != null) {
                        float viscosity = getFluidViscosity(vesselTE.fluid);
                        playFluidSound(FLUID_EMPTY, FLUID_EMPTY_VISCOUS, vesselTE, viscosity, 1, 1);
                    }
                    vesselTE.actionEmpty();
                } else if (guibutton.id == 2) {
                    vesselTE.actionMode();
                    createButtons();
                } else if (guibutton.id == 3 && vesselTE.getFluidLevel() == 0 && vesselTE.getInvCount() == 0) {
                    vesselTE.actionSwitchTab(1, player);
                }
            } else if (guiTab == 1) {
                if (guibutton.id == 1 && vesselTE.getInvCount() == 0) {
                    vesselTE.actionSwitchTab(0, player);
                } else if (guibutton.id == 2) {
                    if (!vesselTE.getSealed()) {
                        vesselTE.actionSeal(1, player);
                        playSound(LARGE_VESSEL_SEAL, vesselTE, loudness, pitch);
                    } else {
                        vesselTE.actionUnSeal(1, player);
                        playSound(LARGE_VESSEL_UNSEAL, vesselTE, loudness, pitch);
                    }

                    createButtons();
                }
            }
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            TFC_Core.bindTexture(TEXTURE);
            GL11.glColor4f(1, 1, 1, 0.5f);
            int w = (width - xSize) / 2;
            int h = (height - ySize) / 2;

            GL11.glPushMatrix();

            if (guiTab == 0) {

                //GL11.glTranslatef(-0.5f, -1.5f, 0.0f);
                drawTexturedModalRect(w, h, 0, 0, xSize, getShiftedYSize());

                if (vesselTE != null && vesselTE.fluid != null) {

                    int scale = vesselTE.getLiquidScaled(50);
                    int color = vesselTE.fluid.getFluid().getColor(vesselTE.fluid);

                    IIcon liquidIcon = vesselTE.fluid.getFluid().getIcon(vesselTE.fluid);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    GL11.glColor4ub((byte)(color >> 16 & 255), (byte)(color >> 8 & 255), (byte)(color & 255), (byte)-86);

                    int div = (int)Math.floor(scale / 8.0);
                    int rem = scale - div * 8;
                    drawTexturedModelRectFromIcon(w + 12, h + 65 - scale, liquidIcon, 8, div > 0 ? 8 : rem);

                    for(int c = 0; div > 0 && c < div; ++c) {
                        drawTexturedModelRectFromIcon(w + 12, h + 65 - (8 + c * 8), liquidIcon, 8, 8);
                    }

                    GL11.glColor3f(0, 0, 0);
                }

                ItemStack inStack = vesselTE.getStackInSlot(0);
                if (vesselTE.getFluidStack() != null) {
                    drawCenteredString(fontRendererObj, vesselTE.fluid.getFluid().getLocalizedName(vesselTE.getFluidStack()), guiLeft + 88, guiTop + 7, 5592405);
                }

                if (vesselTE.sealtime != 0) {
                    drawCenteredString(fontRendererObj, TFC_Time.getDateStringFromHours(vesselTE.sealtime), guiLeft + 88, guiTop + 17, 5592405);
                }

                vesselTE.recipe = BarrelManager.getInstance().findMatchingRecipe(vesselTE.getInputStack(), vesselTE.getFluidStack(), vesselTE.getSealed(), vesselTE.getTechLevel(), vesselTE.isHeated(), vesselTE);
                if (vesselTE.recipe != null) {
                    if (!(vesselTE.recipe instanceof BarrelBriningRecipe)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.Output") + ": " + vesselTE.recipe.getRecipeName(), guiLeft + 88, guiTop + 72, 5592405);
                    } else if (vesselTE.getSealed() && vesselTE.getFluidStack() != null && vesselTE.getFluidStack().getFluid() == TFCFluids.BRINE && inStack != null && inStack.getItem() instanceof IFood && (((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || inStack.getItem() == TFCItems.cheese) && !Food.isBrined(inStack)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.barrel.brining"), guiLeft + 88, guiTop + 72, 5592405);
                    }
                } else if (vesselTE.recipe == null && vesselTE.getSealed() && vesselTE.getFluidStack() != null && inStack != null && inStack.getItem() instanceof IFood && vesselTE.getFluidStack().getFluid() == TFCFluids.VINEGAR && !Food.isPickled(inStack) && Food.getWeight(inStack) / (float)vesselTE.getFluidStack().amount <= 160.0F / (float)vesselTE.getMaxLiquid()) {
                    if ((((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Fruit || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Vegetable || ((IFood)inStack.getItem()).getFoodGroup() == EnumFoodGroup.Protein || (IFood)inStack.getItem() == TFCItems.cheese) && Food.isBrined(inStack)) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate("gui.barrel.pickling"), guiLeft + 88, guiTop + 72, 5592405);
                    }
                } else {
                    BarrelPreservativeRecipe preservative = BarrelManager.getInstance().findMatchingPreservativeRepice(vesselTE, inStack, vesselTE.getFluidStack(), vesselTE.getSealed());
                    if (preservative != null) {
                        drawCenteredString(fontRendererObj, TFC_Core.translate(preservative.getPreservingString()), guiLeft + 88, guiTop + 72, 5592405);
                    }
                }
            } else if (guiTab == 1) {
                drawTexturedModalRect(w, h, 0, 86, xSize, getShiftedYSize());
            }

            GL11.glPopMatrix();
            PlayerInventory.drawInventory(this, width, height, getShiftedYSize());
        }

        @Override
        public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
            if (guiTab == 0 && mouseInRegion(12, 15, 9, 50, mouseX, mouseY)) {
                ArrayList<String> list = new ArrayList();
                list.add(vesselTE.getFluidLevel() + "mB");
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
            if (vesselTE.getSealed()) {
                GL11.glPushMatrix();
                if (guiTab == 0) {
                    Slot inputSlot = inventorySlots.getSlot(0);
                    drawSlotOverlay(inputSlot);
                } else if (guiTab == 1) {
                    for (int i = 0; i < vesselTE.storage.length; ++i) {
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

        public static class TabButton extends GuiButton {
            private LargeVesselGUIWithFastBagAccess screen;
            private IIcon buttonicon;
            private int xPos;
            private int yPos = 172;
            private int xSize = 31;
            private int ySize = 15;

            public TabButton(int index, int xPos, int yPos, int width, int height, LargeVesselGUIWithFastBagAccess gui, IIcon icon, String s) {
                super(index, xPos, yPos, width, height, s);
                screen = gui;
                buttonicon = icon;
            }

            public TabButton(int index, int xPos, int yPos, int width, int height, LargeVesselGUIWithFastBagAccess gui, String s, int xp, int yp, int xs, int ys) {
                super(index, xPos, yPos, width, height, s);
                this.xPos = xp;
                this.yPos = yp;
                screen = gui;
                xSize = xs;
                ySize = ys;
            }

            public void drawButton(Minecraft mc, int x, int y) {
                if (this.visible) {
                    TFC_Core.bindTexture(GuiLargeVessel.TEXTURE);
                    GL11.glColor4f(1, 1, 1, 1);
                    zLevel = 301;
                    drawTexturedModalRect(xPosition, yPosition, xPos, yPos, xSize, ySize);
                    field_146123_n = x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height;
                    GL11.glColor4f(1, 1, 1, 1);
                    TFC_Core.bindTexture(TextureMap.locationBlocksTexture);
                    if (buttonicon != null) {
                        drawTexturedModelRectFromIcon(xPosition + 12, yPosition + 4, buttonicon, 8, 8);
                    }

                    zLevel = 0;
                    mouseDragged(mc, x, y);
                    if (field_146123_n) {
                        screen.drawTooltip(x, y, displayString);
                        GL11.glColor4f(1, 1, 1, 1);
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
