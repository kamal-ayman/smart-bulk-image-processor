package com.team.imageprocessor.model;

import lombok.Builder;
import lombok.Data;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

@Data
@Builder
public class ImageJob {
    private String fileName;
    private Path inputPath;
    private Path outputPath;
    private BufferedImage processedImage;
}
