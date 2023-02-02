/*
 * This file is part of Hibernate Spatial, an extension to the
 *  hibernate ORM solution for spatial (geographic) data.
 *
 *  Copyright © 2007-2012 Geovise BVBA
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.dmitry.jooq.postgis.spatial.jts;

import com.vividsolutions.jts.geom.PrecisionModel;
import net.dmitry.jooq.postgis.spatial.jts.mgeom.MGeometryFactory;

/**
 * A static utility class
 *
 * @author Karel Maesen
 */
public class JTS {

    private static MGeometryFactory defaultGeomFactory = new MGeometryFactory(new PrecisionModel(), 4326);


    /**
     * Make sure nobody can instantiate this class
     */
    private JTS() {
    }

    public static MGeometryFactory getDefaultGeomFactory() {
        return defaultGeomFactory;
    }

    public static void setDefaultGeomFactory(MGeometryFactory defaultGeomFactory) {
        JTS.defaultGeomFactory = defaultGeomFactory;
    }

}
