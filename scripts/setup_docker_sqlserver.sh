# Linux Docker 安装 SQLServer 2019

#!/bin/bash

# 更新软件包列表和安装必要的依赖包
sudo apt-get update && sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 设置稳定的Docker仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 再次更新APT包索引并安装Docker引擎
sudo apt-get update && sudo apt-get install -y docker-ce docker-ce-cli containerd.io

# 验证docker是否安装正确
sudo docker run hello-world

# 拉取最新的SQL Server镜像
sudo docker pull mcr.microsoft.com/mssql/server:2019-latest

# 运行SQL Server容器，并设置为开机自启
sudo docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=Asdf1234' \
  -p 1433:1433 --name sqlserver \
  --mount source=sqlserverdata,target=/var/opt/mssql \
  -d mcr.microsoft.com/mssql/server:2019-latest

docker update --restart=always sqlserver

# 输出完成信息
docker ps
echo "Docker and SQL Server 2019 setup completed.