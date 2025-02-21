#!/bin/bash

# 容器 ID 由外部传入
CONTAINERID=$1
RUNCOMMAND=$2

# 初始化结果变量
RESULTS=()

# 在容器内运行判题逻辑
docker exec "$CONTAINERID" /bin/sh -c "
    cd /app/ || exit 1

    for input_file in *.input; do
        case_id=\${input_file%.input}
        expect=\$case_id.output
        actual=\$case_id.actual
        error=\$case_id.error

        # 运行程序并捕获输出
        $RUNCOMMAND < \$input_file > \$actual 2> \$error
        code=\$?

        # 判题逻辑
        if [ \$code -eq 0 ]; then
            if diff -qZ \$actual \$expect > /dev/null; then
                status='\"AC\"'
            else
                status='\"WA\"'
            fi
        elif [ \$code -eq 124 ]; then
            status='\"TLE\"'
        else
            status='\"RE\"'
        fi

        # 构建 JSON 结果字符串
        RESULTS=\"\$RESULTS {\\\"Case_id\\\": \\\"\$case_id\\\", \\\"Status\\\": \$status, \\\"Code\\\": \\\"\$code\\\", \\\"Actual\\\": \\\"\$(cat \$actual | sed 's/\"/\\\\\"/g')\\\", \\\"Expect\\\": \\\"\$(cat \$expect | sed 's/\"/\\\\\"/g')\\\", \\\"Error\\\": \\\"\$(cat \$error | sed 's/\"/\\\\\"/g')\\\"},\"
    done

    FINAL_RESULT=\"{\\\"Results\\\":[\${RESULTS%,}]}\"
    echo \"\$FINAL_RESULT\"
"