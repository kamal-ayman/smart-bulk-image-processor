package com.team.imageprocessor.processor;

import com.team.imageprocessor.model.ImageJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class GrayscaleProcessor implements ItemProcessor<ImageJob, ImageJob> {

    private static final Logger logger = LoggerFactory.getLogger(GrayscaleProcessor.class);

    @Override
    public ImageJob process(ImageJob item) throws Exception {
        try {
            File inputFile = new File(item.getInputPath());
            if (!inputFile.exists()) {
                logger.error("Input file NOT FOUND: " + item.getInputPath());
                return null;
            }

            // 1. Read image from path
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                logger.error("Could not decode image (ImageIO.read returned null): " + item.getFileName());
                return null;
            }
            
            // 2. Apply Grayscale Logic (Image Processing)
            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color c = new Color(image.getRGB(j, i));
                    int red = (int) (c.getRed() * 0.299);
                    int green = (int) (c.getGreen() * 0.587);
                    int blue = (int) (c.getBlue() * 0.114);
                    int gray = red + green + blue;
                    grayImage.setRGB(j, i, new Color(gray, gray, gray).getRGB());
                }
            }

            // 3. Update Job with processed image
            item.setProcessedImage(grayImage);
            logger.info("Successfully processed image to grayscale: " + item.getFileName());
            return item;
        } catch (Exception e) {
            logger.error("ERROR processing image " + item.getFileName() + ": " + e.getMessage());
            throw e;
        }
    }
}
