package com.team.imageprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.awt.image.BufferedImage;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageJob {
    private String fileName;
    private String inputPath;
    private String outputPath;
    private BufferedImage processedImage;
}
