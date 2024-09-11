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
import net.runelite.api.ItemContainer;
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

			final Multiset<Integer> diff = Multisets.difference(currentInventory, inventorySnapshot);

			log.info(String.valueOf(diff));

			if (!diff.isEmpty()) {
				sendChatNotification();
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

	private void sendChatNotification()
	{
		final ChatMessageBuilder message = new ChatMessageBuilder()
			.append("Item added to inventory");

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
