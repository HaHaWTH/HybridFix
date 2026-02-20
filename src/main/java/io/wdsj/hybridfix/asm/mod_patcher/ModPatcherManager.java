package io.wdsj.hybridfix.asm.mod_patcher;

import io.wdsj.hybridfix.HybridFix;
import io.wdsj.hybridfix.asm.mod_patcher.annotation.ApplyToMod;
import io.wdsj.hybridfix.config.Settings;
import io.wdsj.hybridfix.util.Utils;

import java.util.*;

public enum ModPatcherManager {
    INSTANCE;
    private final ModPatcherEntry[] modPatchers;

    private static class ModPatcherEntry {
        final String[] targetClasses;
        final AbstractModPatcher patcher;

        ModPatcherEntry(String[] targetClasses, AbstractModPatcher patcher) {
            this.targetClasses = targetClasses;
            this.patcher = patcher;
        }

        public byte[] applyTransform(String name, String className, byte[] basicClass) {
            return patcher.transform(name, className, basicClass);
        }

        @Override
        public String toString() {
            return "ModPatcherEntry{" +
                    "targetClasses=" + Arrays.toString(targetClasses) +
                    ", patcher=" + patcher +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ModPatcherEntry)) return false;
            ModPatcherEntry other = (ModPatcherEntry) obj;
            return Arrays.equals(targetClasses, other.targetClasses) && patcher.equals(other.patcher);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Arrays.hashCode(targetClasses), patcher);
        }
    }

    ModPatcherManager() {
        String patcherPackage = ModPatcherManager.class.getPackage().getName() + ".impl";
        Class<?>[] classes = Utils.getClasses(patcherPackage).toArray(new Class[0]);
        Arrays.sort(classes, Comparator.comparing(Class::getSimpleName));
        List<ModPatcherEntry> patchers = new ArrayList<>();
        for (Class<?> clazz : classes) {
            try {
                if (AbstractModPatcher.class.isAssignableFrom(clazz)) {
                    boolean result = registerModPatcher((AbstractModPatcher) clazz.newInstance(), patchers);
                    if (result) {
                        HybridFix.LOGGER.info("Registered mod patcher {}", clazz.getSimpleName());
                    }
                }
            } catch (Exception e) {
                HybridFix.LOGGER.error("Failed to load mod patcher {}", clazz.getName(), e);
            }
        }
        modPatchers = patchers.toArray(new ModPatcherEntry[0]);
    }

    private static boolean registerModPatcher(AbstractModPatcher patcher, List<ModPatcherEntry> modPatchers) {
        if (!Settings.asmModPatcherSettings.enable) return false;
        try {
            ApplyToMod applyToMod = patcher.getClass().getAnnotation(ApplyToMod.class);
            ApplyToMod.Configurable configurable = patcher.getClass().getAnnotation(ApplyToMod.Configurable.class);
            boolean applyToModExists = applyToMod != null;
            boolean configurableExists = configurable != null;
            if (!applyToModExists && !configurableExists) {
                HybridFix.LOGGER.error("Mod patcher {} is not annotated with @ApplyToMod", patcher.getClass().getName());
                return false;
            }
            if (applyToModExists && configurableExists) {
                HybridFix.LOGGER.error("Found multiple annotations in mod patcher {}, skipping.", patcher.getClass().getName());
            }
            if (applyToModExists) {
                return registerApplyTo(patcher, applyToMod, modPatchers);
            }
            return registerConfigurable(patcher, configurable, modPatchers);
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to register mod patcher {}", patcher.getClass().getSimpleName(), e);
        }
        return false;
    }

    private static boolean registerApplyTo(AbstractModPatcher patcher, ApplyToMod applyToMod, List<ModPatcherEntry> modPatchers) {
        String[] modClassNames = applyToMod.value();
        boolean flag = patcher.isEnabled();
        if (flag) {
            register0(patcher, modClassNames, modPatchers);
            return true;
        }
        return false;
    }

    private static boolean registerConfigurable(AbstractModPatcher patcher, ApplyToMod.Configurable ignored, List<ModPatcherEntry> modPatchers) {
        boolean flag = patcher.isEnabled();
        if (!(patcher instanceof ConfigurableModPatcher)) {
            HybridFix.LOGGER.error("Mod patcher {} is not extending ConfigurableModPatcher, skipping.", patcher.getClass().getName());
            return false;
        }
        if (flag) {
            ConfigurableModPatcher configurablePatcher = (ConfigurableModPatcher) patcher;
            register0(configurablePatcher, configurablePatcher.getTargetClasses(), modPatchers);
            return true;
        }
        return false;
    }

    private static void register0(AbstractModPatcher patcher, String[] pluginNames, List<ModPatcherEntry> modPatchers) {
        ModPatcherEntry entry = new ModPatcherEntry(Arrays.stream(pluginNames).map(String::trim).toArray(String[]::new), patcher);
        if (modPatchers.contains(entry)) throw new IllegalStateException("Duplicate mod patcher entry: " + entry);
        modPatchers.add(entry);
    }

    public byte[] processTransform(String name, String className, byte[] basicClass) {
        if (basicClass == null) return null;

        for (ModPatcherEntry entry : modPatchers) {
            for (String targetClass : entry.targetClasses) {
                if (className.equals(targetClass)) {
                    basicClass = entry.applyTransform(name, className, basicClass);
                }
            }
        }
        return basicClass;
    }
}