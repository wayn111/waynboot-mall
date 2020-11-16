package com.wayn.common.core.service.tool.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
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
}
