package com.gamerforea.fontfix;

import java.util.ListIterator;
import com.google.common.collect.ImmutableSet;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public class ASMTransformer implements IClassTransformer {

	private static final String ASCII_TABLE = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
	private static final String ASCII_TABLE_RU = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0451\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u0401\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u0419\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042a\u042b\u042c\u042d\u042e\u042f\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u0439\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044a\u044b\u044c\u044d\u044e\u044f";
	private static final String ASCII_TEXTURE = "textures/font/ascii.png";
	private static final String ASCII_TEXTURE_RU = "textures/font/ascii_ru.png";

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		switch (name) {
			case "bbu":
			case "net.minecraft.client.gui.FontRenderer":
				return patchFontRenderer(bytes);
			case "brs":
				return patchLocale(bytes, true);
			case "net.minecraft.client.resources.Locale":
				return patchLocale(bytes, false);
			case "net.minecraft.client.Minecraft":
				return patchMinecraft(bytes, false);
			case "bao":
				return patchMinecraft(bytes, true);
			case "cofh.core.ProxyClient":
			case "cofh.lib.render.RenderHelper":
			case "buildcraft.silicon.render.PackageFontRenderer":
			case "blusunrize.immersiveengineering.client.IEManualInstance":
				return patchMods(bytes);
		}
		return bytes;
	}

	private byte[] patchFontRenderer(byte[] bytes) {
		ClassNode clazz = Utils.readClass(bytes);

		for (MethodNode method : clazz.methods) {
			ListIterator<AbstractInsnNode> iter = method.instructions.iterator();

			while (iter.hasNext()) {
				AbstractInsnNode insn = iter.next();

				if (insn.getType() == AbstractInsnNode.LDC_INSN) {
					LdcInsnNode ldc = (LdcInsnNode)insn;

					if (ldc.cst.equals(ASCII_TABLE)) {
						ldc.cst = ASCII_TABLE_RU;
						LoadingPlugin.LOGGER.debug("Replaced ASCII table in '{} {}'", clazz.name, method.name + method.desc);
					}
				}
			}
		}

		return Utils.writeClass(clazz, 0);
	}

	private byte[] patchLocale(byte[] bytes, boolean obf) {
		ClassNode clazz = Utils.readClass(bytes);

		final Type classMc = Utils.getObjectType(obf ? "bao" : "net.minecraft.client.Minecraft");
		final Type classSettings = Utils.getObjectType(obf ? "bbj" : "net.minecraft.client.settings.GameSettings");
		final Type classLocale = Utils.getObjectType(obf ? "brs" : "net.minecraft.client.resources.Locale");
		final String findMethod = obf ? "a" : "isUnicode";
		final String getMinecraft = obf ? "B" : "getMinecraft";
		final String gameSettings = obf ? "u" : "gameSettings";

		for (MethodNode method : clazz.methods) {
			if (method.name.equals(findMethod) && method.desc.equals(Type.getMethodDescriptor(Type.BOOLEAN_TYPE))) {
				InsnList list = new InsnList();

				list.add(new MethodInsnNode(INVOKESTATIC, classMc.getInternalName(), getMinecraft, Type.getMethodDescriptor(classMc), false));
				list.add(new VarInsnNode(ASTORE, 1));
				list.add(new VarInsnNode(ALOAD, 1));
				list.add(new FieldInsnNode(GETFIELD, classMc.getInternalName(), gameSettings, classSettings.getDescriptor()));
				list.add(new FieldInsnNode(GETFIELD, classSettings.getInternalName(), obf ? "aK" : "language", Type.getType(String.class).getDescriptor()));
				list.add(new LdcInsnNode("ru_RU"));
				list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(String.class), "equals", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Object.class)), false));
				list.add(new MethodInsnNode(INVOKESTATIC, classMc.getInternalName(), getMinecraft, Type.getMethodDescriptor(classMc), false));
				list.add(new FieldInsnNode(GETFIELD, classMc.getInternalName(), gameSettings, classSettings.getDescriptor()));
				list.add(new FieldInsnNode(GETFIELD, classSettings.getInternalName(), obf ? "aL" : "forceUnicodeFont", Type.BOOLEAN_TYPE.getDescriptor()));
				list.add(new InsnNode(IRETURN));
				list.add(new FrameNode(F_APPEND, 1, new Object[]{classMc.getInternalName()}, 0, null));
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new FieldInsnNode(GETFIELD, classLocale.getInternalName(), obf ? "d" : "field_135029_d", Type.BOOLEAN_TYPE.getDescriptor()));
				list.add(new InsnNode(IRETURN));

				method.instructions = list;
				LoadingPlugin.LOGGER.debug("Fixed unicode detection for 'ru_RU' locale.");
				break;
			}
		}

		return Utils.writeClass(clazz, ClassWriter.COMPUTE_FRAMES);
	}

	private byte[] patchMinecraft(byte[] bytes, boolean obf) {
		ClassNode clazz = Utils.readClass(bytes);
		final Type fondRenderer = Utils.getObjectType(obf ? "bbu" : "net.minecraft.client.gui.FontRenderer");
		final String mStartGame = obf ? "ag" : "startGame";

		for (MethodNode method : clazz.methods) {
			if (method.name.equals(mStartGame) && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE))) {

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
							ldc.cst = ASCII_TEXTURE_RU;
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
	 * Учим моды использовать новую текстуру шрифта
	 */
	private byte[] patchMods(byte[] bytes) {
		ClassNode clazz = Utils.readClass(bytes);
		ImmutableSet<String> methodNames = ImmutableSet.of("<cinit>", "<init>", "registerRenderInformation");

		for (MethodNode method : clazz.methods) {
			if (methodNames.contains(method.name)) {
				replaceFontTexture(clazz, method);
			}
		}

		return Utils.writeClass(clazz, 0);
	}

	private static boolean replaceFontTexture(ClassNode clazz, MethodNode method) {
		final Type resourceLoc = Utils.getObjectType("net.minecraft.util.ResourceLocation"); // SRG Only
		ListIterator<AbstractInsnNode> it = method.instructions.iterator();
		boolean inConstructor = false;

		while (it.hasNext()) {
			AbstractInsnNode node = it.next();

			if (node.getOpcode() == NEW && !inConstructor) {
				TypeInsnNode type = (TypeInsnNode)node;
				if (type.desc.equals(resourceLoc.getInternalName())) {
					inConstructor = true;
				}
			}

			if (inConstructor && node.getType() == AbstractInsnNode.LDC_INSN) {
				LdcInsnNode ldc = (LdcInsnNode)node;
				if (ldc.cst.equals(ASCII_TEXTURE)) {
					ldc.cst = ASCII_TEXTURE_RU;
					LoadingPlugin.LOGGER.debug("Replaced default font texture in '{} {}'", clazz.name, method.name + method.desc);
					return true;
				}
			}

			if (node.getOpcode() == INVOKESPECIAL && inConstructor) {
				inConstructor = false;
			}
		}

		return false;
	}
}
