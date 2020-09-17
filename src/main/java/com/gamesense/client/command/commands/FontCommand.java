package com.gamesense.client.command.commands;

import com.gamesense.api.util.font.CFontRenderer;
import com.gamesense.client.AffinityPlus;
import com.gamesense.client.command.Command;

import java.awt.*;

public class FontCommand extends Command {
    @Override
    public String[] getAlias() {
        return new String[]{
                "font", "setfont"
        };
    }

    @Override
    public String getSyntax() {
        return "font <Name> <Size>";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        String font = args[0].replace("_", " ");
        int size = Integer.parseInt(args[1]);
        AffinityPlus.fontRenderer = new CFontRenderer(new Font(font, Font.PLAIN, size), true, false);
        AffinityPlus.fontRenderer.setFontName(font);
        AffinityPlus.fontRenderer.setFontSize(size);
    }
}
