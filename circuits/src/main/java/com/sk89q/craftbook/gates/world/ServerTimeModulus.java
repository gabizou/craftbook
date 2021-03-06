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

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

public class ServerTimeModulus extends AbstractIC {

    public ServerTimeModulus(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Server Time Modulus";
    }

    @Override
    public String getSignTitle() {

        return "SERVER TIME MOD";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, isServerTimeOdd());
        }
    }

    /**
     * Returns true if the relative time is odd.
     *
     * @return
     */
    private boolean isServerTimeOdd() {

        long time = BukkitUtil.toSign(getSign()).getBlock().getWorld().getTime() % 2;
        if (time < 0) {
            time += 2;
        }
        return time == 1;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ServerTimeModulus(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Outputs high if time is odd.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    null,
                    null
            };
            return lines;
        }
    }
}