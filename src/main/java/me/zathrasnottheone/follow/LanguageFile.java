package me.zathrasnottheone.follow;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageFile
{
	private File languageFile;
	private FileConfiguration language;
	
      
	public String noPermsList;
	public String noPermsReload;
	public String noPermsFollow;
	
	public String follow;
	public String notstalkerconsole;
	public String notstalker;
	public String notingame;
	public String nearestplayer;
	public String reload;
	public String reloadconsole;

	private Follow plugin;
	
	public LanguageFile(Follow plugin){
		this.plugin = plugin;
	}
	
	public void initLanguageFile(){
		saveDefaultLanguageFile();
		loadLanguageFile();
	}
	
	public void saveDefaultLanguageFile() {
		if (languageFile == null) {
			languageFile = new File(plugin.getDataFolder(), "language.yml");
	    }
	    if (!languageFile.exists()) {           
	    	plugin.saveResource("language.yml", false);
	    }   
	}
	  
	public void loadLanguageFile(){
		language = YamlConfiguration.loadConfiguration(languageFile);
		     
	    noPermsList = language.getString("noperms.list");
	    noPermsReload = language.getString("noperms.reload");
	    noPermsFollow = language.getString("noperms.follow");
		follow = language.getString("messages.follow");
		notstalkerconsole = language.getString("messages.notstalkerconsole");
	    notstalker = language.getString("messages.notstalker");
	    notingame = language.getString("messages.notingame");
	    nearestplayer = language.getString("messages.nearestplayer");
	    reload = language.getString("messages.reload");
	    reloadconsole = language.getString("messages.reloadconsole");
	}
}

