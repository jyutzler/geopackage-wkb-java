package mil.nga.wkb.io;

import java.io.IOException;
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
 * Well Known Binary writer
 * 
 * @author osbornb
 */
public class WkbGeometryWriter {

	/**
	 * Write a geometry to the byte writer
	 * 
	 * @param writer
	 * @param geometry
	 * @throws IOException
	 */
	public static void writeGeometry(ByteWriter writer, Geometry geometry)
			throws IOException {

		// Write the single byte order byte
		byte byteOrder = writer.getByteOrder() == ByteOrder.BIG_ENDIAN ? (byte) 0
				: (byte) 1;
		writer.writeByte(byteOrder);

		// Write the geometry type integer
		writer.writeInt(geometry.getWkbCode());

		GeometryType geometryType = geometry.getGeometryType();

		switch (geometryType) {

		case GEOMETRY:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POINT:
			writePoint(writer, (Point) geometry);
			break;
		case LINESTRING:
			writeLineString(writer, (LineString) geometry);
			break;
		case POLYGON:
			writePolygon(writer, (Polygon) geometry);
			break;
		case MULTIPOINT:
			writeMultiPoint(writer, (MultiPoint) geometry);
			break;
		case MULTILINESTRING:
			writeMultiLineString(writer, (MultiLineString) geometry);
			break;
		case MULTIPOLYGON:
			writeMultiPolygon(writer, (MultiPolygon) geometry);
			break;
		case GEOMETRYCOLLECTION:
		case MULTICURVE:
		case MULTISURFACE:
			writeGeometryCollection(writer, (GeometryCollection<?>) geometry);
			break;
		case CIRCULARSTRING:
			writeCircularString(writer, (CircularString) geometry);
			break;
		case COMPOUNDCURVE:
			writeCompoundCurve(writer, (CompoundCurve) geometry);
			break;
		case CURVEPOLYGON:
			writeCurvePolygon(writer, (CurvePolygon<?>) geometry);
			break;
		case CURVE:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case SURFACE:
			throw new WkbException("Unexpected Geometry Type of "
					+ geometryType.name() + " which is abstract");
		case POLYHEDRALSURFACE:
			writePolyhedralSurface(writer, (PolyhedralSurface) geometry);
			break;
		case TIN:
			writeTIN(writer, (TIN) geometry);
			break;
		case TRIANGLE:
			writeTriangle(writer, (Triangle) geometry);
			break;
		default:
			throw new WkbException("Geometry Type not supported: "
					+ geometryType);
		}

	}

	/**
	 * Write a Point
	 * 
	 * @param writer
	 * @param point
	 * @throws IOException
	 */
	public static void writePoint(ByteWriter writer, Point point)
			throws IOException {

		writer.writeDouble(point.getX());
		writer.writeDouble(point.getY());

		if (point.hasZ()) {
			writer.writeDouble(point.getZ());
		}

		if (point.hasM()) {
			writer.writeDouble(point.getM());
		}
	}

	/**
	 * Write a Line String
	 * 
	 * @param writer
	 * @param lineString
	 * @throws IOException
	 */
	public static void writeLineString(ByteWriter writer, LineString lineString)
			throws IOException {

		writer.writeInt(lineString.numPoints());

		for (Point point : lineString.getPoints()) {
			writePoint(writer, point);
		}
	}

	/**
	 * Write a Polygon
	 * 
	 * @param writer
	 * @param polygon
	 * @throws IOException
	 */
	public static void writePolygon(ByteWriter writer, Polygon polygon)
			throws IOException {

		writer.writeInt(polygon.numRings());

		for (LineString ring : polygon.getRings()) {
			writeLineString(writer, ring);
		}
	}

	/**
	 * Write a Multi Point
	 * 
	 * @param writer
	 * @param multiPoint
	 * @throws IOException
	 */
	public static void writeMultiPoint(ByteWriter writer, MultiPoint multiPoint)
			throws IOException {

		writer.writeInt(multiPoint.numPoints());

		for (Point point : multiPoint.getPoints()) {
			writeGeometry(writer, point);
		}
	}

	/**
	 * Write a Multi Line String
	 * 
	 * @param writer
	 * @param multiLineString
	 * @throws IOException
	 */
	public static void writeMultiLineString(ByteWriter writer,
			MultiLineString multiLineString) throws IOException {

		writer.writeInt(multiLineString.numLineStrings());

		for (LineString lineString : multiLineString.getLineStrings()) {
			writeGeometry(writer, lineString);
		}
	}

	/**
	 * Write a Multi Polygon
	 * 
	 * @param writer
	 * @param multiPolygon
	 * @throws IOException
	 */
	public static void writeMultiPolygon(ByteWriter writer,
			MultiPolygon multiPolygon) throws IOException {

		writer.writeInt(multiPolygon.numPolygons());

		for (Polygon polygon : multiPolygon.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a Geometry Collection
	 * 
	 * @param writer
	 * @param geometryCollection
	 * @throws IOException
	 */
	public static void writeGeometryCollection(ByteWriter writer,
			GeometryCollection<?> geometryCollection) throws IOException {

		writer.writeInt(geometryCollection.numGeometries());

		for (Geometry geometry : geometryCollection.getGeometries()) {
			writeGeometry(writer, geometry);
		}
	}

	/**
	 * Write a Circular String
	 * 
	 * @param writer
	 * @param circularString
	 * @throws IOException
	 */
	public static void writeCircularString(ByteWriter writer,
			CircularString circularString) throws IOException {

		writer.writeInt(circularString.numPoints());

		for (Point point : circularString.getPoints()) {
			writePoint(writer, point);
		}
	}

	/**
	 * Write a Compound Curve
	 * 
	 * @param writer
	 * @param compoundCurve
	 * @throws IOException
	 */
	public static void writeCompoundCurve(ByteWriter writer,
			CompoundCurve compoundCurve) throws IOException {

		writer.writeInt(compoundCurve.numLineStrings());

		for (LineString lineString : compoundCurve.getLineStrings()) {
			writeGeometry(writer, lineString);
		}
	}

	/**
	 * Write a Curve Polygon
	 * 
	 * @param writer
	 * @param curvePolygon
	 * @throws IOException
	 */
	public static void writeCurvePolygon(ByteWriter writer,
			CurvePolygon<?> curvePolygon) throws IOException {

		writer.writeInt(curvePolygon.numRings());

		for (Curve ring : curvePolygon.getRings()) {
			writeGeometry(writer, ring);
		}
	}

	/**
	 * Write a Polyhedral Surface
	 * 
	 * @param writer
	 * @param polyhedralSurface
	 * @throws IOException
	 */
	public static void writePolyhedralSurface(ByteWriter writer,
			PolyhedralSurface polyhedralSurface) throws IOException {

		writer.writeInt(polyhedralSurface.numPolygons());

		for (Polygon polygon : polyhedralSurface.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a TIN
	 * 
	 * @param writer
	 * @param tin
	 * @throws IOException
	 */
	public static void writeTIN(ByteWriter writer, TIN tin) throws IOException {

		writer.writeInt(tin.numPolygons());

		for (Polygon polygon : tin.getPolygons()) {
			writeGeometry(writer, polygon);
		}
	}

	/**
	 * Write a Triangle
	 * 
	 * @param writer
	 * @param triangle
	 * @throws IOException
	 */
	public static void writeTriangle(ByteWriter writer, Triangle triangle)
			throws IOException {

		writer.writeInt(triangle.numRings());

		for (LineString ring : triangle.getRings()) {
			writeLineString(writer, ring);
		}
	}

}
