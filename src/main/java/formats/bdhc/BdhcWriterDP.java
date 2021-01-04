
package formats.bdhc;

import utils.BinaryBufferWriter;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Trifindo
 */
public class BdhcWriterDP {

    public static void writeBdhc(Bdhc bdhc, String path) throws IOException{
        byte[] byteData = bdhcToByteArray(bdhc);
        Files.write(new File(path).toPath(), byteData);
    }

    public static byte[] bdhcToByteArray(Bdhc bdhc) throws IOException {

        BinaryBufferWriter writer = new BinaryBufferWriter();

        int[][] pointIndices = new int[bdhc.getPlates().size()][4];
        ArrayList<BdhcPoint> points = getPoints(bdhc, pointIndices);
        int[] slopeIndices = new int[bdhc.getPlates().size()];
        ArrayList<Slope> slopes = getSlopes(bdhc, slopeIndices);
        ArrayList<Integer> distances = getDistances(bdhc);
        ArrayList<IndexedTriangle> tris = getIndexedTris(bdhc, pointIndices, slopeIndices);
        ArrayList<ArrayList<Stripe>> stripes = new ArrayList<>();
        stripes.add(calculateStripeGroup(bdhc, new Rectangle(-16, -16, 16, 16)));
        stripes.add(calculateStripeGroup(bdhc, new Rectangle(0, -16, 16, 16)));
        stripes.add(calculateStripeGroup(bdhc, new Rectangle(-16, 0, 16, 16)));
        stripes.add(calculateStripeGroup(bdhc, new Rectangle(0, 0, 16, 16)));

        //FileOutputStream out = new FileOutputStream(path);

        writeHeader(writer, points, slopes, tris, stripes);

        writePoints(writer, points);

        writeSlopes(writer, slopes);

        writeTriangles(writer, tris, points, distances);

        writeStripeGroups(writer, stripes);

        writeStripes(writer, stripes);

        writeTriIndices(writer, stripes);

        //out.close();
        return writer.toByteArray();
    }

    private static void writeHeader(BinaryBufferWriter writer,
                                    ArrayList<BdhcPoint> points, ArrayList<Slope> slopes,
                                    ArrayList<IndexedTriangle> tris,
                                    ArrayList<ArrayList<Stripe>> stripes) throws IOException {

        writeString(writer, "BDHC");
        writeShortValue(writer, 32);
        writeShortValue(writer, 0);

        writeShortValue(writer, Bdhc.sizePointDP);
        writeShortValue(writer, points.size());
        writeShortValue(writer, Bdhc.sizeSlopeDP);
        writeShortValue(writer, slopes.size());
        writeShortValue(writer, Bdhc.sizePlateDP);
        writeShortValue(writer, tris.size());

        writeShortValue(writer, 28);
        writeShortValue(writer, 2);
        writeShortValue(writer, 4);
        writeShortValue(writer, 10);

        writeShortValue(writer, getMaxNumStripes(stripes));
        writeShortValue(writer, getMaxNumTris(stripes));
        writeShortValue(writer, getNumStripes(stripes) * Bdhc.sizeStripeDP);
        writeShortValue(writer, 0);
        writeShortValue(writer, getNumTriIndices(stripes) * 2);
        writeShortValue(writer, 0);

        writeShortValue(writer, 2);
        writeShortValue(writer, 2);
        writeShortValue(writer, -4096);
        writeShortValue(writer, -17);
        writeShortValue(writer, -4096);
        writeShortValue(writer, -17);
        writeShortValue(writer, 4096);
        writeShortValue(writer, 16);
        writeShortValue(writer, 4096);
        writeShortValue(writer, 16);
        writeShortValue(writer, 4096);
        writeShortValue(writer, 16);
        writeShortValue(writer, 4096);
        writeShortValue(writer, 16);
    }

    private static void writeTriIndices(BinaryBufferWriter writer,
                                        ArrayList<ArrayList<Stripe>> stripes) throws IOException {

        for (int i = 0; i < stripes.size(); i++) {
            for (int j = 0; j < stripes.get(i).size(); j++) {
                Stripe stripe = stripes.get(i).get(j);
                for (int k = 0; k < stripe.plateIndices.size(); k++) {
                    writeShortValue(writer, stripe.plateIndices.get(k) * 2);
                    writeShortValue(writer, stripe.plateIndices.get(k) * 2 + 1);
                }
            }
        }

    }

