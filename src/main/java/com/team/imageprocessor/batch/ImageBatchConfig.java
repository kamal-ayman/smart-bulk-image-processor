package com.team.imageprocessor.batch;

import com.team.imageprocessor.service.ImageFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Spring Batch Configuration.
 * Defines the Job, Step, Reader, Processor, and Writer.
 * Uses Multi-threading via taskExecutor.
 */
@Configuration
@RequiredArgsConstructor
public class ImageBatchConfig {

    private final ImageFilterService filterService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor threadPoolTaskExecutor;

    @Bean
    public Job processImagesJob() {
        return new JobBuilder("processImagesJob", jobRepository)
                .start(processImagesStep())
                .build();
    }

    @Bean
    public Step processImagesStep() {
        return new StepBuilder("processImagesStep", jobRepository)
                .<File, File>chunk(10, transactionManager)
                .reader(imageReader())
                .processor(imageProcessor())
                .writer(imageWriter())
                .taskExecutor(threadPoolTaskExecutor) // Parallelism happens here!
                .build();
    }

    @Bean
    public ItemReader<File> imageReader() {
        File dir = new File("input-images");
        if (!dir.exists()) dir.mkdirs();
        
        File[] files = dir.listFiles(f -> f.getName().toLowerCase().endsWith(".jpg") || 
                                          f.getName().toLowerCase().endsWith(".png") ||
                                          f.getName().toLowerCase().endsWith(".jpeg"));
        
        List<File> fileList = (files != null) ? Arrays.asList(files) : Collections.emptyList();
        return new ListItemReader<>(fileList);
    }

    @Bean
    public ItemProcessor<File, File> imageProcessor() {
        return item -> {
            // Apply a filter (e.g., Grayscale)
            return filterService.processImage(item, "output-images", "grayscale");
        };
    }

    @Bean
    public ItemWriter<File> imageWriter() {
        return items -> {
            // Items are already written to disk by the service in this simple implementation.
            // Here we could log results or move files to a 'processed' folder.
        };
    }
}
