package li.klass.fhem.fcm

import li.klass.fhem.R

enum class AddSelfResult(val resultText: Int) {
    FCM_NOT_ACTIVE(R.string.gcmRegistrationNotActive),
    ALREADY_REGISTERED(R.string.gcmAlreadyRegistered),
    SUCCESS(R.string.gcmSuccessfullyRegistered)
}