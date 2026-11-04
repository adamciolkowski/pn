# Pan UI

## Run with engine (recommended)

From the `engine` directory:

```sh
mvn -q compile exec:java -Dexec.mainClass=pan.http.Server
```

Open [http://localhost:8080](http://localhost:8080). The Java server serves this UI and handles `POST /api/evaluate`.

## Static-only (fallback heuristic)

```sh
python3 -m http.server 8765
```

Open [http://localhost:8765](http://localhost:8765) and set **Evaluate URL** to `http://localhost:8080/api/evaluate` while the engine server is running.
