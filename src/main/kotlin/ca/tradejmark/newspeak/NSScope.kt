package ca.tradejmark.newspeak

import ca.tradejmark.newspeak.error.NoSuchVariableError

class NSScope(val parent: NSScope? = null) {
    private val vars: MutableMap<String, NSValue> = mutableMapOf()
    operator fun get(name: String): NSValue {
        val lcName = name.toLowerCase()
        return vars[lcName]
            ?: parent?.get(lcName)
            ?: throw NoSuchVariableError(name)
    }

    fun shadow(name: String, v: NSValue) {
        vars[name.toLowerCase()] = v
    }

    operator fun set(name: String, v: NSValue) {
        val lcName = name.toLowerCase()
        if (!setVal(lcName, v)) vars[lcName] = v
    }

    private fun setVal(name: String, v: NSValue): Boolean {
        return if (vars.containsKey(name)) {
            vars[name] = v
            true
        } else parent?.setVal(name, v) ?: false
    }
}