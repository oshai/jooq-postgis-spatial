package org.jooq.postgis.spatial.converter

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import org.jooq.Converter
import org.jooq.postgis.spatial.jts.JTS
import org.jooq.postgis.spatial.jts.mgeom.MCoordinate
import org.jooq.postgis.spatial.jts.mgeom.MGeometry
import org.jooq.postgis.spatial.jts.mgeom.MGeometryFactory
import org.jooq.postgis.spatial.jts.mgeom.MLineString
import org.postgis.*

/**
 * @author Dmitry Zhuravlev
 * *         Date: 07.03.16
 */
class JTSGeometryConverter : Converter<Any, Geometry> {

    private val postgisGeometryConverter = PostgisGeometryConverter()

    override fun from(obj: Any?): Geometry? = toJTS(postgisGeometryConverter.from(obj))


    override fun to(geom: Geometry?): Any? = if (geom != null) toNative(geom) else null

    override fun toType(): Class<Geometry>? = Geometry::class.java

    override fun fromType(): Class<Any>? = Any::class.java

    protected fun getGeometryFactory(): MGeometryFactory {
        return JTS.getDefaultGeomFactory()
    }


    fun toJTS(obj: Any?): Geometry? {
        var obj = obj ?: return null
        // in some cases, Postgis returns not PGgeometry objects
        // but org.postgis.Geometry instances.
        // This has been observed when retrieving GeometryCollections
        // as the result of an SQL-operation such as Union.
        if (obj is org.postgis.Geometry) {
            obj = PGgeometry(obj)
        }

        if (obj is PGgeometry) {
            var out: Geometry?
            when (obj.geoType) {
                org.postgis.Geometry.POINT -> out = convertPoint(obj.geometry as Point)
                org.postgis.Geometry.LINESTRING -> out = convertLineString(
                        obj.geometry as LineString)
                org.postgis.Geometry.POLYGON -> out = convertPolygon(obj.geometry as Polygon)
                org.postgis.Geometry.MULTILINESTRING -> out = convertMultiLineString(
                        obj.geometry as MultiLineString)
                org.postgis.Geometry.MULTIPOINT -> out = convertMultiPoint(
                        obj.geometry as MultiPoint)
                org.postgis.Geometry.MULTIPOLYGON -> out = convertMultiPolygon(
                        obj.geometry as MultiPolygon)
                org.postgis.Geometry.GEOMETRYCOLLECTION -> out = convertGeometryCollection(
                        obj.geometry as GeometryCollection)
                else -> throw RuntimeException("Unknown type of PGgeometry")
            }
            out.srid = obj.geometry.srid
            return out
        } else if (obj is PGboxbase) {
            return convertBox(obj)
        } else {
            throw IllegalArgumentException(
                    "Can't convert object of type " + obj.javaClass.canonicalName)

        }

    }

    private fun convertBox(box: PGboxbase): Geometry {
        val ll = box.llb
        val ur = box.urt
        val ringCoords = arrayOfNulls<Coordinate>(5)
        if (box is PGbox2d) {
            ringCoords[0] = Coordinate(ll.x, ll.y)
            ringCoords[1] = Coordinate(ur.x, ll.y)
            ringCoords[2] = Coordinate(ur.x, ur.y)
            ringCoords[3] = Coordinate(ll.x, ur.y)
            ringCoords[4] = Coordinate(ll.x, ll.y)
        } else {
            ringCoords[0] = Coordinate(ll.x, ll.y, ll.z)
            ringCoords[1] = Coordinate(ur.x, ll.y, ll.z)
            ringCoords[2] = Coordinate(ur.x, ur.y, ur.z)
            ringCoords[3] = Coordinate(ll.x, ur.y, ur.z)
            ringCoords[4] = Coordinate(ll.x, ll.y, ll.z)
        }
        val shell = getGeometryFactory().createLinearRing(ringCoords)
        return getGeometryFactory().createPolygon(shell, null)
    }

