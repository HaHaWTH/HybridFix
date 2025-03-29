package io.wdsj.hybridfix.asm;

import io.wdsj.hybridfix.HybridFix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public interface IBytecodePatcher {
    boolean DEBUG_DUMP_BYTECODE = Boolean.getBoolean("hybridfix.asm.debug.export");
    byte[] transform(String className, byte[] basicClass);

    default void dump(String className, byte[] classBytes) {
        if (!DEBUG_DUMP_BYTECODE) return;
        try {
            File dumpDir = new File(".asm.out");
            if (!dumpDir.exists()) {
                // noinspection ResultOfMethodCallIgnored
                dumpDir.mkdirs();
            }
            File outputFile = new File(dumpDir, className.replace('.', File.separatorChar) + ".class");

            // noinspection ResultOfMethodCallIgnored
            outputFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(classBytes);
            }
        } catch (IOException e) {
            HybridFix.LOGGER.error("Failed to dump class {}", className, e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void clearDebugDumpDirectory() {
        if (!DEBUG_DUMP_BYTECODE) return;
        try {
            File dumpDir = new File(".asm.out");
            if (dumpDir.exists()) {
                Path path = Paths.get(dumpDir.getAbsolutePath());
                try (Stream<Path> fileStream = Files.walk(path)) {
                    fileStream.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to clear debug dump directory", e);
        }
    }
}
