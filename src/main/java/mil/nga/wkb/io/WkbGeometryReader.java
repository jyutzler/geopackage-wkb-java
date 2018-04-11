package mil.nga.wkb.io;

import java.nio.ByteOrder;

import mil.nga.wkb.geom.CircularString;
import mil.nga.wkb.geom.CompoundCurve;
import mil.nga.wkb.geom.Curve;
import mil.nga.wkb.geom.CurvePolygon;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryCollection;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.geom.PolyhedralSurface;
import mil.nga.wkb.geom.TIN;
import mil.nga.wkb.geom.Triangle;
import mil.nga.wkb.util.WkbException;

/**
 * Well Known Binary reader
 * 
 * @author osbornb
 */
public class WkbGeometryReader {

	/**
	 * Read a geometry from the byte reader
	 * 
	 * @param reader
	 * @return geometry
	 */
	public static Geometry readGeometry(ByteReader reader) {
		Geometry geometry = readGeometry(reader, null);
		return geometry;
	}

	/**
	 * Read a geometry from the byte reader
	 * 
	 * @param reader
	 * @param expectedType
	 * @return geometry
	 */
	public static <T extends Geometry> T readGeometry(ByteReader reader,
			Class<T> expectedType) {

		// Read the single byte order byte
		byte byteOrderValue = reader.readByte();
		ByteOrder byteOrder = byteOrderValue == 0 ? ByteOrder.BIG_ENDIAN
				: ByteOrder.LITTLE_ENDIAN;
		ByteOrder originalByteOrder = reader.getByteOrder();
		reader.setByteOrder(byteOrder);

		// Read the geometry type integer
		int geometryTypeWkbCode = reader.readInt();

		// Look at the last 2 digits to find the geometry type code (1 - 14)
		int geometryTypeCode = geometryTypeWkbCode % 1000;

		// Look at the first digit to find the options (z when 1 or 3, m when 2
		// or 3)
		int geometryTypeMode = geometryTypeWkbCode / 1000;

		// Determine if the geometry has a z (3d) or m (linear referencing
		// system) value
		boolean hasZ = false;
		boolean hasM = false;
		switch (geometryTypeMode) {
		case 0:
			break;

		case 1:
			hasZ = true;
			break;

		case 2:
			hasM = true;
			break;

		case 3:
			hasZ = true;
			hasM = true;
			break;
		}

		GeometryType geometryType = GeometryType.fromCode(geometryTypeCode);

		Geometry geometry = null;

		switch (geometryType) {

		case GEOMETRY:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POINT:
			geometry = readPoint(reader, hasZ, hasM);
			break;
		case LINESTRING:
			geometry = readLineString(reader, hasZ, hasM);
			break;
		case POLYGON:
			geometry = readPolygon(reader, hasZ, hasM);
			break;
		case MULTIPOINT:
			geometry = readMultiPoint(reader, hasZ, hasM);
			break;
		case MULTILINESTRING:
			geometry = readMultiLineString(reader, hasZ, hasM);
			break;
		case MULTIPOLYGON:
			geometry = readMultiPolygon(reader, hasZ, hasM);
			break;
		case GEOMETRYCOLLECTION:
			geometry = readGeometryCollection(reader, hasZ, hasM);
			break;
		case CIRCULARSTRING:
			geometry = readCircularString(reader, hasZ, hasM);
			break;
		case COMPOUNDCURVE:
			geometry = readCompoundCurve(reader, hasZ, hasM);
			break;
		case CURVEPOLYGON:
			geometry = readCurvePolygon(reader, hasZ, hasM);
			break;
		case MULTICURVE:
			geometry = readGeometryCollection(reader, hasZ, hasM);
			break;
		case MULTISURFACE:
			geometry = readGeometryCollection(reader, hasZ, hasM);
			break;
		case CURVE:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case SURFACE:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POLYHEDRALSURFACE:
			geometry = readPolyhedralSurface(reader, hasZ, hasM);
			break;
		case TIN:
			geometry = readTIN(reader, hasZ, hasM);
			break;
		case TRIANGLE:
			geometry = readTriangle(reader, hasZ, hasM);
			break;
		default:
			throw new WkbException("Geometry Type not supported: "
					+ geometryType);
		}

		// If there is an expected type, verify the geometry if of that type
		if (expectedType != null && geometry != null
				&& !expectedType.isAssignableFrom(geometry.getClass())) {
			throw new WkbException("Unexpected Geometry Type. Expected: "
					+ expectedType.getSimpleName() + ", Actual: "
					+ geometry.getClass().getSimpleName());
		}

		// Restore the byte order
		reader.setByteOrder(originalByteOrder);

		@SuppressWarnings("unchecked")
		T result = (T) geometry;

		return result;
	}

	/**
	 * Read a Point
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return point
	 */
	public static Point readPoint(ByteReader reader, boolean hasZ, boolean hasM) {

		double x = reader.readDouble();
		double y = reader.readDouble();

		Point point = new Point(hasZ, hasM, x, y);

		if (hasZ) {
			double z = reader.readDouble();
			point.setZ(z);
		}

		if (hasM) {
			double m = reader.readDouble();
			point.setM(m);
		}

		return point;
	}

