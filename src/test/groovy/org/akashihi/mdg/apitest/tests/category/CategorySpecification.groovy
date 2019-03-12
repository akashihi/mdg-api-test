package org.akashihi.mdg.apitest.tests.category

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.fixtures.CategoryFixture
import spock.lang.Specification
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.akashihi.mdg.apitest.apiConnectionBase.readSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.setupAPI
import static org.hamcrest.Matchers.*

class CategorySpecification extends Specification {
    def setupSpec() {
        setupAPI()
    }

    def 'User creates new category'() {
        when: 'Category is submitted to the system'
        def categoryId = CategoryFixture.create()

        then: 'Category appears on the category list'
                when().get(API.Categories)
                        .then().spec(readSpec())
                        .body("data", not(empty()))
                        .body("data.find {it.id==${categoryId}}.type", equalTo("category"))
                        .body("data.find {it.id==${categoryId}}.attributes.account_type", equalTo("expense"))
                        .body("data.find {it.id==${categoryId}}.attributes.priority", equalTo(1))
                        .body("data.find {it.id==${categoryId}}.attributes.name", equalTo("Bonuses"))
    }

    def 'User checks category data'() {
        given: "A brand new category"
        def categoryId = CategoryFixture.create()

        when: "Specific category is requested"
        def response = when().get(API.Category, categoryId)

        then: "Category object should be returned"
        response.then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/vnd.mdg+json")
                .body("data.id", equalTo(categoryId))
                .body("data.type", equalTo("category"))
                .body("data.attributes.account_type", equalTo("expense"))
                .body("data.attributes.priority", equalTo(1))
                .body("data.attributes.name", equalTo("Bonuses"))
    }

    def "User modifies category data"() {
        given: "A brand new category"
        def categoryId = CategoryFixture.create()

        when: "New category data is submitted"
        def modifiedCategory = CategoryFixture.category()
        modifiedCategory.data.attributes.name = "Salary"
        modifiedCategory.data.attributes.priority = 9
        given().body(JsonOutput.toJson(modifiedCategory))
                .when().put(API.Category, categoryId)
                .then().spec(modifySpec())
                .body("data.attributes.name", equalTo("Salary"))
                .body("data.attributes.priority", equalTo(9))

        then: "modified data will be retrieved on request"
                when().get(API.Category, categoryId).
                then().spec(readSpec())
                .body("data.attributes.priority", equalTo(9))
                .body("data.attributes.name", equalTo("Salary"))
    }

    def "User deletes category"() {
        given: "A brand new category"
        def categoryId = CategoryFixture.create()

        when: "Category is deleted"
                when().delete(API.Category, categoryId)
                .then().statusCode(204)


        then: "it should disappear from category list"
                when().get(API.Categories)
                .then().spec(readSpec())
                .body("data.findAll {it.id==${categoryId}}", empty())
    }

}