    private static void writeStripes(BinaryBufferWriter writer,
                                     ArrayList<ArrayList<Stripe>> stripes) throws IOException {
        int offset = 0;
        for (int i = 0; i < stripes.size(); i++) {
            for (int j = 0; j < stripes.get(i).size(); j++) {
                Stripe stripe = stripes.get(i).get(j);
                writeShortValue(writer, stripe.plateIndices.size() * 2);
                writeShortValue(writer, 0);
                writeShortValue(writer, stripe.y);
                writeShortValue(writer, offset);
                writeShortValue(writer, 0);
                offset += stripe.plateIndices.size() * 2; //*2;
            }
        }
    }

    private static void writeStripeGroups(BinaryBufferWriter writer,
                                          ArrayList<ArrayList<Stripe>> stripes) throws IOException {
        int offset = 0;
        for (int i = 0; i < stripes.size(); i++) {
            writeShortValue(writer, stripes.get(i).size());
            writeShortValue(writer, offset);
            offset += stripes.get(i).size();
        }
    }

    private static void writeTriangles(BinaryBufferWriter writer,
                                       ArrayList<IndexedTriangle> tris, ArrayList<BdhcPoint> points,
                                       ArrayList<Integer> distances) throws IOException {

        for (int i = 0; i < tris.size(); i++) {
            IndexedTriangle tri = tris.get(i);

            writeShortValue(writer, tri.pointInd1);
            writeShortValue(writer, tri.pointInd2);
            writeShortValue(writer, tri.pointInd3);
            writeShortValue(writer, tri.slopeInd);

            writeIntValue(writer, distances.get(i / 2));
            
            /*
            if (tri.type == Plate.PLANE || tri.type == Plate.BRIDGE) {
                if(tri.coordZ % 1 == 0){
                    writeZValue(out, -tri.coordZ);
                }else{
                    writeZValue(out, -tri.coordZ - 1);
                }
                
            } else {
                switch (tri.type) {
                    case Plate.LEFT_STAIRS:
                        writeSlopeZValue(out, 46341 * (-tri.getMaxX(points) - (int) tri.coordZ));
                        break;
                    case Plate.RIGHT_STAIRS:
                        writeSlopeZValue(out, 46341 * (tri.getMinX(points) - (int) tri.coordZ));
                        break;
                    case Plate.UP_STAIRS:
                        writeSlopeZValue(out, 46341 * (-tri.getMaxY(points) - (int) tri.coordZ));
                        break;
                    case Plate.DOWN_STAIRS:
                        writeSlopeZValue(out, 46341 * (tri.getMinY(points) - (int) tri.coordZ));
                        break;
                }
            }*/
        }

    }

    private static ArrayList<Integer> getDistances(Bdhc bdhc) {
        ArrayList<Integer> distances = new ArrayList<>();

        for (int i = 0; i < bdhc.getPlates().size(); i++) {
            Plate p = bdhc.getPlate(i);

            float z = p.z;
            float d;

            int[] slope = p.getSlope();
            final float xd = slope[0] / Plate.SLOPE_UNIT;
            final float zd = slope[1] / Plate.SLOPE_UNIT;
            final float yd = slope[2] / Plate.SLOPE_UNIT;

            final float mx = -xd / zd;
            final float my = -yd / zd;

            final float zero = 0.001f;
            if (Math.abs(xd) > zero) {
                float x;
                if (xd > zero) {
                    x = p.x + p.width;
                } else {
                    x = p.x;
                }
                float n = z - mx * x;
                d = (float) -(n / Math.sqrt(mx * mx + 1.0f));
            } else if (Math.abs(yd) > zero) {
                float y;
                if (yd > zero) {
                    y = p.y + p.height;
                } else {
                    y = p.y;
                }
                float n = z - my * y;
                d = (float) -(n / Math.sqrt(my * my + 1.0f));
            } else {
                d = -z;
            }

            int zValue = getIntegerValueZ(d);
            //System.out.println(Integer.toHexString(zValue));

            distances.add(zValue);
        }
        return distances;
    }

    private static int getIntegerValueZ(float value) {
        short decimalPart = (short) Math.floor(value);
        short fractionalPart = (short) ((value - decimalPart) * (65536));

        /*
        System.out.println(
                "decimal: " + Integer.toHexString(decimalPart & 0xffff)
                + " fractional: " + Integer.toHexString(fractionalPart & 0xffff));*/

        return ((decimalPart & 0xFFFF) << 16) | (fractionalPart & 0xFFFF);
    }


