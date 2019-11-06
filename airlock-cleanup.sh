#!/bin/bash
#

. src/scripts/resources/airlock-lib.sh

AIRLOCK="192.168.99.50"
BACKEND=minikube

usage()
{
	echo "Usage: $0 [--host {AIRLOCK_WAF_IP}] [--minikube|--minishift]"
	echo "   eg: $0 --host ${AIRLOCK} --minikube"
	echo 
	echo "  --waf        WAF host. DEFAULT:${AIRLOCK}"
	echo "  --minikube   Use minikube backend (DEFAULT)"
	echo "  --minishift  Use minishift backend"
	echo 
	exit 1
}


WORKSPACE=`pwd`
JWT_TOKEN_FILE="${WORKSPACE}/event-listener/src/main/resources/airlock-waf-jwt.token"
CONFIG_ZIP_FILE_NAME="airlock_waf_7_1_base_config.zip"
ACTIVATION_COMMENT="Kubernetes Setup"
COOKIE="/tmp/airlock.cookie"

if [[ ! -f ${JWT_TOKEN_FILE} ]]; then
    echo "Airlock WAF Token File is missing: ${JWT_TOKEN_FILE}"
    exit 1
fi


while [[ $# -gt 0 ]]; do
	case "${1}" in
		--waf)
			shift 1
			AIRLOCK=$1
			;;
		--minikube)
			BACKEND=minikube
			;;
		--minishift)
			BACKEND=minishift
			;;
		*)
			usage
			exit 1
			;;
	esac
	shift 1
done

if [ -z "${AIRLOCK}" ]; then
    echo "Airlock WAF host ist not defined"
    exit 1
fi

KUBERNETES_IP="$(${BACKEND} ip)"
if [ -z "${KUBERNETES_IP}" ]; then
    echo "Could not get ${BACKEND} IP"
    exit 1
fi

echo "using Airlock WAF host: ${AIRLOCK}"
echo "with '${BACKEND}' backend: ${KUBERNETES_IP}"

createWAFConfiguration "${KUBERNETES_IP}"
cleanUpHistory

