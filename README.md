# nonrep-generate-pdf

## PDF Generation Service

The PDF Generation service is hosted within the Non Repudiation Store infrastructure and is available to backend MDTP microservices via Private Link.

The detailed API specification is documented in [Confluence](https://confluence.tools.tax.service.gov.uk/x/gwDXD) with an abbreviated version below

### Endpoints

*   [Version](#version)
*   [Generate PDF](#generate-pdf)
*   [Ping](#ping)

#### Version <a name="version"></a>

This endpoint returns the deployed version designation of this service 
```
GET /generate-pdf/version
```
Sample response:
```
{
    "version": "20201118T150059"
}
```

#### Generate PDF <a name="generate-pdf"></a>

This endpoint provides a mechanism whereby a calling service can specify a template to be populated with the matching JSON from the request body. These will be used to prepare PDF which will then be digitally signed and returned to the calling service

```
POST /generate-pdf/template/{template-id}/signed-pdf
```

An example template-id is `trusts-5mld-1-0-0` which corresponds to the below configuration:

```    
{
    template-id = "trusts-5mld-1-0-0"
    json-schema = "API#1584_Response_Schema-v1.0.0.json"
    pdf-template = "trusts-5mld.dito"
    signing-profile = "pades-t"
    api-key = "<...>"
}
```

The request body should be compliant with the schema associated with the selected `{template-id}`.
An example request based on the [API#1584_Response_Schema-v1.0.0.json](src/main/resources/API%231584_Response_Schema-v1.0.0.json) schema referenced in the above configuration is [1584_control_1.0.0.json](src/test/resources/1584_control_1.0.0.json)

| Responses    | Status    | Description |
| --------|---------|-------|
| Ok  | 200   | Successfully created and digitally signed PDF |
| Bad Request | 400   |  PDF not created, with some reason message |
| Unauthorised | 401   |  Unauthorised request - invalid X-API-Key |

#### Ping <a name="ping"></a>
This endpoint can be used as a lightweight health check for the availability of the service
```
GET /generate-pdf/ping
```
Returns "pong"

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