	/**
	 * Read a Line String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return line string
	 */
	public static LineString readLineString(ByteReader reader, boolean hasZ,
			boolean hasM) {

		LineString lineString = new LineString(hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			Point point = readPoint(reader, hasZ, hasM);
			lineString.addPoint(point);

		}

		return lineString;
	}

	/**
	 * Read a Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return polygon
	 */
	public static Polygon readPolygon(ByteReader reader, boolean hasZ,
			boolean hasM) {

		Polygon polygon = new Polygon(hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			LineString ring = readLineString(reader, hasZ, hasM);
			polygon.addRing(ring);

		}

		return polygon;
	}

	/**
	 * Read a Multi Point
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return multi point
	 */
	public static MultiPoint readMultiPoint(ByteReader reader, boolean hasZ,
			boolean hasM) {

		MultiPoint multiPoint = new MultiPoint(hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			Point point = readGeometry(reader, Point.class);
			multiPoint.addPoint(point);

		}

		return multiPoint;
	}

	/**
	 * Read a Multi Line String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return multi line string
	 */
	public static MultiLineString readMultiLineString(ByteReader reader,
			boolean hasZ, boolean hasM) {

		MultiLineString multiLineString = new MultiLineString(hasZ, hasM);

		int numLineStrings = reader.readInt();

		for (int i = 0; i < numLineStrings; i++) {
			LineString lineString = readGeometry(reader, LineString.class);
			multiLineString.addLineString(lineString);

		}

		return multiLineString;
	}

	/**
	 * Read a Multi Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return multi polygon
	 */
	public static MultiPolygon readMultiPolygon(ByteReader reader,
			boolean hasZ, boolean hasM) {

		MultiPolygon multiPolygon = new MultiPolygon(hasZ, hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			Polygon polygon = readGeometry(reader, Polygon.class);
			multiPolygon.addPolygon(polygon);

		}

		return multiPolygon;
	}

	/**
	 * Read a Geometry Collection
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return geometry collection
	 */
	public static GeometryCollection<Geometry> readGeometryCollection(
			ByteReader reader, boolean hasZ, boolean hasM) {

		GeometryCollection<Geometry> geometryCollection = new GeometryCollection<Geometry>(
				hasZ, hasM);

		int numGeometries = reader.readInt();

		for (int i = 0; i < numGeometries; i++) {
			Geometry geometry = readGeometry(reader, Geometry.class);
			geometryCollection.addGeometry(geometry);

		}

		return geometryCollection;
	}

	/**
	 * Read a Circular String
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return circular string
	 */
	public static CircularString readCircularString(ByteReader reader,
			boolean hasZ, boolean hasM) {

		CircularString circularString = new CircularString(hasZ, hasM);

		int numPoints = reader.readInt();

		for (int i = 0; i < numPoints; i++) {
			Point point = readPoint(reader, hasZ, hasM);
			circularString.addPoint(point);

		}

		return circularString;
	}

	/**
	 * Read a Compound Curve
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return compound curve
	 */
	public static CompoundCurve readCompoundCurve(ByteReader reader,
			boolean hasZ, boolean hasM) {

		CompoundCurve compoundCurve = new CompoundCurve(hasZ, hasM);

		int numLineStrings = reader.readInt();

		for (int i = 0; i < numLineStrings; i++) {
			LineString lineString = readGeometry(reader, LineString.class);
			compoundCurve.addLineString(lineString);

		}

		return compoundCurve;
	}

	/**
	 * Read a Curve Polygon
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return curve polygon
	 */
	public static CurvePolygon<Curve> readCurvePolygon(ByteReader reader,
			boolean hasZ, boolean hasM) {

		CurvePolygon<Curve> curvePolygon = new CurvePolygon<Curve>(hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			Curve ring = readGeometry(reader, Curve.class);
			curvePolygon.addRing(ring);

		}

		return curvePolygon;
	}

	/**
	 * Read a Polyhedral Surface
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return polyhedral surface
	 */
	public static PolyhedralSurface readPolyhedralSurface(ByteReader reader,
			boolean hasZ, boolean hasM) {

		PolyhedralSurface polyhedralSurface = new PolyhedralSurface(hasZ, hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			Polygon polygon = readGeometry(reader, Polygon.class);
			polyhedralSurface.addPolygon(polygon);

		}

		return polyhedralSurface;
	}

	/**
	 * Read a TIN
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return TIN
	 */
	public static TIN readTIN(ByteReader reader, boolean hasZ, boolean hasM) {

		TIN tin = new TIN(hasZ, hasM);

		int numPolygons = reader.readInt();

		for (int i = 0; i < numPolygons; i++) {
			Polygon polygon = readGeometry(reader, Polygon.class);
			tin.addPolygon(polygon);

		}

		return tin;
	}

	/**
	 * Read a Triangle
	 * 
	 * @param reader
	 * @param hasZ
	 * @param hasM
	 * @return triangle
	 */
	public static Triangle readTriangle(ByteReader reader, boolean hasZ,
			boolean hasM) {

		Triangle triangle = new Triangle(hasZ, hasM);

		int numRings = reader.readInt();

		for (int i = 0; i < numRings; i++) {
			LineString ring = readLineString(reader, hasZ, hasM);
			triangle.addRing(ring);

		}

		return triangle;
	}

}
