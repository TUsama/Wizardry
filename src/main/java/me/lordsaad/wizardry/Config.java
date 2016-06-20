package me.lordsaad.wizardry;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import net.minecraft.launchwrapper.Launch;

public class Config {
    //World Gen
    public static int manaPoolRarity;
    public static boolean developmentEnvironment = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
    
    public static void initConfig() {
        Configuration config = new Configuration(new File("config/Wizardry/World.cfg"));
        config.load();
        manaPoolRarity = config.get("General", "ManaPool", 75, "How rare the mana pool is in terms of 1 in X").getInt();
        config.save();
    }
}
