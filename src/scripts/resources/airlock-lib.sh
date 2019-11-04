#!/bin/bash
#

function abortOnError() {
	res=$?
	msg=$1
	if [[ ${res} -ne 0 ]]; then
		echo "ERROR: $msg -> abort"
		exit $res
	fi
}

function createWAFConfiguration() {

    TMP_DIR=`mktemp -d`
    CONFIG_ZIP_PATH="${TMP_DIR}/${CONFIG_ZIP_FILE_NAME}"
    KUBERNETES_IP=$1

    trap "rm -rf ${TMP_DIR}" exit

    set -e
    sed "s/\${MY_KUBERNETES_IP}/${KUBERNETES_IP}/g" src/waf/resources/template_alec_full.xml > ${TMP_DIR}/alec_full.xml
    cp src/waf/resources/certificate_revocation_lists.zip ${TMP_DIR}
    cd ${TMP_DIR}
    zip ${CONFIG_ZIP_FILE_NAME} -q -r .
    cd ${WORKSPACE}
}

function cleanUpHistory() {
    CURL="curl --insecure --silent"
    JWT_TOKEN=`cat ${JWT_TOKEN_FILE}`

    echo "create session..."
    rm -f ${COOKIE}
    ${CURL} "https://${AIRLOCK}/airlock/rest/session/create" \
        -X POST \
        -H "Authorization: Bearer ${JWT_TOKEN}" \
        -H 'Accept: application/json' \
        -c ${COOKIE}
    abortOnError

    echo "collect all activated configurations..."
    CONFIG_IDS=`${CURL} "https://${AIRLOCK}/airlock/rest/configuration/configurations" \
        -X GET \
        -L -b ${COOKIE} \
        -H 'Accept: application/json' \
        | jq '.data[] | select(.attributes.configType!="INITIAL") | .id'`
    abortOnError

    echo "load current active configuration..."
    ${CURL} "https://${AIRLOCK}/airlock/rest/configuration/configurations/load-active" \
        -X POST \
        -H 'Content-Type: application/json' \
        -L -b ${COOKIE} \
        -H 'Accept: application/json'
    abortOnError

    echo "import base configuration..."
    ${CURL} "https://${AIRLOCK}/airlock/rest/configuration/configurations/import" \
        -X PUT \
        -H 'Content-Type: application/zip' \
        -L -b ${COOKIE} \
        -H 'Accept: application/json' \
        --data-binary "@${CONFIG_ZIP_PATH}"
    abortOnError

    echo "activate configuration..."
    STATUS_CODE=`${CURL} "https://${AIRLOCK}/airlock/rest/configuration/configurations/activate" \
        -w "%{http_code}" \
        -X POST \
        -H 'Content-Type: application/json' \
        -L -b ${COOKIE} \
        -H 'Accept: application/json' \
        -d "{ \"comment\" : \"${ACTIVATION_COMMENT}\" }"`
    abortOnError "activate configuration"
    if [[ ${STATUS_CODE} -ne 200 ]]; then
        echo "ERROR: An error occur during activation. HTTP Status code ${STATUS_CODE}"
        exit 1
    fi

    for CONFIG_ID in ${CONFIG_IDS}
    do
        ID=`echo ${CONFIG_ID} | sed 's/"//g'`
        echo "delete legacy configuration with ID ${ID}"
        ${CURL} "https://${AIRLOCK}/airlock/rest/configuration/configurations/${ID}" \
            -X DELETE \
            -H 'Content-Type: application/json' \
            -L -b ${COOKIE} \
            -H 'Accept: application/json'
    done

    echo "terminated session..."
    ${CURL} "https://${AIRLOCK}/airlock/rest/session/terminate" \
        -X POST \
        -H 'Content-Type: application/json' \
        -L -b ${COOKIE}  \
        -H 'Accept: application/json'
}
