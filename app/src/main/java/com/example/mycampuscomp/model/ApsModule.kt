package com.example.mycampuscomp.model

data class ApsModule(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var markText: String = "",
    var creditsText: String = ""
)
