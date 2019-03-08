package org.akashihi.mdg.apitest;

public final class API {
    public static final String Settings = "/setting";
    public static final String Setting = "/setting/{id}";

    public static final String Currencies = "/currency";
    public static final String Currency = "/currency/{id}";

    public static final String Categories = "/category";
    public static final String Category = "/category/{id}";

    public static final String Accounts = "/account";
    public static final String Account = "/account/{id}";

    public static final String Tags = "/tag";

    public static final String Transactions = "/transaction";
    public static final String Transaction = "/transaction/{id}";

    public static final String Budgets = "/budget";
    public static final String Budget = "/budget/{id}";

    public static final String BudgetEntries = "/budget/{id}/entry";
    public static final String BudgetEntry = "/budget/{id}/entry/{entryId}";

    public static final String ReportTotals = "/report/totals";
}
