package com.team.imageprocessor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Service to handle pure image manipulation logic.
 * Follows Single Responsibility Principle.
 */
@Service
@Slf4j
public class ImageFilterService {

    public File processImage(File inputImage, String outputDir, String filterType) throws IOException {
        BufferedImage image = ImageIO.read(inputImage);
        if (image == null) {
            throw new IOException("Failed to read image: " + inputImage.getName());
        }

        BufferedImage result;
        switch (filterType.toLowerCase()) {
            case "grayscale":
                result = applyGrayscale(image);
                break;
            case "resize":
                result = applyResize(image, 800, 600); // Sample resize
                break;
            default:
                result = image;
        }

        File outputFile = Path.of(outputDir, "processed_" + inputImage.getName()).toFile();
        ImageIO.write(result, "jpg", outputFile);
        return outputFile;
    }

    private BufferedImage applyGrayscale(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return result;
    }

    private BufferedImage applyResize(BufferedImage source, int targetWidth, int targetHeight) {
        Image resultingImage = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
