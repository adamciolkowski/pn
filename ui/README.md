# Pan UI

## Run with engine

From the `engine` directory:

```sh
mvn -q compile exec:java -Dexec.mainClass=pan.http.Server
```

Open [http://localhost:8080](http://localhost:8080). The Java server serves this UI and handles `POST /api/evaluate`.

If the engine is unreachable, the UI shows a local heuristic fallback.