    private fun convertGeometryCollection(collection: GeometryCollection): Geometry {
        val geometries = collection.geometries
        val jtsGeometries = arrayOfNulls<Geometry>(geometries.size)
        for (i in geometries.indices) {
            jtsGeometries[i] = toJTS(geometries[i])
            //TODO  - refactor this so the following line is not necessary
            jtsGeometries[i]?.srid = 0 // convert2JTS sets SRIDs, but constituent geometries in a collection must have srid  == 0
        }
        val jtsGCollection = getGeometryFactory().createGeometryCollection(jtsGeometries)
        return jtsGCollection
    }

    private fun convertMultiPolygon(pgMultiPolygon: MultiPolygon): Geometry {
        val polygons = arrayOfNulls<com.vividsolutions.jts.geom.Polygon>(pgMultiPolygon.numPolygons())

        for (i in polygons.indices) {
            val pgPolygon = pgMultiPolygon.getPolygon(i)
            polygons[i] = convertPolygon(pgPolygon) as com.vividsolutions.jts.geom.Polygon
        }

        val out = getGeometryFactory().createMultiPolygon(polygons)
        return out
    }

    private fun convertMultiPoint(pgMultiPoint: MultiPoint): Geometry {
        val points = arrayOfNulls<com.vividsolutions.jts.geom.Point>(pgMultiPoint.numPoints())

        for (i in points.indices) {
            points[i] = convertPoint(pgMultiPoint.getPoint(i))
        }
        val out = getGeometryFactory().createMultiPoint(points)
        out.srid = pgMultiPoint.srid
        return out
    }

    private fun convertMultiLineString(
            mlstr: MultiLineString): Geometry {
        val out: com.vividsolutions.jts.geom.MultiLineString
        if (mlstr.haveMeasure) {
            val lstrs = arrayOfNulls<MLineString>(mlstr.numLines())
            for (i in 0..mlstr.numLines() - 1) {
                val coordinates = toJTSCoordinates(
                        mlstr.getLine(i).points)
                lstrs[i] = getGeometryFactory().createMLineString(coordinates)
            }
            out = getGeometryFactory().createMultiMLineString(lstrs)
        } else {
            val lstrs = arrayOfNulls<com.vividsolutions.jts.geom.LineString>(mlstr.numLines())
            for (i in 0..mlstr.numLines() - 1) {
                lstrs[i] = getGeometryFactory().createLineString(
                        toJTSCoordinates(mlstr.getLine(i).points))
            }
            out = getGeometryFactory().createMultiLineString(lstrs)
        }
        return out
    }

    private fun convertPolygon(
            polygon: Polygon): Geometry {
        val shell = getGeometryFactory().createLinearRing(
                toJTSCoordinates(polygon.getRing(0).points))
        var out: com.vividsolutions.jts.geom.Polygon?
        if (polygon.numRings() > 1) {
            val rings = arrayOfNulls<com.vividsolutions.jts.geom.LinearRing>(polygon.numRings() - 1)
            for (r in 1..polygon.numRings() - 1) {
                rings[r - 1] = getGeometryFactory().createLinearRing(
                        toJTSCoordinates(polygon.getRing(r).points))
            }
            out = getGeometryFactory().createPolygon(shell, rings)
        } else {
            out = getGeometryFactory().createPolygon(shell, null)
        }
        return out
    }

    private fun convertPoint(pnt: Point): com.vividsolutions.jts.geom.Point {
        val g = getGeometryFactory().createPoint(
                this.toJTSCoordinate(pnt))
        return g
    }

    private fun convertLineString(
            lstr: LineString): com.vividsolutions.jts.geom.LineString {
        val out = if (lstr.haveMeasure)
            getGeometryFactory().createMLineString(toJTSCoordinates(lstr.points))
        else
            getGeometryFactory().createLineString(
                    toJTSCoordinates(lstr.points))
        return out
    }

    private fun toJTSCoordinates(points: Array<Point>): Array<MCoordinate?> {
        val coordinates = arrayOfNulls<MCoordinate>(points.size)
        for (i in points.indices) {
            coordinates[i] = this.toJTSCoordinate(points[i])
        }
        return coordinates
    }

