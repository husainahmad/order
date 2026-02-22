package com.harmoni.pos.order.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Utility class for handling image processing tasks such as compression and resizing.
 * <p>
 * This class provides static methods to manipulate image data, such as resizing and converting to JPEG format.
 * </p>
 */
public class ImageUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException always thrown to indicate that this is a utility class
     */
    private ImageUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Compresses the input image byte array by resizing it to a fixed dimension (800x600)
     * and converting it to JPEG format.
     *
     * @param imageData the raw image data as a byte array
     * @return the compressed image as a JPEG byte array
     * @throws IOException if an error occurs during image reading or writing
     */
    public static byte[] compressImage(byte[] imageData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizeImage(image, 800, 600), "jpg", outputStream); // Resize to 800x600
        return outputStream.toByteArray();
    }

    /**
     * Resizes the given {@link BufferedImage} to the specified width and height using smooth scaling.
     *
     * @param originalImage the original image to resize
     * @param targetWidth   the target width in pixels
     * @param targetHeight  the target height in pixels
     * @return the resized {@link BufferedImage}
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
