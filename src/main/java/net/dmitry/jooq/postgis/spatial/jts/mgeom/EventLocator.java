
package net.dmitry.jooq.postgis.spatial.jts.mgeom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

public class EventLocator {

	/**
	 * Returns the point on the specified MGeometry where its measure equals the specified position.
	 *
	 * @param lrs geom
	 * @param position double
	 * @return a Point Geometry
	 * @throws MGeometryException on problem
	 */
	public static Point getPointGeometry(MGeometry lrs, double position)
			throws MGeometryException {
		if (lrs == null) {
			throw new MGeometryException("Non-null MGeometry parameter is required.");
		}
		Coordinate c = lrs.getCoordinateAtM(position);
		Point pnt = lrs.getFactory().createPoint(c);
		copySRID(lrs.asGeometry(), pnt);
		return pnt;
	}

	public static MultiMLineString getLinearGeometry(MGeometry lrs,
													 double begin, double end) throws MGeometryException {

		if (lrs == null) {
			throw new MGeometryException("Non-null MGeometry parameter is required.");
		}
		MGeometryFactory factory = (MGeometryFactory) lrs.getFactory();
		CoordinateSequence[] cs = lrs.getCoordinatesBetween(begin, end);
		List<MLineString> linestrings = new ArrayList<MLineString>(cs.length);
		for (int i = 0; i < cs.length; i++) {
			MLineString ml;
			if (cs[i].size() >= 2) {
				ml = factory.createMLineString(cs[i]);
				linestrings.add(ml);
			}
		}
		MultiMLineString result = factory.createMultiMLineString(linestrings.toArray(new MLineString[linestrings.size()]));
		copySRID(lrs.asGeometry(), result.asGeometry());
		return result;
	}

	public static void copySRID(Geometry source, Geometry target) {
		target.setSRID(source.getSRID());
	}

}
