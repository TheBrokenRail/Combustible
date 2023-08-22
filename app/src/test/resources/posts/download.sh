#!/bin/sh

set -e

LIMIT="$(grep -o 'ELEMENTS_PER_PAGE = [0-9]*' ../../../main/java/com/thebrokenrail/combustible/util/Util.java | cut -d' ' -f3)"
POST_ID="$1"

# Create Directory
rm -rf "${POST_ID}"
mkdir "${POST_ID}"

# Download Page
download_page() {
    PAGE="$1"
    FILE="${POST_ID}/${PAGE}.json"
    wget -O "${FILE}" "https://lemmy.world/api/v3/comment/list?type_=All&post_id=${POST_ID}&limit=${LIMIT}&page=${PAGE}"
    # Count Comments
    COUNT="$(jq '.comments | length' "${FILE}")"
    if [ "${COUNT}" = "0" ]; then
        # Finish
        rm -f "${FILE}"
        exit 0
    fi
}

# Download All Pages
I=1
while true; do
    download_page "${I}"
    I=$((I+1))
done
