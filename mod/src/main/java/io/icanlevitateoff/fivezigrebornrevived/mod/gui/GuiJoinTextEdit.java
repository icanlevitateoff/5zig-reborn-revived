/*
 * Copyright (c) 2019-2020 5zig Reborn
 * Copyright (c) 2015-2019 5zig
 *
 * This file is part of The 5zig Mod
 * The 5zig Mod is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The 5zig Mod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The 5zig Mod.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.icanlevitateoff.fivezigrebornrevived.mod.gui;

import eu.the5zig.mod.gui.*;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.config.JoinText;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;

public class GuiJoinTextEdit extends Gui {

	private final JoinText joinText;
	private boolean waitForSwitch;

	public GuiJoinTextEdit(Gui lastScreen, JoinText joinText) {
		super(lastScreen);
		this.joinText = joinText;
	}

	@Override
	public void initGui() {
		addButton(The5zigMod.getVars().createButton(200, getWidth() / 2 + 5, getHeight() / 6 + 168, 150, 20, The5zigMod.getVars().translate("gui.done")));
		addButton(The5zigMod.getVars().createButton(100, getWidth() / 2 - 155, getHeight() / 6 + 168, 150, 20, The5zigMod.getVars().translate("gui.cancel")));

		waitForSwitch = joinText.isWaitForSwitch();
		addButton(The5zigMod.getVars().createButton(5, getWidth() / 2 - 150, getHeight() / 6 + 100, 300, 20, waitLabel()));

		addTextField(The5zigMod.getVars().createTextfield(1, getWidth() / 2 - 150, getHeight() / 6 + 20, 300, 20, 256));
		addTextField(The5zigMod.getVars().createTextfield(2, getWidth() / 2 - 150, getHeight() / 6 + 65, 240, 20, 256));
		addTextField(The5zigMod.getVars().createTextfield(3, getWidth() / 2 + 100, getHeight() / 6 + 65, 50, 20, 6));

		if (joinText.getMessage() != null) {
			getTextfieldById(1).callSetText(joinText.getMessage());
		}
		if (joinText.getServer() != null) {
			getTextfieldById(2).callSetText(joinText.getServer());
		}
		getTextfieldById(3).callSetText(String.valueOf(joinText.getDelayMs()));
	}

	@Override
	protected void actionPerformed(IButton button) {
		if (button.getId() == 200) {
			joinText.setMessage(getTextfieldById(1).callGetText());
			if (getTextfieldById(2).callGetText().isEmpty()) {
				joinText.setServer(null);
			} else {
				joinText.setServer(getTextfieldById(2).callGetText());
			}
			try {
				joinText.setDelayMs(Integer.parseInt(getTextfieldById(3).callGetText().trim()));
			} catch (NumberFormatException e) {
				The5zigMod.logger.error(e);
			}
			joinText.setWaitForSwitch(waitForSwitch);
			The5zigMod.getJoinTextConfiguration().saveConfig();
		}
		if (button.getId() == 5) {
			waitForSwitch = !waitForSwitch;
			button.setLabel(waitLabel());
		}
		if (button.getId() == 100) {
			if (joinText.getMessage() == null || joinText.getMessage().isEmpty()) {
				The5zigMod.getJoinTextConfiguration().getConfigInstance().getTexts().remove(joinText);
				The5zigMod.getJoinTextConfiguration().saveConfig();
			}
			The5zigMod.getVars().displayScreen(lastScreen);
		}
	}

	private String waitLabel() {
		return I18n.translate("join_texts.edit.wait_for_switch") + ": " + The5zigMod.getVars().translate(waitForSwitch ? "options.on" : "options.off");
	}

	@Override
	protected void tick() {
		getButtonById(200).setEnabled(!getTextfieldById(1).callGetText().isEmpty());
	}

	@Override
	protected void drawScreen(int mouseX, int mouseY, float partialTicks) {
		The5zigMod.getVars().drawString(I18n.translate("join_texts.edit.message"), getWidth() / 2 - 150, getHeight() / 6 + 8);
		The5zigMod.getVars().drawString(I18n.translate("join_texts.edit.server"), getWidth() / 2 - 150, getHeight() / 6 + 54);
		The5zigMod.getVars().drawString(I18n.translate("join_texts.edit.delay"), getWidth() / 2 + 100, getHeight() / 6 + 54);
		int y = getHeight() / 6 + 126;
		for (String line : The5zigMod.getVars().splitStringToWidth(I18n.translate("join_texts.edit.wait_for_switch.desc"), 300)) {
			The5zigMod.getVars().drawString(line, getWidth() / 2 - 150, y, 0xAAAAAA);
			y += 10;
		}
	}

	@Override
	public String getTitleKey() {
		return "join_texts.edit.title";
	}
}
