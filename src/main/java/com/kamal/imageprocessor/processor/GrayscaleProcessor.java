package com.kamal.imageprocessor.processor;

import com.kamal.imageprocessor.model.ImageJob;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Component
public class GrayscaleProcessor implements ItemProcessor<ImageJob, ImageJob> {

    @Override
    public ImageJob process(ImageJob item) throws Exception {
        // 1. قراءة الصورة من المسار
        BufferedImage image = ImageIO.read(new File(item.getInputPath()));
        
        // 2. تطبيق الـ Grayscale Logic (Image Processing)
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                int gray = red + green + blue;
                grayImage.setRGB(j, i, new Color(gray, gray, gray).getRGB());
            }
        }

        // 3. تحديث الـ Job بالصورة المعالجة
        item.setProcessedImage(grayImage);
        return item;
    }
}
