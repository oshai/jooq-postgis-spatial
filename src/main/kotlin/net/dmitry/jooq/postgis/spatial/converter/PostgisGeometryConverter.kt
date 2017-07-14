package net.dmitry.jooq.postgis.spatial.converter

import org.jooq.Converter
import org.postgis.Geometry
import org.postgis.PGgeometry
import org.postgresql.util.PGobject

/**
 * @author Dmitry Zhuravlev
 *         Date: 07.03.16
 */
class PostgisGeometryConverter : Converter<Any, Geometry> {

    override fun from(obj: Any?): Geometry? =
            if (obj == null) {
                null
            } else PGgeometry.geomFromString(obj.toString())


    override fun to(geom: Geometry?): Any? =
            if (geom == null) {
                null
            } else
                PGobject().apply {
                    type = geom.typeString
                    value = geom.value
                }

    override fun toType(): Class<Geometry>? = Geometry::class.java

    override fun fromType(): Class<Any>? = Any::class.java
}