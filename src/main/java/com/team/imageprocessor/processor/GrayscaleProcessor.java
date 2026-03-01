package com.team.imageprocessor.processor;

import com.team.imageprocessor.model.ImageJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

@Slf4j
@Component
public class GrayscaleProcessor implements ItemProcessor<ImageJob, ImageJob> {

    @Override
    public ImageJob process(ImageJob item) throws Exception {
        try {
            if (!Files.exists(item.getInputPath())) {
                log.error("Input file not found: {}", item.getInputPath());
                return null;
            }

            BufferedImage source = ImageIO.read(item.getInputPath().toFile());
            if (source == null) {
                log.error("Unable to decode image: {}", item.getFileName());
                return null;
            }
            
            BufferedImage grayImage = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = grayImage.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();

            item.setProcessedImage(grayImage);
            log.info("Converted to grayscale: {}", item.getFileName());
            
            return item;
        } catch (Exception e) {
            log.error("FAILED processing {}: {}", item.getFileName(), e.getMessage());
            throw e;
        }
    }
}