    private fun toJTSCoordinate(pt: Point): MCoordinate {
        val mc: MCoordinate
        if (pt.dimension == 2) {
            mc = if (pt.haveMeasure)
                MCoordinate.create2dWithMeasure(
                        pt.getX(), pt.getY(), pt.getM())
            else
                MCoordinate.create2d(
                        pt.getX(), pt.getY())
        } else {
            mc = if (pt.haveMeasure)
                MCoordinate.create3dWithMeasure(
                        pt.getX(), pt.getY(), pt.getZ(), pt.getM())
            else
                MCoordinate.create3d(
                        pt.getX(), pt.getY(), pt.getZ())
        }
        return mc
    }


    /**
     * Converts a JTS `Geometry` to a native geometry object.

     * @param jtsGeom    JTS Geometry to convert
     * *
     * @param connection the current database connection
     * *
     * @return native database geometry object corresponding to jtsGeom.
     */
    protected fun toNative(jtsGeom: Geometry): PGgeometry {
        var jtsGeom = jtsGeom
        var geom: org.postgis.Geometry? = null
        jtsGeom = forceEmptyToGeometryCollection(jtsGeom)
        if (jtsGeom is com.vividsolutions.jts.geom.Point) {
            geom = convertJTSPoint(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.LineString) {
            geom = convertJTSLineString(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.MultiLineString) {
            geom = convertJTSMultiLineString(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.Polygon) {
            geom = convertJTSPolygon(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.MultiPoint) {
            geom = convertJTSMultiPoint(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.MultiPolygon) {
            geom = convertJTSMultiPolygon(jtsGeom)
        } else if (jtsGeom is com.vividsolutions.jts.geom.GeometryCollection) {
            geom = convertJTSGeometryCollection(jtsGeom)
        }

        if (geom != null) {
            return PGgeometry(geom)
        } else {
            throw UnsupportedOperationException(
                    "Conversion of "
                            + jtsGeom.javaClass.simpleName
                            + " to PGgeometry not supported")
        }
    }


    //Postgis treats every empty geometry as an empty geometrycollection

    private fun forceEmptyToGeometryCollection(jtsGeom: Geometry): Geometry {
        var forced = jtsGeom
        if (forced.isEmpty) {
            var factory: GeometryFactory? = jtsGeom.factory
            if (factory == null) {
                factory = JTS.getDefaultGeomFactory()
            }
            forced = factory?.createGeometryCollection(null)!!
            forced.setSRID(jtsGeom.srid)
        }
        return forced
    }

    private fun convertJTSMultiPolygon(
            multiPolygon: com.vividsolutions.jts.geom.MultiPolygon): MultiPolygon {
        val pgPolygons = arrayOfNulls<Polygon>(multiPolygon.numGeometries)
        for (i in pgPolygons.indices) {
            pgPolygons[i] = convertJTSPolygon(
                    multiPolygon.getGeometryN(i) as com.vividsolutions.jts.geom.Polygon)
        }
        val mpg = MultiPolygon(pgPolygons)
        mpg.setSrid(multiPolygon.srid)
        return mpg
    }

    private fun convertJTSMultiPoint(
            multiPoint: com.vividsolutions.jts.geom.MultiPoint): MultiPoint {
        val pgPoints = arrayOfNulls<Point>(multiPoint.numGeometries)
        for (i in pgPoints.indices) {
            pgPoints[i] = convertJTSPoint(
                    multiPoint.getGeometryN(i) as com.vividsolutions.jts.geom.Point)
        }
        val mp = MultiPoint(pgPoints)
        mp.setSrid(multiPoint.srid)
        return mp
    }

    private fun convertJTSPolygon(
            jtsPolygon: com.vividsolutions.jts.geom.Polygon): Polygon {
        val numRings = jtsPolygon.numInteriorRing
        val rings = arrayOfNulls<LinearRing>(numRings + 1)
        rings[0] = convertJTSLineStringToLinearRing(
                jtsPolygon.exteriorRing)
        for (i in 0..numRings - 1) {
            rings[i + 1] = convertJTSLineStringToLinearRing(
                    jtsPolygon.getInteriorRingN(i))
        }
        val polygon = Polygon(rings)
        polygon.setSrid(jtsPolygon.srid)
        return polygon
    }

    private fun convertJTSLineStringToLinearRing(
            lineString: com.vividsolutions.jts.geom.LineString): LinearRing {
        val lr = LinearRing(
                toPoints(
                        lineString.coordinates))
        lr.setSrid(lineString.srid)
        return lr
    }

    private fun convertJTSLineString(
            string: com.vividsolutions.jts.geom.LineString): LineString {
        val ls = LineString(
                toPoints(
                        string.coordinates))
        if (string is MGeometry) {
            ls.haveMeasure = true
        }
        ls.setSrid(string.srid)
        return ls
    }

    private fun convertJTSMultiLineString(
            string: com.vividsolutions.jts.geom.MultiLineString): MultiLineString {
        val lines = arrayOfNulls<LineString>(string.numGeometries)
        for (i in 0..string.numGeometries - 1) {
            lines[i] = LineString(
                    toPoints(
                            string.getGeometryN(
                                    i).coordinates))
        }
        val mls = MultiLineString(lines)
        if (string is MGeometry) {
            mls.haveMeasure = true
        }
        mls.setSrid(string.srid)
        return mls
    }

    private fun convertJTSPoint(point: com.vividsolutions.jts.geom.Point): Point {
        val pgPoint = Point()
        pgPoint.srid = point.srid
        pgPoint.x = point.x
        pgPoint.y = point.y
        val coordinate = point.coordinate
        if (java.lang.Double.isNaN(coordinate.z)) {
            pgPoint.dimension = 2
        } else {
            pgPoint.z = coordinate.z
            pgPoint.dimension = 3
        }
        pgPoint.haveMeasure = false
        if (coordinate is MCoordinate && !java.lang.Double.isNaN(coordinate.m)) {
            pgPoint.m = coordinate.m
            pgPoint.haveMeasure = true
        }
        return pgPoint
    }

    private fun convertJTSGeometryCollection(
            collection: com.vividsolutions.jts.geom.GeometryCollection): GeometryCollection {
        var currentGeom: Geometry
        val pgCollections = arrayOfNulls<org.postgis.Geometry>(collection.numGeometries)
        for (i in pgCollections.indices) {
            currentGeom = collection.getGeometryN(i)
            currentGeom = forceEmptyToGeometryCollection(currentGeom)
            if (currentGeom.javaClass == com.vividsolutions.jts.geom.LineString::class.java) {
                pgCollections[i] = convertJTSLineString(currentGeom as com.vividsolutions.jts.geom.LineString)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.LinearRing::class.java) {
                pgCollections[i] = convertJTSLineStringToLinearRing(currentGeom as com.vividsolutions.jts.geom.LinearRing)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.MultiLineString::class.java) {
                pgCollections[i] = convertJTSMultiLineString(currentGeom as com.vividsolutions.jts.geom.MultiLineString)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.MultiPoint::class.java) {
                pgCollections[i] = convertJTSMultiPoint(currentGeom as com.vividsolutions.jts.geom.MultiPoint)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.MultiPolygon::class.java) {
                pgCollections[i] = convertJTSMultiPolygon(currentGeom as com.vividsolutions.jts.geom.MultiPolygon)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.Point::class.java) {
                pgCollections[i] = convertJTSPoint(currentGeom as com.vividsolutions.jts.geom.Point)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.Polygon::class.java) {
                pgCollections[i] = convertJTSPolygon(currentGeom as com.vividsolutions.jts.geom.Polygon)
            } else if (currentGeom.javaClass == com.vividsolutions.jts.geom.GeometryCollection::class.java) {
                pgCollections[i] = convertJTSGeometryCollection(currentGeom as com.vividsolutions.jts.geom.GeometryCollection)
            }
        }
        val gc = GeometryCollection(pgCollections)
        gc.setSrid(collection.srid)
        return gc
    }


    private fun toPoints(coordinates: Array<Coordinate>): Array<Point?> {
        val points = arrayOfNulls<Point>(coordinates.size)
        for (i in coordinates.indices) {
            val c = coordinates[i]
            val pt: Point
            if (java.lang.Double.isNaN(c.z)) {
                pt = Point(c.x, c.y)
            } else {
                pt = Point(c.x, c.y, c.z)
            }
            if (c is MCoordinate) {
                val mc = c
                if (!java.lang.Double.isNaN(mc.m)) {
                    pt.setM(mc.m)
                }
            }
            points[i] = pt
        }
        return points
    }


}
