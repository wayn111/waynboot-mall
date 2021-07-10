#!/usr/bin/env bash
# waynboot-mall项目-阿里云容器服务-docker方式部署脚本

app_name="waynboot_mall"
docker_redistry_prefix="registry.cn-shanghai.aliyuncs.com/wayn111/"
waynboot_mall=("waynboot-mobile-api:82" "waynboot-admin-api:81" "waynboot-message-consumer:85")

echo "${app_name}项目开始安装"
echo "------------------------------------------------------------------"

for var in "${waynboot_mall[@]}"; do
  echo "${var}:初始化安装开始"
  port=${var#*:}
  name=${var%:*}
  docker stop ${name}
  docker rm ${name}
  docker rmi "${docker_redistry_prefix}${name}"
  docker pull "${docker_redistry_prefix}${name}"
  docker run -d -p ${port}:${port} --net=host --name ${name} "${docker_redistry_prefix}${name}"
  echo "${var}:初始化安装结束"
  echo "------------------------------------------------------------------"
done

echo "${app_name}项目安装成功"