    private static void writeSlopes(BinaryBufferWriter writer, ArrayList<Slope> slopes)
            throws IOException {
        for (int i = 0; i < slopes.size(); i++) {
            Slope slope = slopes.get(i);
            writeIntValue(writer, slope.x);
            writeIntValue(writer, slope.y);
            writeIntValue(writer, slope.z);
        }

    }

    private static void writePoints(BinaryBufferWriter writer, ArrayList<BdhcPoint> points)
            throws IOException {
        for (int i = 0; i < points.size(); i++) {
            BdhcPoint point = points.get(i);
            writeShortValue(writer, 0);
            writeShortValue(writer, point.x);
            writeZValue(writer, point.z);
            writeShortValue(writer, 0);
            writeShortValue(writer, point.y);
        }

    }

    private static ArrayList<Stripe> calculateStripeGroup(Bdhc bdhc,
                                                          Rectangle groupBounds) {

        ArrayList<PlateInfo> platesInfo = new ArrayList<>();
        //Get plate info of plates inside group
        for (int i = 0; i < bdhc.getPlates().size(); i++) {
            Plate p = bdhc.getPlate(i);
            if (overlap(groupBounds, p.getBounds())) {
                platesInfo.add(new PlateInfo(i, p.y + p.height));
            }
        }

        //Sort by height
        Collections.sort(platesInfo);

        //Group by height and form stripes
        ArrayList<Stripe> stripes = new ArrayList<>();
        int previousY = -16;
        for (int i = 0; i < platesInfo.size(); i++) {
            int y = platesInfo.get(i).y;
            if (y != previousY) {
                int yMinBounds = previousY;
                int yMaxBounds = y;

                Stripe stripe = new Stripe(y);
                for (int j = 0; j < platesInfo.size(); j++) {
                    Plate p = bdhc.getPlate(platesInfo.get(j).plateIndex);
                    if (yMinBounds < p.y + p.height && yMaxBounds > p.y) {
                        stripe.plateIndices.add(platesInfo.get(j).plateIndex);
                    }
                }
                stripes.add(stripe);
                previousY = y;
            }

        }

        //Sort stripes elements on X axis
        for (int i = 0; i < stripes.size(); i++) {
            stripes.get(i).sortPlateIndices(bdhc.getPlates());
        }

        return stripes;
    }

    private static ArrayList<IndexedTriangle> getIndexedTris(Bdhc bdhc,
                                                             int[][] pointIndices, int[] slopeIndices) {
        ArrayList<IndexedTriangle> plates = new ArrayList<>();
        for (int i = 0; i < bdhc.getPlates().size(); i++) {
            Plate p = bdhc.getPlate(i);

            plates.add(new IndexedTriangle(
                    pointIndices[i][0],
                    pointIndices[i][1],
                    pointIndices[i][2],
                    slopeIndices[i],
                    p.z,
                    p.type)); //TODO: Change this
            plates.add(new IndexedTriangle(
                    pointIndices[i][0],
                    pointIndices[i][2],
                    pointIndices[i][3],
                    slopeIndices[i],
                    p.z,
                    p.type)); //TODO: Change this
        }
        return plates;
    }

    private static ArrayList<Slope> getSlopes(Bdhc bdhc, int[] indices) {
        ArrayList<Slope> slopes = new ArrayList<>();

        for (int i = 0; i < bdhc.getPlates().size(); i++) {
            Plate p = bdhc.getPlate(i);
            Slope slope = new Slope(p.getSlope());
            int index = slopes.indexOf(slope);
            if (index == -1) {
                index = slopes.size();
                slopes.add(slope);
            }
            indices[i] = index;
        }

        return slopes;
    }

