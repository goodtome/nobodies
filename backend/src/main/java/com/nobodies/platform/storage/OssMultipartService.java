package com.nobodies.platform.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.nobodies.platform.upload.entity.UploadChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OssMultipartService {

    private final OSS ossClient;

    @Value("${aliyun.oss.bucket}")
    private String bucketName;

    public String initMultipartUpload(String objectKey, String contentType) {
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        request.setObjectMetadata(metadata);
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    public String uploadPart(String objectKey, String uploadId, int partNumber, InputStream inputStream, long partSize) {
        UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(bucketName);
        request.setKey(objectKey);
        request.setUploadId(uploadId);
        request.setInputStream(inputStream);
        request.setPartSize(partSize);
        request.setPartNumber(partNumber);
        UploadPartResult result = ossClient.uploadPart(request);
        return result.getPartETag().getETag();
    }

    public void completeMultipartUpload(String objectKey, String uploadId, List<UploadChunk> chunks) {
        List<PartETag> partETags = chunks.stream()
            .map(chunk -> new PartETag(chunk.getChunkNumber(), chunk.getEtag()))
            .toList();
        CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(bucketName, objectKey, uploadId, partETags);
        ossClient.completeMultipartUpload(request);
    }

    public void abortMultipartUpload(String objectKey, String uploadId) {
        ossClient.abortMultipartUpload(new AbortMultipartUploadRequest(bucketName, objectKey, uploadId));
    }

    public URL generatePresignedUrl(String objectKey, Duration duration) {
        return ossClient.generatePresignedUrl(bucketName, objectKey, Date.from(Instant.now().plus(duration)));
    }
}
