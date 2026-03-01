package com.team.imageprocessor.batch.writer;

import com.team.imageprocessor.model.ImageJob;
import com.team.imageprocessor.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageJobWriter {

    private final ImageProcessingService processingService;

    @Bean
    public ItemWriter<ImageJob> imageWriter() {
        return items -> {
            for (ImageJob item : items) {
                if (item.getProcessedImage() != null) {
                    try {
                        String format = getFormat(item.getFileName());
                        processingService.save(item.getProcessedImage(), item.getOutputPath(), format);
                        log.info("SAVED: {}", item.getFileName());
                    } catch (Exception e) {
                        log.error("ERROR saving {}: {}", item.getFileName(), e.getMessage());
                    }
                }
            }
        };
    }

    private String getFormat(String fileName) {
        return fileName.toLowerCase().endsWith(".png") ? "png" : "jpg";
    }
}
