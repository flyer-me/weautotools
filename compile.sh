if [ "$#" -ne 2 ]; then
    echo "Usage: $0 DIRECTORY IMAGE"
    exit 1
fi

CONTAINERID="$1"
COMPILECOMMAND="$2"

docker exec $CONTAINERID sh -c "$COMPILECOMMAND"
EXIT_CODE=$?
# 根据退出状态码判断编译结果
if [ "$EXIT_CODE" -eq 0 ]; then
    echo "Compilation successful."
else
    echo "Compilation failed with exit code $EXIT_CODE."
fi

exit $EXIT_CODE