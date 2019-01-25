# PoolGP

## Build
```
./build
```
Optional: `-r VERSION` specifies a release and `-s TASK_DEFN` starts a docker container

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

## Issues/TODO
### Bugs
- [ ] Balls sometimes still seem to be "sticky" and orbit each other briefly (especially on breaks)
- [ ] Editor bug: placing balls into collisions yields a nil value in a vector operation
- [ ] Ball has a tendency to stick to the wall
- [ ] Cue pockets count as points

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
