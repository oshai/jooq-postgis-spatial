
package net.dmitry.jooq.postgis.spatial.jts.mgeom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;

import java.io.Serializable;

/**
 * Creates MCoordinateSequenceFactory internally represented as an array of
 * {@link MCoordinate}s.
 */
public class MCoordinateSequenceFactory implements CoordinateSequenceFactory,
		Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static MCoordinateSequenceFactory instance = new MCoordinateSequenceFactory();

	private MCoordinateSequenceFactory() {
	}

	/**
	 * Returns the singleton instance of MCoordinateSequenceFactory
	 *
	 * @return instance
	 */
	public static MCoordinateSequenceFactory instance() {
		return instance;
	}

	/**
	 * Returns an MCoordinateSequence based on the given array -- the array is
	 * used directly if it is an instance of MCoordinate[]; otherwise it is
	 * copied.
	 */
	public CoordinateSequence create(Coordinate[] coordinates) {
		return coordinates instanceof MCoordinate[] ? new MCoordinateSequence(
				(MCoordinate[]) coordinates
		) : new MCoordinateSequence(
				coordinates
		);
	}

	public CoordinateSequence create(CoordinateSequence coordSeq) {
		return new MCoordinateSequence(coordSeq);
	}

	/**
	 * Creates a MCoordinateSequence instance initialized to the size parameter.
	 * Note that the dimension argument is ignored.
	 *
	 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(int, int)
	 */
	public CoordinateSequence create(int size, int dimension) {
		return new MCoordinateSequence(size);
	}

}
