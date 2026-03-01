package com.team.imageprocessor.config;

import com.team.imageprocessor.model.ImageJob;
import com.team.imageprocessor.processor.GrayscaleProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public ItemReader<ImageJob> imageReader() {
        // Use user home relative path or current project path to avoid permission issues
        String projectRoot = System.getProperty("user.dir");
        File inputFolder = new File(projectRoot, "images/input");
        List<ImageJob> jobs = new ArrayList<>();
        
        if (!inputFolder.exists()) {
            inputFolder.mkdirs();
            logger.info("Created input directory: " + inputFolder.getAbsolutePath());
        }

        File[] files = inputFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    String outputDirPath = projectRoot + File.separator + "images" + File.separator + "output" + File.separator;
                    jobs.add(new ImageJob(file.getName(), file.getAbsolutePath(), outputDirPath + file.getName(), null));
                }
            }
        }
        logger.info("Reader found {} images to process in: {}", jobs.size(), inputFolder.getAbsolutePath());
        return new ListItemReader<>(jobs);
    }

    @Bean
    public ItemWriter<ImageJob> imageWriter() {
        return items -> {
            for (ImageJob item : items) {
                if (item.getProcessedImage() != null) {
                    File outputFile = new File(item.getOutputPath());
                    // Ensure directory exists
                    outputFile.getParentFile().mkdirs();
                    
                    // Clear existing file if it exists to avoid permission/lock issues
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }

                    try {
                        // Use ImageIO write to a file directly
                        boolean success = ImageIO.write(item.getProcessedImage(), "jpg", outputFile);
                        if (success) {
                            logger.info("SUCCESS: Saved processed image to -> {}", outputFile.getAbsolutePath());
                        } else {
                            logger.error("FAILED: ImageIO write returned false for -> {}", item.getFileName());
                        }
                    } catch (Exception e) {
                        logger.error("ERROR writing image {}: {}. Target: {}", item.getFileName(), e.getMessage(), outputFile.getAbsolutePath());
                    }
                }
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(4);
        return executor;
    }

    @Bean
    public Step imageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         ItemReader<ImageJob> reader, GrayscaleProcessor processor, ItemWriter<ImageJob> writer,
                         TaskExecutor taskExecutor) {
        return new StepBuilder("imageStep", jobRepository)
                .<ImageJob, ImageJob>chunk(2, transactionManager) 
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job imageJob(JobRepository jobRepository, Step imageStep) {
        return new JobBuilder("imageJob", jobRepository)
                .start(imageStep)
                .build();
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }
}
