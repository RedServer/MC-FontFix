package me.theandrey.fontfix;

import java.util.ListIterator;
import cpw.mods.fml.relauncher.IClassTransformer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import static org.objectweb.asm.Opcodes.*;

public class ASMTransformer implements IClassTransformer {

	private static final String ASCII_TEXTURE = "/font/default.png";
	private static final String CYRILLIC_TEXTURE = "/font/cyrillic.png";

	@Override
	public byte[] transform(String name, byte[] bytes) {
		switch (name) {
			case "net.minecraft.client.Minecraft":
				return patchMinecraft(bytes);
		}
		return bytes;
	}

	/**
	 * @noinspection ConstantConditions
	 */
	private byte[] patchMinecraft(byte[] bytes) {
		ClassNode clazz = Utils.readClass(bytes);
		final Type fondRenderer = Utils.getObjectType("net.minecraft.client.gui.FontRenderer");
		final String mStartGame = "startGame";

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

}
