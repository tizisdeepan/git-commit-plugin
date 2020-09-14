package org.master.git

import com.intellij.openapi.ui.ComponentValidator
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.layout.panel
import java.awt.Dimension
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.event.DocumentEvent


class AddItemDialog(var items: List<ItemModel>, var onAdd: (ItemModel) -> Unit) : DialogWrapper(true) {

    private val variableRegex = "^[_a-z]\\w*$"

    private val nameField = JTextField("")
    private val optionsField = JTextField("")
    private val typeField = JComboBox(arrayOf(ItemModel.TEXT, ItemModel.OPTIONS))
    private val variableField = JTextField("")

    override fun createCenterPanel(): JComponent? {
        doOkValidations()
        return createAddPanel()
    }

    private fun createAddPanel(): JPanel {
        val optionsLabel = JLabel("Comma separated values")
        optionsLabel.isVisible = false
        optionsField.isVisible = false
        optionsLabel.maximumSize = Dimension(200, nameField.preferredSize.height)
        nameField.minimumSize = Dimension(300, nameField.preferredSize.height)
        optionsField.minimumSize = Dimension(300, optionsField.preferredSize.height)
        variableField.minimumSize = Dimension(300, typeField.preferredSize.height)
        typeField.minimumSize = Dimension(300, typeField.preferredSize.height)
        val panel = panel {
            row {
                label("Name")
                nameField(grow)
            }
            row {
                label("Type")
                typeField(grow)
            }
            row {
                label("Variable Name")
                variableField(grow)
            }
            row {
                optionsLabel(grow)
                optionsField(grow)
            }
        }
        typeField.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                when (it.item as? String) {
                    ItemModel.OPTIONS -> {
                        optionsLabel.isVisible = true
                        optionsField.isVisible = true
                    }
                    else -> {
                        optionsLabel.isVisible = false
                        optionsField.isVisible = false
                    }
                }
            }
        }
        addNameValidator()
        addVariableValidator()
        return panel
    }

    private fun addNameValidator() {
        ComponentValidator(disposable).withValidator { v: ComponentValidator ->
            if (nameField.text.isNotEmpty()) {
                v.updateInfo(null)
            } else {
                v.updateInfo(ValidationInfo("Name cannot be empty", nameField).withOKEnabled())
            }
            doOkValidations()
        }.installOn(nameField)

        nameField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                ComponentValidator.getInstance(nameField).ifPresent { v: ComponentValidator -> v.revalidate() }
            }
        })
    }

    private fun addVariableValidator() {
        ComponentValidator(disposable).withValidator { v: ComponentValidator ->
            val variable = variableField.text
            if (variable.isNotEmpty()) {
                if (items.any { it.variableName == variable }) {
                    v.updateInfo(ValidationInfo("Variable Name already present", variableField).withOKEnabled())
                } else if (!variable.matches(Regex(variableRegex))) {
                    v.updateInfo(
                        ValidationInfo(
                            "Variable Name has to match the regex: ${variableRegex}",
                            variableField
                        ).withOKEnabled()
                    )
                } else v.updateInfo(null)
            } else {
                v.updateInfo(ValidationInfo("Variable Name cannot be empty", variableField).withOKEnabled())
            }
            doOkValidations()
        }.installOn(variableField)

        variableField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                ComponentValidator.getInstance(variableField).ifPresent { v: ComponentValidator -> v.revalidate() }
            }
        })
    }

    private fun validateVariableName(variable: String) =
        variable.isNotEmpty() && !items.any { it.variableName == variable } && variable.matches(Regex(variableRegex))

    private fun doOkValidations() {
        okAction.isEnabled = validateVariableName(variableField.text) && nameField.text.isNotEmpty()
    }

    override fun doOKAction() {
        val itemModel =
            ItemModel(nameField.text, typeField.selectedItem as String, variableField.text, optionsField.text)
        println(itemModel.toString())
        onAdd(itemModel)
        super.doOKAction()
    }

    init {
        init()
        title = "Add Value"
    }
}