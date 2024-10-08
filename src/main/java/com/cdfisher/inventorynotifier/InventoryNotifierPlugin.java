package com.cdfisher.inventorynotifier;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Inventory Notifier"
)

public class InventoryNotifierPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private InventoryNotifierConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	private Multiset<Integer> inventorySnapshot;

	private boolean skipNextEmptyVial = false;

	@Override
	protected void startUp() throws Exception
	{
		takeSnapshot();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Inventory Notifier stopped!");
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY))
		{
			final ItemContainer inventoryContainer = itemContainerChanged.getItemContainer();
			Multiset<Integer> currentInventory = HashMultiset.create();
			Arrays.stream(inventoryContainer.getItems())
				.forEach(item -> currentInventory.add(item.getId(), item.getQuantity()));

			final Multiset<Integer> diff = Multisets.difference(currentInventory, inventorySnapshot); // Items entering inventory
			final Multiset<Integer> diffr = Multisets.difference(inventorySnapshot, currentInventory); // Items leaving inventory

			if (config.serum207Mode())
			{
				if (diffr.contains(ItemID.SERUM_207_1))
				{
					skipNextEmptyVial = true;
				}

				if (!diff.isEmpty() && skipNextEmptyVial)
				{
					if (diff.contains(ItemID.EMPTY_VIAL))
					{
						diff.remove(ItemID.EMPTY_VIAL);
						skipNextEmptyVial = false;
					}
				}
			}

			if (!diff.isEmpty() && config.notifyOnGain())
			{
				sendItemAddedNotification(diff);
			}

			if (!diffr.isEmpty() && config.notifyOnLoss())
			{
				sendItemRemovedNotification(diffr);
			}

			takeSnapshot();
		}
	}

	private void takeSnapshot()
	{
		inventorySnapshot = HashMultiset.create();
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer != null)
		{
			Arrays.stream(itemContainer.getItems())
				.forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
		}
	}

	private void sendItemAddedNotification(Multiset<Integer> d)
	{
		String pluginName;
		if (config.showPluginName())
		{
			pluginName = "[Inventory Notifier] ";
		} else {
			pluginName = "";
		}

		ChatMessageBuilder message = new ChatMessageBuilder()
			.append(pluginName)
			.append("Item added to inventory:");

		for (Integer id : d.elementSet()) {
			Item i = new Item(id, d.count(id));
			message
				.append(" ")
				.append(client.getItemDefinition(id).getName())
				.append(" x ")
				.append(String.valueOf(i.getQuantity()));
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message.build())
			.build());
	}

	private void sendItemRemovedNotification(Multiset<Integer> d)
	{
		String pluginName;
		if (config.showPluginName())
		{
			pluginName = "[Inventory Notifier] ";
		} else {
			pluginName = "";
		}

		ChatMessageBuilder message = new ChatMessageBuilder()
			.append(pluginName)
			.append("Item removed from inventory:");

		for (Integer id : d.elementSet()) {
			Item i = new Item(id, d.count(id));
			message
				.append(" ")
				.append(client.getItemDefinition(id).getName())
				.append(" x ")
				.append(String.valueOf(i.getQuantity()));
		}

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message.build())
			.build());
	}

	@Provides
	InventoryNotifierConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InventoryNotifierConfig.class);
	}
}
