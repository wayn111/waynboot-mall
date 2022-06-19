# 该镜像需要依赖的基础镜像
FROM adoptopenjdk:11-jre-openj9
WORKDIR /root/workspace
# 将当前目录下的jar包复制到docker容器的/目录下
ADD waynboot-admin-api/target/waynboot-admin-api-1.1.0.jar /opt/waynboot-mall/waynboot-admin-api-1.1.0.jar
# 运行过程中创建一个mall-tiny-docker-file.jar文件
RUN bash -c 'touch /opt/waynboot-mall/waynboot-admin-api-1.1.0.jar'
# 声明服务运行在8080端口
EXPOSE 81
# 指定docker容器启动时运行jar包
ENTRYPOINT ["sh", "-c", "exec java -jar -Xms812m -Xmx812m -Xss512k /opt/waynboot-mall/waynboot-admin-api-1.1.0.jar"]
# 指定维护者的名字
MAINTAINER wayn111
