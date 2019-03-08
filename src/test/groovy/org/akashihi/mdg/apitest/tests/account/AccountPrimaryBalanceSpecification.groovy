package org.akashihi.mdg.apitest.tests.account

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class AccountPrimaryBalanceSpecification extends Specification {
    def account = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "expense",
                            "currency_id" : 978,
                            "name"        : "Rent"
                    ]
            ]
    ]

    def incomeAccount = [
            "data": [
                    "type"      : "account",
                    "attributes": [
                            "account_type": "income",
                            "currency_id" : 978,
                            "name"        : "Salary"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def "User creates new account"() {
        given: "Account in a non-default currency"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/account/"))
                .body("data.type", equalTo("account"))
                .extract().path("data.id")

        def incomeAccountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(incomeAccount))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/account/"))
                .body("data.type", equalTo("account"))
                .extract().path("data.id")

        when: "New operation on that account is made"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-04T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accountId,
                                                "amount"    : 1000
                                        ],
                                        [
                                                "account_id": incomeAccountId,
                                                "amount"    : -1000
                                        ]
                                ]
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        then: "Account appears on the accounts list"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Accounts)
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data"), not(empty()))
        assertThat(body.read("data[?(@.id == ${accountId})].type", List.class).first(), equalTo("account"))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.balance", List.class).first(), equalTo(1000))
        assertThat(body.read("data[?(@.id == ${accountId})].attributes.primary_balance", List.class).first(), equalTo(1190))
    }

    def "User checks account data"() {
        given: "Account in a non-default currency"
        def accountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(account))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/account/"))
                .body("data.type", equalTo("account"))
                .extract().path("data.id")

        def incomeAccountId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(incomeAccount))
                .post(API.Accounts).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/account/"))
                .body("data.type", equalTo("account"))
                .extract().path("data.id")

        when: "New operation on that account is made"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2017-02-04T16:45:36",
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accountId,
                                                "amount"    : 1000
                                        ],
                                        [
                                                "account_id": incomeAccountId,
                                                "amount"    : -1000
                                        ]
                                ]
                        ]
                ]
        ]

        given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(transaction))
                .post(API.Transactions).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().path("data.id")

        then: "Account object should be returned"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Account, accountId)
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("account"))
                .body("data.attributes.balance", equalTo(1000))
                .body("data.attributes.primary_balance", equalTo(1190))
    }
}
