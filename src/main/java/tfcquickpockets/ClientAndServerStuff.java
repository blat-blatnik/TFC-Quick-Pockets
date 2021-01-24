// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

package tfcquickpockets;

import com.dunk.tfc.Food.ItemFoodTFC;
import com.dunk.tfc.api.Food;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

public class ClientAndServerStuff {

    public static SimpleNetworkWrapper network;

    public void initializeConfig(FMLPreInitializationEvent event) {}

    public void initialize(FMLInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(QuickPockets.ID);
        network.registerMessage(SwapInventorySlotsPacket.Handler.class, SwapInventorySlotsPacket.class, 0, Side.CLIENT);
        network.registerMessage(SwapInventorySlotsPacket.Handler.class, SwapInventorySlotsPacket.class, 1, Side.SERVER);
        network.registerMessage(CycleInventoryRowsPacket.Handler.class, CycleInventoryRowsPacket.class, 2, Side.CLIENT);
        network.registerMessage(CycleInventoryRowsPacket.Handler.class, CycleInventoryRowsPacket.class, 3, Side.SERVER);
        network.registerMessage(StackFoodPacket.Handler.class, StackFoodPacket.class, 4, Side.CLIENT);
        network.registerMessage(StackFoodPacket.Handler.class, StackFoodPacket.class, 5, Side.SERVER);
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
    }

    public static boolean isFood(ItemStack stack) {
        if (stack != null && stack.getItem() != null) {
            Item item = stack.getItem();
            return item instanceof ItemFoodTFC && stack.hasTagCompound() && stack.getTagCompound().hasKey(Food.WEIGHT_TAG);
        } else
            return false;
    }

    public static boolean isSameFood(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.getItem() instanceof ItemFoodTFC && stack2.getItem() instanceof ItemFoodTFC) {

            ItemFoodTFC food1 = (ItemFoodTFC)stack1.getItem();
            ItemFoodTFC food2 = (ItemFoodTFC)stack2.getItem();

            if (food1.foodID == food2.foodID) {

                int[] taste1      = Food.getFoodTasteProfile(stack1);
                int[] fuel1       = Food.getFuelProfile(stack1);
                int[] cooked1     = Food.getCookedProfile(stack1);
                int cookCategory1 = ((int)Food.getCooked(stack1) - 600) / 120;

                int[] taste2      = Food.getFoodTasteProfile(stack2);
                int[] fuel2       = Food.getFuelProfile(stack2);
                int[] cooked2     = Food.getCookedProfile(stack2);
                int cookCategory2 = ((int)Food.getCooked(stack2) - 600) / 120;

                return
                    cookCategory1 == cookCategory2 &&
                    Food.isSameSmoked(taste1, taste2) &&
                    Food.isSameSmoked(fuel1, fuel2) &&
                    Food.isSameSmoked(cooked1, cooked2);
            }
        }

