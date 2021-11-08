package gg.xp.events.state;

import gg.xp.context.SubState;
import gg.xp.events.actlines.data.Job;
import gg.xp.events.models.XivEntity;
import gg.xp.events.models.XivPlayerCharacter;
import gg.xp.events.models.XivWorld;
import gg.xp.events.models.XivZone;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XivState implements SubState {

	private static final Logger log = LoggerFactory.getLogger(XivState.class);

	private XivZone zone;
	// EARLY player info before we have combatant data
	private XivEntity playerPartial;
	// FULL player info after we get the stuff we need
	private XivPlayerCharacter player;
	private @NotNull List<XivPlayerCharacter> partyList = Collections.emptyList();
	private @NotNull Map<Long, CombatantInfo> combatants = Collections.emptyMap();

	// Note: can be null until we have all the required data, but this should only happen very early on in init
	public XivEntity getPlayer() {
		return player;
	}

	public void setPlayer(XivEntity player) {
		log.info("Player changed to {}", player);
		this.playerPartial = player;
		recalcState();
	}

	// Note: can be null until we've seen a 01-line
	public XivZone getZone() {
		return zone;
	}

	public void setZone(XivZone zone) {
		log.info("Zone changed to {}", zone);
		this.zone = zone;
	}

	public void setPartyList(List<XivPlayerCharacter> partyList) {
		this.partyList = new ArrayList<>(partyList);
		recalcState();
		log.info("Party list changed to {}", this.partyList.stream().map(XivEntity::getName).collect(Collectors.joining(", ")));
	}

	public List<XivPlayerCharacter> getPartyList() {
		return new ArrayList<>(partyList);
	}

	// TODO: concurrency issues?
	private void recalcState() {
		// *Shouldn't* happen, but no reason to fail when we'll get the data soon anyway
		if (playerPartial == null) {
			return;
		}
		long playerId = playerPartial.getId();
		CombatantInfo combatantInfo = combatants.get(playerId);
		if (combatantInfo != null) {
			player = new XivPlayerCharacter(
					combatantInfo.getId(),
					combatantInfo.getName(),
					Job.getById(combatantInfo.getJobId()),
					// TODO
					new XivWorld(),
					// TODO
					0);
		}
		if (player != null) {
			partyList.sort(Comparator.comparing(p -> {
				if (player.getId() == p.getId()) {
					// Always sort main player first
					return -1;
				}
				else {
					// TODO: customizable party sorting
					return p.getJob().partySortOrder();
				}
			}));
		}
		log.info("Recalculated state, player is {}, party is {}", player, partyList);

	}

	public boolean zoneIs(long zoneId) {
		XivZone zone = getZone();
		return zone != null && zone.getId() == zoneId;
	}

	public void setCombatants(List<CombatantInfo> combatants) {
		this.combatants = new HashMap<>(combatants.size());
		combatants.forEach(combatant -> {
			long id = combatant.getId();
			// Fake/environment actors
			if (id == 0xE0000000L) {
				return;
			}
			CombatantInfo old = this.combatants.put(id, combatant);
			if (old != null) {
				log.warn("Duplicate combatant data for id {}: old ({}) vs new ({})", id, old, combatant);
			}
		});
		log.info("Received info on {} combatants", combatants.size());
		recalcState();
	}

	public Map<Long, CombatantInfo> getCombatants() {
		return Collections.unmodifiableMap(combatants);
	}
}