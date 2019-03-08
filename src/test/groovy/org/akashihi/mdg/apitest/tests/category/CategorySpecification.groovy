package org.akashihi.mdg.apitest.tests.category

import com.jayway.jsonpath.JsonPath
import groovy.json.JsonOutput
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class CategorySpecification extends Specification {
    def category = [
            "data": [
                    "type"      : "category",
                    "attributes": [
                            "name": "Bonuses",
                            "priority" : 1,
                            "account_type" : "expense"
                    ]
            ]
    ]

    def setupSpec() {
        setupAPI()
    }

    def 'User creates new category'() {
        given: 'A new category'


        when: 'Category is submitted to the system'
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post(API.Categories).
                then()
                .assertThat().statusCode(201)
                .assertThat().contentType("application/vnd.mdg+json")
                .assertThat().header("Location", containsString("/api/category/"))
                .body("data.type", equalTo("category"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.priority", equalTo(1))
                .body("data.attributes.name", equalTo("Bonuses"))
                .extract().path("data.id")

        then: 'Category appears on the category list'
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Categories)
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data"), not(empty()))
        assertThat(body.read("data[?(@.id == ${categoryId})].type", List.class).first(), equalTo("category"))
        assertThat(body.read("data[?(@.id == ${categoryId})].attributes.account_type", List.class).first(), equalTo("expense"))
        assertThat(body.read("data[?(@.id == ${categoryId})].attributes.priority", List.class).first(), equalTo(1))
        assertThat(body.read("data[?(@.id == ${categoryId})].attributes.name", List.class).first(), equalTo("Bonuses"))
    }

    def 'User checks category data'() {
        given: "A brand new category"
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post(API.Categories).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Specific category is requested"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Category, categoryId)

        then: "Category object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("category"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.priority", equalTo(1))
                .body("data.attributes.name", equalTo("Bonuses"))
    }

    def "User modifies category data"() {
        given: "A brand new category"
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post(API.Categories).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "New category data is submitted"
        def modifiedCategory = category.clone()
        modifiedCategory.data.attributes.name = "Salary"
        modifiedCategory.data.attributes.priority = 9
        given()
                .contentType("application/vnd.mdg+json")
                .when()
                .request().body(JsonOutput.toJson(modifiedCategory))
                .put(API.Category, categoryId).
                then()
                .assertThat().statusCode(202)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.attributes.name", equalTo("Salary"))
                .body("data.attributes.priority", equalTo(9))

        then: "modified data will be retrieved on request"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Category, categoryId).
                then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.type", equalTo("category"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.priority", equalTo(9))
                .body("data.attributes.name", equalTo("Salary"))
    }

    def "User deletes category"() {
        given: "A brand new category"
        def categoryId = given()
                .contentType("application/vnd.mdg+json").
                when()
                .request().body(JsonOutput.toJson(category))
                .post(API.Categories).
                then()
                .assertThat().statusCode(201)
                .extract().path("data.id")

        when: "Category is deleted"
        given()
                .contentType("application/vnd.mdg+json").
                when()
                .delete(API.Category, categoryId)
                .then()
                .assertThat().statusCode(204)


        then: "it should disappear from category list"
        def response = given()
                .contentType("application/vnd.mdg+json").
                when()
                .get(API.Categories)
        def body = JsonPath.parse(response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .extract().asString())

        assertThat(body.read("data[?(@.id == ${categoryId})]", List.class), empty())
    }

}