package com.nextpage.backend.service;

import com.nextpage.backend.error.exception.image.ImageDownloadException;
import com.nextpage.backend.error.exception.image.ImageUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {
    private final AmazonS3 amazonS3;

    @Value("${AWS_BUCKET}")
    private String bucketName;

    public ImageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * 1) DALL·E에서 받은 URL로 이미지를 다운로드
     * 2) 원본 이미지를 S3에 업로드 (람다 트리거)
     * 3) 리사이징된 S3 버킷의 URL 반환
     */
    public String uploadImageToS3UsingLambda(String imageUrl) throws ImageDownloadException, ImageUploadException {
        // 1) 이미지 다운로드
        File imageFile = downloadImage(imageUrl);

        // 2) 원본 업로드
        String originalKey = "dalle/" + UUID.randomUUID() + getExtension(imageFile);
        uploadFile(bucketName, originalKey, imageFile);

        // 3) Lambda가 만든 리사이즈된 파일 키
        String resizedKey = "resized-" + originalKey;
        String resizedBucket = bucketName + "-resize";

        // 4) 최종 URL 반환
        return amazonS3.getUrl(resizedBucket, resizedKey).toString();
    }

    private File downloadImage(String imageUrl) throws ImageDownloadException {
        log.info("Downloading image from URL: {}", imageUrl);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                File tempFile = File.createTempFile("dalle-", getExtensionFromUrl(imageUrl));
                try (InputStream is = response.body(); OutputStream os = new FileOutputStream(tempFile)) {
                    is.transferTo(os);
                }
                return tempFile;
            } else {
                log.error("Image download failed, status: {}", status);
                throw new ImageDownloadException("이미지 다운로드 실패: status ");
            }
        } catch (ImageDownloadException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during image download", e);
            throw new ImageDownloadException("이미지 다운로드 실패");
        }
    }

    private void uploadFile(String bucket, String key, File file) throws ImageUploadException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            metadata.setContentType(detectContentType(key));
            amazonS3.putObject(new PutObjectRequest(bucket, key, fis, metadata));
        } catch (Exception e) {
            log.error("Exception during S3 upload", e);
            throw new ImageUploadException("이미지 업로드 실패");
        }
    }

    private String getExtension(File file) {
        String name = file.getName();
        return name.substring(name.lastIndexOf('.'));
    }

    private String getExtensionFromUrl(String url) {
        int idx = url.lastIndexOf('.');
        if (idx > 0) {
            String ext = url.substring(idx);
            int q = ext.indexOf('?');
            return q > 0 ? ext.substring(0, q) : ext;
        }
        return ".png";
    }

    private String detectContentType(String key) {
        if (key.endsWith(".png")) return "image/png";
        if (key.endsWith(".jpg") || key.endsWith(".jpeg")) return "image/jpeg";
        if (key.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
