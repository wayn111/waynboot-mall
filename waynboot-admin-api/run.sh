#!/usr/bin/env bash
# 定义应用组名
group_name='waynboot-mall'
# 定义应用名称
app_name='waynboot-admin-api'
app_port=81
# 定义应用版本
app_version='1.1.0'
# 定义应用环境
profile_active='dev'
# coding docker host
coding_docker_host="waynaqua-docker.pkg.coding.net/wayn"
echo '----copy jar----'
docker stop ${app_name}
echo '----stop container----'
docker rm ${app_name}
echo '----rm container----'
docker rmi ${coding_docker_host}/${group_name}/${app_name}:${app_version}
echo '----rm image----'
# 获取docker镜像
docker pull ${coding_docker_host}/${group_name}/${app_name}:${app_version}
echo '----pull image----'
docker run -p ${app_port}:${app_port} --name ${app_name} \
  --link mysql:db \
  -e 'spring.profiles.active'=${profile_active} \
  -e TZ="Asia/Shanghai" \
  -v /etc/localtime:/etc/localtime \
  -v /mydata/app/${app_name}/logs:/var/logs \
  -d ${group_name}/${app_name}:${app_version}
echo '----start container----'
