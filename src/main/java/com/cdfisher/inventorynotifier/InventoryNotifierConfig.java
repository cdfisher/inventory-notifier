package com.cdfisher.inventorynotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("inventorynotifier")
public interface InventoryNotifierConfig extends Config
{
	@ConfigItem(
		keyName = "notifyOnGain",
		name = "Notify on inventory gain",
		description = "Send a notification when an item enters the inventory."
	)
	default boolean notifyOnGain()
	{
		return true;
	}
}
