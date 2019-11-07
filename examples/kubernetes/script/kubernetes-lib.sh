#!/bin/bash
#

function prepare() {
    if !(which minikube | grep -q 'minikube'); then
        echo "Please install minikube..."
        exit 1
    elif (minikube status | grep 'minikube' | grep -q 'Stopped'); then
        echo "Please start minikube..."
        exit 1
    fi
    echo "prepare minikube..."
    minikube addons enable ingress
    eval $(minikube docker-env)
}

function cleanup {
    prepare
    echo "clean up..."
    rm -f ${EVENT_LISTENER_RESOURCES}/client.crt
    rm -f ${EVENT_LISTENER_RESOURCES}/client.key
    kubectl delete ing,services,pods,deployments -l 'partition=poc' --wait=true

    NOT_RUNNING_CONTAINER_IDS=( `docker ps --filter "status=exited" -q` )
    if [[ ${#NOT_RUNNING_CONTAINER_IDS[@]} -gt 0 ]]; then
        docker rm ${NOT_RUNNING_CONTAINER_IDS}
    fi

    EVENT_LISTENER_IMAGE_ID=( `docker images --filter "reference=com.airlock.waf.kubernetes/poc-kubernetes-event-listener:*" -q` )
    if [[ ${#EVENT_LISTENER_IMAGE_ID[@]} -gt 0 ]]; then
        docker rmi ${EVENT_LISTENER_IMAGE_ID}
    fi

    ECHO_SEVER_IMAGE_ID=( `docker images --filter "reference=com.airlock.waf/echoserver:*" -q` )
    if [[ ${#ECHO_SEVER_IMAGE_ID[@]} -gt 0 ]]; then
        docker rmi ${ECHO_SEVER_IMAGE_ID}
    fi

    DANGLING_IMAGE_IDS=( `docker images --filter "dangling=true" -q` )
    if [[ ${#DANGLING_IMAGE_IDS[@]} -gt 0 ]]; then
        docker rmi ${DANGLING_IMAGE_IDS}
    fi
}

function build() {
    prepare
    echo "build event listener..."
    cp ~/.minikube/client.crt ${EVENT_LISTENER_RESOURCES}/client.crt
    cp ~/.minikube/client.key ${EVENT_LISTENER_RESOURCES}/client.key
    cd ${ROOT_PROJECT}
    ./gradlew clean build docker -q
}

function deployEventListener() {
    prepare
    echo "deploy event listener to kubernetes node..."
    kubectl create -f ${KUBERNETES_CONFIGS}/ingress-event-listener-deployment.yaml
}

function deployBackEndApplication() {
    prepare
    echo "deploy demo back-end application..."
    kubectl create -f ${KUBERNETES_CONFIGS}/echo-server-deployment.yaml
    kubectl create -f ${KUBERNETES_CONFIGS}/echo-server-service.yaml
}

function configureIngress() {
    prepare
    echo "configure ingress..."
    kubectl create -f ${KUBERNETES_CONFIGS}/echo-server-ingress.yaml
    POD_NAME=`kubectl get pods -l 'app=k8s-event' -o jsonpath='{.items[*].metadata.name}'`
}

function showEventListenerLogs() {
    prepare
    echo "show logs of event listener..."
    kubectl get pods -l 'app=k8s-event' -o jsonpath='{.items[*].metadata.name}' | xargs kubectl logs
}
