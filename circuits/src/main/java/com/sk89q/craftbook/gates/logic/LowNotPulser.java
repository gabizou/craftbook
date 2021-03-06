package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;

/**
 * @author Silthus
 */
public class LowNotPulser extends NotPulser {

    public LowNotPulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Low Not Pulser";
    }

    @Override
    public String getSignTitle() {

        return "LOW NOT PULSER";
    }

    @Override
    protected boolean getInput(ChipState chip) {

        return !chip.getInput(0);
    }

    @Override
    protected void setOutput(ChipState chip, boolean on) {

        chip.setOutput(0, !on);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LowNotPulser(getServer(), sign, this);
        }
    }
}
