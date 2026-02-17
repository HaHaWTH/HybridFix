package io.wdsj.hybridfix.asm;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.wdsj.hybridfix.HybridFix;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface IBytecodePatcher {
    boolean DEBUG_DUMP_BYTECODE = Boolean.getBoolean("hybridfix.asm.debug.export");

    ExecutorService DUMP_EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("HybridFix ASM Dump Thread")
                    .setDaemon(true)
                    .setPriority(Thread.NORM_PRIORITY - 2)
                    .build()
    );

    byte[] transform(String untransformedName, String className, byte[] basicClass);

    default void dump(String className, byte[] classBytes) {
        if (!DEBUG_DUMP_BYTECODE) return;

        DUMP_EXECUTOR.submit(() -> {
            try {
                File dumpDir = new File(".asm.out");
                if (!dumpDir.exists()) {
                    // noinspection ResultOfMethodCallIgnored
                    dumpDir.mkdirs();
                }

                File outputFile = new File(dumpDir, className.replace('.', File.separatorChar) + ".class");

                File parentFile = outputFile.getParentFile();
                if (!parentFile.exists()) {
                    // noinspection ResultOfMethodCallIgnored
                    parentFile.mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(classBytes);
                }
            } catch (IOException e) {
                HybridFix.LOGGER.error("Failed to dump class {}", className, e);
            }
        });
    }

    default void log(String className) {
        HybridFix.LOGGER.info("[{}] Transformed class {}", getClass().getSimpleName(), className);
    }

    boolean isEnabled();

    static void clearDebugDumpDirectory() {
        if (!DEBUG_DUMP_BYTECODE) return;
        try {
            File dumpDir = new File(".asm.out");
            if (dumpDir.exists()) {
                Path rootPath = dumpDir.toPath();
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                        if (exc != null) throw exc;
                        if (!dir.equals(rootPath)) {
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Exception e) {
            HybridFix.LOGGER.error("Failed to clear debug dump directory", e);
        }
    }

    static boolean isCommonPackage(String packageName) {
        return packageName.contains("fastutil") || packageName.contains("org.apache") || packageName.contains("javax");
    }
}