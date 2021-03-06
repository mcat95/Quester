package me.ragan262.quester.commands;

import java.util.HashSet;
import java.util.Set;
import me.ragan262.commandmanager.annotations.Command;
import me.ragan262.commandmanager.annotations.CommandLabels;
import me.ragan262.quester.Quester;
import me.ragan262.quester.commandmanager.QuesterCommandContext;
import me.ragan262.quester.exceptions.QuesterException;
import me.ragan262.quester.profiles.ProfileManager;
import me.ragan262.quester.quests.QuestFlag;
import me.ragan262.quester.quests.QuestManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ModifierCommands {
	
	final QuestManager qMan;
	final ProfileManager profMan;
	
	public ModifierCommands(final Quester plugin) {
		qMan = plugin.getQuestManager();
		profMan = plugin.getProfileManager();
	}
	
	private QuestFlag[] getModifiers(final String[] args) {
		final Set<QuestFlag> modifiers = new HashSet<>();

		for(String arg : args) {
			final QuestFlag flag = QuestFlag.getByName(arg);
			if(flag != null && flag != QuestFlag.ACTIVE) {
				modifiers.add(flag);
			}
		}
		
		return modifiers.toArray(new QuestFlag[modifiers.size()]);
	}
	
	@CommandLabels({ "add", "a" })
	@Command(section = "QMod", desc = "adds quest modifier", min = 1, usage = "<modifier1> ...")
	public void add(final QuesterCommandContext context, final CommandSender sender) throws QuesterException {
		final QuestFlag[] modArray = getModifiers(context.getArgs());
		if(modArray.length < 1) {
			sender.sendMessage(ChatColor.RED + context.getSenderLang().get("ERROR_MOD_UNKNOWN"));
			sender.sendMessage(ChatColor.RED + context.getSenderLang().get("USAGE_MOD_AVAIL")
					+ ChatColor.WHITE + QuestFlag.stringize(QuestFlag.values()));
			return;
		}
		qMan.addQuestFlag(profMan.getSenderProfile(sender), modArray, context.getSenderLang());
		sender.sendMessage(ChatColor.GREEN + context.getSenderLang().get("Q_MOD_ADDED"));
	}
	
	@CommandLabels({ "remove", "r" })
	@Command(section = "QMod", desc = "removes quest modifier", min = 1, usage = "<modifier1> ...")
	public void set(final QuesterCommandContext context, final CommandSender sender) throws QuesterException {
		final QuestFlag[] modArray = getModifiers(context.getArgs());
		qMan.removeQuestFlag(profMan.getSenderProfile(sender), modArray, context.getSenderLang());
		sender.sendMessage(ChatColor.GREEN + context.getSenderLang().get("Q_MOD_REMOVED"));
	}
}
