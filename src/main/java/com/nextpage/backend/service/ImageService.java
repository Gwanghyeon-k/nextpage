package com.nextpage.backend.service;

import com.nextpage.backend.error.exception.image.ImageDownloadException;
import com.nextpage.backend.error.exception.image.ImageUploadException;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
     * Lambda 리사이즈 흐름: download → upload original (Lambda triggers resize) → return resized URL
     */
    public String uploadWithLambda(String imageUrl) throws ImageDownloadException, ImageUploadException {
        File imageFile = downloadImage(imageUrl);
        String originalKey = "dalle/" + UUID.randomUUID() + getExtension(imageFile);
        uploadFile(bucketName, originalKey, imageFile);
        String resizedBucket = bucketName + "-resize";
        String resizedKey = "resized-" + originalKey;
        return amazonS3.getUrl(resizedBucket, resizedKey).toString();
    }

    /**
     * Thumbnailator + WebP 흐름: download → resize with Thumbnailator → convert to WebP → upload → return URL
     */
    public String uploadWithThumbnailator(String imageUrl) throws ImageDownloadException, ImageUploadException {
        File imageFile = downloadImage(imageUrl);
        try {
            // 리사이즈
            BufferedImage thumb = Thumbnails.of(imageFile)
                    .size(512, 512)
                    .asBufferedImage();
            File thumbPng = File.createTempFile("thumb-", ".png");
            ImageIO.write(thumb, "png", thumbPng);

            // WebP 변환
            ImmutableImage img = ImmutableImage.loader().fromFile(thumbPng);
            File webpFile = File.createTempFile("thumb-", ".webp");
            img.output(WebpWriter.DEFAULT, webpFile);

            // 업로드
            String key = "dalle-thumb/" + UUID.randomUUID() + ".webp";
            uploadFile(bucketName, key, webpFile);
            return amazonS3.getUrl(bucketName, key).toString();
        } catch (Exception e) {
            log.error("Thumbnailator flow failed", e);
            throw new ImageUploadException("Thumbnailator 흐름 실패", e);
        }
    }

    private File downloadImage(String imageUrl) throws ImageDownloadException {
        log.info("Downloading image from URL: {}", imageUrl);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();
            HttpResponse<InputStream> resp = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = resp.statusCode();
            if (status >= 200 && status < 300) {
                File tmp = File.createTempFile("dalle-", getExtensionFromUrl(imageUrl));
                try (InputStream is = resp.body(); OutputStream os = new FileOutputStream(tmp)) {
                    is.transferTo(os);
                }
                return tmp;
            }
            throw new ImageDownloadException("다운로드 실패 status=" + status);
        } catch (ImageDownloadException e) {
            throw e;
        } catch (Exception e) {
            log.error("Download exception", e);
            throw new ImageDownloadException("이미지 다운로드 실패");
        }
    }

    private void uploadFile(String bucket, String key, File file) throws ImageUploadException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            meta.setContentType(detectContentType(key));
            PutObjectRequest req = new PutObjectRequest(bucket, key, file);
            amazonS3.putObject(req);
        } catch (Exception e) {
            log.error("Upload exception", e);
            throw new ImageUploadException("이미지 업로드 실패", e);
        }
    }

    private String getExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf('.'));
    }

    private String getExtensionFromUrl(String url) {
        int idx = url.lastIndexOf('.');
        if (idx > 0) {
            String ext = url.substring(idx);
            int q = ext.indexOf('?');
            return q>0?ext.substring(0,q):ext;
        }
        return ".png";
    }

    private String detectContentType(String key) {
        if (key.endsWith(".png")) return "image/png";
        if (key.endsWith(".jpg")||key.endsWith(".jpeg")) return "image/jpeg";
        if (key.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
