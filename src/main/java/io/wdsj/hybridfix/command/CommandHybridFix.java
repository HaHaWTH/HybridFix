package io.wdsj.hybridfix.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.duck.bridge.IEntityGetter;
import io.wdsj.hybridfix.util.FormatUtils;
import io.wdsj.hybridfix.util.Updater;
import io.wdsj.hybridfix.util.Utils;
import io.wdsj.hybridfix.util.entity.EntityUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandHybridFix extends Command {
    private static final Cache<String, String> versionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .maximumSize(1L)
            .build();
    private static final boolean forceEraseEntity = Boolean.getBoolean("hybridfix.command.eraseentity.force");
    public static final String ERASE_ENTITY_PERMISSION = "hybridfix.command.eraseentity.use";

    public CommandHybridFix(String name) {
        super(name);
        this.description = "HybridFix commands";
        this.usageMessage = "/hybridfix dumpitem|dumpblock|dumpentity|eraseentity|version";
        setPermission("hybridfix.command.use");
        setAliases(Collections.singletonList("hf"));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "version":
                sender.sendMessage("This server is running HybridFix version " + HybridFix.VERSION + "-" + HybridFix.VERSION_CHANNEL + " (" + Bukkit.getVersion() + ")");
                if (Settings.checkForUpdates) {
                    sender.sendMessage(ChatColor.ITALIC + "Checking version, please wait...");
                    String cachedLatestVersion = versionCache.getIfPresent(HybridFix.VERSION);
                    if (cachedLatestVersion != null) {
                        if (!cachedLatestVersion.equals(HybridFix.VERSION)) {
                            sender.sendMessage(ChatColor.YELLOW + "* There is an update available: " + cachedLatestVersion + ", you're on: " + HybridFix.VERSION + ".");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "* You are running the latest version.");
                        }
                    } else {
                        CompletableFuture.supplyAsync(Updater::checkNow, Utils.commonWorker())
                                .thenAccept(
                                        result -> {
                                            if (result.isUpdateAvailable()) {
                                                sender.sendMessage(ChatColor.YELLOW + "* There is an update available: " + result.getLatestVersion() + ", you're on: " + HybridFix.VERSION + ".");
                                            } else {
                                                if (!result.isError()) {
                                                    sender.sendMessage(ChatColor.GREEN + "* You are running the latest version.");
                                                } else {
                                                    sender.sendMessage(ChatColor.RED + "* Error obtaining version information.");
                                                }
                                            }
                                            versionCache.put(HybridFix.VERSION, result.getLatestVersion());
                                        }
                                );
                    }
                }
                break;
            case "eraseentity":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }
                if (!sender.hasPermission(ERASE_ENTITY_PERMISSION)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
                EntityPlayer nmsPlayer = ((CraftPlayer) sender).getHandle();
                EntityLivingBase target = EntityUtils.rayTraceLivingEntity(nmsPlayer);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "No entity found.");
                    return true;
                }
                final int dist = (int) Math.ceil(nmsPlayer.getDistance(target));
                ResourceLocation rl = EntityList.getKey(target);
                String name = rl != null ? rl.toString() : "unknown:unknown";
                try {
                    if (forceEraseEntity) target.isDead = true;
                    else target.setHealth(0);
                    sender.sendMessage(ChatColor.GREEN + "Erased entity with name " + name + ". (" + dist + " blocks away)");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Failed to erase entity with name " + name + ".");
                    HybridFix.LOGGER.error("Failed to erase entity with name {}", name, e);
                }
                break;
            case "dumpentity":
                handleDumpEntity(sender, args);
                break;
            case "dumpblock":
                handleDumpBlock(sender, args);
                break;
            case "dumpitem":
                handleDumpItem(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
                return false;
        }
        return true;
    }

    private void handleDumpItem(CommandSender sender, String[] ignored) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        Player player = (Player) sender;
        org.bukkit.inventory.ItemStack bItemStack = player.getInventory().getItemInMainHand().clone();
        ItemStack itemInHand = CraftItemStack.asNMSCopy(bItemStack);
        if (itemInHand.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You are not holding any item.");
            return;
        }
        sender.sendMessage(FormatUtils.formatItemStackToPrettyString(itemInHand));
        sender.sendMessage(ChatColor.YELLOW + "Bukkit Material: " + ChatColor.GREEN + bItemStack.getType());

        Class<?> nmsItemClass = itemInHand.getItem().getClass();
        TextComponent classComponent = new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "NMS Item Class: " + net.md_5.bungee.api.ChatColor.GREEN + nmsItemClass.getName() + "\n");
        TextComponent classHierarchy = new TextComponent(Utils.classHierarchyToString(nmsItemClass));
        classHierarchy.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{classHierarchy});
        classComponent.setHoverEvent(hoverEvent);
        sender.spigot().sendMessage(classComponent);

        TextComponent message = new TextComponent("[Click to insert give command]");
        message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, FormatUtils.itemStackToGiveCommand(itemInHand)));
        sender.spigot().sendMessage(message);
    }

    private void handleDumpEntity(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(net.md_5.bungee.api.ChatColor.RED + "Only players can use this command.");
            return;
        }
        EntityPlayer nmsPlayer = ((CraftPlayer) sender).getHandle();
        EntityLivingBase target;
        String name;
        if (args.length > 1 && args[1].toLowerCase(Locale.ROOT).equals("self")) {
            target = nmsPlayer;
        } else {
            target = EntityUtils.rayTraceLivingEntity(nmsPlayer);
        }
        if (target == null) {
            sender.sendMessage(net.md_5.bungee.api.ChatColor.RED + "No entity found.");
            return;
        }

        if (target instanceof EntityPlayer) {
            name = target.getName();
        } else {
            ResourceLocation rl = EntityList.getKey(target);
            name = rl != null ? rl.toString() : "unknown:unknown";
        }

        BlockPos pos = target.getPosition();
        Class<? extends Entity> clazz = target.getClass();

        TextComponent message = new TextComponent();

        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.AQUA + "Info for entity " + name + " at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "):\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Health: " + net.md_5.bungee.api.ChatColor.GREEN + target.getHealth() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Max Health: " + net.md_5.bungee.api.ChatColor.GREEN + target.getMaxHealth() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Is Dead: " + net.md_5.bungee.api.ChatColor.GREEN + target.isDead + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Armor Count: " + net.md_5.bungee.api.ChatColor.GREEN + target.getTotalArmorValue() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Ticks Survived: " + net.md_5.bungee.api.ChatColor.GREEN + target.ticksExisted + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Active Potion Effects: " + net.md_5.bungee.api.ChatColor.GREEN + target.getActivePotionEffects() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Dimension: " + net.md_5.bungee.api.ChatColor.GREEN + target.dimension + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Boss: " + net.md_5.bungee.api.ChatColor.GREEN + !target.isNonBoss() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Entity ID: " + net.md_5.bungee.api.ChatColor.GREEN + target.getEntityId() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "UUID: " + net.md_5.bungee.api.ChatColor.GREEN + target.getUniqueID() + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Bukkit EntityType: " + net.md_5.bungee.api.ChatColor.GREEN + ((IEntityGetter) target).getBukkitEntity().getType().toString() + "\n"));
        // noinspection deprecation
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Bukkit EntityType ID: " + net.md_5.bungee.api.ChatColor.GREEN + ((IEntityGetter) target).getBukkitEntity().getType().getTypeId() + "\n"));

        TextComponent classComponent = new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Entity Class: " + net.md_5.bungee.api.ChatColor.GREEN + clazz.getName());
        TextComponent classHierarchy = new TextComponent(Utils.classHierarchyToString(clazz));
        classHierarchy.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{classHierarchy});
        classComponent.setHoverEvent(hoverEvent);
        message.addExtra(classComponent);

        sender.spigot().sendMessage(message);
    }

    private void handleDumpBlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }

        boolean stopOnLiquid = true;
        boolean far = false;
        if (args.length > 1) {
            String[] strippedArgs = Arrays.copyOfRange(args, 1, args.length);
            for (String arg : strippedArgs) {
                if (arg.equalsIgnoreCase("ignoreliquid")) {
                    stopOnLiquid = false;
                } else if (arg.equalsIgnoreCase("far")) {
                    far = true;
                }
            }
        }
        Player player = (Player) sender;
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        World world = nmsPlayer.world;
        BlockPos pos = EntityUtils.rayTraceBlock(nmsPlayer, far ? FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getViewDistance() * 16 : 7.0D, stopOnLiquid, false);
        if (pos == null) {
            sender.sendMessage(ChatColor.RED + "No block found.");
            return;
        }
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block == Blocks.AIR) {
            sender.sendMessage(ChatColor.RED + "No block found.");
            return;
        }
        TextComponent message = new TextComponent();

        ResourceLocation blockRegName = Block.REGISTRY.getNameForObject(block);
        String blockName = blockRegName.toString();
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.AQUA + "Info for block " + blockName + " at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "):" + "\n"));

        Map<IProperty<?>, Comparable<?>> properties = blockState.getProperties();
        if (!properties.isEmpty()) {
            message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Block State Properties:" + "\n"));
            properties.forEach((property, value) -> message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + " - " + property.getName() + ": " + net.md_5.bungee.api.ChatColor.GREEN + value.toString() + "\n")));
        } else {
            message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "No Block State Properties." + "\n"));
        }

        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Hardness: " + net.md_5.bungee.api.ChatColor.GREEN + blockState.getBlockHardness(world, pos) + "\n"));
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Light Level: " + net.md_5.bungee.api.ChatColor.GREEN + blockState.getLightValue(world, pos) + "\n"));


        if (block.hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null) {
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Tile Entity:" + "\n"));

                ResourceLocation tileEntityName = TileEntity.getKey(tileEntity.getClass());
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + " - Type: " + net.md_5.bungee.api.ChatColor.GREEN + (tileEntityName != null ? tileEntityName.toString() : "unknown:unknown") + "\n"));
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + " - Pos: " + net.md_5.bungee.api.ChatColor.GREEN + tileEntity.getPos().getX() + ", " + tileEntity.getPos().getY() + ", " + tileEntity.getPos().getZ() + "\n"));
            } else {
                message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.RED + "Tile Entity expected but not found!" + "\n"));
            }
        } else {
            message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "No Tile Entity associated." + "\n"));
        }

        TextComponent classComponent = new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "NMS Block Class: " + net.md_5.bungee.api.ChatColor.GREEN + block.getClass().getName() + "\n");
        TextComponent classHierarchy = new TextComponent(Utils.classHierarchyToString(block.getClass()));
        classHierarchy.setColor(net.md_5.bungee.api.ChatColor.GRAY);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{classHierarchy});
        classComponent.setHoverEvent(hoverEvent);
        message.addExtra(classComponent);

        org.bukkit.block.Block bBlock = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        message.addExtra(new TextComponent(net.md_5.bungee.api.ChatColor.YELLOW + "Bukkit Material: " + net.md_5.bungee.api.ChatColor.GREEN + bBlock.getType().toString()));

        sender.spigot().sendMessage(message);
    }
}
