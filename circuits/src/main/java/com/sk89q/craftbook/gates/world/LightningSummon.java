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

package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICUtil;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SignUtil;

public class LightningSummon extends AbstractIC {

    private Block center;

    public LightningSummon(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
        load();
    }

    private void load() {

        try {
            String line = getSign().getLine(2);
            if (line.length() > 0) {
                center = ICUtil.parseBlockLocation(getSign());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Zeus Bolt";
    }

    @Override
    public String getSignTitle() {

        return "ZEUS BOLT";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            Block target = center;
            if (target == null) {
                target = LocationUtil.getNextFreeSpace(SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()), BlockFace.UP);
            }
            target.getWorld().strikeLightning(target.getLocation());
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LightningSummon(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Strike location with lightning!";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "x:y:z block offset",
                    null
            };
            return lines;
        }
    }
}