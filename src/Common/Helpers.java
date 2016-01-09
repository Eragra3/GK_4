package Common;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Helpers {

    public static byte[] whitePixelsData = new byte[Configuration.IMAGE_HEIGHT * Configuration.IMAGE_WIDTH * 3];

    public static double[][] zBufferInitial = new double[Configuration.IMAGE_WIDTH][Configuration.IMAGE_HEIGHT];

    static {
        for (int i = 0; i < whitePixelsData.length; i++)
            whitePixelsData[i] = (byte) 255;

        for (double[] column : zBufferInitial) {
            for (int i = 0; i < column.length; i++) {
                column[i] = Double.MAX_VALUE;
            }
        }
    }

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
}
