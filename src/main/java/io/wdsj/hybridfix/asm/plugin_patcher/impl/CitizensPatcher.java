package io.wdsj.hybridfix.asm.plugin_patcher.impl;

import io.wdsj.hybridfix.asm.IBytecodePatcher;
import io.wdsj.hybridfix.asm.plugin_patcher.ServerSoftware;
import io.wdsj.hybridfix.asm.plugin_patcher.annotation.ApplyTo;
import io.wdsj.hybridfix.util.ObfHelper;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ListIterator;
import java.util.Objects;

@ApplyTo(ServerSoftware.OTHERS)
public class CitizensPatcher implements IBytecodePatcher {
    private static boolean isPatchNeeded = true;

    static {
        try {
            Method method = PathFinder.class.getDeclaredMethod(ObfHelper.getName("findPath", "func_186335_a"), PathPoint.class, PathPoint.class, float.class);
            int mod = method.getModifiers();
            isPatchNeeded = Modifier.isPrivate(mod);
        } catch (Exception ignored) {
        }
    }

    @Override
    public byte[] transform(String className, byte[] basicClass) {
        if (className.equals("net.citizensnpcs.nms.v1_12_R1.util.PlayerPathfinder") && isPatchNeeded) {
            byte[] patched = remapPlayerPathfinder(basicClass);
            dump(className, patched);
            return patched;
        }
        return basicClass;
    }

    private byte[] remapPlayerPathfinder(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            ListIterator<AbstractInsnNode> insnIterator = method.instructions.iterator();
            while (insnIterator.hasNext()) {
                AbstractInsnNode insnNode = insnIterator.next();
                if (insnNode instanceof MethodInsnNode) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
                    if (Objects.equals(methodInsnNode.name, "func_186334_a") || Objects.equals(methodInsnNode.name, "func_75853_a") || Objects.equals(methodInsnNode.name, "func_186335_a")) {
                        methodInsnNode.name = "a"; // Don't remap to private func
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);

        return writer.toByteArray();
    }
}
