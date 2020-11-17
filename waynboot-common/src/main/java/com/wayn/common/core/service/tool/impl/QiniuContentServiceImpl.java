package com.wayn.common.core.service.tool.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.domain.tool.QiniuContent;
import com.wayn.common.core.mapper.tool.QiniuContentMapper;
import com.wayn.common.core.service.tool.IQiniuConfigService;
import com.wayn.common.core.service.tool.IQiniuContentService;
import com.wayn.common.util.file.FileUtils;
import com.wayn.common.util.file.QiniuUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 * <p>
 * 七牛云文件存储 服务实现类
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
@Service
public class QiniuContentServiceImpl extends ServiceImpl<QiniuContentMapper, QiniuContent> implements IQiniuContentService {

    @Autowired
    private QiniuContentMapper qiniuContentMapper;

    @Autowired
    private IQiniuConfigService iQiniuConfigService;

    @Override
    public IPage<QiniuContent> listPage(Page<QiniuContent> page, QiniuContent qiniuContent) {
        return qiniuContentMapper.selectQiniuContentListPage(page, qiniuContent);
    }

    @Override
    public QiniuContent upload(MultipartFile file, QiniuConfig qiniuConfig) throws IOException {
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiniuUtil.getRegion(qiniuConfig.getRegion()));
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        String upToken = auth.uploadToken(qiniuConfig.getBucket());
        String key = file.getOriginalFilename();
        if (getOne(new QueryWrapper<QiniuContent>().eq("name", FilenameUtils.getBaseName(key))) != null) {
            key = QiniuUtil.getKey(key);
        }
        Response response = uploadManager.put(file.getBytes(), key, upToken);

        DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        //存入数据库
        QiniuContent qiniuContent = new QiniuContent();
        qiniuContent.setSuffix(FilenameUtils.getExtension(key));
        qiniuContent.setBucket(qiniuConfig.getBucket());
        if (qiniuConfig.getType() == 0) {
            qiniuContent.setType("公开");
        } else {
            qiniuContent.setType("私有");
        }
        qiniuContent.setName(FilenameUtils.getBaseName(key));
        qiniuContent.setUrl(qiniuConfig.getHost() + "/" + putRet.key);
        qiniuContent.setSize(FileUtils.getSize(Integer.parseInt(file.getSize() + "")));
        qiniuContent.setCreateTime(new Date());
        return qiniuContent;
    }

    @Override
    public String download(Long contentId, QiniuConfig qiniuConfig) {
        QiniuContent content = getById(contentId);
        String finalUrl;
        String type = "公开";
        if (type.equals(content.getType())) {
            finalUrl = content.getUrl();
        } else {
            Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
            // 1小时，可以自定义链接过期时间
            long expireInSeconds = 3600;
            finalUrl = auth.privateDownloadUrl(content.getUrl(), expireInSeconds);
        }
        return finalUrl;
    }

    @Override
    public boolean syncQiniu(QiniuConfig config) {
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiniuUtil.getRegion(config.getRegion()));
        Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        // 文件名前缀
        String prefix = "";
        // 每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 1000;
        // 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";
        // 列举空间文件列表
        BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(config.getBucket(), prefix, limit, delimiter);
        while (fileListIterator.hasNext()) {
            // 处理获取的file list结果
            QiniuContent qiniuContent;
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                if (getOne(new QueryWrapper<QiniuContent>().eq("name", FilenameUtils.getBaseName(item.key))) == null) {
                    qiniuContent = new QiniuContent();
                    qiniuContent.setSize(FileUtils.getSize(Integer.parseInt(item.fsize + "")));
                    qiniuContent.setSuffix(FilenameUtils.getExtension(item.key));
                    qiniuContent.setName(FilenameUtils.getBaseName(item.key));
                    if (config.getType() == 0) {
                        qiniuContent.setType("公开");
                    } else {
                        qiniuContent.setType("私有");
                    }
                    qiniuContent.setBucket(config.getBucket());
                    qiniuContent.setUrl(config.getHost() + "/" + item.key);
                    qiniuContent.setCreateTime(new Date());
                    save(qiniuContent);
                }
            }
        }
        return true;
    }

    @Override
    public boolean delete(Long id, QiniuConfig config) throws QiniuException {
        QiniuContent content = getById(id);
        // 构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(QiniuUtil.getRegion(config.getRegion()));
        Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        Response response = bucketManager.delete(content.getBucket(), content.getName() + "." + content.getSuffix());
        if (!response.isOK()) {
            return false;
        }
        removeById(content);
        return true;
    }
}
