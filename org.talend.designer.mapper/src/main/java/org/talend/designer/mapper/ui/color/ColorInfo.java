// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.designer.mapper.ui.color;

import org.eclipse.swt.graphics.RGB;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public enum ColorInfo {

    COLOR_ENTRY_HIGHLIGHTED(176, 231, 0), // green
    COLOR_ENTRY_ERROR(100, 200, 255), // ?
    COLOR_ENTRY_WARNING(0, 200, 60), // ?
    COLOR_ENTRY_NORMAL(170, 170, 170), // ?
    COLOR_RED(255, 0, 0), // red
    COLOR_ENTRY_NONE(255, 255, 255), // white

    COLOR_BACKGROUND_LINKS(210, 210, 196), // gray
    COLOR_UNSELECTED_METADATA_LINK(196, 196, 180), // light gray
    COLOR_LINK_HIGHLIGHTED(255, 255, 0), // white
    COLOR_LINK_CONSTRAINT(255, 150, 20), // yellow
    COLOR_HIGHLIGHTED_TEXT_ROW(240, 240, 240), // light gray

    COLOR_BACKGROUND_ERROR_EXPRESSION_CELL(255, 0, 0), // red
    COLOR_FOREGROUND_ERROR_EXPRESSION_CELL(255, 255, 255), // red
    COLOR_BACKGROUND_VALID_EXPRESSION_CELL(255, 255, 255), // white

    COLOR_DRAGGING_INSERTION_INDICATOR(0, 78, 152), // blue
    ;

    private int red;

    private int green;

    private int blue;

    private ColorInfo(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getGreen() {
        return this.green;
    }

    public int getRed() {
        return this.red;
    }

    public RGB getRGB() {
        return new RGB(red, green, blue);
    }

}
