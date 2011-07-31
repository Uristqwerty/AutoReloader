package net.zhuoweizhang.autoreloader;

import java.io.File;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;

public class AutoReloader extends JavaPlugin
{
	private final AutoReloaderBlockListener blockListener = new AutoReloaderBlockListener(this);
	
    public void onEnable()
    {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener,  Priority.Monitor, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled." );
    	
    }
    
    public void onDisable()
    {
    	
    }
    
}
