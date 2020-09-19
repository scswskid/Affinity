package com.gamesense.client.command.commands;

import com.gamesense.client.AffinityPlus;
import com.gamesense.client.clickgui.ClickGUI;
import com.gamesense.client.command.Command;

public class ResetGuiCommand extends Command {
    @Override public String[] getAlias() {
        return new String[] {
            "resetgui",
            "guireset"
        };
    }
    @Override public String getSyntax() {
        return "resetgui";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        AffinityPlus.getInstance().clickGUI = new ClickGUI();
    }
}
