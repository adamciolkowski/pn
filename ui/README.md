# Pan UI

## Run with engine

From the `engine` directory:

```sh
mvn -q compile exec:java -Dexec.mainClass=pan.http.Server
```

Open [http://localhost:8080](http://localhost:8080) (UI and API on one server).

For a separate static server (e.g. `python3 -m http.server 8765`), keep the engine running on port 8080 — the UI POSTs to `http://localhost:8080/api/evaluate`. Without the engine, evaluation stays empty.
