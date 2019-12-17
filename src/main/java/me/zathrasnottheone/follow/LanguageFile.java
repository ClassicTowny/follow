package me.zathrasnottheone.follow;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageFile
{
	private File languageFile;
	private FileConfiguration language;
	
	public String prefix;
      
	public String noPermsList;
	public String noPermsReload;
	public String noPermsFollow;
	
	public String follow;
	public String followconsole;
	public String notstalkerconsole;
	public String notstalker;
	public String notingame;
	public String nearestplayer;
	public String notonlineplayer;
	public String nofollowself;
	public String cantfollowifsuspect;
	public String notinteger;
	public String noonetofollow;
	public String unfollow;
	public String unfollowconsole;
	public String notfollowinganyone;
	public String sentlist;
	public String followtime;
	public List<String> help;
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
		
		prefix = language.getString("prefix");
	    noPermsList = language.getString("noperms.list");
	    noPermsReload = language.getString("noperms.reload");
	    noPermsFollow = language.getString("noperms.follow");
		follow = language.getString("messages.follow");
		followconsole = language.getString("messages.followconsole");
		notstalkerconsole = language.getString("messages.notstalkerconsole");
	    notstalker = language.getString("messages.notstalker");
	    notingame = language.getString("messages.notingame");
	    nearestplayer = language.getString("messages.nearestplayer");
	    notonlineplayer = language.getString("messages.notonlineplayer");
	    nofollowself = language.getString("messages.nofollowself");
	    cantfollowifsuspect = language.getString("messages.cantfollowifsuspect");
	    notinteger = language.getString("messages.notinteger");
	    noonetofollow = language.getString("messages.noonetofollow");
	    unfollow = language.getString("messages.unfollow");
	    unfollowconsole = language.getString("messages.unfollowconsole");
	    notfollowinganyone = language.getString("messages.notfollowinganyone");
	    sentlist = language.getString("messages.sentlist");
	    followtime = language.getString("messages.followtime");
	    help = language.getStringList("messages.help");
	    reload = language.getString("messages.reload");
	    reloadconsole = language.getString("messages.reloadconsole");
	}
}

