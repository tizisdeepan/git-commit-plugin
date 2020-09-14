package org.master.git

data class ItemModel(
    var name: String,
    var type: String,
    var variableName: String,
    var label: String
) {
    companion object {
        const val TEXT: String = "Text"
        const val OPTIONS: String = "Options"
    }
}