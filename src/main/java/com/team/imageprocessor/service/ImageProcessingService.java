package com.team.imageprocessor.service;

import com.team.imageprocessor.strategy.ImageProcessingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    public BufferedImage process(Path inputPath, ImageProcessingStrategy strategy) throws IOException {
        BufferedImage source = ImageIO.read(inputPath.toFile());
        if (source == null) {
            throw new IOException("Unable to decode image: " + inputPath.getFileName());
        }
        return strategy.process(source);
    }

    public void save(BufferedImage image, Path outputPath, String format) throws IOException {
        ImageIO.write(image, format, outputPath.toFile());
    }
}
