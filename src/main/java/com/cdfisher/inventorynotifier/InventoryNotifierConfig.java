package com.cdfisher.inventorynotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("inventorynotifier")
public interface InventoryNotifierConfig extends Config
{
	@ConfigSection(
		name = "Notifications",
		description = "Settings for what types of notifications are fired and when to send them.",
		position = 0,
		closedByDefault = false
	)
	String notificationOptions = "notificationOptions";

	@ConfigItem(
		keyName = "notifyOnGain",
		name = "Notify on inventory gain",
		description = "Send a notification when an item enters the inventory.",
		position = 1,
		section = notificationOptions
	)
	default boolean notifyOnGain()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyOnLoss",
		name = "Notify on inventory loss",
		description = "Send a notification when an item leaves the inventory.",
		position = 2,
		section = notificationOptions
	)
	default boolean notifyOnLoss()
	{
		return false;
	}

	@ConfigSection(
		name = "Special Behaviors",
		description = "Settings to control various sets of conditions affecting when notifications fire",
		position = 3,
		closedByDefault = false
	)
	String specialBehaviors = "specialBehaviors";

	@ConfigItem(
		keyName = "serum207Mode",
		name = "Serum 207 mode",
		description = "When enabled, does not show a notification the next time an empty vial enters the inventory</br>" +
			"after a Serum 207 (1) leaves the inventory.",
		position = 4,
		section = specialBehaviors
	)
	default boolean serum207Mode()
	{
		return false;
	}

	@ConfigSection(
		name = "Miscellaneous",
		description = "Miscellaneous settings.",
		position = 5,
		closedByDefault = false
	)
	String misc = "misc";

	@ConfigItem(
		keyName = "showPluginName",
		name = "Show plugin name",
		description = "Show plugin name in chatbox notifications.",
		position = 6,
		section = misc
	)
	default boolean showPluginName()
	{
		return true;
	}
}
