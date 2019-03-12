package org.akashihi.mdg.apitest.fixtures

import groovy.json.JsonOutput
import org.akashihi.mdg.apitest.API

import static io.restassured.RestAssured.given
import static org.akashihi.mdg.apitest.apiConnectionBase.createSpec
import static org.akashihi.mdg.apitest.apiConnectionBase.modifySpec
import static org.hamcrest.Matchers.equalTo

class CategoryFixture {
    static def category(parent_id = null) {
        def category = [
                "data": [
                        "type"      : "category",
                        "attributes": [
                                "name"        : "Bonuses",
                                "priority"    : 1,
                                "account_type": "expense"
                        ]
                ]
        ]
        if (parent_id != null) {
            category.data.attributes.parent_id = parent_id
        }
        return category
    }

    static Integer create() {
        return create(category())
    }

    static Integer create(category) {
        return given().body(JsonOutput.toJson(category))
                .when().post(API.Categories)
                .then().spec(createSpec("/api/category/"))
                .body("data.type", equalTo("category"))
                .body("data.attributes.account_type", equalTo(category.data.attributes.account_type))
                .body("data.attributes.priority", equalTo(1))
                .body("data.attributes.name", equalTo("Bonuses"))
                .extract().path("data.id")
    }

    static update(id, category) {
        given().body(JsonOutput.toJson(category))
                .when().put(API.Category, id)
                .then().spec(modifySpec())
    }
}
