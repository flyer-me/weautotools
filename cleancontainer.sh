
# 输入验证：确保 CONTAINERID 只包含合法字符
#if ! [[ $CONTAINERID =~ ^[a-zA-Z0-9_-]+$ ]]; then
#    echo "Error: Invalid container ID. Only alphanumeric characters, underscores, and hyphens are allowed."
#    exit 1
#fi

# 检查容器是否存在
#if ! docker ps -a --format "{{.ID}}" | grep -q $CONTAINERID; then
#    echo "Warn: Container $CONTAINERID does not exist."
#    exit 0
#fi

# 停止容器
echo "Stopping container $CONTAINERID..."
if ! docker stop "$CONTAINERID"; then
    echo "Error: Failed to stop container $CONTAINERID."
    exit 1
fi

# 删除容器
#echo "Removing container $CONTAINERID..."
#if ! docker rm "$CONTAINERID"; then
#    echo "Warn: Failed to remove container $CONTAINERID."
#    exit 0
#fi

echo "success."