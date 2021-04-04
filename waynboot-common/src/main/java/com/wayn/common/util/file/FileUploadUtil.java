package com.wayn.common.util.file;

import com.wayn.common.exception.BusinessException;
import com.wayn.common.util.date.DateUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件上传帮助类
 */
public class FileUploadUtil {

    /**
     * 上传文件
     *
     * @param file     spring MultipartFile文件对象
     * @param filePath 要保存的文件目录
     * @return 新文件名称
     * @throws IOException 上传异常
     */
    public static String uploadFile(MultipartFile file, String filePath) throws IOException {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > 100) {
            throw new BusinessException("文件名称过长");
        }
        String fileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        String encodingFilename = FileUtils.encodingFilename(fileName);
        fileName = genNewFilename(encodingFilename, extension);
        File desc = new File(filePath, fileName);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        file.transferTo(desc);
        return fileName;
    }

    /**
     * @param fileBytes 文件base64编码后内容
     * @param fileName  文件名称
     * @param filePath  要保存的文件按目录
     * @return 新文件名称
     * @throws IOException 上传异常
     */
    public static String uploadFile(byte[] fileBytes, String fileName, String filePath) throws IOException {
        int fileNameLength = Objects.requireNonNull(fileName).length();
        if (fileNameLength > 100) {
            throw new BusinessException("文件名称过长");
        }
        String encodingFilename = FileUtils.encodingFilename(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        fileName = genNewFilename(encodingFilename, extension);
        File desc = new File(filePath, fileName);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        IOUtils.write(fileBytes, new FileOutputStream(desc));
        return fileName;
    }

    /**
     * 生成新的文件名称
     *
     * @param encodingFilename 编码后文件名
     * @param extension        扩展名称
     * @return 新的文件名称
     */
    private static String genNewFilename(String encodingFilename, String extension) {
        return DateUtils.datePath() + "/" + encodingFilename + "." + extension;
    }

}
