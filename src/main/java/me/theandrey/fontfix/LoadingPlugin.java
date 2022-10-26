package me.theandrey.fontfix;

import java.util.Map;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"me.theandrey.fontfix.ASMTransformer"};
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
		// NO-OP
	}

	@Override
	public String[] getLibraryRequestClass() {
		return new String[0];
	}
}
