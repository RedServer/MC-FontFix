package com.gamerforea.fontfix;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.Launch;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(900) // Obf
public class LoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"com.gamerforea.fontfix.ASMTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> map) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static boolean isDeobfuscatedEnvironment() {
		return (boolean)Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false);
	}
}
