package com.team.imageprocessor.processor;

import com.team.imageprocessor.model.ImageJob;
import com.team.imageprocessor.service.ImageProcessingService;
import com.team.imageprocessor.strategy.ImageProcessingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.file.Files;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrayscaleProcessor implements ItemProcessor<ImageJob, ImageJob> {

    private final ImageProcessingService processingService;
    private final ImageProcessingStrategy grayscaleStrategy;

    @Override
    public ImageJob process(ImageJob item) throws Exception {
        try {
            if (!Files.exists(item.getInputPath())) {
                log.error("Input file not found: {}", item.getInputPath());
                return null;
            }

            BufferedImage processed = processingService.process(item.getInputPath(), grayscaleStrategy);
            item.setProcessedImage(processed);
            
            log.info("Processed with strategy: {}", grayscaleStrategy.getFilterName());
            return item;
        } catch (Exception e) {
            log.error("FAILED processing {}: {}", item.getFileName(), e.getMessage());
            throw e;
        }
    }
}

