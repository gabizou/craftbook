// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.bukkit;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sk89q.craftbook.LanguageManager;
import com.sk89q.craftbook.Mechanic;
import com.sk89q.craftbook.MechanicFactory;
import com.sk89q.craftbook.MechanicManager;
import com.sk89q.craftbook.MechanismsConfiguration;
import com.sk89q.craftbook.bukkit.Metrics.Graph;
import com.sk89q.craftbook.bukkit.commands.MechanismCommands;
import com.sk89q.craftbook.mech.AIMechanic;
import com.sk89q.craftbook.mech.Ammeter;
import com.sk89q.craftbook.mech.Bookcase;
import com.sk89q.craftbook.mech.Bridge;
import com.sk89q.craftbook.mech.Cauldron;
import com.sk89q.craftbook.mech.ChunkAnchor;
import com.sk89q.craftbook.mech.Command;
import com.sk89q.craftbook.mech.CookingPot;
import com.sk89q.craftbook.mech.CustomDrops;
import com.sk89q.craftbook.mech.Door;
import com.sk89q.craftbook.mech.Elevator;
import com.sk89q.craftbook.mech.Gate;
import com.sk89q.craftbook.mech.HiddenSwitch;
import com.sk89q.craftbook.mech.LightStone;
import com.sk89q.craftbook.mech.LightSwitch;
import com.sk89q.craftbook.mech.MapChanger;
import com.sk89q.craftbook.mech.PaintingSwitch;
import com.sk89q.craftbook.mech.Payment;
import com.sk89q.craftbook.mech.Snow;
import com.sk89q.craftbook.mech.Teleporter;
import com.sk89q.craftbook.mech.XPStorer;
import com.sk89q.craftbook.mech.area.Area;
import com.sk89q.craftbook.mech.area.CopyManager;
import com.sk89q.craftbook.mech.cauldron.ImprovedCauldron;
import com.sk89q.craftbook.mech.crafting.CustomCrafting;
import com.sk89q.craftbook.mech.dispenser.DispenserRecipes;
import com.sk89q.craftbook.mech.dispenser.Recipe;


/**
 * Plugin for CraftBook's mechanisms.
 *
 * @author sk89q
 */
@SuppressWarnings("deprecation")
public class MechanismsPlugin extends BaseBukkitPlugin {

    protected MechanismsConfiguration config;

    private final CopyManager copyManager = new CopyManager();
    private MechanicManager manager;
    public static Economy economy = null;  //TODO this probably should be implemented differently

    private DispenserRecipes dRecipes = null;

    private static MechanismsPlugin instance;

    @Override
    public void onEnable() {

        instance = this;

        super.onEnable();

        // Register command classes
        registerCommand(MechanismCommands.class);

        createDefaultConfiguration("books.txt", false);
        createDefaultConfiguration("cauldron-recipes.txt", false);
        createDefaultConfiguration("config.yml", false);
        createDefaultConfiguration("custom-mob-drops.txt", false);
        createDefaultConfiguration("custom-block-drops.txt", false);
        createDefaultConfiguration("recipes.txt", false);
        createDefaultConfiguration("cauldron-recipes.yml", false);
        createDefaultConfiguration("crafting-recipes.yml", false);

        config = new MechanismsConfiguration(getConfig(), getDataFolder());
        saveConfig();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
        }

        manager = new MechanicManager(this);
        MechanicListenerAdapter adapter = new MechanicListenerAdapter(this);
        adapter.register(manager);

        // Let's register mechanics!
        if (getLocalConfiguration().ammeterSettings.enable) {
            registerMechanic(new Ammeter.Factory(this));
        }
        if (getLocalConfiguration().bookcaseSettings.enable) {
            registerMechanic(new Bookcase.Factory(this));
        }
        if (getLocalConfiguration().gateSettings.enable) {
            registerMechanic(new Gate.Factory(this));
        }
        if (getLocalConfiguration().bridgeSettings.enable) {
            registerMechanic(new Bridge.Factory(this));
        }
        if (getLocalConfiguration().doorSettings.enable) {
            registerMechanic(new Door.Factory(this));
        }
        if (getLocalConfiguration().elevatorSettings.enable) {
            registerMechanic(new Elevator.Factory(this));
        }
        if (getLocalConfiguration().teleporterSettings.enable) {
            registerMechanic(new Teleporter.Factory(this));
        }
        if (getLocalConfiguration().areaSettings.enable) {
            registerMechanic(new Area.Factory(this));
        }
        if (getLocalConfiguration().commandSettings.enable) {
            registerMechanic(new Command.Factory(this));
        }
        if (getLocalConfiguration().anchorSettings.enable) {
            registerMechanic(new ChunkAnchor.Factory(this));
        }
        if (getLocalConfiguration().lightStoneSettings.enable) {
            registerMechanic(new LightStone.Factory(this));
        }
        if (getLocalConfiguration().lightSwitchSettings.enable) {
            registerMechanic(new LightSwitch.Factory(this));
        }
        if (getLocalConfiguration().hiddenSwitchSettings.enable) {
            registerMechanic(new HiddenSwitch.Factory(this));
        }
        if (getLocalConfiguration().cookingPotSettings.enable) {
            registerMechanic(new CookingPot.Factory(this));
        }
        if (getLocalConfiguration().cauldronSettings.enable) {
            registerMechanic(new Cauldron.Factory(this));
        }
        if (getLocalConfiguration().cauldronSettings.enableNew) {
            registerMechanic(new ImprovedCauldron.Factory(this));
        }
        if (getLocalConfiguration().xpStorerSettings.enabled) {
            registerMechanic(new XPStorer.Factory(this));
        }
        if (getLocalConfiguration().mapChangerSettings.enabled) {
            registerMechanic(new MapChanger.Factory(this));
        }
        if (getLocalConfiguration().customCraftingSettings.enable) {
            new CustomCrafting(this);
        }

