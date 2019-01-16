# PoolGP

## Build
```
./build
```
Optional: -r specifies a release and -s starts a docker container

## Dependencies
```clojure
[org.clojure/tools.cli "0.4.1"]
[org.clojure/core.async "0.4.490"]
[org.clojure/data.json "0.2.6"]
```

## Instruction Definitions
Instructs are defined here: poolgp.simulation.players.push.instructions

## Task Definition Structure
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

        ],
        "p2-analytics" : [

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
    },
    "demo" : true               (optional)
  }
}
```

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
