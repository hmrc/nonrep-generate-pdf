## iText DITO Editor

Run the following command to create the docker image:

```
docker build -t dito-editor:0.1 .
```

Run the image:
```
docker run -p 9210:8080 dito-editor:0.1
```

Access the Editor UI on http://localhost:9210
