package req2test.tool.approach.processor.adapter

interface AIAdapter {
    fun getAnswerFromQuestion(question: String, context: String? = null): String
}

class MockAIAdapter: AIAdapter {
    companion object {
        var count = 0
    }
    override fun getAnswerFromQuestion(question: String, context: String?): String {
        if(count == 3)
            count = 0
        count++
        if (count == 1)
            return """
                {
                  "dataTest": [
                    {
                      "className": "ValidTestCase",
                      "isValidInput": true,
                      "input": {
                        "C": 10,
                        "B": 8,
                        "A": 5
                      },
                      "errorMessage": ""
                    },
                    {
                      "className": "Var1OutOfRangeLow",
                      "isValidInput": false,
                      "input": {
                        "C": 15,
                        "B": 12,
                        "A": 0
                      },
                      "errorMessage": "A must be greater than zero and less than 20"
                    },
                    {
                      "className": "Var1OutOfRangeHigh",
                      "isValidInput": false,
                      "input": {
                        "C": 10,
                        "B": 14,
                        "A": 21
                      },
                      "errorMessage": "A must be greater than zero and less than 20"
                    },
                    {
                      "className": "Var2Invalid",
                      "isValidInput": false,
                      "input": {
                        "C": 11,
                        "B": 0,
                        "A": 6
                      },
                      "errorMessage": "B must be greater than 0"
                    },
                    {
                      "className": "Var3Invalid",
                      "isValidInput": false,
                      "input": {
                        "C": 0,
                        "B": 7,
                        "A": 9
                      },
                      "errorMessage": "C must be greater than 0"
                    },
                    {
                      "className": "InequalityConstraintsVar1",
                      "isValidInput": false,
                      "input": {
                        "C": 8,
                        "B": 6,
                        "A": 1
                      },
                      "errorMessage": "A must be less than B + C"
                    },
                    {
                      "className": "InequalityConstraintsVar2",
                      "isValidInput": false,
                      "input": {
                        "C": 7,
                        "B": 1,
                        "A": 12
                      },
                      "errorMessage": "B must be less than A + C"
                    },
                    {
                      "className": "InequalityConstraintsVar3",
                      "isValidInput": false,
                      "input": {
                        "C": 14,
                        "B": 7,
                        "A": 5
                      },
                      "errorMessage": "C must be less than A + B"
                    }
                  ]
                }
            """.trimIndent()
        else if(count == 2)
        return """
            {
              "dataTest": [
                {
                  "className": "ValidCase",
                  "isValidInput": true,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-06-20",
                    "CNH": "12345",
                    "fullName": "ValidString"
                  },
                  "errorMessage": ""
                },
                {
                  "className": "InvalidVar4WithNumbers",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-06-20",
                    "CNH": "12345",
                    "fullName": "Invalid123"
                  },
                  "errorMessage": "Validation failed for fullName: It does not allow numbers and special characters."
                },
                {
                  "className": "InvalidVar4WithSpecialCharacters",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-06-20",
                    "CNH": "12345",
                    "fullName": "Invalid@str"
                  },
                  "errorMessage": "Validation failed for fullName: It does not allow numbers and special characters."
                },
                {
                  "className": "InvalidVar5WithAlphaCharacters",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-06-20",
                    "CNH": "12ab45",
                    "fullName": "ValidString"
                  },
                  "errorMessage": "Validation failed for CNH: Only numbers are allowed."
                },
                {
                  "className": "InvalidVar6BeforeCurrentDate",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-06-10",
                    "CNH": "12345",
                    "fullName": "ValidString"
                  },
                  "errorMessage": "Validation failed for CNHExpirationDate: Date must be greater than the current date."
                },
                {
                  "className": "InvalidVar6AfterOneMonth",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2023-12-31",
                    "CNHExpirationDate": "2024-07-12",
                    "CNH": "12345",
                    "fullName": "ValidString"
                  },
                  "errorMessage": "Validation failed for CNHExpirationDate: Date must be within one month from the current date."
                },
                {
                  "className": "InvalidVar7AfterCurrentYear",
                  "isValidInput": false,
                  "input": {
                    "birthDate": "2025-01-01",
                    "CNHExpirationDate": "2024-06-20",
                    "CNH": "12345",
                    "fullName": "ValidString"
                  },
                  "errorMessage": "Validation failed for birthDate: Date must be less than the current year."
                }
              ]
            }
        """.trimIndent()

        else if(count == 3)
            return """
                {
                  "dataTest": [
                    {
                      "className": "validInputTest",
                      "isValidInput": true,
                      "input": {
                        "reservationDate": "2023-11-02",
                        "customer": {
                          "fullName": "example",
                          "CNH": "123456",
                          "CNHExpirationDate": "2023-10-29",
                          "birthDate": "2022-12-31"
                        },
                        "reservationTime": "09:00",
                        "carType": "SUV"
                      },
                      "errorMessage": ""
                    },
                    {
                      "className": "reservationDate_incorrect_date",
                      "isValidInput": false,
                      "input": {
                        "reservationDate": "2023-08-01",
                        "customer": {
                          "fullName": "example",
                          "CNH": "123456",
                          "CNHExpirationDate": "2023-10-29",
                          "birthDate": "2022-12-31"
                        },
                        "reservationTime": "09:00",
                        "carType": "SUV"
                      },
                      "errorMessage": "reservationDate must be a valid date greater than the current day."
                    },
                    {
                      "className": "reservationTime_out_of_bound_time",
                      "isValidInput": false,
                      "input": {
                        "reservationDate": "2023-11-02",
                        "customer": {
                          "fullName": "example",
                          "CNH": "123456",
                          "CNHExpirationDate": "2023-10-29",
                          "birthDate": "2022-12-31"
                        },
                        "reservationTime": "07:00",
                        "carType": "SUV"
                      },
                      "errorMessage": "reservationTime must be a valid time between 08:00 and 18:00."
                    },
                    {
                      "className": "carType_invalid_value",
                      "isValidInput": false,
                      "input": {
                        "reservationDate": "2023-11-02",
                        "customer": {
                          "fullName": "example",
                          "CNH": "123456",
                          "CNHExpirationDate": "2023-10-29",
                          "birthDate": "2022-12-31"
                        },
                        "reservationTime": "09:00",
                        "carType": "luxury"
                      },
                      "errorMessage": "carType must be one of the following values: compact, air compact, executive, SUV."
                    },
                    {
                      "className": "customer_CNHExpirationDate_invalid_date_range",
                      "isValidInput": false,
                      "input": {
                        "reservationDate": "2023-11-02",
                        "customer": {
                          "fullName": "example",
                          "CNH": "123456",
                          "CNHExpirationDate": "2023-12-01",
                          "birthDate": "2022-12-31"
                        },
                        "reservationTime": "09:00",
                        "carType": "SUV"
                      },
                      "errorMessage": "customer.CNHExpirationDate must be a valid date less than the current date."
                    }
                  ]
                }
            """.trimIndent()
        else
            return ""
    }

}