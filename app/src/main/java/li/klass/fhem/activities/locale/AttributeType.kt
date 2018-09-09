package li.klass.fhem.activities.locale

enum class AttributeType(val description: String) {
    STATE("STATE"),
    ATTRIBUTE("ATTRIBUTE"),
    INT("INTERNAL");

    override fun toString() = description

    companion object {
        fun getFor(value: String) = values().firstOrNull { it.description == value }

        fun positionFor(value: String) =
                getFor(value)?.let { AttributeType.values().indexOf(it) } ?: 0

        fun atPosition(position: Int) = AttributeType.values()[position]
    }
}