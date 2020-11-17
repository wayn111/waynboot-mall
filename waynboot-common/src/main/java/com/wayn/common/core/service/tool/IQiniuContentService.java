package com.wayn.common.core.service.tool;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiniu.common.QiniuException;
import com.wayn.common.core.domain.tool.QiniuConfig;
import com.wayn.common.core.domain.tool.QiniuContent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 七牛云文件存储 服务类
 * </p>
 *
 * @author wayn
 * @since 2020-11-15
 */
public interface IQiniuContentService extends IService<QiniuContent> {

    /**
     * 查询七牛云文件存储分页列表
     *
     * @param page         分页对象
     * @param qiniuContent 查询参数
     * @return qiniuContent分页列表
     */
    IPage<QiniuContent> listPage(Page<QiniuContent> page, QiniuContent qiniuContent);

    /**
     * 上传文件至七牛云
     *
     * @param file        待上传文件
     * @param qiniuConfig 七牛云配置
     * @return 返回qiniuContent对象
     * @throws IOException
     */
    QiniuContent upload(MultipartFile file, QiniuConfig qiniuConfig) throws IOException;

    /**
     * 七牛云文件下载
     *
     * @param contentId   qiniuContent对象ID
     * @param qiniuConfig 七牛云配置
     * @return 下载链接
     */
    String download(Long contentId, QiniuConfig qiniuConfig);

    /**
     * 同步七牛云存储文件至qiniuContent表中
     * @param config
     */
    boolean syncQiniu(QiniuConfig config);

    boolean delete(Long id,QiniuConfig config) throws QiniuException;
}
