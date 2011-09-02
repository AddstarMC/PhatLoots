
package PhatLoots;

import com.griefcraft.lwc.LWC;
import com.nijiko.permissions.PermissionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Codisimus
 */
public class PhatLootsMain extends JavaPlugin {
    protected static PermissionHandler permissions;
    protected static PluginManager pm;
    protected static LWC lwc;
    protected static Server server;
    protected static String defaultResetTime;
    protected static int defaultNumberCollectiveLoots;
    protected static String defaultResetType;
    protected static boolean autoLock;
    private Properties p;

    @Override
    public void onDisable () {
    }

    @Override
    public void onEnable () {
        server = getServer();
        checkFiles();
        SaveSystem.loadFromFile();
        loadConfig();
        pm = server.getPluginManager();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new PluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PhatLootsPlayerListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new PhatLootsPlayerListener(), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_DAMAGE, new PhatLootsBlockListener(), Priority.Normal, this);
        System.out.println("PhatLoots "+this.getDescription().getVersion()+" is enabled!");
    }

    /**
     * Makes sure all needed files exist
     * Register.jar is for economy support
     */
    private void checkFiles() {
        File file = new File("lib/Register.jar");
        if (!file.exists() || file.length() < 43000)
            moveFile("Register.jar");
        file = new File("plugins/PhatLoots/config.properties");
        if (!file.exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from PhatLoots.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            JarFile jar = new JarFile("plugins/PhatLoots.jar");
            ZipEntry entry = jar.getEntry(fileName);
            String destination = "plugins/PhatLoots/";
            if (fileName.equals("Register.jar")) {
                System.out.println("[PhatLoots] Moving Files... Please Reload Server");
                destination = "lib/";
            }
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    private void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/PhatLoots/config.properties"));
        }
        catch (Exception e) {
        }
        Register.economy = loadValue("Economy");
        PluginListener.useOP = Boolean.parseBoolean(loadValue("UseOP"));
        PhatLoots.autoLoot = Boolean.parseBoolean(loadValue("AutoLoot"));
        PhatLoots.autoLootMsg = loadValue("AutoLootMessage").replaceAll("&", "§");
        defaultResetTime = loadValue("DefaultResetTime");
        defaultResetType = loadValue("DefaultResetType");
        defaultNumberCollectiveLoots = Integer.parseInt(loadValue("DefaultNumberOfCollectiveLootItemsRecieved"));
        autoLock = Boolean.parseBoolean(loadValue("AutoLockPhatLootChestsWithLWC"));
    }

    /**
     * Prints error for missing values
     * 
     */
    private String loadValue(String key) {
        if (!p.containsKey(key)) {
            System.err.println("[PhatLoots] Missing value for "+key+" in config file");
            System.err.println("[PhatLoots] Please regenerate config file");
        }
        return p.getProperty(key);
    }
    
    /**
     * Returns boolean value of whether the given player has the specific permission
     * 
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        if (permissions != null)
            return permissions.has(player, "phatloots."+type);
        else
            if (type.equals("use"))
                return true;
            else
                return player.isOp();
    }

    /**
     * Finds a neighboring Block that is also a chest
     * 
     * @param chest The original Block
     * @return The neighboring chest or null if no neighbors are chests
     */
    protected static Block getBigChest(Block chest) {
        Block bigChest = null;
        Block neighbor = chest.getRelative(BlockFace.NORTH);
        if (neighbor.getType().equals(Material.CHEST))
            bigChest = neighbor;
        neighbor = chest.getRelative(BlockFace.EAST);
        if (neighbor.getType().equals(Material.CHEST))
            bigChest = neighbor;
        neighbor = chest.getRelative(BlockFace.SOUTH);
        if (neighbor.getType().equals(Material.CHEST))
            bigChest = neighbor;
        neighbor = chest.getRelative(BlockFace.WEST);
        if (neighbor.getType().equals(Material.CHEST))
            bigChest = neighbor;
        return bigChest;
    }
}