        return false;
    }

    // adapted from com.dunk.tfc.Handlers.FoodCraftingHandler
    public static void stackPlayerFood(EntityPlayer player, int inputFoodSlot) {
        if (player != null) {
            Container inventory = player.inventoryContainer;
            if (inputFoodSlot >= 9 && inputFoodSlot < 9 + 4 * 9) {

                ItemStack inputFoodStack = inventory.getSlot(inputFoodSlot).getStack();
                if (isFood(inputFoodStack)) {

                    float inputWeight = Food.getWeight(inputFoodStack);
                    float inputDecay = Food.getDecay(inputFoodStack);
                    float inputDecayFraction = inputDecay / inputWeight;

                    for (int i = 9; i < 9 + 4 * 9 && inputWeight > 0; ++i) {

                        // this is just so that we stack in the hotbar first - and then in the rest of the inventory
                        int foodSlot = i;
                        if (foodSlot < 9 + 9)
                            foodSlot += 3 * 9;
                        else if (foodSlot >= 9 + 3 * 9)
                            foodSlot -= 3 * 9;

                        if (foodSlot != inputFoodSlot) {
                            ItemStack foodStack = inventory.getSlot(foodSlot).getStack();
                            if (isFood(foodStack)) {

                                float weight = Food.getWeight(foodStack);
                                float decay = Food.getDecay(foodStack);

                                if (weight < 160 && isSameFood(inputFoodStack, foodStack)) {

                                    float newWeight = Math.min(weight + inputWeight, 160);
                                    float weightChange = newWeight - weight;
                                    float inputWeightChangeFraction = weightChange / inputWeight;
                                    float newDecay = decay + inputDecay * inputWeightChangeFraction;
                                    Food.setWeight(foodStack, newWeight);
                                    Food.setDecay(foodStack, newDecay);
                                    inventory.putStackInSlot(foodSlot, foodStack); // probably don't need to do this

                                    inputWeight -= weightChange;
                                    inputDecay -= weightChange * inputDecayFraction;
                                    // change water loss??
                                    if (inputWeight < 1e-3) // floating-point rounding nonsense
                                        inputWeight = 0;
                                }
                            }
                        }
                    }

                    if (inputWeight > 0) { // still have some leftover food
                        Food.setWeight(inputFoodStack, inputWeight);
                        Food.setDecay(inputFoodStack, inputDecay);
                        inventory.putStackInSlot(inputFoodSlot, inputFoodStack);
                    } else {
                        inventory.putStackInSlot(inputFoodSlot, null);
                    }
                }
            }
        }
    }

    public static void swapPlayerInventorySlots(EntityPlayer player, int slot1, int slot2) {
        if (player != null) {
            Container inventory = player.inventoryContainer;
            ItemStack stack1 = inventory.getSlot(slot1).getStack();
            ItemStack stack2 = inventory.getSlot(slot2).getStack();
            inventory.putStackInSlot(slot1, stack2);
            inventory.putStackInSlot(slot2, stack1);
        }
    }

    public static void cyclePlayerInventoryRows(EntityPlayer player, boolean cycleUp) {
        Container inventory = player.inventoryContainer;
        int numRows = 4;
        int numColumns = 9;
        for (int column = 0; column < numColumns; ++column) {
            if (cycleUp) {
                for (int row = 0; row < numRows - 1; ++row) {
                    int slot1 = 9 + row * 9 + column;
                    int slot2 = 9 + (row + 1) * 9 + column;
                    ItemStack stack1 = inventory.getSlot(slot1).getStack();
                    ItemStack stack2 = inventory.getSlot(slot2).getStack();
                    inventory.putStackInSlot(slot1, stack2);
                    inventory.putStackInSlot(slot2, stack1);
                }
            } else {
                for (int row = numRows - 2; row >= 0; --row) {
                    int slot1 = 9 + row * 9 + column;
                    int slot2 = 9 + (row + 1) * 9 + column;
                    ItemStack stack1 = inventory.getSlot(slot1).getStack();
                    ItemStack stack2 = inventory.getSlot(slot2).getStack();
                    inventory.putStackInSlot(slot1, stack2);
                    inventory.putStackInSlot(slot2, stack1);
                }
            }
        }
    }

    public static class ServerEventHandler {
        @SubscribeEvent(priority=EventPriority.LOW) @SuppressWarnings("unused")
        public void stopZombiesAndSpidersFromSpawningUselessJunk(LivingDropsEvent event) {
            for (int i = 0; i < event.drops.size(); ++i) {
                EntityItem entityItem = event.drops.get(i);
                if (entityItem != null) {
                    ItemStack stack = entityItem.getEntityItem();
                    if (stack != null) {
                        Item item = stack.getItem();
                        if (item != null) {
                            boolean cancelDrop =
                                (item == Items.rotten_flesh && Config.disableRottenFlesh) ||
                                (item == Items.spider_eye && Config.disableSpiderEyes);
                            if (cancelDrop) {
                                event.drops.remove(i);
                                --i;
                            }
                        }
                    }
                }
            }
        }
    }

    public static class StackFoodPacket implements IMessage {
        public int foodSlotIndex;

        @SuppressWarnings("unused")
        public StackFoodPacket() {}

        public StackFoodPacket(int foodSlotIndex) {
            this.foodSlotIndex = foodSlotIndex;
        }

        @Override
        public void fromBytes(ByteBuf bytes) {
            foodSlotIndex = bytes.getByte(0);
        }

        @Override
        public void toBytes(ByteBuf bytes) {
            bytes.writeByte(foodSlotIndex);
        }

        public static class Handler implements IMessageHandler<StackFoodPacket, IMessage> {
            @Override
            public IMessage onMessage(StackFoodPacket stackFood, MessageContext context) {
                if (context.side.isServer()) {
                    try {
                        int slot = stackFood.foodSlotIndex;
                        EntityPlayer player = context.getServerHandler().playerEntity;
                        stackPlayerFood(player, slot);
                        Container inventory = player.inventoryContainer;
                        inventory.detectAndSendChanges();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }
    }

    public static class SwapInventorySlotsPacket implements IMessage {
        public int slotIndex1;
        public int slotIndex2;

        @SuppressWarnings("unused")
        public SwapInventorySlotsPacket() {}

        public SwapInventorySlotsPacket(int slotIndex1, int slotIndex2) {
            this.slotIndex1 = slotIndex1;
            this.slotIndex2 = slotIndex2;
        }

        @Override
        public void fromBytes(ByteBuf bytes) {
            this.slotIndex1 = bytes.getByte(0);
            this.slotIndex2 = bytes.getByte(1);
        }

        @Override
        public void toBytes(ByteBuf bytes) {
            bytes.writeByte(slotIndex1);
            bytes.writeByte(slotIndex2);
        }

        public static class Handler implements IMessageHandler<SwapInventorySlotsPacket, IMessage> {
            @Override
            public IMessage onMessage(SwapInventorySlotsPacket swapSlots, MessageContext context) {
                if (context.side.isServer()) {
                    try {
                        int slot1 = swapSlots.slotIndex1;
                        int slot2 = swapSlots.slotIndex2;
                        EntityPlayer player = context.getServerHandler().playerEntity;
                        swapPlayerInventorySlots(player, slot1, slot2);
                        Container inventory = player.inventoryContainer;
                        inventory.detectAndSendChanges();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }
    }

    public static class CycleInventoryRowsPacket implements IMessage {
        public boolean cycleUp; // if this is false then we cycle down

        @SuppressWarnings("unused")
        public CycleInventoryRowsPacket() {}

        public CycleInventoryRowsPacket(boolean cycleUp) {
            this.cycleUp = cycleUp;
        }

        @Override
        public void fromBytes(ByteBuf bytes) {
            this.cycleUp = bytes.getBoolean(0);
        }

        @Override
        public void toBytes(ByteBuf bytes) {
            bytes.writeBoolean(cycleUp);
        }

        public static class Handler implements IMessageHandler<CycleInventoryRowsPacket, IMessage> {
            @Override
            public IMessage onMessage(CycleInventoryRowsPacket cycleSlots, MessageContext context) {
                if (context.side.isServer()) {
                    try {
                        EntityPlayer player = context.getServerHandler().playerEntity;
                        cyclePlayerInventoryRows(player, cycleSlots.cycleUp);
                        Container inventory = player.inventoryContainer;
                        inventory.detectAndSendChanges();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }
    }

}