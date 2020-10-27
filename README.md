# nonrep-generate-pdf

## PDF Generation Service

This API provides a mechanism whereby a calling service can specify a template to be populated with the matching JSON from the request body. These will be used to prepare PDF which will then be digitally signed and returned to the calling service.

## Table of Contents

*   [Endpoints](#endpoints)

## Endpoints <a name="endpoints"></a>

### Generate PDF

```
POST  /pdf-generator/template/trusts5mld/signed-pdf
```

| Responses    | Status    | Description |
| --------|---------|-------|
| Ok  | 200   | Successfully created and digitally signed PDF |
| Bad Request | 400   |  PDF not created, with some reason message |
| Unauthorised | 401   |  Unauthorised request - invalid X-API-Key |

#### Example
Request (POST): /pdf-generator/template/{templateId}/signed-pdf

Body:
``` json
{
	"submissionDate":"12-12-2020",
	"trustName":"HMRC Trust",
	"identifiers":{
		"utr":"2134514321",
		"urn":"XATRUST80000001"
	},
	"trustStartDate":"12-12-2020",
	"correspondence":{
		"address":{
			"line1":"1010 EASY ST",
			"line2":"OTTAWA",
			"line3":"ONTARIO",
			"line4":"ONTARIO",
			"postCode":"tf2 9yt",
			"country":"GB"
		},
		"welsh":true,
		"braille":true
	},
	"entities":{
		"leadTrustee":{
			"leadTrusteeIndividual":{
				"firstName":"Peter",
				"middleName":"Thomas",
				"lastName":"Paul",
				"dateOfBirth":"12-1500",
				"countryOfResidence":"AD"
			}
		},
		"trustees":[
			{
				"trusteeIndividual":{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1500",
					"nationality":"AD",
					"countryOfResidence":"AD"
				}
			},
			{
				"trusteeIndividual":{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1500",
					"nationality":"AD",
					"countryOfResidence":"AD"
				}
			},
			{
				"trusteeCompany":{
					"name":"ABC Ltd",
					"countryOfResidence":"AD"
				}
			}
		],
		"naturalPerson":[
			{
				"firstName":"Peter",
				"middleName":"Thomas",
				"lastName":"Paul",
				"dateOfBirth":"12-1800",
				"nationality":"AD",
				"countryOfResidence":"AD"
			},
			{
				"firstName":"Tom",
				"middleName":"Thomas",
				"lastName":"Paul",
				"dateOfBirth":"12-1900",
				"nationality":"AD",
				"countryOfResidence":"AD"
			}
		],
		"settlors":{
			"settlorIndividual":[
				{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1600",
					"nationality":"AD",
					"countryOfResidence":"AD"
				},
				{
					"firstName":"Christopher",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1700",
					"nationality":"AD",
					"countryOfResidence":"AD"
				}
			],
			"settlorCompany":[
				{
					"name":"ABC Ltd",
					"countryOfResidence":"AD"
				},
				{
					"name":"XYZ Ltd",
					"countryOfResidence":"AD"
				}
			]
		},
		"protectors":{
			"protectorIndividual":[
				{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-2000",
					"nationality":"AD",
					"countryOfResidence":"AD"
				},
				{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-2020",
					"nationality":"AD",
					"countryOfResidence":"AD"
				}
			],
			"protectorCompany":[
				{
					"name":"ABC Ltd",
					"countryOfResidence":"AD"
				},
				{
					"name":"XYZ Ltd",
					"countryOfResidence":"AD"
				}
			]
		},
		"deceased":{
			"firstName":"Peter",
			"middleName":"Thomas",
			"lastName":"Paul",
			"dateOfBirth":"12-1950",
			"nationality":"AD",
			"countryOfResidence":"AD"
		},
		"beneficiary":{
			"individual":[
				{
					"firstName":"Peter",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1999",
					"nationality":"AD",
					"countryOfResidence":"AD"
				},
				{
					"firstName":"Tom",
					"middleName":"Thomas",
					"lastName":"Paul",
					"dateOfBirth":"12-1897",
					"nationality":"AD",
					"countryOfResidence":"AD"
				}
			],
			"company":[
				{
					"name":"ABC Company Ltd",
					"countryOfResidence":"AD"
				},
				{
					"name":"XYZ Company Ltd",
					"countryOfResidence":"AD"
				}
			],
			"trust":[
				{
					"name":"ABC Trust Ltd",
					"countryOfResidence":"AD"
				},
				{
					"name":"XYZ Trust Ltd",
					"countryOfResidence":"AD"
				}
			],
			"charity":[
				{
					"name":"ABC Charity Ltd",
					"countryOfResidence":"AD"
				},
				{
					"name":"XYZ Charity Ltd",
					"countryOfResidence":"AD"
				}
			],
			"unidentified":[
				{
					"beneficiaryDescription":"Beneficiary Description 1"
				},
				{
					"beneficiaryDescription":"Beneficiary Description 2"
				}
			],
			"large":[
				{
					"beneficiaryDescription":"Description of your Beneficiaries as written in the Trust deed",
					"beneficiaryDescription1":"Description of your Beneficiaries as written in the Trust deed 1",
					"beneficiaryDescription2":"Description of your Beneficiaries as written in the Trust deed 2",
					"beneficiaryDescription3":"Description of your Beneficiaries as written in the Trust deed 3",
					"beneficiaryDescription4":"Description of your Beneficiaries as written in the Trust deed 4",
					"companyDetails":{
						"name":"ABC Large Ltd",
						"countryOfResidence":"AD"
					}
				},
				{
					"beneficiaryDescription":"Description of your Beneficiaries as written in the Trust deed",
					"beneficiaryDescription1":"Description of your Beneficiaries as written in the Trust deed 1",
					"beneficiaryDescription2":"Description of your Beneficiaries as written in the Trust deed 2",
					"beneficiaryDescription3":"Description of your Beneficiaries as written in the Trust deed 3",
					"beneficiaryDescription4":"Description of your Beneficiaries as written in the Trust deed 4",
					"companyDetails":{
						"name":"XYZ Large Ltd",
						"countryOfResidence":"GB"
					}
				}
			],
			"other":[
				{
					"otherBeneficiaryDescription":"Other Beneficiary Description 1",
					"countryOfResidence":"AD"
				},
				{
					"otherBeneficiaryDescription":"Other Beneficiary Description 2",
					"countryOfResidence":"AE"
				}
			]
		}
	}
}
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
