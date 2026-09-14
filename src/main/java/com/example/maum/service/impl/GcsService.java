package com.example.maum.service.impl;

import com.example.maum.service.IGcsService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Slf4j
public class GcsService implements IGcsService {

    @Value("${secure.gcs.bucket-name}")
    private String bucketName;

    // maumPy의 GCP_CREDENTIALS_JSON과 동일하게, 파일 경로가 아니라 서비스 계정 JSON 내용 자체를 값으로 받음
    @Value("${secure.gcs.credentials-json}")
    private String credentialsJson;

    private Storage storage;

    @PostConstruct
    private void init() {
        try (ByteArrayInputStream credentialsStream =
                     new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (Exception e) {
            // 서비스 계정 키가 아직 준비되지 않은 환경(로컬 세팅 전)에서도 앱 전체가 죽지 않도록
            // 여기서는 로그만 남기고, 실제 업로드 시점에 storage가 null이면 그때 에러를 던짐
            log.error("GCS 인증 초기화 실패 - credentials-json 설정을 확인하세요: {}", e.getMessage());
        }
    }

    @Override
    public String uploadImage(MultipartFile file, Integer diaryNo) throws Exception {
        if (storage == null) {
            throw new IllegalStateException("GCS 클라이언트가 초기화되지 않았습니다. 서비스 계정 설정을 확인하세요.");
        }

        String objectName = "diary/" + diaryNo + "/" + UUID.randomUUID() + getExtension(file.getOriginalFilename());

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (storage == null || imageUrl == null) return;

        try {
            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            if (!imageUrl.startsWith(prefix)) {
                log.warn("알 수 없는 이미지 URL 형식이라 GCS에서 삭제하지 않음: {}", imageUrl);
                return;
            }
            String objectName = imageUrl.substring(prefix.length());
            storage.delete(BlobId.of(bucketName, objectName));
        } catch (Exception e) {
            log.error("GCS 이미지 삭제 실패: {}", e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
