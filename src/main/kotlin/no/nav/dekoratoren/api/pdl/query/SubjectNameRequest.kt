package no.nav.dekoratoren.api.pdl.query

class SubjectNameRequest(val variables: QueryVariables) {

    val query: String
        get() = """
            query (${"$"}ident: ID!) {
                person: hentPerson(ident: ${"$"}ident) {
                     navn { fornavn, mellomnavn, etternavn }
                } 
            }
        """.compactJson()
}

fun createSubjectNameRequest(ident: String): SubjectNameRequest {
    return SubjectNameRequest(QueryVariables(ident))
}

private fun String.compactJson(): String =
    trimIndent().replace("\r", " ").replace("\n", " ").replace("\\s+".toRegex(), " ")