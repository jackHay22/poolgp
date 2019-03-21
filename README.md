# PoolGP

## Using PoolGP

### Building the project
```
./build
```
Optional: `-d TASK_DEFN` builds a docker image and pushes it to docker hub

### Project run options (NOTE: an option is required)
(Example: `java -jar poolgp-0.1.0-SNAPSHOT-standalone.jar -e example_evaluation_state.json`)
- `-d --demo PATH` Runs in demo mode given configuration
- `-e --eval PATH` Runs in server mode with specified task definition
- `-b --builder PATH` Opens editing mode and writes to file provided (must exist)
- `-n --new FILENAME` Creates a blank configuration file (with required fields) (this is meant to be subsequently edited)

### Project Dependencies
(Run `lein deps`)
```clojure
[org.clojure/tools.cli "0.4.1"]
[org.clojure/core.async "0.4.490"]
[org.clojure/data.json "0.2.6"]
[clojush.poolgp "3.17.1-1-SNAPSHOT"] ;Note: this is the poolgp fork of clojush and contains new functions
```

### Task Definition Structure
- Warning: if using `:interactive` as a player type, there should only be one analysis state
  (only one state is visible to demo)
```json
{
  "simulation": {
    "analysis" : [
      {
        "game" : {
          "table" : {
            "balls" : [
              {"x" : 100, "y" : 150, "id" : 1, "type" : "striped"},
              {"x" : 500, "y" : 285, "id" : "cue", "type" : "cue"}
            ]
          }
        }
      },
    ],
    "max-iterations" : 100000,  (optional)
    "watching" : 0,             (optional)
    "p1" : {
      "genetic" : true,
      "strategy" : "(integer_**)"
    },
    "p2" : {
      "genetic" : true,
      "strategy" : "(integer_+)"
    }
  },
  "eval-worker" : {
    "indiv-ingress" : 9999,
    "indiv-egress" : 8000,
    "opp-pool-req" : 8888,
    "engine-hostname" : "engine"
  }
}
```

## Server Mode

### "Packet" structure
Here is the structure for an individual being sent from Clojush to
evaluation instances:
```clojure
{:indiv indiv ;clojush.individual containing :program
 :cycle (int)}
```

### Creating a Docker Swarm
- Follow the [tutorial](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/) on creating a Docker swarm.
- Copy `docker/docker-compose.yml` to the master node of your swarm
- From the master node, run the following:
```bash
  docker stack deploy --compose-file docker-compose.yml poolgp
```

## Configuring Clojush to communicate with PoolGP workers
Here are the steps required for setting up Clojush for communication with PoolGP evaluation workers.

Add this dependency to your `project.clj` file: [![Clojars Project](https://img.shields.io/clojars/v/poolgp.distribute.svg)](https://clojars.org/poolgp.distribute).

When your engine is ready to evaluate the entire population, include the following code:
In your ns declaration: `(:require [poolgp.distribute :as poolgp])`

(Note: this should be in `clojush.src.pushgp.pushgp/compute-errors`)

```clojure
(Thread/sleep 10000)

(poolgp/start-dist-services {
  :incoming-port 8000
  :outgoing-port 9999
  :opp-pool-req-p 8888
  :host "eval"})

(poolgp/register-opponents (map deref pop-agents))

(dorun (map #((if use-single-thread swap! send)
             %1 poolgp/eval-indiv)
           pop-agents))

(when-not use-single-thread (apply await pop-agents)) ;; SYNCHRONIZE

(let [opps (map deref pop-agents)]
 (dorun (map #((if use-single-thread swap! send)
               %1 evaluate-individual (fn [i] (poolgp/merge-fitness i opps)) %2
                                               (assoc argmap :reuse-errors false))
             pop-agents
             rand-gens)))
```

### Setting up the eval swarm on AWS

![Swarm Diagram](https://raw.githubusercontent.com/jackHay22/poolgp/master/docs/ec2_swarm_diagram.png)

- Make sure you have an AWS account that can support charges incurred by running ec2 instances at high load.  (These costs add up)
- Create an ec2 keypair.  In `ec2_launcher`, rename the keypair argument with this keypair name.
- Download the keypair pem file for use during deployment.  
- Change the profile name in in `ec2_launcher` to your own (remove arg if using default profile)
- Determine the CIDR range of your default AWS VPC (or custom VPC)
- Determine the CIDR range to allow SSH access from (your IP)
- Execute: `ec2_launcher <keypair.pem> <ssh_cidr_block> <vpc_cidr_block>`
- Accept any SSH/SCP prompts
- This script will end by connecting you to the master node
- SSH into all nodes (including master) using public IP addresses or public DNS
- Run `./docker_installer` and accept any prompts
- From the master, execute `sudo docker swarm init --advertise-addr <instance_private_ip> --data-path-addr <instance_private_ip>`
- Then in all nodes, copy the outputted join command (run with sudo), and append `--advertise-addr <instance_private_ip> --data-path-addr <instance_private_ip>`
- make a directory called `logs`
- From master, execute `sudo docker stack deploy --compose-file docker-compose.yml poolgp`
- From there, list and inspect docker volumes to verify the location of log files

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
