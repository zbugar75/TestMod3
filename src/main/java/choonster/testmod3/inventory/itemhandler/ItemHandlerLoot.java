package choonster.testmod3.inventory.itemhandler;

import choonster.testmod3.util.IWorldContainer;
import choonster.testmod3.util.InventoryUtils;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * An inventory that generates its contents from a {@link LootTable} the first time it's accessed by a player.
 * <p>
 * Adapted from {@link TileEntityLockableLoot}.
 *
 * @author Choonster
 */
public class ItemHandlerLoot extends ItemHandlerNameable implements ILootContainer {
	/**
	 * The {@link IWorldContainer} to get the {@link World} from.
	 */
	protected final IWorldContainer worldContainer;

	/**
	 * The location of the {@link LootTable} to generate loot from.
	 * <p>
	 * This will be {@code null} if no {@link LootTable} has been set or loot has already been generated.
	 */
	protected ResourceLocation lootTableLocation;

	/**
	 * The random seed to use when generating loot.
	 */
	protected long lootTableSeed;

	public ItemHandlerLoot(final ITextComponent defaultName, final IWorldContainer worldContainer) {
		super(defaultName);
		this.worldContainer = worldContainer;
	}

	public ItemHandlerLoot(final int size, final ITextComponent defaultName, final IWorldContainer worldContainer) {
		super(size, defaultName);
		this.worldContainer = worldContainer;
	}

	public ItemHandlerLoot(final NonNullList<ItemStack> stacks, final ITextComponent defaultName, final IWorldContainer worldContainer) {
		super(stacks, defaultName);
		this.worldContainer = worldContainer;
	}

	/**
	 * Write the {@link LootTable} location and seed to NBT if they're present.
	 *
	 * @param compound The compound tag
	 * @return Was the location written to NBT?
	 */
	protected boolean checkLootAndWrite(final NBTTagCompound compound) {
		if (lootTableLocation != null) {
			compound.putString("LootTable", lootTableLocation.toString());

			if (lootTableSeed != 0L) {
				compound.putLong("LootTableSeed", lootTableSeed);
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Read the {@link LootTable} location and seed from NBT if they're present.
	 *
	 * @param compound The compound tag
	 * @return Was the location read from NBT?
	 */
	protected boolean checkLootAndRead(final NBTTagCompound compound) {
		if (compound.contains("LootTable", Constants.NBT.TAG_STRING)) {
			lootTableLocation = new ResourceLocation(compound.getString("LootTable"));
			lootTableSeed = compound.getLong("LootTableSeed");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		final NBTTagCompound tagCompound = super.serializeNBT();

		if (checkLootAndWrite(tagCompound)) { // If the LootTable location exists, don't write the inventory contents to NBT
			tagCompound.remove("Items");
		}

		return tagCompound;
	}

	@Override
	public void deserializeNBT(final NBTTagCompound nbt) {
		if (checkLootAndRead(nbt)) { // If the LootTable location exists, don't read the inventory contents from NBT
			setSize(nbt.contains("Size", Constants.NBT.TAG_INT) ? nbt.getInt("Size") : stacks.size());
			onLoad();
		} else {
			super.deserializeNBT(nbt);
		}
	}

	/**
	 * Fill this inventory with loot.
	 * <p>
	 * Does nothing if no loot table has been set, loot has already been generated or this is being called on the client side.
	 *
	 * @param player The player whose Luck to use when generating loot
	 */
	public void fillWithLoot(@Nullable final EntityPlayer player) {
		final World world = worldContainer.getContainedWorld();
		if (lootTableLocation != null && !world.isRemote) {
			final MinecraftServer server = Preconditions.checkNotNull(world.getServer());
			final LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(lootTableLocation);
			lootTableLocation = null;

			final Random random = lootTableSeed == 0 ? new Random() : new Random(lootTableSeed);

			final LootContext.Builder builder = new LootContext.Builder((WorldServer) world);

			if (player != null) {
				builder.withLuck(player.getLuck());
			}

			InventoryUtils.fillItemHandlerWithLoot(this, lootTable, random, builder.build());
		}
	}

	@Override
	public ItemStack getStackInSlot(final int slot) {
		fillWithLoot(null);
		return super.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
		fillWithLoot(null);
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
		fillWithLoot(null);
		return super.extractItem(slot, amount, simulate);
	}

	@Override
	public void setStackInSlot(final int slot, final ItemStack stack) {
		fillWithLoot(null);
		super.setStackInSlot(slot, stack);
	}

	@Nullable
	@Override
	public ResourceLocation getLootTable() {
		return lootTableLocation;
	}
}
