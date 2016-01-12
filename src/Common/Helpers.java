package Common;

import Common.Models.Vector3DModel;
import com.sun.javafx.geom.Matrix3f;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Helpers {

    public static final byte[] whitePixelsData = new byte[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH * 3];
    public static final int[] whitePixelsARGBData = new int[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH];

    public static final double[][] zBufferInitial = new double[Configuration.IMAGE_WIDTH][Configuration.IMAGE_HEIGHT];

    static {
        for (int i = 0; i < whitePixelsData.length; i++)
            whitePixelsData[i] = -1;

        for (int i = 0; i < whitePixelsARGBData.length; i++)
            whitePixelsData[i] = 0xffffffff;

        for (double[] column : zBufferInitial) {
            for (int i = 0; i < column.length; i++) {
                column[i] = 10000.0;
            }
        }
    }

    private static RealMatrix vector = new Array2DRowRealMatrix(4, 1);

    public static double max(double... n) {
        int i = 0;
        double max = n[i];

        while (++i < n.length)
            if (n[i] > max)
                max = n[i];

        return max;
    }

    public static double min(double... n) {
        int i = 0;
        double min = n[i];

        while (++i < n.length)
            if (n[i] < min)
                min = n[i];

        return min;
    }

    public final static double dotProduct(Vector3DModel v1, Vector3DModel v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public final static void resetZBuffer(double[][] zBuffer) {
        for (double[] column : zBuffer) {
            for (int i = 0; i < column.length; i++) {
                column[i] = Double.MAX_VALUE;
            }
        }
    }

    public final static void resetZBuffer(double[] zBuffer) {
        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Double.MAX_VALUE;
        }
    }

    public final static void resetPixelData(byte[] pixelData) {
        for (int i = 0; i < pixelData.length; i++)
            pixelData[i] = -1;
    }

    public final static void resetPixelData(int[] pixelData) {
        for (int i = 0; i < pixelData.length; i++)
            pixelData[i] = 0xff000000;
    }

    public final static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public final static RealMatrix getColumnVector(double x, double y, double z) {
        vector.setEntry(0, 0, x);
        vector.setEntry(1, 0, y);
        vector.setEntry(2, 0, z);
        vector.setEntry(3, 0, 1);
        return vector;
    }


}
