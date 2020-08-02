// This code is in the public domain. You can do anything you want with it, and you don't even
// have to give credits if you don't feel like it, although that would obviously be appreciated.

/*TODO
 * --- 1.0b ----------------------------------------------------
 * [+] Make hotkeys for switching to/from different tools
 * [+] Cycle through inventory rows with mouse scroll
 * [+] Render a preview of inventory when cycling
 * [-] Render preview of item inventory in player inventory
 * [+] Fix player head rotation in inventory
 * [+] Fix water sack switching inventory places
 * [+] Fix basket and large vessel GUI
 * [-] Animate toolbar swapping
 * [+] Config
 * [+] Beta release
 * --- 1.0.1b ---------------------------------------------------
 * [+] Fix bug with item temperature and make sure this is stable
 * --- 1.1b -----------------------------------------------------
 * [+] Fix inventory tool swapping cycle
 * [+] Stop zombies and spiders from spawning useless drops
 * [ ] Walk in inventory
 * [ ] Replace blocks/tools in inventory when they are used up
 * [ ] Inventory sorting
 * [?] Make this work client-side only
 * [ ] Code cleanup and todos
 * [ ] 1.0 release
 * [ ] Sound effect when swapping stuff
 * [ ] Go back and forth between containers
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
    public static final String VERSION = "1.1b";
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