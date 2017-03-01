package org.akashihi.mdg.apitest.tests

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.BudgetFixture
import org.akashihi.mdg.apitest.fixtures.TransactionFixture
import spock.lang.Specification

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class BudgetEntrySpecification extends Specification {
    static BudgetFixture bFixture = new BudgetFixture();
    static TransactionFixture tFixture = new TransactionFixture();
    static def accounts;

    def setupSpec() {
        setupAPI();
        accounts = tFixture.prepareAccounts();
        bFixture.makeBudget(bFixture.marBudget);
    }

    def cleanupSpec() {
        bFixture.removeBudget("20170301")
    }

    def 'Budget entries are created for non-asset accounts during budget creation'() {
        given: 'Newly created budget'

        when: 'BudgetEntry list is requested'
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry")

        then: 'BudgetEntries for non-asset accounts hould be in the list'
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data.*.type", List.class).first(), equalTo("budgetentry"))
        assertThat(listBody.read("data.*.attributes.account_id", List.class), containsInAnyOrder(accounts.valueSet()))
    }

    def 'Budget entries are created in existing budget when new account is created'() {
        given: "Existing budget and new accounts"
        def newAccounts = tFixture.prepareAccounts()

        when: 'BudgetEntry list is requested'
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry")

        then: 'BudgetEntries for newly created accounts should be in the list'
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        assertThat(listBody.read("data", List.class).size(), is(not(0)))
        assertThat(listBody.read("data.*.type", List.class).first(), equalTo("budgetentry"))
        assertThat(listBody.read("data.*.attributes.account_id", List.class), containsInAnyOrder(newAccounts.valueSet()))
    }

    def 'Budget entries should be accessible by id'() {
        given: "List of budget entries"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entryId = listBody.read("data.*.id", List.class).first()

        when: "Entry is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry/{id}", entryId)

        then: "Entry should be returned"
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data.id"), equalTo(entryId))
        assertThat(body.read("data.type"), equalTo("budgetentry"))
    }

    def 'Budget entries should be changeable by id'() {
        given: "Some budget entry"
        def listResponse = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry")
        def listBody =  JsonPath.parse(listResponse.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())
        def entryId = listBody.read("data.*.id", List.class).first()

        when: "Entry is updated"
        def newEntry = [
                "data": [
                        "type"      : "budgetentry",
                        "attributes": [
                                "even_distribution" : true,
                                "expected_amount" : 9000,
                        ]
                ]
        ]
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(newEntry))
                .put("/budget/20170301/entry/{id}", entryId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.even_distribution", is(true))
                .body("data.attributes.expected_amount", is(9000))

        then: "New values should be returned"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get("/budget/20170301/entry/{id}", entryId)

        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data.attributes.expected_amount"), equalTo(9000))
        assertThat(body.read("data.attributes.even_distribution"), is(true))
    }
}
