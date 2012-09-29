package com.gmail.molnardad.quester.objectives;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.gmail.molnardad.quester.QuestData;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.qevents.Qevent;
import com.gmail.molnardad.quester.utils.Util;

public abstract class Objective {

	@SuppressWarnings("unchecked")
	private static Class<? extends Objective>[] classes = new Class[]{
		BreakObjective.class,
		CollectObjective.class,
		CraftObjective.class,
		DeathObjective.class,
		EnchantObjective.class,
		ExpObjective.class,
		FishObjective.class,
		ItemObjective.class,
		LocObjective.class,
		MilkObjective.class,
		MobKillObjective.class,
		MoneyObjective.class,
		PlaceObjective.class,
		PlayerKillObjective.class,
		ShearObjective.class,
		SmeltObjective.class,
		TameObjective.class,
		WorldObjective.class
	};
	List<Qevent> qevents = new ArrayList<Qevent>();
	String desc = "";
	Set<Integer> prerequisites = new HashSet<Integer>();
	
	public abstract String getType();
	
	public void addQevent(Qevent newQevent) {
		qevents.add(newQevent);
	}
	
	public void removeQevent(int id) {
		if(id >= 0 && id < qevents.size())
			qevents.remove(id);
	}
	
	public List<Qevent> getQevents() {
		return qevents;
	}
	
	public void runQevents(Player player) {
		for(Qevent qv : qevents) {
			qv.execute(player);
		}
	}
	
	public String stringQevents() {
		String result = "";
		for(int i=0; i<qevents.size(); i++) {
			result += "\n " + ChatColor.RESET + " <" + i + "> " + qevents.get(i).toString();
		}
		return result;
	}
	
	public Set<Integer> getPrerequisites() {
		return prerequisites;
	}
	
	public void addPrerequisity(int newPre) {
		prerequisites.add(newPre);
	}
	
	public void removePrerequisity(int pre) {
		prerequisites.remove(pre);
	}
	
	public String coloredDesc() {
		String des = "";
		if(!prerequisites.isEmpty()) {
			des += " PRE: " + Util.serializePrerequisites(prerequisites, ",");
		}
		if(!desc.isEmpty()) {
			des += "\n  - " + ChatColor.translateAlternateColorCodes('&', desc) + ChatColor.RESET;
		}
		return des;
	}
	
	public void addDescription(String msg) {
		this.desc += (" " + msg).trim();
	}
	
	public void removeDescription() {
		this.desc = "";
	}
	
	public int getTargetAmount() {
		return 1;
	}
	
	public boolean isComplete(Player player, int progress) {
		return progress > 0;
	}
	
	public boolean tryToComplete(Player player) {
		return false;
	}
	
	public boolean finish(Player player) {
		return true;
	}
	
	public abstract String progress(int progress);
	public abstract String toString();
	
	public abstract void serialize(ConfigurationSection section);
	
	void serialize(ConfigurationSection section, String type) {
		section.set("type", type);
		if(!desc.isEmpty())
			section.set("description", desc);
		if(!prerequisites.isEmpty())
			section.set("prerequisites", Util.serializePrerequisites(prerequisites));
		if(!qevents.isEmpty()) {
			ConfigurationSection sub = section.createSection("events");
			for(int i=0; i<qevents.size(); i++) {
				qevents.get(i).serialize(sub.createSection("" + i));
			}
		}
	}
	
	public static Objective deserialize(ConfigurationSection section) {
		if(section == null) {
			Quester.log.severe("Objective deserialization error: section null.");
			return null;
		}
		Objective obj = null;
		String type = null, des = null;
		List<Qevent> qvts = new ArrayList<Qevent>();
		Set<Integer> prereq = new HashSet<Integer>();
		
		if(section.isString("type"))
			type = section.getString("type");
		else {
			Quester.log.severe("Objective type missing.");
			return null;
		}
		if(section.isString("description")) {
			des = section.getString("description");
		}
		if(section.isString("prerequisites")) {
			try {
				prereq = Util.deserializePrerequisites(section.getString("prerequisites"));
			} catch (Exception ignore) {}
		}
		if(section.isConfigurationSection("events")) {
			ConfigurationSection evs = section.getConfigurationSection("events");
			for(String key : evs.getKeys(false)) {
				if(evs.isConfigurationSection(key)) {
					Qevent qv = Qevent.deserialize(evs.getConfigurationSection(key));
					if(qv != null)
						qvts.add(qv);
					else
						Quester.log.severe("Error occured when deserializing event ID:" + key + " in objective.");
				}
			}
		}
		
		boolean success = false;
		for(Class<? extends Objective> c : classes) {
			try {
				if(((String) c.getField("TYPE").get(null)).equalsIgnoreCase(type)) {
					try {
						success = true;
						Method deser = c.getMethod("deser", ConfigurationSection.class);
						obj = (Objective) deser.invoke(null, section);
						if(obj == null)
							return null;
						if(des != null)
							obj.addDescription(des);
						if(!qvts.isEmpty()) {
							for(Qevent q : qvts) {
								obj.addQevent(q);
							}
						}
						if(!prereq.isEmpty()) {
							for(int i : prereq) {
								obj.addPrerequisity(i);
							}
						}
						break;
					} catch (Exception e) {
						Quester.log.severe("Error when deserializing " + c.getName() + ". Method deser() missing or broken. " + e.getClass().getName());
						if(QuestData.debug)
							e.printStackTrace();
						return null;
					}
				}
			} catch (Exception e) {
				Quester.log.severe("Error when deserializing " + c.getName() + ". Field 'TYPE' missing or access denied. " + e.getClass().getName());
				if(QuestData.debug)
					e.printStackTrace();
				return null;
			}
		}
		if(!success)
			Quester.log.severe("Unknown objective type: '" + type  + "'");
		
		return obj;
	}
}
