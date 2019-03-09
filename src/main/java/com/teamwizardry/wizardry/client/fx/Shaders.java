package com.teamwizardry.wizardry.client.fx;

import com.teamwizardry.librarianlib.features.shader.Shader;
import com.teamwizardry.librarianlib.features.shader.ShaderHelper;
import com.teamwizardry.librarianlib.features.shader.uniforms.UniformFloat;
import com.teamwizardry.librarianlib.features.shader.uniforms.UniformInt;


public class Shaders {

	public static BurstShader burst;
	public static Shader rawColor;

	static {
		burst = ShaderHelper.INSTANCE.addShader(new BurstShader(null, "/assets/wizardry/shader/sparkle.frag"));
		rawColor = ShaderHelper.INSTANCE.addShader(new Shader(null, "/assets/wizardry/shader/rawColor.frag"));
		ShaderHelper.INSTANCE.initShaders();
	}

	public static class BurstShader extends Shader {

		public UniformFloat
				fanSpeedMin = new UniformFloat(-0.7f),
				fanSpeedMax = new UniformFloat(0.7f),
				fanSizeMin = new UniformFloat(0.7f),
				fanSizeMax = new UniformFloat(1.0f),
				fanJitterMin = new UniformFloat(-0.3f),
				fanJitterMax = new UniformFloat(0.0f);
		public UniformInt
				fanBladesMin = new UniformInt(5),
				fanBladesMax = new UniformInt(8),
				fanCount = new UniformInt(8);

		public BurstShader(String vert, String frag) {
			super(vert, frag);
		}
	}

}
