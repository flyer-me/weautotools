if [ "$#" -ne 2 ]; then
    echo "Usage: $0 DIRECTORY IMAGE"
    exit 1
fi

DIRECTORY="$1"
IMAGE="$2"

docker run --rm -v $DIRECTORY:/app -w /app -ti --memory=512m --cpus=2 --security-opt=no-new-privileges:true --name=CW_${DIRECTORY: -6} -d $IMAGE tail -f /dev/null

# ./startcontainer.sh test123456 python