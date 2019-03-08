package org.akashihi.mdg.apitest.tests.report

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.TotalsReportFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class TotalsReportSpecification extends Specification {
    TotalsReportFixture f = new TotalsReportFixture()

    def setupSpec() {
        setupAPI()
    }

    def 'Totals report should follow balances'() {
        given: "Several accounts"
        def accounts = f.prepareAccounts()

        when: "New transaction on those accounts is submitted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : '2017-02-05T16:45:36',
                                "comment"   : "Test transaction",
                                "tags"      : ["test", "transaction"],
                                "operations": [
                                        [
                                                "account_id": accounts["income"],
                                                "amount"    : -388
                                        ],
                                        [
                                                "account_id": accounts["usdAsset"],
                                                "amount"    : 150
                                        ],
                                        [
                                                "account_id": accounts["asset"],
                                                "rate"      : 1.19,
                                                "amount"    : 200
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

        then: "Balance on accounts is changed"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/report/totals")

        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data.attributes.value[?(@.asset_type == 'broker')].primary_balance", List.class).first(), equalTo(388))
        assertThat(body.read("data.attributes.value[?(@.asset_type == 'broker')].totals[?(@.currency_id == 840)].balance", List.class).first(), equalTo(150))
        assertThat(body.read("data.attributes.value[?(@.asset_type == 'broker')].totals[?(@.currency_id == 978)].balance", List.class).first(), equalTo(200))
    }
}
