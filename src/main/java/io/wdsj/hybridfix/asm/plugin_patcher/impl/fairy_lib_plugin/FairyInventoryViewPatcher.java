package io.wdsj.hybridfix.asm.plugin_patcher.impl.fairy_lib_plugin;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.plugin_patcher.IPluginPatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyToPlugin;
import io.wdsj.hybridfix.config.Settings;
import org.bukkit.inventory.InventoryView;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

/**
 * Redirect InventoryView {@code INVOKEINTERFACE} calls to {@code INVOKEVIRTUAL}.
 */
@ApplyToPlugin("fairy-lib-plugin")
public class FairyInventoryViewPatcher implements IPluginPatcher {

    private static final String TARGET_PACKAGE_PREFIX = "io.fairyproject";
    private static final String INVENTORY_VIEW_OWNER = "org/bukkit/inventory/InventoryView";

    private static final boolean isCurrentImplNeedPatch;
    static {
        isCurrentImplNeedPatch = !InventoryView.class.isInterface();
    }

    @Override
    public byte[] transform(String className, byte[] basicClass) {
        if (className.startsWith(TARGET_PACKAGE_PREFIX)) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if (patchInventoryViewCalls(classNode)) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                byte[] bytes = classWriter.toByteArray();

                dump(className, bytes);
                HybridFix.LOGGER.info("Transformed class {}", className);
                return bytes;
            }
        }
        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return isCurrentImplNeedPatch && Settings.pluginPatcherSettings.patchFairyLibPlugin;
    }

    private boolean patchInventoryViewCalls(ClassNode classNode) {
        boolean changed = false;

        for (MethodNode method : classNode.methods) {
            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (INVENTORY_VIEW_OWNER.equals(methodInsn.owner)) {
                        if (methodInsn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                            methodInsn.setOpcode(Opcodes.INVOKEVIRTUAL);
                            methodInsn.itf = false;
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }
}