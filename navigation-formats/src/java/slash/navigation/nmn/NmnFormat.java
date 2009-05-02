/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.nmn;

import slash.navigation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The base of all Navigon Mobile Navigator formats.
 *
 * @author Christian Pesch
 */

public abstract class NmnFormat extends SimpleLineBasedFormat<NmnRoute> {
    protected static final char SEPARATOR_CHAR = '|';
    protected static final String SEPARATOR = "\\" + SEPARATOR_CHAR;
    protected static final String WILDCARD = "[.[^" + SEPARATOR_CHAR + "]]*";
    protected static final char LEFT_BRACE = '[';
    protected static final char RIGHT_BRACE = ']';

    static final Pattern COMMENT_PATTERN = Pattern.compile("(\\d+ )?(.[^,;]+),(.[^ ,;]+)( .[^,;]+)?");

    private static final double DUPLICATE_OFFSET = 0.0001;
    
    public String getExtension() {
        return ".rte";
    }

    public <P extends BaseNavigationPosition> NmnRoute createRoute(RouteCharacteristics characteristics, String name, List<P> positions) {
        return new NmnRoute(this, characteristics, null, (List<NmnPosition>) positions);
    }

    public BaseNavigationPosition getDuplicateFirstPosition(BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route) {
        List<BaseNavigationPosition> positions = route.getPositions();
        BaseNavigationPosition first = positions.get(0);
        return new NmnPosition(first.getLongitude() + DUPLICATE_OFFSET,
                first.getLatitude() + DUPLICATE_OFFSET, (Double)null, null, null, "Start:" + first.getComment());
    }

    static String escapeSeparator(String string) {
        return string != null ? string.replaceAll(SEPARATOR, ";") : null;
    }
}
