package com.nextpage.backend.service;

import com.nextpage.backend.error.exception.image.ImageConversionException;
import com.nextpage.backend.error.exception.image.ImageDownloadException;
import com.nextpage.backend.error.exception.image.ImageResizeException;
import com.nextpage.backend.error.exception.image.ImageUploadException;
import com.sksamuel.scrimage.ImmutableImage;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

@Service
@Slf4j
public class ImageService {
    private final AmazonS3 amazonS3;

    @Value("${AWS_BUCKET}")
    private String bucketName;

    public ImageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadImageToS3(String imageUrl) throws ImageDownloadException, ImageResizeException, ImageConversionException, ImageUploadException {
        long startTime = System.currentTimeMillis();
        File imageFile = downloadImage(imageUrl);
        long download = System.currentTimeMillis();
        File resizedImage = resizeImage(imageFile);
        long afterresized = System.currentTimeMillis();
        File webpImage = convertToWebP(resizedImage);
        long endTime = System.currentTimeMillis();
        System.out.println("Download Time: " + (download - startTime) + " ms");
        System.out.println("Resize Time: " + (afterresized - download) + " ms");
        System.out.println("Convert to WebP Time: " + (endTime - afterresized) + " ms");


        return uploadFileToS3(webpImage);
    }

    private File downloadImage(String imageUrl) throws ImageDownloadException {
        log.info("Downloading image to file from URL: {}", imageUrl);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .build();
        try {
            File tempFile = File.createTempFile("image", ".tmp");
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile.toPath()));
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return tempFile;
            }
                throw new ImageDownloadException();
        } catch (Exception e) {
            throw new ImageDownloadException();
        }
    }

    private File resizeImage(File imageFile) throws ImageResizeException {
        try {
            BufferedImage resizedImage = Thumbnails.of(imageFile)
                    .size(512, 512)
                    .asBufferedImage();
            File tempFile = new File(imageFile.getParent(), "resized_" + System.currentTimeMillis() + ".png");
            ImageIO.write(resizedImage, "png", tempFile);
            return tempFile;
        } catch (Exception e) {
            throw new ImageResizeException();
        }
    }

    private File convertToWebP(File imageFile) throws ImageConversionException {
        try {
            ImmutableImage image = ImmutableImage.loader().fromFile(imageFile);
            File outputFile = new File(imageFile.getParent(), System.currentTimeMillis() + ".webp");
            image.output(WebpWriter.DEFAULT, outputFile);
            return outputFile;
        } catch (Exception e) {
            throw new ImageConversionException();
        }
    }

    private String uploadFileToS3(File file) throws ImageUploadException {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, file.getName(), new FileInputStream(file), metadata);
            amazonS3.putObject(putObjectRequest);
            return amazonS3.getUrl(bucketName, file.getName()).toString();
        } catch (Exception e) {
            throw new ImageUploadException();
        }
    }
}
