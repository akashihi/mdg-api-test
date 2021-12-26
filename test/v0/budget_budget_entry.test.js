/* This is broken on the backend side and completely untestable, just copying the reasteasy test there

class BudgetStateSpecification extends Specification {
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
        BudgetFixture.create(BudgetFixture.budget("2016-12-01", "2016-12-31"))
    }

    def cleanupSpec() {
        BudgetFixture.remove("20161201")
    }

    def 'Budget expected income should follow entries expected income'() {
        given: "Empty budget"
        BigDecimal expectedIncome = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.income.expected")

        assertThat(expectedIncome, equalTo(new BigDecimal(0)))

        when: "Budget entry is modified"
        def listBody = when().get(API.BudgetEntries,"20161201")
                .then().spec(readSpec())
                .extract().asString()
        def entry = JsonPath.parse(listBody).read("data[?(@.attributes.account_id == ${incomeId})]", List.class).first()
        entry.attributes.expected_amount=3620
        given().body(JsonOutput.toJson(["data": entry]))
                .when().put(API.BudgetEntry,"20161201", entry.id).
                then().spec(modifySpec())

        then: "Budget expected income should be 3620"
        BigDecimal updatedAmount = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.income.expected")

        BigDecimal expected = rateConverter.applyRate(3620, rateConverter.getCurrencyForAccount(incomeId))

        assertThat(updatedAmount.toBigInteger(), equalTo(expected.toBigInteger()))
    }

    def 'Budget actual income should follow income transactions'() {
        BigDecimal actualIncome = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.income.actual")

        assertThat(actualIncome, equalTo(new BigDecimal(0)))

        when: "Transaction os posted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2016-12-01T16:45:36",
                                "comment"   : "Income transaction",
                                "tags": [],
                                "operations": [
                                        [
                                                "account_id": incomeId,
                                                "amount"    : -9000
                                        ],
                                        [
                                                "account_id": assetId,
                                                "amount"    : 9000
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(transaction)

        then: "Budget expected income should be 9000"
        BigDecimal updatedAmount = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.income.actual")

        BigDecimal expected = rateConverter.applyRate(9000, rateConverter.getCurrencyForAccount(incomeId))

        assertThat(updatedAmount.toBigInteger(), equalTo(expected.toBigInteger()))

        when().delete(API.Transaction, txId)
    }

    def 'Budget expected expenses should follow entries expected spendings'() {
        given: "Empty budget"
        BigDecimal expectedExpenses = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.expense.expected")

        assertThat(expectedExpenses, equalTo(new BigDecimal(0)))

        when: "Budget entry is modified"
        def listBody = when().get(API.BudgetEntries,"20161201")
                .then().spec(readSpec())
                .extract().asString()
        def entry = JsonPath.parse(listBody).read("data[?(@.attributes.account_id == ${expenseId})]", List.class).first()
        entry.attributes.expected_amount=1924
        given().body(JsonOutput.toJson(["data": entry]))
                .when().put(API.BudgetEntry,"20161201", entry.id).
                then().spec(modifySpec())

        then: "Budget expected expense should be 1924"
        BigDecimal updatedAmount = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.expense.expected")

        BigDecimal expected = rateConverter.applyRate(1924, rateConverter.getCurrencyForAccount(expenseId))

        assertThat(updatedAmount.toBigInteger(), equalTo(expected.toBigInteger()))
    }

    def 'Budget actual spendings should follow income transactions'() {
        given: "Empty budget"
        BigDecimal actualExpenses = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.expense.actual")

        assertThat(actualExpenses, equalTo(new BigDecimal(0)))

        when: "Transaction is posted"
        def transaction = [
                "data": [
                        "type"      : "transaction",
                        "attributes": [
                                "timestamp" : "2016-12-01T16:45:36",
                                "comment"   : "Income transaction",
                                "tags": [],
                                "operations": [
                                        [
                                                "account_id": assetId,
                                                "amount"    : -9000
                                        ],
                                        [
                                                "account_id": expenseId,
                                                "amount"    : 9000
                                        ]
                                ]
                        ]
                ]
        ]
        def txId = TransactionFixture.create(transaction)

        then: "Budget expected income should be 9000"
        BigDecimal updatedAmount = when().get(API.Budget, "20161201")
                .then().spec(readSpec())
                .extract().path("data.attributes.state.expense.actual")

        BigDecimal expected = rateConverter.applyRate(9000, rateConverter.getCurrencyForAccount(expenseId))

        assertThat(updatedAmount.toBigInteger(), equalTo(expected.toBigInteger()))

        when().delete(API.Transaction, txId)
    }
}
     */
