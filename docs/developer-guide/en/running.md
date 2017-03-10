# Running

Kapua can be run in a few differnt ways. See th following chapters for
more information about how to start and run Kapua.

{% hint style='info' %}
Most of the following descriptions focus on a developer centric
setup and not on a full blown production setup.
{% endhint %}

## Docker containers

Running Kapua on local docker containers is as easy as:

1. Install docker
1. Run `mvn -f assembly -Pdocker` once
1. Run the docker images you want to start, see [assembly/README.md](https://github.com/eclipse/kapua/blob/develop/assembly/README.md) 

## OpenShift

OpenShift is a PaaS (Platform As A Service) platform based on Kubernetes. Kapua support deployments into [OpenShift Origin](https://www.openshift.org),
which is an open source community project. Origin sources, can be found [here](https://www.openshift.org/). We support Kapua on OpenShift Origin **1.4.1**.

For running Kapua on an OpenShift you need to
have OpenShift cluster installed and started in a first place. You can install it by yourself or rely on the script we provides:

    sudo kapua/dev-tools/src/main/openshift/openshift-start.sh
    
If you are running your OpenShift cluster for a first time, execute the following initialized script as well:

    kapua/dev-tools/src/main/openshift/openshift-initialize.sh
    
Initialization script is responsible for logging you into a cluster and creating new OpenShift project for Kapua.

If for some reasons, you cannot start your cluster, try to execute the startup script with option `DOCKERIZED=FALSE`:

    sudo DOCKERIZED=FALSE kapua/dev-tools/src/main/openshift/openshift-start.sh
    
Option `DOCKERIZED=FALSE` tells startup script to use standard binary installation of OpenShift Origin instead of Docker-based `oc cluster up` command.

## Vagrant

Kapua can also be run with Vagrant.

### Installing Vagrant

Before Vargant can be used to run Kapua it needs to be installed. This is different on each distribution.

#### Installing Vagrant on Fedora 25

{% hint style='info' %}
Do not use Vagrant from Fedora 25. Kapua requires to use the VirtualBox provide from Vargant and cannot run
with the `libvirt` provider which Fedora uses. So it is necessary to install Virtualbox and Vagrant from different locations.
{% endhint %} 

Run the following commands in order to install Vagrant (all as `root`):

    dnf install kernel-devel
    
    wget https://www.virtualbox.org/download/oracle_vbox.asc
    rpm --import oracle_vbox.asc
    
    dnf install http://download.virtualbox.org/virtualbox/5.1.14/VirtualBox-5.1-5.1.14_112924_fedora25-1.x86_64.rpm
    dnf install https://releases.hashicorp.com/vagrant/1.9.2/vagrant_1.9.2_x86_64.rpm

{% hint style='tip' %}
There may by more up to date versions of the binaries. You should check and install more recent versions, if possible.
{% endhint %}

{% hint style='danger' %}
By manually installing RPMs you won't receive updates for those packages. You will need to manually check for security updates and bug fixes.
{% endhint %}

### Running Kapua

After Vagrant is installed you can run Kapua by running:

    cd dev-tools/src/main/vagrant
    sudo ./start.sh base-box
