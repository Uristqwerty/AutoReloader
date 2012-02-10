package net.zhuoweizhang.autoreloader;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;

public class AutoReloader extends JavaPlugin {

	private final AutoReloaderBlockListener blockListener = new AutoReloaderBlockListener(this);

	public Material containerMaterial, inputMaterial, outputMaterial, fuelMaterial;

	public boolean furnaceEnabled, dispenserEnabled, brewingStandEnabled;

	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();

		FileConfiguration config = this.getConfig();
		config.options().copyDefaults(true);

		containerMaterial = Material.matchMaterial(config.getString("material.container"));
		inputMaterial = Material.matchMaterial(config.getString("material.input"));
		outputMaterial = Material.matchMaterial(config.getString("material.output"));
		fuelMaterial = Material.matchMaterial(config.getString("material.fuel"));

		if (containerMaterial == null) {
			this.getLogger().severe("The container signifying material specified in the configuration file is invalid. ");
			return;
		}

		if (inputMaterial == null) {
			this.getLogger().severe("The input chest material specified in the configuration file is invalid. ");
			return;
		}

		if (outputMaterial == null) {
			this.getLogger().severe("The output chest material specified in the configuration file is invalid. ");
			return;
		}

		if (fuelMaterial == null) {
			this.getLogger().severe("The fuel chest material specified in the configuration file is invalid. ");
			return;
		}

		furnaceEnabled = config.getBoolean("enabled.furnace");
		dispenserEnabled = config.getBoolean("enabled.dispenser");
		brewingStandEnabled = config.getBoolean("enabled.brewing-stand");

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(blockListener, this);

		this.saveConfig();

		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled." );
		System.out.println("For: Furnace: " + furnaceEnabled + " Dispenser: " + dispenserEnabled + " Brewing Stand: " + brewingStandEnabled);

	}

	public void onDisable() {

	}
}
