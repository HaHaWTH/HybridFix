package io.wdsj.hybridfix.asm.mod_patcher.impl.hack;

import io.wdsj.hybridfix.asm.mod_patcher.ConfigurableModPatcher;
import io.wdsj.hybridfix.asm.mod_patcher.annotation.ApplyToMod;
import io.wdsj.hybridfix.config.Settings;
import net.minecraftforge.common.util.FakePlayer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Removes instanceof {@link FakePlayer} and {@link io.wdsj.hybridfix.util.HybridFixFakePlayer.HybridFixDummyPlayer} check from PlayerEvent.Clone event handlers.
 */
@ApplyToMod.Configurable
public class FakePlayerInstOfPatcher extends ConfigurableModPatcher {
    private static final String EVENT_DESC = "(Lnet/minecraftforge/event/entity/player/PlayerEvent$Clone;)V";
    private static final String FAKE_PLAYER_INTERNAL_NAME = "net/minecraftforge/common/util/FakePlayer";
    @Override
    public byte[] transform(String untransformedName, String className, byte[] basicClass) {
        ClassNode cn = new ClassNode();
        new ClassReader(basicClass).accept(cn, 0);

        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            if (mn.desc.equals(EVENT_DESC)) {
                ListIterator<AbstractInsnNode> it = mn.instructions.iterator();
                while (it.hasNext()) {
                    AbstractInsnNode insn = it.next();
                    if (insn.getOpcode() == Opcodes.INSTANCEOF) {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        String desc = tin.desc;
                        if (desc.equals(FAKE_PLAYER_INTERNAL_NAME)) {
                            InsnList list = new InsnList();
                            list.add(new InsnNode(Opcodes.POP));
                            list.add(new InsnNode(Opcodes.ICONST_0));
                            mn.instructions.insert(insn, list);
                            mn.instructions.remove(insn);
                            changed = true;
                        }
                    }
                }
            }
        }

        if (changed) {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            byte[] bytes = cw.toByteArray();
            log(className);
            dump(className, bytes);
            return bytes;
        }
        return basicClass;
    }

    @Override
    public boolean isEnabled() {
        return Settings.asmModPatcherSettings.removeFakePlayerInstOf;
    }

    @Override
    public String[] getTargetClasses() {
        return Settings.asmModPatcherSettings.removeFakePlayerInstOfClasses;
    }
}
