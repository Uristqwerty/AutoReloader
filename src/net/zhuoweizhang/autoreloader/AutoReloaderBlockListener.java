package net.zhuoweizhang.autoreloader;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AutoReloaderBlockListener extends BlockListener
{
	//private final AutoReloader plugin;
	
	public AutoReloaderBlockListener(final AutoReloader instance)
	{
		//plugin = instance;
	}
	
	public void onBlockRedstoneChange(BlockRedstoneEvent event)
	{
		Block block = event.getBlock();
		
		if(block.getType() != Material.REDSTONE_WIRE)
			return;
		
		int directions = getRedstoneDirections(block);
		boolean powered = event.getNewCurrent() > 0;
		
		if((directions & 1) != 0)
			updateBlock(block.getRelative(1, 0, 0), powered);
		
		if((directions & 2) != 0)
			updateBlock(block.getRelative(-1, 0, 0), powered);
		
		if((directions & 4) != 0)
			updateBlock(block.getRelative(0, 0, 1), powered);
		
		if((directions & 8) != 0)
			updateBlock(block.getRelative(0, 0, -1), powered);
		
		if((directions & 16) != 0)
			updateBlock(block.getRelative(0, 1, 0), powered);
	}
	
	private void updateBlock(Block block, boolean powered)
	{
		if(block.getType() != Material.DIAMOND_BLOCK)
			return;
		
		Block above = block.getRelative(0, 1, 0);
		Material type = above.getType();
		
		if(type == Material.DISPENSER)
			dispenserUpdate(above, powered);
		else if(type == Material.FURNACE || type == Material.BURNING_FURNACE)
			furnaceUpdate(above, powered);
	}
	
	private int getRedstoneDirections(Block block)
	{
		int directions = 0;
		
		boolean above = block.getRelative(0, 1, 0).getType() == Material.AIR;
		
		if(willWireConnect(block.getRelative(1, 0, 0), above))
			directions |= 1;
		if(willWireConnect(block.getRelative(-1, 0, 0), above))
			directions |= 2;
		if(willWireConnect(block.getRelative(0, 0, 1), above))
			directions |= 4;
		if(willWireConnect(block.getRelative(0, 0, -1), above))
			directions |= 8;
		
		if(directions == 1 || directions == 2)
			directions = 3;
		else if(directions == 4 || directions == 8)
			directions = 12;
		else if(directions == 0)
			directions = 15;
		
		return directions | (above?0:16);
	}
	
	private boolean willWireConnect(Block block, boolean above)
	{
		Material type = block.getType();
		if(type == Material.REDSTONE_WIRE || type == Material.REDSTONE_TORCH_ON
				|| type == Material.REDSTONE_TORCH_OFF || type == Material.STONE_BUTTON)
			return true;
		else if(type == Material.AIR)
		{
			type = block.getRelative(0, -1, 0).getType();
			if(type == Material.REDSTONE_WIRE || type == Material.REDSTONE_TORCH_ON
					|| type == Material.REDSTONE_TORCH_OFF || type == Material.STONE_BUTTON)
				return true;
		}
		else if(above)
		{
			type = block.getRelative(0, 1, 0).getType();
			if(type == Material.REDSTONE_WIRE)
				return true;
		}
		return false;
	}
	
	private void dispenserUpdate(Block block, boolean powered)
	{
		if(!powered) //Apparently, This used to give an error relating to some abstract method or something later
			return;//within craftbukkit code. When I tested it on Bukkit #1000, it worked fine.
		
		Block[] neighbours = new Block[8];
		Block temp;
		
		temp = block.getRelative(1, 0, 0);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[0] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[1] = temp;
		}
		
		temp = block.getRelative(-1, 0, 0);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[2] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[3] = temp;
		}
		
		temp = block.getRelative(0, 0, 1);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[4] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[5] = temp;
		}
		
		temp = block.getRelative(0, 0, -1);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[6] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[7] = temp;
		}
		
		Dispenser dispenser = (Dispenser) block.getState();
		
		Inventory inventory = dispenser.getInventory();
		if(inventory == null)
		{
			System.out.println("AutoReloader: null dispenser inventory.");
			return;
		}
		
		ItemStack[] stack = inventory.getContents();
		
		for(int index=0; index<inventory.getSize(); index++)
		{
			if(stack[index] == null || stack[index].getAmount() == 0 || stack[index].getType() == Material.AIR)
			{
				Block b;
			search:
				for(int j=0; j<8; j++)
				{
					b = neighbours[j];
					if(b != null && neighbours[j & ~1] != null && neighbours[j & ~1].getRelative(0, -1, 0).getType() == Material.LAPIS_BLOCK)
					{
						Inventory i = ((Chest) b.getState()).getInventory();
						if(i == null)
							continue;
						
						ItemStack[] contents = i.getContents();
						
						for(ItemStack s: contents)
						{
							if(s != null && s.getAmount() > 0)
							{
								inventory.setItem(index, s);
								i.clear(i.first(s));
								break search;
							}
						}
					}
				}
			}
		}
	}
	
	private void furnaceUpdate(Block block, boolean powered)
	{
		if(!powered)
			return;
		
		Block[] neighbours = new Block[8];
		Block temp;
		
		temp = block.getRelative(1, 0, 0);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[0] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[1] = temp;
		}
		
		temp = block.getRelative(-1, 0, 0);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[2] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[3] = temp;
		}
		
		temp = block.getRelative(0, 0, 1);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[4] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[5] = temp;
		}
		
		temp = block.getRelative(0, 0, -1);
		if(temp.getType() == Material.CHEST)
		{
			neighbours[6] = temp;
			temp = DoubleChest(temp);
			if(temp != null)
				neighbours[7] = temp;
		}
		
		Furnace furnace = (Furnace) block.getState();
		
		Inventory inventory = furnace.getInventory();
		
		ItemStack[] stack = inventory.getContents();

		if(stack[0] == null || stack[0].getAmount() == 0 || stack[0].getType() == Material.AIR)
		{
			Block b;
		search:
			for(int j=0; j<8; j++)
			{
				b = neighbours[j];
				if(b != null && neighbours[j & ~1] != null && neighbours[j & ~1].getRelative(0, -1, 0).getType() == Material.LAPIS_BLOCK)
				{
					Inventory i = ((Chest) b.getState()).getInventory();
					if(i == null)
						continue;
					
					ItemStack[] contents = i.getContents();
					
					for(ItemStack s: contents)
					{
						if(s != null && s.getAmount() > 0)
						{
							inventory.setItem(0, s);
							i.clear(i.first(s));
							break search;
						}
					}
				}
			}
		}
		
		if(stack[1] == null || stack[1].getAmount() == 0 || stack[1].getType() == Material.AIR)
		{
			Block b;
		search:
			for(int j=0; j<8; j++)
			{
				b = neighbours[j];
				if(b != null && neighbours[j & ~1] != null && neighbours[j & ~1].getRelative(0, -1, 0).getType() == Material.OBSIDIAN)
				{
					Inventory i = ((Chest) b.getState()).getInventory();
					if(i == null)
						continue;
					
					ItemStack[] contents = i.getContents();
					
					for(ItemStack s: contents)
					{
						if(s != null && s.getAmount() > 0)
						{
							inventory.setItem(1, s);
							i.clear(i.first(s));
							break search;
						}
					}
				}
			}
		}
		
		if(stack[2] != null && stack[2].getAmount() != 0)
		{
			Block b;
			for(int j=0; j<8; j++)
			{
				b = neighbours[j];
				if(b != null && neighbours[j & ~1] != null && neighbours[j & ~1].getRelative(0, -1, 0).getType() == Material.GOLD_BLOCK)
				{
					HashMap<Integer, ItemStack> h = ((Chest) b.getState()).getInventory().addItem(stack[2]);
					if(!h.isEmpty())
						stack[2] = h.get(0);
					else
					{
						stack[2].setAmount(0);
						break;
					}
				}
			}
			if(stack[2] != null && stack[2].getAmount() > 0)
				inventory.setItem(2, stack[2]);
			else
				inventory.clear(2);
		}
	}
	
	Block DoubleChest(Block block)
	{
		Block temp;
		
		temp = block.getRelative(1, 0, 0);
		if(temp.getType() == Material.CHEST)
			return temp;
		
		temp = block.getRelative(-1, 0, 0);
		if(temp.getType() == Material.CHEST)
			return temp;
		
		temp = block.getRelative(0, 0, 1);
		if(temp.getType() == Material.CHEST)
			return temp;
		
		temp = block.getRelative(0, 0, -1);
		if(temp.getType() == Material.CHEST)
			return temp;
		
		return null;
	}
}
