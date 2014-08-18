package com.coryjuhlin.Extractor;


import org.objectweb.asm.ClassReader;


import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Extracts the signatures of declared methods in supplied file (either a .class or .jar file)
 */
public class MethodSignaturesExtract {
    private final Path toScan;
    private LinkedHashSet<String> methodSignatures = new LinkedHashSet<>();

    public MethodSignaturesExtract(Path scanFile) {
        this.toScan = scanFile;

        // Scan for method signatures
        String fileName = toScan.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        if (extension.equals(".jar")) {
            this.scanJARFile();
        } else {
            this.scanFile();
        }
    }

    public LinkedHashSet<String> getMethodSignatures() {
        return new LinkedHashSet<>(methodSignatures);
    }

    private void scanFile() {
        try (FileInputStream classFile = new FileInputStream(toScan.toFile())) {
            ClassReader reader = new ClassReader(classFile);
            reader.accept(new ClassInspector(methodSignatures), 0);
        } catch (Exception e) {
            System.out.format("ERROR: Problem reading file \"%s\"\n", toScan.toAbsolutePath().toString());
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void scanJARFile() {
        try (JarFile jarFile = new JarFile(toScan.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            ArrayList<JarEntry> jarClassFiles = new ArrayList<>();

            // Find every .class file in JAR
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String elementName = entry.getName();
                int extensionStart = elementName.lastIndexOf('.');

                if (extensionStart < 0) {
                    continue;
                }

                String extension = elementName.substring(elementName.lastIndexOf('.'));

                if (extension.equals(".class")) {
                    jarClassFiles.add(entry);
                }
            }

            // Parse each .class file
            for (JarEntry classFile : jarClassFiles) {
                ClassReader reader = new ClassReader(jarFile.getInputStream(classFile));
                reader.accept(new ClassInspector(methodSignatures), 0);
            }
        } catch (IOException e) {
            System.out.printf("\n\nERROR reading JAR file \"%s\"", toScan.toAbsolutePath().toString());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
