package com.team.imageprocessor.config;

import com.team.imageprocessor.model.ImageJob;
import com.team.imageprocessor.processor.GrayscaleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class BatchConfig {

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

    @Bean
    public ItemWriter<ImageJob> imageWriter() {
        return items -> {
            for (ImageJob item : items) {
                if (item.getProcessedImage() != null) {
                    try {
                        String format = getFormat(item.getFileName());
                        ImageIO.write(item.getProcessedImage(), format, item.getOutputPath().toFile());
                        log.info("SAVED: {}", item.getFileName());
                    } catch (Exception e) {
                        log.error("ERROR saving {}: {}", item.getFileName(), e.getMessage());
                    }
                }
            }
        };
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("ImgProc-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step imageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         ItemReader<ImageJob> reader, GrayscaleProcessor processor, ItemWriter<ImageJob> writer,
                         TaskExecutor threadPoolTaskExecutor) {
        return new StepBuilder("imageStep", jobRepository)
                .<ImageJob, ImageJob>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(threadPoolTaskExecutor)
                .build();
    }

    @Bean
    public Job imageJob(JobRepository jobRepository, Step imageStep) {
        return new JobBuilder("imageJob", jobRepository)
                .start(imageStep)
                .build();
    }

    private boolean isImageFile(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }

    private String getFormat(String fileName) {
        if (fileName.toLowerCase().endsWith(".png")) return "png";
        return "jpg";
    }
}
