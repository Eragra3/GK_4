package Renderers;

import javafx.scene.image.PixelWriter;

/**
 * Created by bider_000 on 10.01.2016.
 */
public interface IRenderer {
    void render();

    void setPixelWriter(PixelWriter pixelWriter);
}
