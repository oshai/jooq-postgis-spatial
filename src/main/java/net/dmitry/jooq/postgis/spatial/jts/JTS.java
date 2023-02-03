
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
