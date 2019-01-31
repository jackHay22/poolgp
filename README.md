# PoolGP

## Build
```
./build
```
Optional: `-r VERSION` specifies a release and `-d TASK_DEFN` builds a docker image

## Options (an option is required)
- `-d --demo PATH` Runs in demo mode given configuration
- `-e --eval PATH` Runs in server mode with specified task definition
- `-b --builder PATH` Opens editing mode and writes to file provided (must exist)
- `-n --new FILENAME` Creates a blank configuration file (with required fields) (this is meant to be subsequently edited)

## Dependencies
```clojure
[org.clojure/tools.cli "0.4.1"]
[org.clojure/core.async "0.4.490"]
[org.clojure/data.json "0.2.6"]
```

## Instruction Definitions
- Instructions are defined here: `poolgp.simulation.players.push.instructions`
- Analytics are defined here: `poolgp.simulation.analysis.definitions`

## Task Definition Structure
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
          "max-push-iterations" : 1000, (optional)
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
    "port": 9000,               (optional)
    "watching" : 0,             (optional)
    "p1" : {
      "genetic" : true,
      "strategy" : "(integer_**)"
    },
    "p2" : {
      "genetic" : true,
      "strategy" : "(integer_+)"
    }
  }
}
```

## Individual Evaluation

Here is the structure for an individual being sent from Clojush to
evaluation instances:
```clojure
{
  :cycle 1       ;This is the current cycle of the system
  :strategy "()" ;This is the push code for the individual
  :eval-id 2309438724 ;This is the individual's evaluation id
  :type :opponent/:individual  ;Individuals marked as :opponent will be used in games to test
  ;individuals marked as :individual.  Only :individuals are given a fitness and returned to the gp engine
  ;Note: opponents should be sent first and then individuals afterwards so that the system has time to load them
}
```


Here is the structure that evaluation instances return:
(TODO)

## Server Mode
Load testing:
I use the following code to test poolgp under load.
```bash

send_traffic() {
  echo "Testing server $1 on port $2"
  echo "Sending opponents..."
  for i in `seq 1 1000`;
    do
      echo "{:strategy '(integer_mult) :eval-id $i :cycle 0 :type :opponent}" | nc $1 $2
    done
  echo "Sending individuals..."
  for i in `seq 1 1000`;
    do
      echo "{:strategy '(integer_mult) :eval-id $i :cycle 0 :type :individual}" | nc $1 $2
    done
}

while [[ -n "$2" ]]; do
  send_traffic $1 $2 &
  shift 2
done
```

## Issues/TODO
### Bugs
- [ ] Balls sometimes still seem to be "sticky" and orbit each other briefly (especially on breaks)
- [ ] Editor bug: placing balls into collisions yields a nil value in a vector operation
- [ ] Ball has a tendency to stick to the wall
- [ ] Cue pockets count as points

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
