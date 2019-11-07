# Tutorial
The following sections describe how to test the setup in a local environment. In case of Kubernetes 
it requires minikube and the ingress addon and in case of OpenShift it requires minishift. As back-end application an
HTTP echo server will be deployed, which mirrors all HTTP client requests.

## Parameterization
The demo application uses two Airlock metadata annotations:
* waf.airlock.com/mapping.name: Specifies the name of the mapping created on Airlock WAF
* waf.airlock.com/mapping.template.id: Contains the ID of the mapping template used

The referenced mapping template is stored in the ConfigMap. The demo application retrieves the mapping template from the ConfigMap and imports it to Airlock WAF prior to creation of the mapping.

## Environment
* Linux environment
* [minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) incl. kubectl
* [minishift](https://docs.okd.io/latest/minishift/getting-started/installing.html) incl. oc
* curl

## Airlock WAF
1. Start minikube or minishift
1. Copy the Airlock WAF JWT token (API Key) in `event-listener/src/main/resources/airlock-waf-jwt.token`
1. The default IP of the WAF host is '192.168.99.50'. To change this edit `src/main/resources/application.properties` accordingly
1. Set environment variable 'AIRLOCK_WAF_IP' in the shell which will be used for the tasks below: `AIRLOCK_WAF_IP="192.168.99.50"`

## Kubernetes
1. Clean Up Airlock WAF configuration history and import initial configuration: `./airlock-cleanup.sh --waf "${AIRLOCK_WAF_IP}" --minikube`
1. Delete previous setup: `./kubernetes-setup.sh --clean`
1. Build and deploy the event listener application: `./kubernetes-setup.sh --add-event-listener`
1. Deploy mirror back-end application: `./kubernetes-setup.sh --add-backend`
1. Enable ingress to mirror application: `./kubernetes-setup.sh --add-ingress`
1. Verify that the event listener application collects the ingress event: `./kubernetes-setup.sh --show-event-listener-logs`
1. HTTP request over the Airlock WAF: `curl -vk "${AIRLOCK_WAF_IP}:8080" -H "Host: myminikube.info"`

## OpenShift
1. Clean Up Airlock WAF configuration history and import initial configuration: `./airlock-cleanup.sh --waf "${AIRLOCK_WAF_IP}" --minishift`
1. Delete previous setup: `./openshift-setup.sh --clean`
1. Build and deploy the event listener application: `./openshift-setup.sh --add-event-listener`
1. Deploy mirror back-end application: `./openshift-setup.sh --add-backend`
1. Enable ingress to mirror application: `./openshift-setup.sh --add-route`
1. Verify that the event listener application collect the ingress event: `./openshift-setup.sh --show-event-listener-logs`
1. HTTP request over the Airlock WAF: `curl -vk "${AIRLOCK_WAF_IP}:8080" -H "Host: myminishift.info"`