
package net.dmitry.jooq.postgis.spatial.jts.mgeom;

/**
 * @author Karel Maesen
 */
public class MGeometryException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public final static int OPERATION_REQUIRES_MONOTONE = 1;

	public final static int UNIONM_ON_DISJOINT_MLINESTRINGS = 2;

	public final static int GENERAL_MGEOMETRY_EXCEPTION = 0;

	// type of exception
	private final int type;

	public MGeometryException(String s) {
		super(s);
		type = 0;
	}

	public MGeometryException(int type) {
		super();
		this.type = type;
	}

	public MGeometryException(int type, String msg) {
		super(msg);
		this.type = type;
	}

	public int getType() {
		return type;
	}

}