        //Special mechanics.
        if (economy != null) {
            registerMechanic(new Payment.Factory(this));
        }

        setupSelfTriggered(manager);

        // Register events
        registerEvents();

        languageManager = new LanguageManager(this);

        try {
            Metrics metrics = new Metrics(this);

            Graph graph = metrics.createGraph("Language");
            for (String lan : languageManager.getLanguages()) {
                graph.addPlotter(new Metrics.Plotter(lan) {

                    @Override
                    public int getValue() {

                        return 1;
                    }
                });
            }

            metrics.start();
        } catch (Exception ignored) {
        }
    }

    /**
     * Setup the required components of INSTANCE-triggered Mechanics..
     */
    private void setupSelfTriggered(MechanicManager manager) {

        logger.info("CraftBook: Enumerating chunks for INSTANCE-triggered components...");

        long start = System.currentTimeMillis();
        int numWorlds = 0;
        int numChunks = 0;

        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                manager.enumerate(chunk);
                numChunks++;
            }

            numWorlds++;
        }

        long time = System.currentTimeMillis() - start;

        logger.info("CraftBook: " + numChunks + " chunk(s) for " + numWorlds + " world(s) processed "
                + "(" + Math.round(time / 1000.0 * 10) / 10 + "s elapsed)");

        // Set up the clock for INSTANCE-triggered Mechanics.
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new MechanicClock(manager), 0, 2);
    }

    @Override
    protected void registerEvents() {

        if (getLocalConfiguration().dispenserSettings.enable) {
            getServer().getPluginManager().registerEvents(dRecipes = new DispenserRecipes(this), this);
        }
        if (getLocalConfiguration().snowSettings.enable || getLocalConfiguration().snowSettings.placeSnow) {
            getServer().getPluginManager().registerEvents(new Snow(this), this);
        }
        if (getLocalConfiguration().customDropSettings.enable) {
            getServer().getPluginManager().registerEvents(new CustomDrops(this), this);
        }
        if (getLocalConfiguration().aiSettings.enabled) {
            getServer().getPluginManager().registerEvents(new AIMechanic(this), this);
        }
        //if (getLocalConfiguration().chairSettings.enable)
        //    getServer().getPluginManager().registerEvents(new Chair(this), this);
        if (getLocalConfiguration().paintingSettings.enabled) {
            getServer().getPluginManager().registerEvents(new PaintingSwitch(this), this);
        }
    }

    @Override
    public MechanismsConfiguration getLocalConfiguration() {

        return config;
    }

    public CopyManager getCopyManager() {

        return copyManager;
    }

    private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net
                .milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return economy != null;
    }

    public static MechanismsPlugin getInst() {

        return instance;
    }

    /**
     * Register a mechanic if possible
     *
     * @param factory
     */
    private void registerMechanic(MechanicFactory<? extends Mechanic> factory) {

        manager.register(factory);
    }

    /**
     * Register a array of mechanics if possible
     *
     * @param factories
     */
    @SuppressWarnings("unused")
    private void registerMechanic(MechanicFactory<? extends Mechanic>[] factories) {

        for (MechanicFactory<? extends Mechanic> aFactory : factories) {
            registerMechanic(aFactory);
        }
    }

    /**
     * Unregister a mechanic if possible
     * TODO Ensure no remnants are left behind
     *
     * @param factory
     *
     * @return true if the mechanic was successfully unregistered.
     */
    @SuppressWarnings("unused")
    private boolean unregisterMechanic(MechanicFactory<? extends Mechanic> factory) {

        return manager.unregister(factory);
    }

    /**
     * Register a Dispenser Recipe
     *
     * @param recipe
     *
     * @return if successfully added.
     */
    public boolean registerDispenserRecipe(Recipe recipe) {

        return getLocalConfiguration().dispenserSettings.enable && dRecipes.addRecipe(recipe);
    }

    @Override
    public void reloadConfiguration() {
        config = new MechanismsConfiguration(getConfig(), getDataFolder());
        saveConfig();
    }
}