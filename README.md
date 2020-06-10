
# nonrep-generate-pdf

This is a placeholder README.md for a new repository

## iText DiTO evaluation

In order to run the DITO SDK
```
docker run -it --name dito-hmrc-eval -v ~/workspace/nonrep-generate-pdf/dito/config:/etc/opt/dito/shared -v ~/workspace/nonrep-generate-pdf/dito/work:/var/opt/dito -v ~/workspace/nonrep-generate-pdf/dito/log:/var/log/dito -e DITO_LICENSE_FILE=dito_trial_HMRC.xml -p 42:8080 itext/dito-sdk:1.3.6
```

In order to run the DITO Editor
```
docker run dito-editor:0.1
```

The editor can be downloaded separately from https://repo.itextsupport.com/webapp/#/artifacts/browse/tree/General/dito/com/itextpdf/dito/editor-server/1.3.6/editor-server-1.3.6.jar


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