    private static ArrayList<BdhcPoint> getPoints(Bdhc bdhc, int[][] coordIndices) {
        ArrayList<BdhcPoint> coords = new ArrayList<>();
        for (int i = 0; i < bdhc.getPlates().size(); i++) {
            Plate p = bdhc.getPlate(i);
            int[] zOffsets = new int[4];
            if (p.type == Plate.PLANE || p.type == Plate.BRIDGE) {
                zOffsets[0] = 0;
                zOffsets[1] = 0;
                zOffsets[2] = 0;
                zOffsets[3] = 0;
            } else { //TODO: Improve this using lookup table
                switch (p.type) {
                    case Plate.LEFT_STAIRS:
                        zOffsets[0] = p.width;
                        zOffsets[3] = p.width;
                        break;
                    case Plate.RIGHT_STAIRS:
                        zOffsets[1] = p.width;
                        zOffsets[2] = p.width;
                        break;
                    case Plate.UP_STAIRS:
                        zOffsets[2] = p.height;
                        zOffsets[3] = p.height;
                        break;
                    case Plate.DOWN_STAIRS:
                        zOffsets[0] = p.height;
                        zOffsets[1] = p.height;
                        break;
                }
            }
            addCoordinate(coords, new BdhcPoint(p.x, p.y + p.height, p.z + zOffsets[0]),
                    i, coordIndices, 0);
            addCoordinate(coords, new BdhcPoint(p.x + p.width, p.y + p.height, p.z + zOffsets[1]),
                    i, coordIndices, 1);
            addCoordinate(coords, new BdhcPoint(p.x + p.width, p.y, p.z + zOffsets[2]),
                    i, coordIndices, 2);
            addCoordinate(coords, new BdhcPoint(p.x, p.y, p.z + zOffsets[3]),
                    i, coordIndices, 3);

        }
        return coords;
    }

    private static void addCoordinate(ArrayList<BdhcPoint> coords,
                                      BdhcPoint coord, int pointIndex, int[][] indices, int indOffset) {
        /*
        coords.add(coord);
        indices[pointIndex][indOffset] = coords.size() - 1;
         */

        int index = coords.indexOf(coord);
        if (index == -1) {
            index = coords.size();
            coords.add(coord);
        }
        indices[pointIndex][indOffset] = index;
    }

    public static int getNumStripes(ArrayList<ArrayList<Stripe>> stripes) {
        int numStripes = 0;
        for (int i = 0; i < stripes.size(); i++) {
            numStripes += stripes.get(i).size();
        }
        return numStripes;
    }

    public static int getMaxNumStripes(ArrayList<ArrayList<Stripe>> stripes) {
        int maxNumStripesInGroup = 0;
        for (int i = 0; i < stripes.size(); i++) {
            if (stripes.get(i).size() > maxNumStripesInGroup) {
                maxNumStripesInGroup = stripes.get(i).size();
            }
        }
        return maxNumStripesInGroup;
    }

    public static int getMaxNumTris(ArrayList<ArrayList<Stripe>> stripes) {
        int maxNumPlates = 0;
        for (int i = 0; i < stripes.size(); i++) {
            for (int j = 0; j < stripes.get(i).size(); j++) {
                int numPlates = stripes.get(i).get(j).plateIndices.size();
                if (numPlates > maxNumPlates) {
                    maxNumPlates = numPlates;
                }
            }
        }
        return maxNumPlates * 2;
    }

    public static int getNumTriIndices(ArrayList<ArrayList<Stripe>> stripes) {
        int numIndices = 0;
        for (int i = 0; i < stripes.size(); i++) {
            for (int j = 0; j < stripes.get(i).size(); j++) {
                numIndices += stripes.get(i).get(j).plateIndices.size();
            }
        }
        return numIndices * 2;
    }

    public static boolean overlap(Rectangle r1, Rectangle r2) {
        return r2.x < r1.x + r1.width && r2.x + r2.width > r1.x && r2.y < r1.y + r1.height && r2.y + r2.height > r1.y;
    }

    private static void writeString(BinaryBufferWriter writer, String s)
            throws IOException {
        writer.writeString(s);
    }

    private static void writeIntArray(BinaryBufferWriter writer, int[] data)
            throws IOException {
        for (int i : data) {
            writeShortValue(writer, i);
        }
    }

    private static void writeShortValue(BinaryBufferWriter writer, int value)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        writer.write(buffer.putShort((short) value).array());
    }

    private static void writeIntValue(BinaryBufferWriter writer, int value)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        writer.write(buffer.putInt(value).array());
    }

    private static void writeZValue(BinaryBufferWriter writer, float value)
            throws IOException {
        short decimalPart = (short) value;
        short fractionalPart = (short) ((value - decimalPart) * (65536));
        writeShortValue(writer, fractionalPart);
        writeShortValue(writer, decimalPart);
    }

    private static void writeSlopeZValue(BinaryBufferWriter writer, int value)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        writer.write(buffer.putInt(value).array());
    }

}
