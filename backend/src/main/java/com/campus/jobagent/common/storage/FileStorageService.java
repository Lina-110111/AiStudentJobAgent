package com.campus.jobagent.common.storage;

import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地文件存储：简历附件、面试录音等。
 *
 * <p>生产环境可替换为对象存储（OSS / MinIO），只需换掉本类实现，
 * 上层 Service 不感知存储介质的变化。
 */
@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXT = List.of("pdf", "doc", "docx", "txt", "png", "jpg", "jpeg");
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String localPath;
    private final String urlPrefix;
    private final long maxSizeBytes;

    public FileStorageService(@Value("${app.storage.local-path:./data/upload}") String localPath,
                              @Value("${app.storage.url-prefix:/files}") String urlPrefix,
                              @Value("${app.storage.max-size-mb:10}") long maxSizeMb) {
        this.localPath = localPath;
        this.urlPrefix = urlPrefix;
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
    }

    /**
     * 保存文件并返回可访问的相对路径。
     *
     * @param file    上传文件
     * @param bizType 业务子目录，如 resume / interview
     */
    public StoredFile store(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw BizException.of(ResultCode.PARAM_INVALID, "上传文件为空");
        }
        if (file.getSize() > maxSizeBytes) {
            throw BizException.of(ResultCode.PARAM_INVALID, "文件大小超过限制");
        }
        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unnamed";
        String ext = extensionOf(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw BizException.of(ResultCode.PARAM_INVALID, "不支持的文件类型：" + ext);
        }
        String dateDir = LocalDate.now().format(DATE_DIR);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(localPath, bizType, dateDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            file.transferTo(target.toFile());
            String relative = bizType + "/" + dateDir + "/" + storedName;
            log.info("文件已保存：{}", target);
            return new StoredFile(originalName, relative, urlPrefix + "/" + relative, file.getSize());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw BizException.of(ResultCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 存储结果。 */
    public record StoredFile(String originalName, String relativePath, String url, long size) {
    }
}
