package me.neznamy.tab.shared.features.layout;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.YellowNumber;

public class PlayerSlot {

	private YellowNumber yellowNumber;
	private Playerlist playerlist;
	private Layout layout;
	private UUID id;
	private String fakeplayer;
	private TabPlayer player;
	
	public PlayerSlot(Layout layout, UUID id, int slot) {
		yellowNumber = (YellowNumber) TAB.getInstance().getFeatureManager().getFeature("tabobjective");
		playerlist = (Playerlist) TAB.getInstance().getFeatureManager().getFeature("playerlist");
		this.layout = layout;
		this.id = id;
		this.fakeplayer = layout.formatSlot(slot);
	}
	
	public UUID getUUID() {
		return id;
	}
	
	public String getFakePlayer() {
		return fakeplayer;
	}
	
	public void setPlayer(TabPlayer newPlayer) {
		if (player == newPlayer) return;
		this.player = newPlayer;
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(id));
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(packet, layout);
			onJoin(all);
			if (yellowNumber != null) {
				int newYellowNumber = player == null ? 0 : TAB.getInstance().getErrorManager().parseInteger(newPlayer.getProperty(PropertyUtils.YELLOW_NUMBER).get(), 0, "yellow number");
				all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, fakeplayer, newYellowNumber), layout);
			}
		}
	}
	
	public void onJoin(TabPlayer p) {
		PlayerInfoData data;
		if (player != null) {
			data = new PlayerInfoData(fakeplayer, id, player.getSkin(), player.getPing(), EnumGamemode.SURVIVAL, playerlist == null ? new IChatBaseComponent(player.getName()) : playerlist.getTabFormat(player, p));
		} else {
			data = new PlayerInfoData(fakeplayer, id, null, 0, EnumGamemode.SURVIVAL, new IChatBaseComponent(""));
		}
		p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, data), layout);
	}
}
