package com.kamal.imageprocessor.config;

import com.kamal.imageprocessor.model.ImageJob;
import com.kamal.imageprocessor.processor.GrayscaleProcessor;
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

    @Bean
    public ItemReader<ImageJob> imageReader() {
        // Read images from a specific folder (configurable)
        File folder = new File("images/input");
        List<ImageJob> jobs = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                    jobs.add(new ImageJob(file.getName(), file.getAbsolutePath(), "images/output/" + file.getName(), null));
                }
            }
        }
        return new ListItemReader<>(jobs);
    }

    @Bean
    public ItemWriter<ImageJob> imageWriter() {
        return items -> {
            for (ImageJob item : items) {
                if (item.getProcessedImage() != null) {
                    File output = new File(item.getOutputPath());
                    output.getParentFile().mkdirs();
                    ImageIO.write(item.getProcessedImage(), "jpg", output);
                }
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        // Multi-threading: Enable parallel processing
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(4); // Number of Threads
        return executor;
    }

    @Bean
    public Step imageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                         ItemReader<ImageJob> reader, GrayscaleProcessor processor, ItemWriter<ImageJob> writer,
                         TaskExecutor taskExecutor) {
        return new StepBuilder("imageStep", jobRepository)
                .<ImageJob, ImageJob>chunk(10, transactionManager) // Each Batch contains 10 images
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor) // Run Step with Multi-threading
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, Step imageStep) {
        return new JobBuilder("imageJob", jobRepository)
                .start(imageStep)
                .build();
    }
}
