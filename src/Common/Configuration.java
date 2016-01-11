package Common;

import Common.Models.LightSourceModel;
import Common.Models.ObserverModel;
import Common.Models.Vertex3DModel;
import javafx.scene.image.PixelFormat;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Configuration {
    public static final int IMAGE_WIDTH = 400;
    public static final int IMAGE_HEIGHT = 400;

    public static final int IMAGE_WIDTH_HALF = IMAGE_WIDTH / 2;
    public static final int IMAGE_HEIGHT_HALF = IMAGE_HEIGHT / 2;

    public final static PixelFormat pixelRGBFormat = PixelFormat.getByteRgbInstance();
    public final static PixelFormat pixelARGBFormat = PixelFormat.getIntArgbInstance();

    private static int OBSERVER_X = 0;
    private static int OBSERVER_Y = 0;
    private static int OBSERVER_Z = -100;
    private static double OBSERVER_ANGLE_X = 60.0;
    private static double OBSERVER_ANGLE_Y = 60.0;
    public static ObserverModel observer = new ObserverModel(OBSERVER_X, OBSERVER_Y, OBSERVER_Z, OBSERVER_ANGLE_X, OBSERVER_ANGLE_Y);

    public static int ambientLightR = 0;
    public static int ambientLightG = 0;
    public static int ambientLightB = 0;
    public static LightSourceModel lightSource = new LightSourceModel(0, 0, 200, 200, 200, 200);

    public static final int SYSTEM_X = 0;
    public static final int SYSTEM_Y = 0;
    public static final int SYSTEM_Z = 0;

    public static final byte BACKGROUND_R = 127;
    public static final byte BACKGROUND_G = 127;
    public static final byte BACKGROUND_B = 127;

    public static final double objectScale = 5.0;
}
