This is an experimental branch for Virtualbox Snapshot testing.


If you create a snapshot in a virtual machine this snapshot will be listed in the Jenkins node configuration at the Virtual Machine Name as <machineName>//<Snapshot>. It is also possible to create snapshot trees. In this case the Virtual Machine Name is build as <machineName>//<Snapshot>/<Snapshot2>.

When the virtualbox-plugin starts and stops the virtual machine the selected snapshot is restored. This allows you to setup a virtual machine in a defined statewith e.g. test data.

Note: An error will be caused, if the virtual machine is relaunched while it already runs. Therefore the virtual machine has to be stopped (maybe the plugin can do this in one of the next releases). To avoid this, set the Availability in the Jenkins node configuration to 'Take this slave on-line when in demand and off-line when idle', 'In demand delay' and 'Idle delay' to 1, and add a label which is also used for the build job definition. Jenkins will start the virtual machine when the build job is started and stops the virtual machine (the plugin will also restore the virtual machine).


Note: Snapshots are implemented for Virtualbox 4.0 and 4.1. Version 3.x isn't supported (at the moment).


----

Some 'git stuff' for this branch:


git clone https://github.com/jenkinsci/virtualbox-plugin.git
## or git pull

git branch -r
## shows remote branch origin/snap

git branch --track snap origin/snap
## tracks the (see also ################################################################################)

git checkout snap
git://github.com/jenkinsci/virtualbox-plugin.git

Wiki page at 

https://wiki.jenkins-ci.org/display/JENKINS/VirtualBox+Plugin
