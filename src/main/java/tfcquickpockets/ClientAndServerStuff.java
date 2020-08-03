// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

package tfcquickpockets;

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

        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
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
                        swapPlayerInventorySlots(context.getServerHandler().playerEntity, slot1, slot2);
                        Container inventory = context.getServerHandler().playerEntity.inventoryContainer;
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
                        cyclePlayerInventoryRows(context.getServerHandler().playerEntity, cycleSlots.cycleUp);
                        Container inventory = context.getServerHandler().playerEntity.inventoryContainer;
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