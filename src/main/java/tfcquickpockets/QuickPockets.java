// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

/*
CHANGES:
- added fishing rod sounds
- added bloomery and blast furnace sounds

TODO:
- WHY THE HELL does auto refill not always work???
- sounds when stacking ingots
*/

package tfcquickpockets;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid=QuickPockets.ID, name=QuickPockets.NAME, version=QuickPockets.VERSION, dependencies=QuickPockets.DEPENDENCIES, guiFactory=QuickPockets.GUI_FACTORY)
public class QuickPockets {

    public static final String ID = "tfcquickpockets";
    public static final String NAME = "TFC+ Quick Pockets";
    public static final String VERSION = "1.1.2";
    public static final String DEPENDENCIES = "required-after:terrafirmacraftplus;";
    public static final String GUI_FACTORY = ID + ".Config";
    public static final String CLIENT_SIDE = ID + ".ClientStuff";
    public static final String SERVER_SIDE = ID + ".ClientAndServerStuff";

    @Mod.Instance(ID) @SuppressWarnings("unused")
    public static QuickPockets instance;

    @SidedProxy(clientSide=CLIENT_SIDE, serverSide=SERVER_SIDE)
    public static ClientAndServerStuff sidedStuff;

    @EventHandler @SuppressWarnings("unused")
    public void initializeConfig(FMLPreInitializationEvent event) {
        sidedStuff.initializeConfig(event);
    }

    @EventHandler @SuppressWarnings("unused")
    public void initialize(FMLInitializationEvent event) {
        sidedStuff.initialize(event);
    }
}