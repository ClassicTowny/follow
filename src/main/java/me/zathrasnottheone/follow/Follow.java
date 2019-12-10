package me.zathrasnottheone.follow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import me.drkmatr1984.customevents.CustomEvents;
import me.zathrasnottheone.follow.CommandHandler;
import me.zathrasnottheone.follow.FollowConfig;
import me.zathrasnottheone.follow.PlayerListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Follow extends JavaPlugin {
   
	private final List<Listener> _listenerList = new ArrayList<Listener>();
	public static Follow plugin;
    private static UUIDFetcher fetch;
    private LanguageFile lang;
    private CustomEvents customEvents;
   
    public void onDisable() {
    	Logger logger = this.getLogger();
        this.saveConfig();
        this.getServer().getScheduler().cancelTasks(this);
        logger.info("Disabled.");
    }

    public void onEnable() {
    	plugin = this;
        Logger logger = this.getLogger();
        this.lang = new LanguageFile(this);
        this.lang.initLanguageFile();
        FollowConfig.getInstance().initiatlize(this);
        this._listenerList.add(new PlayerListener(this));
        Iterator<Listener> pdfFile = this._listenerList.iterator();
        fetch = new UUIDFetcher(this);
        customEvents = new CustomEvents(this, false, false, true, false, false);
        customEvents.initializeLib();

        while(pdfFile.hasNext()) {
           Listener ch = (Listener)pdfFile.next();
           this.getServer().getPluginManager().registerEvents(ch, this);
        }

        CommandHandler ch1 = new CommandHandler(this);
        this.getCommand("follow").setExecutor(ch1);
        this.getCommand("unfollow").setExecutor(ch1);

        PluginDescriptionFile pdfFile2 = this.getDescription();
        logger.info(pdfFile2.getName() + " version " + pdfFile2.getVersion() + " is enabled.");
    }
    
    

    public static UUIDFetcher getFetch() {
	    return fetch;
   	}
   
   	public static Follow getFollowPlugin() {
   		return plugin;
   	}
   	
   	public FollowRoster getFollowRoster() {
   		return FollowRoster.getInstance();
   	}
   	
   	public LanguageFile getLang() {
   		return this.lang;
   	}
     
}

