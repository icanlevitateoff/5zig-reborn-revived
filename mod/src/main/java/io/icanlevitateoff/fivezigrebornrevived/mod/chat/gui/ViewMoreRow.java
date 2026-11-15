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

package io.icanlevitateoff.fivezigrebornrevived.mod.chat.gui;

import io.icanlevitateoff.fivezigrebornrevived.mod.I18n;
import io.icanlevitateoff.fivezigrebornrevived.mod.The5zigMod;
import io.icanlevitateoff.fivezigrebornrevived.mod.chat.entity.Message;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.IButton;
import io.icanlevitateoff.fivezigrebornrevived.mod.gui.elements.RowExtended;
import eu.the5zig.util.Callback;

/**
 * Created by 5zig.
 * All rights reserved © 2015
 * <p/>
 * ViewMoreRow is a GuiList-Row that extends ChatLine and is used to add a button where you can click to view more messages.
 */
public class ViewMoreRow extends ChatLine implements RowExtended {

	private IButton button;
	private Callback<IButton> actionPerformed;

	/**
	 * @param center          The x-Value of the Center of the ChatBox.
	 * @param actionPerformed The Action Performed Callback.
	 */
	public ViewMoreRow(int center, Callback<IButton> actionPerformed) {
		super(new Message(null, -1, "", "", -1, Message.MessageType.CENTERED));
		String str = I18n.translate("chat.view_more");
		int strWidth = The5zigMod.getVars().getStringWidth(str);
		this.button = The5zigMod.getVars().createStringButton(51, center - strWidth / 2, 0, strWidth, 10, str);
		this.actionPerformed = actionPerformed;
	}

	@Override
	public void draw(int x, int y) {
	}

	@Override
	public void draw(int x, int y, int slotHeight, int mouseX, int mouseY) {
		button.setY(y + 2);
		button.draw(mouseX, mouseY);
	}

	@Override
	public IButton mousePressed(int mouseX, int mouseY) {
		if (button.mouseClicked(mouseX, mouseY)) {
			actionPerformed.call(button);
			return button;
		}
		return null;
	}

}
