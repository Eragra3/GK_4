package Common;

import Common.Models.LookAtModel;
import Common.Models.LightSourceModel;
import Common.Models.ObserverModel;
import javafx.scene.image.PixelFormat;

/**
 * Created by bider_000 on 08.01.2016.
 */
public class Configuration {
    public static final int IMAGE_WIDTH = 400;
    public static final int IMAGE_HEIGHT = 400;

    public static final double NEAR_Z = 1;
    public static final double FAR_Z = 500;

    public static final int IMAGE_WIDTH_HALF = IMAGE_WIDTH / 2;
    public static final int IMAGE_HEIGHT_HALF = IMAGE_HEIGHT / 2;

    public final static PixelFormat pixelARGBFormat = PixelFormat.getIntArgbInstance();

//    private final static int OBSERVER_X = 0;
//    private final static int OBSERVER_Y = -200;
//    private final static int OBSERVER_Z = 200;
    private final static int OBSERVER_X = -243;
    private final static int OBSERVER_Y = -332;
    private final static int OBSERVER_Z = 30;
    private final static double OBSERVER_FOV_X = 60.0;
    private final static double OBSERVER_FOV_Y = 60.0;
    private final static double OBSERVER_X_ANGLE = 0.0;
    private final static double OBSERVER_Y_ANGLE = 0.0;
    private final static double OBSERVER_Z_ANGLE = 0.0;
    public static ObserverModel observer = new ObserverModel(OBSERVER_X, OBSERVER_Y, OBSERVER_Z, OBSERVER_FOV_X, OBSERVER_FOV_Y,
            OBSERVER_Y_ANGLE, OBSERVER_X_ANGLE, OBSERVER_Z_ANGLE);
//    public static LookAtModel lookAtPoint = new LookAtModel(0, -100, 100, 0, 0, 0);
    public static LookAtModel lookAtPoint = new LookAtModel(-62, -98, 231, 0, 0, 0);


    public static int ambientLightR = 20;
    public static int ambientLightG = 20;
    public static int ambientLightB = 20;
    public static LightSourceModel lightSource = new LightSourceModel(0, -120, 75, 200, 200, 200);

    public static final int SYSTEM_X = 0;
    public static final int SYSTEM_Y = 0;
    public static final int SYSTEM_Z = 0;

    public static final byte BACKGROUND_R = 127;
    public static final byte BACKGROUND_G = 127;
    public static final byte BACKGROUND_B = 127;

    public static final double objectScale = 5.0;
}
