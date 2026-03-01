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
        // Use absolute path to avoid ambiguity
        File folder = new File("/Volumes/kamal-mac/smart-bulk-image-processor/images/input");
        List<ImageJob> jobs = new ArrayList<>();
        
        if (!folder.exists()) {
            folder.mkdirs();
            logger.info("Created input directory: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    String outputDirPath = "/Volumes/kamal-mac/smart-bulk-image-processor/images/output/";
                    jobs.add(new ImageJob(file.getName(), file.getAbsolutePath(), outputDirPath + file.getName(), null));
                }
            }
        }
        logger.info("Reader found {} images to process", jobs.size());
        return new ListItemReader<>(jobs);
    }

    @Bean
    public ItemWriter<ImageJob> imageWriter() {
        return items -> {
            for (ImageJob item : items) {
                if (item.getProcessedImage() != null) {
                    File output = new File(item.getOutputPath());
                    output.getParentFile().mkdirs();
                    
                    try {
                        boolean success = ImageIO.write(item.getProcessedImage(), "jpg", output);
                        if (success) {
                            logger.info("SUCCESS: Saved processed image to -> {}", output.getPath());
                        } else {
                            logger.error("FAILED: ImageIO could not write -> {}", item.getFileName());
                        }
                    } catch (Exception e) {
                        logger.error("ERROR writing image {}: {}", item.getFileName(), e.getMessage());
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
