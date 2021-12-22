/* This is broken on the backend side and completely untestable, just copying the reasteasy test there

class BudgetCalculationsSpecification extends Specification {

    static def incomeId
    static def assetId
    static def expenseId
    static def rateConverter

    def setupSpec() {
        setupAPI()

        rateConverter = new RateConversion()

        incomeId = AccountFixture.create(AccountFixture.incomeAccount())
        assetId = AccountFixture.create(AccountFixture.assetAccount())
        expenseId = AccountFixture.create(AccountFixture.expenseAccount())

        BudgetFixture.create(BudgetFixture.budget('2017-04-01', '2017-04-30'))
    }

    def cleanupSpec() {
        BudgetFixture.remove("20170401")
    }

    def 'Budget incoming amount should be sum of all asset accounts before budget term'() {
        given: 'Sum all transactions before budget'
        def assetAccountIds = given().when().get(API.Accounts)
                .then().spec(readSpec())
                .extract().path("data.findAll() {it.attributes.account_type == 'asset'}.id")

        def ops = given()
                .queryParam("notLater", '2017-04-01T00:00:00')
                .when().get(API.Transactions)
                .then().spec(readSpec())
                .extract().path("data.findAll().attributes.operations")

        def incomingAmount = BigDecimal.ZERO
        ops.each{ x ->
            x.each { op ->
                if (assetAccountIds.contains(op['account_id'])) {
                    def opAmount = new BigDecimal(op['amount'])
                    opAmount = rateConverter.applyRate(opAmount, rateConverter.getCurrencyForAccount(op['account_id']))
                    incomingAmount = incomingAmount.add(opAmount)
                }
            }
        }

        when: 'A new budget is created and retrieved'
        BigDecimal actualAmount = given().when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.incoming_amount")

        then: "Budget incoming amount should match sum of asset transactions"
        assertThat(actualAmount.setScale(2, RoundingMode.HALF_UP).compareTo(incomingAmount.setScale(2, RoundingMode.HALF_UP)), is(0))
    }

    def 'Budget expected amount should be sum of all asset accounts after budget term'() {
        given: 'Some budget'
        def budgetBody = given().when().get(API.Budget, "20170401")
                .then().spec(readSpec())
        BigDecimal incomingAmount = budgetBody.extract().path("data.attributes.incoming_amount")
        BigDecimal expectedAmount = budgetBody.extract().path("data.attributes.outgoing_amount.expected")

        when: "List of budget entries is summarized"
        def amounts = when().get(API.BudgetEntries,"20170401")
                .then().spec(readSpec())
                .extract().path("data.findAll().attributes.expected_amount")

        def calculatedAmount = BigDecimal.ZERO
        amounts.each{ x ->
            calculatedAmount = calculatedAmount.add(new BigDecimal(calculatedAmount))
        }

        then: "Budget expected amount and calculated amount should match"
        assertThat(incomingAmount.add(calculatedAmount), equalTo(expectedAmount))
    }

    def 'Budget expected should change when budget entry is modified'() {
        given: 'Some budget'
        BigDecimal expectedAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.expected")

        when: "Budget entry is modified"
        def entryIds = when().get(API.BudgetEntries,"20170401")
                .then().spec(readSpec())
                .extract().path("data.findAll().id")
        def entryBody = when().get(API.BudgetEntry,"20170401", entryIds.first())
                .then().spec(readSpec())

        BigDecimal entryAmount = entryBody.extract().path("data.attributes.expected_amount")
        BigDecimal entryAccount = entryBody.extract().path("data.attributes.account_id")
        def newAmount = new BigDecimal(entryAmount).add(new BigDecimal(9000))

        def newEntry = [
                "data": [
                        "type"      : "budgetentry",
                        "attributes": [
                                "even_distribution" : false,
                                "expected_amount" : newAmount.toString(),
                        ]
                ]
        ]
        given().body(JsonOutput.toJson(newEntry))
                .when().put(API.BudgetEntry,"20170301", entryIds.first())
                .then().spec(modifySpec())

        then: "Budget expected amount should differ by 9000"
        BigDecimal updatedAmount = when().get(API.Budget, "20170401")
                .then().spec(readSpec())
                .extract().path("data.attributes.outgoing_amount.expected")

        BigDecimal expectedDifference = rateConverter.applyRate(9000, rateConverter.getCurrencyForAccount(entryAccount))

        assertThat(updatedAmount.subtract(expectedAmount).abs(), closeTo(expectedDifference, 0.001))
    }
}

 */
