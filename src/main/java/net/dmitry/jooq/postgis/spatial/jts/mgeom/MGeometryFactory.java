
package net.dmitry.jooq.postgis.spatial.jts.mgeom;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Extension of the GeometryFactory for constructing Geometries with Measure
 * support.
 *
 * @see com.vividsolutions.jts.geom.GeometryFactory
 */
public class MGeometryFactory extends GeometryFactory {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MGeometryFactory(PrecisionModel precisionModel, int SRID,
							MCoordinateSequenceFactory coordinateSequenceFactory) {
		super(precisionModel, SRID, coordinateSequenceFactory);
	}

	public MGeometryFactory(MCoordinateSequenceFactory coordinateSequenceFactory) {
		super(coordinateSequenceFactory);
	}

	public MGeometryFactory(PrecisionModel precisionModel) {
		this(precisionModel, 0, MCoordinateSequenceFactory.instance());
	}

	public MGeometryFactory(PrecisionModel precisionModel, int SRID) {
		this(precisionModel, SRID, MCoordinateSequenceFactory.instance());
	}

	public MGeometryFactory() {
		this(new PrecisionModel(), 0);
	}

	/**
	 * Constructs a MLineString using the given Coordinates; a null or empty
	 * array will create an empty MLineString.
	 *
	 * @param coordinates array of MCoordinate defining this geometry's vertices
	 * @return An instance of MLineString containing the coordinates
	 * @see #createLineString(com.vividsolutions.jts.geom.Coordinate[])
	 */
	public MLineString createMLineString(MCoordinate[] coordinates) {
		return createMLineString(
				coordinates != null ? getCoordinateSequenceFactory()
						.create(coordinates)
						: null
		);
	}

	public MultiMLineString createMultiMLineString(MLineString[] mlines,
												   double mGap) {
		return new MultiMLineString(mlines, mGap, this);
	}

	public MultiMLineString createMultiMLineString(MLineString[] mlines) {
		return new MultiMLineString(mlines, 0.0d, this);
	}

	/**
	 * Creates a MLineString using the given CoordinateSequence; a null or empty
	 * CoordinateSequence will create an empty MLineString.
	 *
	 * @param coordinates a CoordinateSequence possibly empty, or null
	 * @return An MLineString instance based on the <code>coordinates</code>
	 * @see #createLineString(com.vividsolutions.jts.geom.CoordinateSequence)
	 */
	public MLineString createMLineString(CoordinateSequence coordinates) {
		return new MLineString(coordinates, this);
	}

}
