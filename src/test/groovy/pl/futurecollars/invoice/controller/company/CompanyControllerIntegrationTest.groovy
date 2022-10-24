package pl.futurecollars.invoice.controller.company

import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import pl.futurecollars.invoice.controller.AbstractControllerTest

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.futurecollars.invoice.TestHelpers.COMPANIES_ENDPOINT
import static pl.futurecollars.invoice.TestHelpers.company

@Transactional
class CompanyControllerIntegrationTest extends AbstractControllerTest {


    def setup() {
        getAllInvoices().each { invoice -> deleteInvoice(invoice.id) }
        getAllCompanies().each { company -> deleteCompany(company.id) }
    }


    def "empty array is returned when no companies were added"() {
        expect:
        getAllCompanies() == []
    }

    def "add company returns sequential id"() {
        expect:
        def firstId = addCompanyAndReturnId(company(1))
        addCompanyAndReturnId(company(2)) == firstId + 1
        addCompanyAndReturnId(company(3)) == firstId + 2
        addCompanyAndReturnId(company(4)) == firstId + 3
        addCompanyAndReturnId(company(5)) == firstId + 4
    }

    def "all companies are returned when getting all companies"() {
        given:
        def numberOfCompanies = 3
        def expectedCompanies = addUniqueCompanies(numberOfCompanies)

        when:
        def companies = getAllCompanies()

        then:
        companies.size() == numberOfCompanies
        companies == expectedCompanies
    }

    def "correct company is returned when getting by id"() {
        given:
        def expectedCompanies = addUniqueCompanies(5)
        def expectedCompany = expectedCompanies.get(2)

        when:
        def company = getCompanyById(expectedCompany.getId())

        then:
        company == expectedCompany
    }

    def "404 is returned when company id is not found when getting by id [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(get("$COMPANIES_ENDPOINT/$id"))
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 168, 1256]
    }

    def "404 is returned when company id is not found when deleting company [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(
                delete("$COMPANIES_ENDPOINT/$id")
        )
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 1000]
    }

    def "404 is returned when company id is not found when updating company [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(
                put("$COMPANIES_ENDPOINT/$id")
                        .content(getAsJson(company(1)))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 1000]
    }

    def "should company can be modified"() {
        given:
        def id = addCompanyAndReturnId(company(4))
        def updatedCompany = company(1)
        updatedCompany.id = id

        expect:
        mockMvc.perform(
                put("$COMPANIES_ENDPOINT/$id")
                        .content(jsonService.toJson(updatedCompany))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())

        def companyFromDbAfterUpdate = getCompanyById(id).toString()
        def expectedCompany = updatedCompany.toString()
        companyFromDbAfterUpdate == expectedCompany
    }

    def "company can be deleted"() {
        given:
        def companies = addUniqueCompanies(69)

        expect:
        companies.each { company -> deleteCompany(company.getId()) }
        getAllCompanies().size() == 0
    }

}