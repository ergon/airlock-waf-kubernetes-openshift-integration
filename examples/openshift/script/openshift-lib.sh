#!/bin/bash
#

function prepare() {
    if !(which minishift | grep -q 'minishift'); then
        echo "Please install minishift..."
        exit 1
    elif (minishift status | grep 'minishift' | grep -q 'Stopped'); then
        echo "Please start minishift..."
        exit 1
    fi

    if [[ `minishift addons list | grep registry-route | grep enabled | wc -l` -ne 1 ]]; then
        echo "prepare minishift..."
        minishift addons install --defaults
        minishift addons enable registry-route
    fi

    eval $(minishift oc-env)
    oc login --username=system:admin
    oc adm policy --as system:admin add-cluster-role-to-user cluster-admin minikube
    oc adm policy --as system:admin add-cluster-role-to-user admin minikube
    oc adm policy --as system:admin add-cluster-role-to-user cluster-admin system:serviceaccount:myproject:default
    oc adm policy --as system:admin add-cluster-role-to-user admin system:serviceaccount:myproject:default

    oc login --username=minikube --password=minikube
    oc project myproject

    eval $(minishift docker-env)
}

function cleanup() {
    prepare
    echo "clean up..."
    rm -f src/main/resources/client.crt
    rm -f src/main/resources/client.key

    oc delete deployments,routes,services,pods -l 'partition=poc' --now=true --cascade=true
    until [[ -z `oc get routes,services,pods,deployments -l 'partition=poc' -o jsonpath='{.items[*].metadata.name}'` ]];
    do
        echo "Wait until all openshift objects has been removed..."
        sleep 5
    done

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
    cp ~/.minikube/client.crt src/main/resources/client.crt
    cp ~/.minikube/client.key src/main/resources/client.key
    ./gradlew clean build docker -q
}

function deployEventListener() {
    prepare
    echo "deploy event listener to minishift node..."
    oc create -f src/openshift/resources/ingress-event-listener-deployment.yaml
}

function deployBackEndApplication() {
    prepare
    echo "deploy demo back-end application..."
    docker build -q -f src/openshift/resources/Dockerfile.echoserver -t com.airlock.waf/echoserver:1.0-SNAPSHOT .
    oc create -f src/openshift/resources/echo-server-deployment.yaml
    oc create -f src/openshift/resources/echo-server-service.yaml
}

function configureRoute() {
    prepare
    echo "configure route..."
    oc create -f src/openshift/resources/echo-server-route.yaml
    POD_NAME=`kubectl get pods -l 'app=k8s-event' -o jsonpath='{.items[*].metadata.name}'`
}

function showEventListenerLogs() {
    prepare
    echo "show logs of event listener..."
    oc get pods -l 'app=k8s-event' -o jsonpath='{.items[*].metadata.name}' | xargs kubectl logs
}
