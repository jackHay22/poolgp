# PoolGP

## Contents
TODO

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
[clojush "3.17.1"]
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
          },
          "push-inputs" : [ "balls", "cue", "pockets"] (optional)
        },
        "p1-analytics" : [
          "score", "scored_turns", "scratches", "advanced_balls"
        ],
        "p2-analytics" : [
          "score", "scored_turns", "scratches", "advanced_balls"
        ]
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
    "engine-hostname" : "poolgp-engine"
  }
}
```

## Server Mode

### "Packet" structure
Here is the structure for an individual being sent from Clojush to
evaluation instances:
```clojure
{:indiv indiv ;clojush.individual containing :program
 :cycle (int)
 :type :individual
 :eval-id (int)}
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

Add this dependency to your `project.clj` file: [![poolgp.distribute](https://img.shields.io/clojars/v/poolgp.distribute.svg)](https://clojars.org/poolgp.distribute).

When your engine is ready to evaluate the entire population, include the following code:
In your ns declaration: `(:require [poolgp.distribute :as poolgp])`

(Note: this should be in `clojush.src.pushgp.pushgp/compute-errors`)

```clojure
(poolgp/eval-indivs individuals-list
  {
    :incoming-port 8000
    :outgoing-port 9999
    :opp-pool-req-p 8888
    :host "eval"          ;If running nodes in a swarm (recommended) this will be the service name
    :accepted-return 1    ;percent total individuals required to be returned before stopping
  })
```
This function returns the set of individuals with computed fitness (given the task definition used to run the workers)

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
