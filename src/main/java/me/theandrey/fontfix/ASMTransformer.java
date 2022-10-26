package me.theandrey.fontfix;

import java.util.ListIterator;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public class ASMTransformer implements IClassTransformer {

	/** Fixed font.txt table */
	private static final String CYRILLIC_TABLE = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_¸abcdefghijklmnopqrstuvwxyz{|}~ё" +
			"\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u0401\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF" +
			"\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF" +
			"\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00AB\u00BB" +
			"\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF" +
			"\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u0419\u041A\u041B\u041C\u041D\u041E\u041F" +
			"\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042A\u042B\u042C\u042D\u042E\u042F" +
			"\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u0439\u043A\u043B\u043C\u043D\u043E\u043F" +
			"\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044A\u044B\u044C\u044D\u044E\u044F";
	private static final String ASCII_TEXTURE = "/font/default.png";
	private static final String CYRILLIC_TEXTURE = "/font/default_ru.png";

	@Override
	public byte[] transform(String name, byte[] bytes) {
		switch (name) {
			case "bn":
				return patchStringTranslate(bytes, true);
			case "net.minecraft.util.StringTranslate":
				return patchStringTranslate(bytes, false);
			case "net.minecraft.client.Minecraft":
				return patchMinecraft(bytes, LoadingPlugin.isGameObfuscated());
			case "u":
			case "net.minecraft.util.ChatAllowedCharacters":
				return patchAllowedCharacters(bytes);
		}
		return bytes;
	}

	/**
	 * Делает русскую локаль не-unicode
	 */
	private byte[] patchStringTranslate(byte[] bytes, boolean obf) {
		ClassNode clazz = Utils.readClass(bytes);

		final String findMethod = obf ? "a" : "setLanguage";
		final String isUnicode = obf ? "e" : "isUnicode";
		final String currentLanguage = obf ? "d" : "currentLanguage";

		for (MethodNode method : clazz.methods) {
			if (method.name.equals(findMethod) && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)))) {

				ListIterator<AbstractInsnNode> iter = method.instructions.iterator();

				while (iter.hasNext()) {
					AbstractInsnNode insn = iter.next();

					if (insn.getOpcode() == PUTFIELD) {
						FieldInsnNode fieldInsn = ((FieldInsnNode)insn);

						// Присвоения значения 'currentLanguage'
						if (fieldInsn.owner.equals(clazz.name) && fieldInsn.name.equals(currentLanguage) && fieldInsn.desc.equals(Type.getType(String.class).getDescriptor())) {

							LabelNode label = new LabelNode();
							InsnList list = new InsnList();
							list.add(new LdcInsnNode("ru_RU"));
							list.add(new VarInsnNode(ALOAD, 1)); // #1 param
							list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "equals", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class)), false));
							list.add(new JumpInsnNode(IFEQ, label));
							list.add(new VarInsnNode(ALOAD, 0)); // this
							list.add(new InsnNode(ICONST_0));
							list.add(new FieldInsnNode(PUTFIELD, clazz.name, isUnicode, Type.BOOLEAN_TYPE.getDescriptor())); // this
							list.add(label);

							method.instructions.insert(insn, list);
							break;
						}
					}
				}

				break;
			}
		}

		return Utils.writeClass(clazz, ClassWriter.COMPUTE_FRAMES);
	}

	/**
	 * Меняет название текстуры шрифта по-умолчанию
	 * @noinspection ConstantConditions
	 */
	private byte[] patchMinecraft(byte[] bytes, boolean obf) {
		ClassNode clazz = Utils.readClass(bytes);
		final Type fondRenderer = Utils.getObjectType(obf ? "atq" : "net.minecraft.client.gui.FontRenderer");
		final String startGame = obf ? "a" : "startGame";

		for (MethodNode method : clazz.methods) {
			if (method.name.equals(startGame) && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE))) {

				ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
				boolean inConstructor = false;

				while (iter.hasNext()) {
					AbstractInsnNode insn = iter.next();

					if (insn.getOpcode() == NEW && !inConstructor) {
						TypeInsnNode type = (TypeInsnNode)insn;
						if (type.desc.equals(fondRenderer.getInternalName())) {
							inConstructor = true;
						}
					}

					if (insn.getType() == AbstractInsnNode.LDC_INSN && inConstructor) {
						LdcInsnNode ldc = (LdcInsnNode)insn;
						if (ldc.cst.equals(ASCII_TEXTURE)) {
							ldc.cst = CYRILLIC_TEXTURE;
						}
					}

					if (insn.getOpcode() == INVOKESPECIAL && inConstructor) {
						MethodInsnNode call = (MethodInsnNode)insn;
						if (call.owner.equals(fondRenderer.getInternalName())) {
							inConstructor = false;
						}
					}
				}

				break;
			}
		}

		return Utils.writeClass(clazz, 0);
	}

	/**
	 * Исправляет проблему отсутствия нужных символов в font.txt
	 */
	private byte[] patchAllowedCharacters(byte[] bytes) {
		ClassNode clazz = Utils.readClass(bytes);

		for (MethodNode method : clazz.methods) {
			if ((method.name.equals("a") || method.name.equals("getAllowedCharacters")) && method.desc.equals(Type.getMethodDescriptor(Type.getType(String.class)))) {
				InsnList list = new InsnList();
				list.add(new LdcInsnNode(CYRILLIC_TABLE));
				list.add(new InsnNode(ARETURN));
				method.tryCatchBlocks.clear();
				method.instructions = list;
				break;
			}
		}

		return Utils.writeClass(clazz, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	}
}
