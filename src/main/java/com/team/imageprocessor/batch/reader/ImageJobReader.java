package com.team.imageprocessor.batch.reader;

import com.team.imageprocessor.model.ImageJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class ImageJobReader {

    private final Path inputFolder = Paths.get("images", "input");
    private final Path outputFolder = Paths.get("images", "output");

    @Bean
    public ItemReader<ImageJob> imageReader() {
        List<ImageJob> jobs = new ArrayList<>();
        try {
            Files.createDirectories(inputFolder);
            Files.createDirectories(outputFolder);

            try (Stream<Path> paths = Files.list(inputFolder)) {
                paths.filter(Files::isRegularFile)
                     .filter(this::isImageFile)
                     .forEach(p -> jobs.add(ImageJob.builder()
                             .fileName(p.getFileName().toString())
                             .inputPath(p)
                             .outputPath(outputFolder.resolve(p.getFileName()))
                             .build()));
            }
        } catch (Exception e) {
            log.error("Error initializing reader: {}", e.getMessage());
        }

        log.info("Found {} images to process", jobs.size());
        return new ListItemReader<>(jobs);
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }
}
