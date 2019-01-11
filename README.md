# PoolGP

## Build
```
./build
```

## Dependencies
```clojure
[org.clojure/tools.cli "0.4.1"]
[org.clojure/core.async "0.4.490"]
[org.clojure/data.json "0.2.6"]
```

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
          }
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
      "strategy" : "()"
    },
    "p2" : {
      "genetic" : true,
      "strategy" : "()"
    },
    "demo" : true               (optional)
  }
}
```

## License

Copyright © 2018 Jack Hay

Distributed under the Eclipse Public License version 1.0
