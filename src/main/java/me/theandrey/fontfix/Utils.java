package me.theandrey.fontfix;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class Utils {

	private static final OpenOption[] WRITE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

	public static ClassNode readClass(byte[] bytes) {
		ClassNode node = new ClassNode();
		new ClassReader(bytes).accept(node, 0);
		return node;
	}

	public static byte[] writeClass(ClassNode node, int flags) {
		ClassWriter writer = new ClassWriter(flags);
		node.accept(writer);
		return writer.toByteArray();
	}

	public static void saveDump(ClassNode node) {
		byte[] data = writeClass(node, 0);
		Path dir = Paths.get("classdump");
		String filename = node.name.replace('/', '.').concat(".class");

		try {
			if (!Files.exists(dir)) {
				Files.createDirectory(dir);
			}

			Files.write(dir.resolve(filename), data, WRITE_OPTIONS);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create file: " + filename, e);
		}
	}

	public static Type getObjectType(String clazz) {
		return Type.getType('L' + clazz.replace('.', '/') + ';');
	}
}
