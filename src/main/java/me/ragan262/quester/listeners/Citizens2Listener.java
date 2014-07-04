package me.ragan262.quester.listeners;

import java.util.List;

import me.ragan262.quester.ActionSource;
import me.ragan262.quester.QConfiguration;
import me.ragan262.quester.Quester;
import me.ragan262.quester.elements.Objective;
import me.ragan262.quester.holder.QuestHolder;
import me.ragan262.quester.holder.QuestHolderActionHandler;
import me.ragan262.quester.holder.QuesterTrait;
import me.ragan262.quester.objectives.NpcKillObjective;
import me.ragan262.quester.objectives.NpcObjective;
import me.ragan262.quester.profiles.PlayerProfile;
import me.ragan262.quester.quests.Quest;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Citizens2Listener extends QuestHolderActionHandler<NPC> implements Listener {
	
	public Citizens2Listener(final Quester plugin) {
		super(plugin);
	}
	
	@Override
	public String getHeaderText(final Player player, final QuestHolder qh, final NPC data) {
		return data.getName() + "'s quests";
	}
	
	@Override
	public String getUsePermission() {
		return QConfiguration.PERM_USE_NPC;
	}
	
	@Override
	public void assignHolder(final QuestHolder qh, final NPC data) {
		data.getTrait(QuesterTrait.class).setHolderID(qh.getId());
	}
	
	@Override
	public void unassignHolder(final QuestHolder qh, final NPC data) {
		data.getTrait(QuesterTrait.class).setHolderID(-1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNPCLeftClick(final NPCLeftClickEvent event) {
		if(event.getNPC().hasTrait(QuesterTrait.class)) {
			final QuestHolder qh =
					holMan.getHolder(event.getNPC().getTrait(QuesterTrait.class).getHolderID());
			final Player player = event.getClicker();
			onLeftClick(player, qh, event.getNPC());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNPCRightClick(final NPCRightClickEvent event) {
		if(event.getNPC().hasTrait(QuesterTrait.class)) {
			final QuestHolder qh =
					holMan.getHolder(event.getNPC().getTrait(QuesterTrait.class).getHolderID());
			final Player player = event.getClicker();
			onRightClick(player, qh, event.getNPC());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAnyClick(final NPCRightClickEvent event) {
		final Player player = event.getClicker();
		final PlayerProfile prof = profMan.getProfile(player.getName());
		final Quest quest = prof.getQuest();
		if(quest != null) {
			if(!quest.allowedWorld(player.getWorld().getName().toLowerCase())) {
				return;
			}
			final List<Objective> objs = quest.getObjectives();
			for(int i = 0; i < objs.size(); i++) {
				if(objs.get(i).getType().equalsIgnoreCase("NPC")) {
					if(!profMan.isObjectiveActive(prof, i)) {
						continue;
					}
					final NpcObjective obj = (NpcObjective) objs.get(i);
					if(obj.checkNpc(event.getNPC().getId())) {
						profMan.incProgress(player, ActionSource.listenerSource(event), i);
						if(obj.getCancel()) {
							event.setCancelled(true);
						}
						return;
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNpcDeath(final NPCDeathEvent event) {
		// npc must be spawned to die, or no?
		@SuppressWarnings("deprecation")
		final Player player = event.getNPC().getBukkitEntity().getKiller();
		if(player == null) {
			return;
		}
		final PlayerProfile prof = profMan.getProfile(player.getName());
		final Quest quest = prof.getQuest();
		if(quest != null) {
			if(!quest.allowedWorld(player.getWorld().getName().toLowerCase())) {
				return;
			}
			final List<Objective> objs = quest.getObjectives();
			for(int i = 0; i < objs.size(); i++) {
				if(objs.get(i).getType().equalsIgnoreCase("NPCKILL")) {
					if(!profMan.isObjectiveActive(prof, i)) {
						continue;
					}
					final NpcKillObjective obj = (NpcKillObjective) objs.get(i);
					if(obj.checkNpc(event.getNPC().getName())) {
						profMan.incProgress(player, ActionSource.listenerSource(event), i);
						return;
					}
				}
			}
		}
	}
}
