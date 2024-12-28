package io.wdsj.hybridfix.handler;

import com.mojang.authlib.GameProfile;
import io.wdsj.hybridfix.duck.bridge.permission.IEntityPermissionGetter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BukkitForgePermissionHandler implements IPermissionHandler {
    public static void registerDefaultPermission(String name, DefaultPermissionLevel level, String desc) {
        PermissionDefault permissionDefault;
        switch (level) {
            case ALL:
                permissionDefault = PermissionDefault.TRUE;
                break;
            case OP:
                permissionDefault = PermissionDefault.OP;
                break;
            default:
                permissionDefault = PermissionDefault.FALSE;
        };
        Permission permission = new Permission(name, desc, permissionDefault);
        DefaultPermissions.registerPermission(permission);
    }
    @Override
    public void registerNode(String node, DefaultPermissionLevel level, String desc) {
        registerDefaultPermission(node, level, desc);
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        List<String> list = new ArrayList<>();
        for (Permission permission : Bukkit.getPluginManager().getPermissions()) {
            String name = permission.getName();
            list.add(name);
        }
        return list;
    }

    @Override
    public boolean hasPermission(GameProfile profile, String node, @Nullable IContext context) {
        if (context != null) {
            EntityPlayer player = context.getPlayer();
            if (player != null) {
                return ((IEntityPermissionGetter) player).getBukkitEntity().hasPermission(node);
            }

        }
        Player player = Bukkit.getServer().getPlayer(profile.getId());
        if (player != null) {
            return player.hasPermission(node);
        } else {
            Permission perm = Bukkit.getServer().getPluginManager().getPermission(node);
            boolean isOp = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(profile);
            if (perm != null) {
                return perm.getDefault().getValue(isOp);
            } else {
                return Permission.DEFAULT_PERMISSION.getValue(isOp);
            }
        }
    }

    @Override
    public String getNodeDescription(String node) {
        Permission permission = Bukkit.getPluginManager().getPermission(node);
        return permission == null ? "" : permission.getDescription();
    }
}